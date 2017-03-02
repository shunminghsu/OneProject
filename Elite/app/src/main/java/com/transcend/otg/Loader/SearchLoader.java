package com.transcend.otg.Loader;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;

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
        if (baseRootUri != null) {
            mOTGPath = FileFactory.getOuterStoragePath(mContext, Constant.otg_key_path);
            if (mOTGPath != null && FileFactory.getMountedState(mContext, mOTGPath)) {
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
                    MediaStore.Files.FileColumns.DATA};
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
                while (cursor.moveToNext()) {
                    Uri fileUri = ContentUris.withAppendedId(contextUri,
                            cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)));
                    String path = cursor.getString(pathColumnIndex);
                    String name = path.substring(path.lastIndexOf('/')+1);
                    String mimeType = cursor.getString(mimeColumnIndex);
                    Long time = 1000 * cursor.getLong(timeColumnIndex);

                    if (!path.contains("/.") && name.toLowerCase().contains(mQueryText.toLowerCase())) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = path;
                        fileInfo.name = name;
                        fileInfo.time = FileInfo.getTime(time);
                        switch (cursor.getInt(typeColumnIndex)) {
                            case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                                fileInfo.type = FileInfo.TYPE.PHOTO;
                                fileInfo.uri = fileUri;
                                break;
                            case MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO:
                                fileInfo.type = FileInfo.TYPE.MUSIC;
                                break;
                            case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                                fileInfo.type = FileInfo.TYPE.VIDEO;
                                break;
                            default:
                                if (mimeType != null && (mimeType.contains(TEXT) || mimeType.contains(PDF) || mimeType.contains(WORD) || mimeType.contains(PPT) || mimeType.contains(EXCEL))) {
                                    fileInfo.type = FileInfo.TYPE.FILE;
                                } else {
                                    File file = new File(path);
                                    if (file.exists() && file.isDirectory()) {
                                        fileInfo.type = FileInfo.TYPE.DIR;
                                    } else if (name.contains(ENCRYPT)) {
                                        fileInfo.type = FileInfo.TYPE.ENCRYPT;
                                    } else {
                                        fileInfo.type = FileInfo.TYPE.OTHERS;
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

        Cursor encCursor = mContext.getContentResolver().query(_rootUri, proj, null, null, null);
        int cursor_index_ID = encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
        while (encCursor.moveToNext()) {
            if (!encCursor.getString(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = encCursor.getString(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                String name = encCursor.getString(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                if (type.contains(DIR)) {
                    searchAllOtg(DocumentsContract.buildChildDocumentsUriUsingTree(_rootUri, encCursor.getString(cursor_index_ID)));
                }
                if (name.toLowerCase().contains(mQueryText.toLowerCase())) {
                    FileInfo item = new FileInfo();
                    item.name = name;
                    item.time = FileInfo.getTime(encCursor.getLong(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = encCursor.getLong(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    String[] split = encCursor.getString(cursor_index_ID).split(":");
                    item.path = mOTGPath + "/" + split[1];
                    item.uri = DocumentsContract.buildDocumentUriUsingTree(_rootUri, encCursor.getString(cursor_index_ID));

                    if (type.contains(IMAGE) || type.contains(PNG) || type.contains(JPG)) {
                        item.type = FileInfo.TYPE.PHOTO;
                    } else if (type.contains(VIDEO)) {
                        item.type = FileInfo.TYPE.VIDEO;
                    } else if (type.contains(AUDIO)) {
                        item.type = FileInfo.TYPE.MUSIC;
                    } else if (type.contains(TEXT) || type.contains(PDF) || type.contains(WORD) || type.contains(PPT) || type.contains(EXCEL)) {
                        item.type = FileInfo.TYPE.FILE;
                    } else if (name.contains(ENCRYPT)) {
                        item.type = FileInfo.TYPE.ENCRYPT;
                    } else if (type.contains(DIR)) {
                        item.type = FileInfo.TYPE.DIR;
                    } else {
                        item.type = FileInfo.TYPE.OTHERS;
                    }

                    mFileList.add(item);
                }
            }
        }
        encCursor.close();

        return mFileList;
    }

}
