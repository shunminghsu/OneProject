package com.transcend.otg.Loader;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.transcend.otg.R;
import com.transcend.otg.Utils.MathUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangbojie on 2017/3/20.
 */
public class LocalCopytoOTGDecryptLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = LocalCopytoOTGDecryptLoader.class.getSimpleName();

    private Activity mActivity;
    private HandlerThread mThread;
    private Handler mHandler;
    private Runnable mWatcher;
    private DocumentFile mDesDocumentFile;
    private List<String> mSrcFile;

    public LocalCopytoOTGDecryptLoader(Context context, List<String> src, ArrayList<DocumentFile> des) {
        super(context);
        mActivity = (Activity) context;
        mSrcFile = src;
        mDesDocumentFile = des.get(0);
    }

    @Override
    public Boolean loadInBackground() {
        try {
            return Copy();
        } catch (IOException e) {
            e.printStackTrace();
            closeProgressWatcher();
            updateResult(getContext().getString(R.string.error));
        }
        return false;
    }

    private boolean Copy() throws IOException {
        updateProgress(getContext().getResources().getString(R.string.loading), 0, 0);
        for (String path : mSrcFile) {
            File source = new File(path);
            if (source.isDirectory()) {
                copyDirectoryTask(mActivity, source, mDesDocumentFile);
            } else {
                copyFileTask(mActivity, source, mDesDocumentFile);
            }
        }
        updateResult(getContext().getString(R.string.done));
        return true;
    }

    private void copyDirectoryTask(Context context, File srcFileItem, DocumentFile destFileItem) throws IOException {
        String name = createUniqueFoloderName(srcFileItem, destFileItem);
        DocumentFile destDirectory = destFileItem.createDirectory(name);
        File[] files = srcFileItem.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                copyDirectoryTask(mActivity, file, destDirectory);
            } else {//is file
                copyFileTask(mActivity, file, destDirectory);
            }
        }
    }

    private void copyFileTask(Context context, File srcFileItem, DocumentFile destFileItem) throws IOException {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String type = fileNameMap.getContentTypeFor(srcFileItem.getAbsolutePath());
        if (type == null) type = "";
        DocumentFile destFile = destFileItem.createFile(type, srcFileItem.getName());
        int total = (int) srcFileItem.length();
        startProgressWatcher(destFile, total);
        copyFile(context, srcFileItem, destFile);
        closeProgressWatcher();
        updateProgress(destFile.getName(), total, total);
    }

    public boolean copyFile(Context context, File srcFileItem, DocumentFile destFileItem) {
        if (srcFileItem.isFile()) {
            OutputStream out = null;
            InputStream in = null;
            ContentResolver resolver = context.getContentResolver();
            try {
                in = resolver.openInputStream(Uri.fromFile(srcFileItem));
                out = resolver.openOutputStream(destFileItem.getUri());
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
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

    private String createUniqueFoloderName(File source, DocumentFile destination) {
        String sourceName = source.getName();
        String alreadyExistDest = "";
        String finalUniqueName = sourceName;
        DocumentFile[] intoDir = destination.listFiles();
        List<String> names = new ArrayList<String>();
        if (intoDir != null) {
            for (DocumentFile tmpFile : intoDir) {
                names.add(tmpFile.getName());
                if (tmpFile.isDirectory() && tmpFile.getName().equals(sourceName))
                    alreadyExistDest = tmpFile.getName();
            }
            if (alreadyExistDest.equals("")) {
                return sourceName;
            } else {
                int index = 1;
                while (names.contains(finalUniqueName)) {
                    finalUniqueName = String.format(sourceName + "_%d", index++);
                }
                return finalUniqueName;
            }
        } else {
            destination.createDirectory(sourceName);
            return sourceName;
        }

    }

    private void startProgressWatcher(final DocumentFile target, final int total) {
        mThread = new HandlerThread(TAG);
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
        mHandler.post(mWatcher = new Runnable() {
            @Override
            public void run() {
                int count = (int) target.length();
                if (mHandler != null) {
                    mHandler.postDelayed(mWatcher, 1000);
                    updateProgress(target.getName(), count, total);
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

    private void updateProgress(String name, int count, int total) {
        Log.w(TAG, "progress: " + count + "/" + total + ", " + name);

        int max = (count == total) ? 0 : 100;
        int progress = 0;
        if (total > 100 && count > 0)
            progress = (total > 0) ? count / (total / 100) : 0;
        boolean indeterminate = (total == 0);
        int icon = R.mipmap.icon_elite_logo;

        String type = getContext().getResources().getString(R.string.encrypt);
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
        ntfMgr.notify(0, builder.build());
    }
}
