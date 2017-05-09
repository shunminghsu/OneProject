package com.transcend.otg.Task;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;

import com.transcend.otg.R;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Utils.FileFactory;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by henry_hsu on 2017/5/8.
 * This task is created for PhotoActivity
 */

public class EncryptTask extends AsyncTask<String, Void, Integer> {

    private Context mContext;
    private String mCacheDirPath, mEncPath, mEncName;
    private String mPassword;
    private File mEncFile;
    private FileInfo mSourceFileInfo;
    private static final int RESULT_SUCCESS = 0;
    private static final int RESULT_FAIL = 1;
    private static final int RESULT_EXIST = 2;

    public EncryptTask(Context context, String enc_name, String password, FileInfo fileInfo) {
        mContext = context;
        mEncName = enc_name + context.getResources().getString(R.string.encrypt_subfilename);
        mPassword = password;
        mSourceFileInfo = fileInfo;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");
        String currentDateTimeString = sdf.format(Calendar.getInstance().getTime());
        mCacheDirPath = Constant.ROOT_CACHE + File.separator + currentDateTimeString;
        File dir = new File(mCacheDirPath);
        if (!dir.exists())
            dir.mkdirs();

        if (mSourceFileInfo.storagemode == Constant.STORAGEMODE_LOCAL) {
            mEncPath = Constant.ROOT_LOCAL + File.separator + mEncName;
        } else {
            mEncPath = mCacheDirPath + File.separator + mEncName;
        }

    }

    @Override
    protected Integer doInBackground(String... strings) {
        if (isEncFileExist(mSourceFileInfo.storagemode))
            return RESULT_EXIST;
        //updateProgress(mContext.getResources().getString(R.string.loading), mContext);
        List<String> folderList = new ArrayList<String>();
//        ArrayList fileList = new ArrayList();
        File file = new File(mCacheDirPath);
        if (file.isDirectory()) {
            folderList.add(mCacheDirPath);
        } else if (file.isFile()) {
//            fileList.add(file);
        }

        try {
            ZipFile zipFile = new ZipFile(mEncPath);
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
            parameters.setPassword(mPassword);
            //startProgressWatcher();
//            if (fileList.size() != 0)
//                zipFile.addFiles(fileList, parameters);

            if (folderList.size() != 0) {
                for (String path : folderList) {
                    zipFile.addFolder(path, parameters);
                }
            }
        }catch (Exception e){
            return RESULT_FAIL;
        }
        //updateResult(getContext().getString(R.string.done));
        //closeProgressWatcher();
        return RESULT_SUCCESS;
    }

    @Override
    protected void onPostExecute(Integer result) {

        if (result == RESULT_SUCCESS) {
            if (mSourceFileInfo.storagemode == Constant.STORAGEMODE_LOCAL) {
                mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mEncFile)));
                updateProgress(mContext.getString(R.string.done), mContext);
            } else if (mSourceFileInfo.storagemode == Constant.STORAGEMODE_SD) {
                String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
                FileInfo encFileInfo = new FileInfo();
                encFileInfo.name = mEncName;
                encFileInfo.path = mEncPath;
                new MoveEncFileBackTask(mContext, encFileInfo, Constant.STORAGEMODE_SD, sdPath).execute();
            } else {
                FileInfo encFileInfo = new FileInfo();
                encFileInfo.name = mEncName;
                encFileInfo.path = mEncPath;
                new MoveEncFileBackTask(mContext, encFileInfo, Constant.STORAGEMODE_OTG, null).execute(Constant.mRootDocumentFile);
            }
        } else if (result == RESULT_EXIST) {
            String exist = mContext.getResources().getString(R.string.file_exist);
            createDialog(mContext, exist);
            updateProgress(mContext.getString(R.string.fail), mContext);
        } else {
            updateProgress(mContext.getString(R.string.fail), mContext);
        }
    }

    private boolean isEncFileExist(int storage) {
        if (storage == Constant.STORAGEMODE_LOCAL) {
            mEncFile = new File(mEncPath);
            return mEncFile.exists();
        } else if (storage == Constant.STORAGEMODE_SD) {
            // Constant.mSDRootDocumentFile should have been init
            return (Constant.mSDRootDocumentFile.findFile(mEncName) != null);
        } else {
            return (Constant.mRootDocumentFile.findFile(mEncName) != null);
        }
    }

    private void createDialog(Context context, final String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.mipmap.icon_elite_logo);
        builder.setTitle(context.getResources().getString(R.string.app_name));
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateProgress(String action, Context context) {

        int icon = R.mipmap.icon_elite_logo;
        String text = context.getResources().getString(R.string.encrypt) + " - " + action;

        NotificationManager ntfMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(icon);
        builder.setContentTitle(context.getResources().getString(R.string.app_name));
        builder.setContentText(text);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        ntfMgr.notify(777, builder.build());
    }
}
