package com.transcend.otg.Loader;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.text.format.Formatter;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Utils.FileFactory;


import java.io.File;
import java.util.ArrayList;


/**
 * Created by henry_hsu on 2017/2/10.
 */

public class SearchLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {
    private ArrayList<FileInfo> mFileList;
    private Context mContext;
    private String mQueryText;
    private String mOTGPath;
    private String mSdCardPath;
    private Uri baseRootUri;

    public SearchLoader(Context context, String query_text) {
        super(context);
        mQueryText = query_text;
        mFileList = new ArrayList<>();
        mContext = context;
        if (Constant.rootUri != null)
            baseRootUri = DocumentsContract.buildChildDocumentsUriUsingTree(Constant.rootUri, DocumentsContract.getTreeDocumentId(Constant.rootUri));
    }

    @Override
    public ArrayList<FileInfo> loadInBackground() {
        mFileList.clear();
        mSdCardPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
        if (baseRootUri != null) {
            mOTGPath = FileFactory.getOuterStoragePath(mContext, Constant.otg_key_path);
            if (mOTGPath != null) {
                searchAllOtg(baseRootUri);
            }
        }
        return searchAll();

    }

    @Override
    protected void onStartLoading() {
        if (takeContentChanged() || mFileList.size() == 0)
            forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        cancelLoad();
    }

    private ArrayList<FileInfo> searchAll() {

        try {
            String[] proj = {
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.DATE_MODIFIED,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.SIZE};
            Uri contextUri = MediaStore.Files.getContentUri("external");

            final String orderBy = MediaStore.Files.FileColumns.DATE_MODIFIED;
            Cursor cursor = mContext.getContentResolver().query(
                    contextUri, proj,
                    null, null, orderBy + " DESC");
            if (cursor != null) {
                int pathColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                int mimeColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE);
                int typeColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
                int timeColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED);
                int sizeColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);
                while (cursor.moveToNext()) {
                    Uri fileUri = ContentUris.withAppendedId(contextUri,
                            cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)));
                    String path = cursor.getString(pathColumnIndex);
                    String name = path.substring(path.lastIndexOf('/')+1);
                    String mimeType = cursor.getString(mimeColumnIndex);
                    Long time = 1000 * cursor.getLong(timeColumnIndex);
                    Long size = cursor.getLong(sizeColumnIndex);
                    File check_file = new File(path);
                    if (check_file.exists() == false)
                        continue;
                    if (!path.contains("/.") && name.toLowerCase().contains(mQueryText.toLowerCase())) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = path;
                        if(mSdCardPath != null){
                            if (path.contains(mSdCardPath))
                                fileInfo.storagemode = Constant.STORAGEMODE_SD;
                        }

                        fileInfo.name = name;
                        fileInfo.time = FileInfo.getTime(time);
                        switch (cursor.getInt(typeColumnIndex)) {
                            case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                                fileInfo.type = Constant.TYPE_PHOTO;
                                fileInfo.uri = fileUri;
                                fileInfo.format_size = Formatter.formatFileSize(mContext, size);
                                break;
                            case MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO:
                                fileInfo.type = Constant.TYPE_MUSIC;
                                break;
                            case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                                fileInfo.type = Constant.TYPE_VIDEO;
                                break;
                            default:
                                if (mimeType != null && (mimeType.contains(TEXT) || mimeType.contains(PDF) || mimeType.contains(WORD) || mimeType.contains(PPT) || mimeType.contains(EXCEL))) {
                                    fileInfo.type = Constant.TYPE_DOC;
                                } else {
                                    File file = new File(path);
                                    if (file.exists() && file.isDirectory()) {
                                        fileInfo.type = Constant.TYPE_DIR;
                                    } else if (name.contains(ENCRYPT)) {
                                        fileInfo.type = Constant.TYPE_ENCRYPT;
                                    } else {
                                        fileInfo.type = Constant.TYPE_OTHER_FILE;
                                    }
                                }
                                break;
                        }
                        mFileList.add(fileInfo);
                    }
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return mFileList;
        }
        return mFileList;
    }

    String[] proj = {DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_MIME_TYPE};
    String APPLICATION = "application";
    String IMAGE = "image";
    String WORD = "word";
    String PDF = "pdf";
    String PPT = "powerpoint";
    String EXCEL = "excel";
    String TEXT = "text";
    String VIDEO = "video";
    String AUDIO = "audio";
    String ENCRYPT = "enc";
    String DIR = "directory";
    String PNG = "png";
    String JPG = "jpg";
    private ArrayList<FileInfo> searchAllOtg(Uri _rootUri) {

        Cursor cursor = mContext.getContentResolver().query(_rootUri, proj, null, null, null);
        if (cursor == null) {
            cancelLoad();
            return null;
        }
        int cursor_index_ID = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
        while (cursor.moveToNext()) {
            if (!cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                String name = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                if (type.contains(DIR)) {
                    searchAllOtg(DocumentsContract.buildChildDocumentsUriUsingTree(_rootUri, cursor.getString(cursor_index_ID)));
                }
                if (name.toLowerCase().contains(mQueryText.toLowerCase())) {
                    FileInfo item = new FileInfo();
                    item.name = name;
                    item.time = FileInfo.getTime(cursor.getLong(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = cursor.getLong(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.format_size = Formatter.formatFileSize(mContext, item.size);
                    String[] split = cursor.getString(cursor_index_ID).split(":");
                    item.path = mOTGPath + "/" + split[1];
                    item.storagemode = Constant.STORAGEMODE_OTG;
                    item.uri = DocumentsContract.buildDocumentUriUsingTree(_rootUri, cursor.getString(cursor_index_ID));

                    if (type.contains(IMAGE) || type.contains(PNG) || type.contains(JPG)) {
                        item.type = Constant.TYPE_PHOTO;
                    } else if (type.contains(VIDEO)) {
                        item.type = Constant.TYPE_VIDEO;
                    } else if (type.contains(AUDIO)) {
                        item.type = Constant.TYPE_MUSIC;
                    } else if (type.contains(TEXT) || type.contains(PDF) || type.contains(WORD) || type.contains(PPT) || type.contains(EXCEL)) {
                        item.type = Constant.TYPE_DOC;
                    } else if (name.contains(ENCRYPT)) {
                        item.type = Constant.TYPE_ENCRYPT;
                    } else if (type.contains(DIR)) {
                        item.type = Constant.TYPE_DIR;
                    } else {
                        item.type = Constant.TYPE_OTHER_FILE;
                    }

                    mFileList.add(item);
                }
            }
        }
        cursor.close();

        return mFileList;
    }

}
