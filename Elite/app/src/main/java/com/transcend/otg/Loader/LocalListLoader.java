package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.Formatter;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.LocalPreferences;
import com.transcend.otg.Constant.FileInfo;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/2/6.
 */

public class LocalListLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = LocalListLoader.class.getSimpleName();

    private ArrayList<FileInfo> mFileList;
    private String mPath;
    private Context mContext;
    private int mSortBy;
    private Boolean mSortOrderAsc = false;
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

    public LocalListLoader(Context context, String path) {
        super(context);
        mContext = context;
        mFileList = new ArrayList<FileInfo>();
        mPath = path;
        mSortBy = LocalPreferences.getPref(mContext, LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_DATE);
        mSortOrderAsc = LocalPreferences.getPref(mContext,
                LocalPreferences.BROWSER_SORT_ORDER_PREFIX, Constant.SORT_ORDER_AS) == Constant.SORT_ORDER_AS;
    }

    @Override
    public Boolean loadInBackground() {
        return updateFileList();
    }

    private boolean updateFileList() {
        if (mPath == null)
            return false;
        try {


            String[] proj = {
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.DATE_MODIFIED,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.SIZE};
            Uri contextUri = MediaStore.Files.getContentUri("external");

            String orderBy = MediaStore.Files.FileColumns.DATE_MODIFIED;
            if (mSortBy == Constant.SORT_BY_NAME)
                orderBy = MediaStore.Files.FileColumns.DISPLAY_NAME;
            else if (mSortBy == Constant.SORT_BY_SIZE)
                orderBy = MediaStore.Files.FileColumns.SIZE;
            String order = mSortOrderAsc ? " ASC" : " DESC";
            Cursor cursor = mContext.getContentResolver().query(
                    contextUri, proj,
                    null, null, orderBy + order);
            if (cursor != null) {
                int pathColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                int mimeColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE);
                int typeColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
                int timeColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED);
                int sizeColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);
                while (cursor.moveToNext()) {
                    String path = cursor.getString(pathColumnIndex);
                    File check_file = new File(path);
                    if (check_file.exists() == false)
                        continue;
                    if (!path.contains("/.") && path.contains(mPath) && path.lastIndexOf('/') == mPath.length()) {
                        Uri fileUri = ContentUris.withAppendedId(contextUri,
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)));
                        String name = path.substring(path.lastIndexOf('/') + 1);
                        String mimeType = cursor.getString(mimeColumnIndex);
                        Long time = 1000 * cursor.getLong(timeColumnIndex);
                        Long size = cursor.getLong(sizeColumnIndex);

                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = path;

                        if (mPath == Constant.ROOT_LOCAL)
                            fileInfo.storagemode = Constant.STORAGEMODE_LOCAL;
                        else
                            fileInfo.storagemode = Constant.STORAGEMODE_SD;
                        fileInfo.name = name;
                        fileInfo.time = FileInfo.getTime(time);
                        fileInfo.size = size;
                        fileInfo.format_size = Formatter.formatFileSize(mContext, size);
                        switch (cursor.getInt(typeColumnIndex)) {
                            case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                                fileInfo.type = Constant.TYPE_PHOTO;
                                fileInfo.uri = fileUri;
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
            return false;


        }
        return true;
    }

    public String getPath() {
        return mPath;
    }

    public ArrayList<FileInfo> getFileList() {
        return mFileList;
    }

}
