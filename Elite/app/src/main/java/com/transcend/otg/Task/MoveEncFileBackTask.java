package com.transcend.otg.Task;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.NotificationCompat;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.R;
import com.transcend.otg.Utils.FileFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by henry_hsu on 2017/5/9.
 */

public class MoveEncFileBackTask extends AsyncTask<DocumentFile, String, Boolean> {
    private Context mContext;
    private FileInfo mSource;
    private DocumentFile desDfile;//for sendBroadcast Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
    private int mDesStorageMode;
    private String mDesDirPath;
    private File mNewFile;//for sendBroadcast Intent.ACTION_MEDIA_SCANNER_SCAN_FILE

    MoveEncFileBackTask(Context context, FileInfo source, int des_storage_mode, String des_dir_path) {
        mContext = context;
        mSource = source;
        mDesStorageMode = des_storage_mode;
        mDesDirPath = des_dir_path;
    }


    @Override
    protected Boolean doInBackground(DocumentFile... params) {
        String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
        if (mDesStorageMode == Constant.STORAGEMODE_SD) {//local to sd

            DocumentFile destDDir = findDocumentFilefromPath(mDesDirPath, sdPath, Constant.mSDRootDocumentFile);

            File parent = new File(mDesDirPath);
            File f2 = new File(parent, mSource.name);
            mNewFile = f2;
            desDfile = destDDir.createFile("image", mSource.name);
            return copydFile(new File(mSource.path), desDfile);
        } else if (mDesStorageMode == Constant.STORAGEMODE_OTG) {//local to otg

            DocumentFile destDDir = params[0];

            return copydFile(new File(mSource.path), destDDir.createFile("image", mSource.name));
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {

        super.onPostExecute(result);
        if (result) {
            updateProgress(mContext.getString(R.string.done), mContext);
            if (mDesStorageMode == Constant.STORAGEMODE_SD) {
                MediaScannerConnection.scanFile(mContext, new String[]{mNewFile.getPath()}, new String[]{desDfile.getType()}, null);
            }
            File f = new File(mSource.path);
            f.delete();
        } else {
            updateProgress(mContext.getString(R.string.fail), mContext);
        }
    }

    private boolean copydFile(File source, DocumentFile destination) {
        if (destination == null)
            return false;
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = mContext.getContentResolver().openOutputStream(destination.getUri());
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private DocumentFile findDocumentFilefromPath(String des_path, String sd_Path, DocumentFile currentDocumentFile){
        DocumentFile mDocumentFile = null;

        if(currentDocumentFile != null){
            String path = des_path;
            path = path.replace(sd_Path, "");
            String[] array = path.split("/");
            DocumentFile tmp = currentDocumentFile;
            for(int i= 1;i<array.length;i++){
                tmp = tmp.findFile(array[i]);
            }
            mDocumentFile = tmp;
        }
        return mDocumentFile;
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