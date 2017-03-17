package com.transcend.otg.Task;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.provider.DocumentFile;
import android.text.format.Formatter;
import android.widget.TextView;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Utils.FileFactory;

import java.io.File;

/**
 * Created by henry_hsu on 2017/3/17.
 */

public class ComputeFilsTotalSizeTask extends AsyncTask<String, Void, String> {
    private final FileInfo mFileInfo;
    private TextView mFileTotalSizeView;
    private Context mContext;

    public ComputeFilsTotalSizeTask(Context context, FileInfo fileInfo, TextView textview) {
        mContext = context;
        mFileInfo = fileInfo;
        mFileTotalSizeView = textview;
    }

    @Override
    protected String doInBackground(String... params) {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) &&
                mFileInfo.storagemode == Constant.STORAGEMODE_OTG) {
            DocumentFile dfile = FileFactory.findDocumentFilefromName(mContext, mFileInfo);
            return "";
        } else if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) &&
                mFileInfo.storagemode == Constant.STORAGEMODE_SD) {
            return getFilsTotalSize(mFileInfo.path);
        } else {
            return getFilsTotalSize(mFileInfo.path);
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (mFileTotalSizeView != null)
            mFileTotalSizeView.setText(result);
    }

    private String getFilsTotalSize(String folder_path) {
        long size = 0;
        try {
            String[] proj = {
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATA};
            Uri contextUri = MediaStore.Files.getContentUri("external");
            Cursor cursor = mContext.getContentResolver().query(
                    contextUri, proj, null, null, null);
            if (cursor != null) {
                int pathColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                int sizeColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);
                while (cursor.moveToNext()) {
                    String path = cursor.getString(pathColumnIndex);
                    if (!path.contains("/.") && path.startsWith(folder_path)) {
                        File check_file = new File(path);
                        if (check_file.exists() == false || check_file.isDirectory())
                            continue;
                        size += cursor.getLong(sizeColumnIndex);
                    }
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return Formatter.formatFileSize(mContext, size);
        }
        return Formatter.formatFileSize(mContext, size);
    }
}