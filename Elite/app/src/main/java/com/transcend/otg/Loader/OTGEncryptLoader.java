package com.transcend.otg.Loader;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.transcend.otg.R;
import com.transcend.otg.Utils.FileFactory;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangbojie on 2016/6/28.
 */
public class OTGEncryptLoader extends AsyncTaskLoader<Boolean> {
    private static final String TAG = OTGEncryptLoader.class.getSimpleName();

    private Activity mActivity;
    private HandlerThread mThread;
    private Handler mHandler;
    private Runnable mWatcher;

    private String mSrcs, mDes, mPassword;
    private int mNotificationID = 0;

    public OTGEncryptLoader(Context context, List<String> encryptList){
                              //String srcs, String password, String name) {
        super(context);
        mActivity = (Activity) context;
        mSrcs = encryptList.get(0);
        mDes = encryptList.get(1);
        mPassword = encryptList.get(2);
        mNotificationID = FileFactory.getInstance().getNotificationID();
    }

    @Override
    public Boolean loadInBackground() {
        try {
            return encrypt();
        } catch (IOException e) {
            e.printStackTrace();
            updateResult(getContext().getString(R.string.error));
        } catch (ZipException e) {
            e.printStackTrace();
            updateResult(getContext().getString(R.string.error));
        }
        return false;
    }

    private boolean encrypt() throws IOException, ZipException {
        updateProgress(getContext().getResources().getString(R.string.loading));
        List<String> folderList = new ArrayList<String>();
        ArrayList fileList = new ArrayList();
        File file = new File(mSrcs);
        if (file.isDirectory()) {
            folderList.add(mSrcs);
        } else if (file.isFile()) {
            fileList.add(file);
        }
        String dst = mDes + mActivity.getString(R.string.encrypt_subfilename);
        File check_file = new File(dst);
        if (check_file.exists()) {
            return false;
        }
        try {
            ZipFile zipFile = new ZipFile(dst);
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
            parameters.setPassword(mPassword);
            startProgressWatcher();
            if (fileList.size() != 0)
                zipFile.addFiles(fileList, parameters);
            if (folderList.size() != 0) {
                for (String path : folderList) {
                    zipFile.addFolder(path, parameters);
                }
            }
        }catch (Exception e){
            return false;
        }
        File target = new File(dst);
        mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(target)));
        closeProgressWatcher();
        return true;
    }

    private void startProgressWatcher() {
        mThread = new HandlerThread(TAG);
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
        mHandler.post(mWatcher = new Runnable() {
            @Override
            public void run() {
                if (mHandler != null) {
                    mHandler.postDelayed(mWatcher, 1000);
                    updateProgress(getContext().getResources().getString(R.string.encrypt));
                }
            }
        });
    }

    private void closeProgressWatcher() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mWatcher);
            mHandler = null;
        }
        if (mThread != null) {
            mThread.quit();
            mThread = null;
        }
    }

    private void updateResult(String result) {
        Log.w(TAG, "result: " + result);

        int icon = R.mipmap.icon_elite_logo;
        String name = getContext().getResources().getString(R.string.app_name);
        String type = getContext().getResources().getString(R.string.encrypt);
        String text = String.format("%s - %s", type, result);

        NotificationManager ntfMgr = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = mActivity.getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
        builder.setSmallIcon(icon);
        builder.setContentTitle(name);
        builder.setContentText(text);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        ntfMgr.notify(mNotificationID, builder.build());
        FileFactory.getInstance().releaseNotificationID(mNotificationID);
    }

    private void updateProgress(String name) {

        int icon = R.mipmap.icon_elite_logo;

        String type = getContext().getResources().getString(R.string.encrypting);
        String text = String.format("%s", type);

        NotificationManager ntfMgr = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = mActivity.getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
        builder.setSmallIcon(icon);
        builder.setContentTitle(name);
        builder.setContentText(text);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        ntfMgr.notify(mNotificationID, builder.build());
    }
}
