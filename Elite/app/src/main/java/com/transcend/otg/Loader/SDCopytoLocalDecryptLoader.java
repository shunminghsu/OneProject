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
import com.transcend.otg.Utils.FileFactory;
import com.transcend.otg.Utils.MathUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangbojie on 2017/3/20.
 */
public class SDCopytoLocalDecryptLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = SDCopytoLocalDecryptLoader.class.getSimpleName();

    private Activity mActivity;
    private HandlerThread mThread;
    private Handler mHandler;
    private Runnable mWatcher;
    private ArrayList<DocumentFile> mSrcDocumentFileList;
    private String mDesFile;
    private int mNotificationID = 0;

    public SDCopytoLocalDecryptLoader(Context context, ArrayList<DocumentFile> src, String des) {
        super(context);
        mActivity = (Activity) context;
        mSrcDocumentFileList = src;
        mDesFile = des;
        mNotificationID = FileFactory.getInstance().getNotificationID();
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
        for (DocumentFile file : mSrcDocumentFileList) {
            if (file.isDirectory()) {
                copyDirectoryTask(mActivity, file, mDesFile);
            } else {
                copyFileTask(mActivity, file, mDesFile);
            }
        }
        updateResult(getContext().getString(R.string.done));
        return true;
    }

    private void copyDirectoryTask(Context context, DocumentFile srcFileItem, String destFileItem) throws IOException {
        String uniqueName = createUniqueFoloderName(srcFileItem, destFileItem);
        File destDirectory = new File(uniqueName);
        destDirectory.mkdir();
        mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(destDirectory)));

        DocumentFile[] files = srcFileItem.listFiles();
        for (DocumentFile file : files) {
            if (file.isDirectory()) {
                copyDirectoryTask(mActivity, file, destDirectory.getAbsolutePath());
            } else {//is file
                copyFileTask(mActivity, file, destDirectory.getAbsolutePath());
            }
        }
    }

    private String createUniqueFoloderName(DocumentFile source, String destination) {
        String sourceName = source.getName();
        String alreadyExistDest = "";
        String finalUniqueName = sourceName;
        File dir = new File(destination);
        File[] intoDir = dir.listFiles();
        List<String> names = new ArrayList<String>();
        if (intoDir != null) {
            for (File tmpFile : intoDir) {
                names.add(tmpFile.getName());
                if (tmpFile.isDirectory() && tmpFile.getName().equals(sourceName))
                    alreadyExistDest = tmpFile.getName();
            }
            if (alreadyExistDest.equals("")) {
                return destination + File.separator + sourceName;
            } else {
                int index = 1;
                while (names.contains(finalUniqueName)) {
                    finalUniqueName = String.format(sourceName + "_%d", index++);
                }
                return destination + File.separator + finalUniqueName;
            }
        } else {
            File desFile = new File(destination);
            desFile.mkdir();
            return destination + File.separator + sourceName;
        }

    }

    private String createUniqueFileName(DocumentFile source, String destination) {
        String sourceName = source.getName();
        String[] sourceNameSplit = sourceName.split("\\.");
        String Pre = getPre(sourceNameSplit);
        String sourceType = "." + sourceNameSplit[sourceNameSplit.length - 1];
        String alreadyExistDest = "";
        String finalUniqueName = sourceName;
        File dir = new File(destination);
        File[] intoDir = dir.listFiles();
        List<String> names = new ArrayList<String>();
        if (intoDir != null) {
            for (File tmpFile : intoDir) {
                names.add(tmpFile.getName());
                if (tmpFile.isFile() && tmpFile.getName().equals(sourceName))
                    alreadyExistDest = tmpFile.getName();
            }
            if (alreadyExistDest.equals("")) {
                return destination + File.separator + sourceName;
            } else {
                int index = 1;
                while (names.contains(finalUniqueName)) {
                    finalUniqueName = String.format(Pre + "_%d" + sourceType, index++);
                }
                return destination + File.separator + finalUniqueName;
            }
        } else {
            File desFile = new File(destination);
            desFile.mkdir();
            return destination + File.separator + sourceName;
        }

    }

    private String getPre(String[] sourceArray) {
        String Pre = "";
        for (int index = 0; index < sourceArray.length - 1; index++) {
            Pre += sourceArray[index];
        }
        return Pre;
    }

    private void copyFileTask(Context context, DocumentFile srcFileItem, String destFileItem) throws IOException {
        String uniqueName = createUniqueFileName(srcFileItem, destFileItem);
        File destFile = new File(uniqueName);
        destFile.createNewFile();
        int total = (int) srcFileItem.length();
        startProgressWatcher(destFile, total);
        copyFile(context, srcFileItem, destFile);
        mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(destFile)));
        closeProgressWatcher();
        updateProgress(destFile.getName(), total, total);
    }

    public boolean copyFile(Context context, DocumentFile srcFileItem, File destFileItem) {
        if (srcFileItem.isFile()) {
            OutputStream out = null;
            InputStream in = null;
            ContentResolver resolver = context.getContentResolver();
            try {
                in = resolver.openInputStream(srcFileItem.getUri());
                out = resolver.openOutputStream(Uri.fromFile(destFileItem));
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

    private void startProgressWatcher(final File target, final int total) {
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

        String type = getContext().getResources().getString(R.string.decrypt);
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
        ntfMgr.notify(mNotificationID, builder.build());
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
}
