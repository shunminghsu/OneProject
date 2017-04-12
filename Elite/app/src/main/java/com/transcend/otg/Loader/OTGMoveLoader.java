package com.transcend.otg.Loader;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.R;
import com.transcend.otg.Utils.MathUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/3/17.
 */
public class OTGMoveLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = OTGMoveLoader.class.getSimpleName();

    private Activity mActivity;
    private HandlerThread mThread;
    private Handler mHandler;
    private Runnable mWatcher;
    private ArrayList<DocumentFile> mSrcDocumentFileList;
    private DocumentFile mDesDocumentFile;
    private boolean b_SDCard = false;
    private String destinationPath;

    public OTGMoveLoader(Context context, ArrayList<DocumentFile> src, ArrayList<DocumentFile> des, String path) {
        super(context);
        mActivity = (Activity) context;
        mSrcDocumentFileList = src;
        mDesDocumentFile = des.get(0);
        if(Constant.mSDRootDocumentFile != null){
            if(mDesDocumentFile.getUri().toString().contains(Constant.mSDRootDocumentFile.getUri().toString())){
                b_SDCard = true;
                destinationPath = path;
            }
        }
    }

    @Override
    public Boolean loadInBackground() {
        try {
            return move();
        } catch (IOException e) {
            e.printStackTrace();
            closeProgressWatcher();
            updateResult(getContext().getString(R.string.error));
        }
        return false;
    }

    private boolean move() throws IOException {
        try {
            updateProgress(getContext().getResources().getString(R.string.loading), 0, 0);
            for (DocumentFile file : mSrcDocumentFileList) {
                if (file.isDirectory()) {
                    moveDirectoryTask(mActivity, file, mDesDocumentFile);
                } else {
                    moveFileTask(mActivity, file, mDesDocumentFile);
                }
            }
            updateResult(getContext().getString(R.string.done));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void moveDirectoryTask(Context context, DocumentFile srcFileItem, DocumentFile destFileItem) throws IOException {
        try {
            if (srcFileItem.length() > 0 && destFileItem.length() > 0) {
                DocumentFile destDirectory = destFileItem.createDirectory(srcFileItem.getName());
                if(b_SDCard){
                    String sdPath = destinationPath + File.separator + srcFileItem.getName();
                    MediaScannerConnection.scanFile(mActivity, new String[]{sdPath}, new String[]{destDirectory.getType()}, null);
                }
                DocumentFile[] files = srcFileItem.listFiles();
                for (DocumentFile file : files) {
                    if (file.isDirectory()) {
                        moveDirectoryTask(mActivity, file, destDirectory);
                    } else {//is file
                        moveFileTask(mActivity, file, destDirectory);
                    }
                }
                srcFileItem.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void moveFileTask(Context context, DocumentFile srcFileItem, DocumentFile destFileItem) throws IOException {

        if (srcFileItem.length() > 0 && destFileItem.length() > 0) {
            try {
                DocumentFile destfile = destFileItem.createFile(srcFileItem.getType(), srcFileItem.getName());
                int total = (int) srcFileItem.length();
                startProgressWatcher(destfile, total);
                moveFile(context, srcFileItem, destfile);
                if(b_SDCard){
                    String sdPath = destinationPath + File.separator + srcFileItem.getName();
                    MediaScannerConnection.scanFile(mActivity, new String[]{sdPath}, new String[]{destfile.getType()}, null);
                }
                closeProgressWatcher();
                updateProgress(destfile.getName(), total, total);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean moveFile(Context context, DocumentFile srcFileItem, DocumentFile destFileItem) throws IOException {
        if (srcFileItem.isFile()) {
            OutputStream out = null;
            InputStream in = null;
            ContentResolver resolver = context.getContentResolver();
            try {
                in = resolver.openInputStream(srcFileItem.getUri());
                out = resolver.openOutputStream(destFileItem.getUri());
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                srcFileItem.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else if (srcFileItem.isDirectory()) {
            return true;
        } else {
            try {
                throw new Exception("item is not a file");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

    }

    private void startProgressWatcher(final DocumentFile target, final int total) {
        try {
            mThread = new HandlerThread(TAG);
            mThread.start();
            mHandler = new Handler(mThread.getLooper());
            mHandler.post(mWatcher = new Runnable() {
                @Override
                public void run() {
                    int count = 0;
                    if (target != null)
                        count = (int) target.length();
                    if (mHandler != null) {
                        mHandler.postDelayed(mWatcher, 1000);
                        updateProgress(target.getName(), count, total);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void updateProgress(String name, int count, int total) {
        try {
            Log.w(TAG, "progress: " + count + "/" + total + ", " + name);

            int max = (count == total) ? 0 : 100;
            int progress = 0;
            if (total > 100 && count > 0)
                progress = (total > 0) ? count / (total / 100) : 0;
            boolean indeterminate = (total == 0);
            int icon = R.mipmap.icon_elite_logo;

            String type = getContext().getResources().getString(R.string.move);
            String stat = String.format("%s / %s", MathUtils.getBytes(count), MathUtils.getBytes(total));
            String text = String.format("%s - %s", type, stat);
            String info = String.format("%d%%", progress);

            NotificationManager ntfMgr = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = mActivity.getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
            builder.setSmallIcon(icon);
            builder.setContentTitle(name);
            builder.setContentText(text);
            builder.setContentInfo(info);
            builder.setProgress(max, progress, indeterminate);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);
            ntfMgr.notify(0, builder.build());
        } catch (Exception e) {

        }

    }

    private void updateResult(String result) {
        try {
            Log.w(TAG, "result: " + result);

            int icon = R.mipmap.icon_elite_logo;
            String name = getContext().getResources().getString(R.string.app_name);
            String type = getContext().getResources().getString(R.string.move);
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
            ntfMgr.notify(0, builder.build());
        } catch (Exception e) {

        }

    }
}
