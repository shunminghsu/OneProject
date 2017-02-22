package com.transcend.otg.Loader;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import com.transcend.otg.Constant.FileInfo;


import java.io.File;
import java.util.ArrayList;


/**
 * Created by henry_hsu on 2017/2/10.
 */

public class SearchLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {
    private ArrayList<FileInfo> mFileList;
    private Context mContext;
    private String mQueryText;

    public SearchLoader(Context context, String query_text) {
        super(context);
        mQueryText = query_text;
        mFileList = new ArrayList<>();
        mContext = context;
    }

    @Override
    public ArrayList<FileInfo> loadInBackground() {
        mFileList.clear();
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
                    MediaStore.Files.FileColumns.DATA};
            Uri contextUri = MediaStore.Files.getContentUri("external");

            final String orderBy = MediaStore.Files.FileColumns.TITLE;
            Cursor cursor = mContext.getContentResolver().query(
                    contextUri, proj,
                    null, null, orderBy + " ASC");
            if (cursor != null) {
                int pathColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                int mimeColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE);
                int typeColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
                while (cursor.moveToNext()) {
                    Uri fileUri = ContentUris.withAppendedId(contextUri,
                            cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)));
                    String path = cursor.getString(pathColumnIndex);
                    String name = path.substring(path.lastIndexOf('/')+1);

                    if (!path.contains("/.") && name.toLowerCase().contains(mQueryText.toLowerCase())) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = path;
                        fileInfo.name = name;
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
                                if (cursor.getString(mimeColumnIndex) != null) {
                                    fileInfo.type = FileInfo.TYPE.FILE;
                                } else {
                                    File file = new File(path);
                                    if (file.exists()) {
                                        fileInfo.type = file.isDirectory() ? FileInfo.TYPE.DIR : FileInfo.TYPE.FILE;
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

}
