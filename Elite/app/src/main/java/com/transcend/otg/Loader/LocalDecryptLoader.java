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

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by wangbojie on 2017/3/23.
 */
public class LocalDecryptLoader extends AsyncTaskLoader<Boolean> {
    private static final String TAG = LocalDecryptLoader.class.getSimpleName();

    private Activity mActivity;
    private HandlerThread mThread;
    private Handler mHandler;
    private Runnable mWatcher;

    private String mPassword, mFolderPath, mFilePath;
    private int mNotificationID = 0;

    public LocalDecryptLoader(Context context, List<String> decryptList) {
        super(context);
        mActivity = (Activity) context;
        mFolderPath = decryptList.get(0);
        mPassword = decryptList.get(1);
        mFilePath = decryptList.get(2);
        mNotificationID = FileFactory.getInstance().getNotificationID();
    }

    @Override
    public Boolean loadInBackground() {
        try {
            return decrypt();
        } catch (IOException e) {
            updateResult(getContext().getString(R.string.error));
            closeProgressWatcher();
            e.printStackTrace();
        } catch (ZipException e) {
            updateResult(getContext().getString(R.string.password_incorrect));
            closeProgressWatcher();
            File extractFile = new File(mFolderPath);
            if (extractFile.exists())
                deleteDirectory(extractFile);
            e.printStackTrace();
        }
        return false;
    }

    private boolean decrypt() throws IOException, ZipException {
        updateProgress(getContext().getResources().getString(R.string.loading));
        ZipFile zipFile = new ZipFile(mFilePath);
        if (zipFile.isEncrypted()) {
            zipFile.setPassword(mPassword);
        }
        File extractFile = new File(mFolderPath);
        boolean b_mkdir = false;
        if (!extractFile.exists())
            b_mkdir = extractFile.mkdir();
        if(b_mkdir){
            startProgressWatcher();
            zipFile.extractAll(extractFile.getPath());
            closeProgressWatcher();
        }
        updateResult(getContext().getString(R.string.done));
        mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(extractFile)));
        File[] broadcastFiles = extractFile.listFiles();
        for (File file : broadcastFiles) {
            if (file.isHidden()) continue;
            if (file.isDirectory())
                checkDirectory(file);
            else
                mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        }
        return true;
    }

    private void checkDirectory(File source) {
        mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(source)));
        File[] files = source.listFiles();
        for (File file : files) {
            if (file.isHidden())
                continue;
            if (file.isDirectory())
                checkDirectory(file);
            else
                mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        }
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
                    updateProgress(getContext().getResources().getString(R.string.decrypt));
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
        String type = getContext().getResources().getString(R.string.decrypt);
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

        String type = getContext().getResources().getString(R.string.decrypting);
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

    private void deleteDirectory(File dir) {
        for (File target : dir.listFiles()) {
            if (target.isDirectory())
                deleteDirectory(target);
            else
                target.delete();
        }
        dir.delete();
    }
}
