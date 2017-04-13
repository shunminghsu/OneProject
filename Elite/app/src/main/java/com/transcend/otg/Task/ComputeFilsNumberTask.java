package com.transcend.otg.Task;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.provider.DocumentFile;
import android.widget.TextView;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.R;
import com.transcend.otg.Utils.FileFactory;

import java.io.File;

/**
 * Created by henry_hsu on 2017/3/17.
 */

public class ComputeFilsNumberTask extends AsyncTask<String, Void, int[]> {
    private final FileInfo mFileInfo;
    private TextView mFileNumbersView;
    private Context mContext;


    public ComputeFilsNumberTask(Context context, FileInfo fileInfo, TextView textview) {
        mContext = context;
        mFileInfo = fileInfo;
        mFileNumbersView = textview;
    }

    @Override
    protected int[] doInBackground(String... params) {
        int[] file_directory_numbers = {0, 0};
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) &&
                mFileInfo.storagemode == Constant.STORAGEMODE_OTG) {
            //Uri baseRootUri = DocumentsContract.buildChildDocumentsUriUsingTree(Constant.rootUri, DocumentsContract.getTreeDocumentId(Constant.rootUri));
            //getFilsNumber(baseRootUri, mFileInfo.path, file_directory_numbers);
            getDocumentFilsNumber(mFileInfo, file_directory_numbers);
        } else if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) &&
                mFileInfo.storagemode == Constant.STORAGEMODE_SD) {
            getFilsDirNumber(mFileInfo.path, file_directory_numbers);
        } else {
            getFilsDirNumber(mFileInfo.path, file_directory_numbers);
        }
        return file_directory_numbers;
    }

    @Override
    protected void onPostExecute(int[] result) {
        if (mFileNumbersView != null) {
            if (result[0] == 0 && result[1] == 0) {
                mFileNumbersView.setText(mContext.getResources().getString(R.string.empty));
            } else {
                String file_text;
                if (result[0] <= 1)
                    file_text = mContext.getResources().getString(R.string.info_file_number_text1);
                else
                    file_text = mContext.getResources().getString(R.string.info_file_number_text2);
                String dir_text;
                if (result[1] <= 1)
                    dir_text = mContext.getResources().getString(R.string.info_dir_number_text1);
                else
                    dir_text = mContext.getResources().getString(R.string.info_dir_number_text2);

                mFileNumbersView.setText(result[0] + " " + file_text + ",  " + result[1] + " " + dir_text);
            }
        }
    }

    private void getFilsNumber(DocumentFile dfile, int[] _file_directory_numbers) {
        for (DocumentFile df : dfile.listFiles()) {
            if (df.isDirectory()) {
                getFilsNumber(df, _file_directory_numbers);
                _file_directory_numbers[1]++;
            } else {
                _file_directory_numbers[0]++;
            }
        }
    }

    private void getDocumentFilsNumber(FileInfo fileInfo,  int[] _file_directory_numbers) {
        DocumentFile dfile = FileFactory.findDocumentFilefromName(mContext, fileInfo);

        for (DocumentFile df : dfile.listFiles()) {
            if (df.isDirectory()) {
                getFilsNumber(df, _file_directory_numbers);
                _file_directory_numbers[1]++;
            } else {
                _file_directory_numbers[0]++;
            }
        }
    }
/*
    private void getFilsNumber(Uri _rootUri, String folder_path, int[] _file_directory_numbers) {
        String[] proj = {
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_MIME_TYPE};
        Cursor cursor = mContext.getContentResolver().query(_rootUri, proj, null, null, null);
        if (cursor == null)
            return;
        while (cursor.moveToNext()) {
            String type = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
            int cursor_index_ID = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
            if (type.contains("directory")) {
                getFilsNumber(DocumentsContract.buildChildDocumentsUriUsingTree(_rootUri, cursor.getString(cursor_index_ID)), folder_path, _file_directory_numbers);
                _file_directory_numbers[1]++;
            } else {
                String[] split = cursor.getString(cursor_index_ID).split(":");
                String path = "/" + split[1];
                if (!path.contains("/.") && path.startsWith(folder_path)) {
                    _file_directory_numbers[0]++;
                }
            }
        }
        cursor.close();
    }
*/
    private void getFilsDirNumber(String folder_path, int[] _file_directory_numbers) {
        int f_number = 0;
        int d_number = 0;
        try {
            String[] proj = {
                    MediaStore.Files.FileColumns.DATA};
            Uri contextUri = MediaStore.Files.getContentUri("external");
            Cursor cursor = mContext.getContentResolver().query(
                    contextUri, proj, null, null, null);
            if (cursor != null) {
                int pathColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                while (cursor.moveToNext()) {
                    String path = cursor.getString(pathColumnIndex);
                    if (!path.contains("/.") && path.startsWith(folder_path)) {
                        File check_file = new File(path);
                        if (check_file.exists() == false || folder_path.equals(path))
                            continue;
                        if (check_file.isDirectory())
                            d_number++;
                        else
                            f_number++;
                    }
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        _file_directory_numbers[0] = f_number;
        _file_directory_numbers[1] = d_number;
    }
}