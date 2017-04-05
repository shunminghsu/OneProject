package com.transcend.otg.Loader;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.provider.DocumentFile;
import android.text.format.Formatter;

import com.transcend.otg.Browser.BrowserFragment;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.LocalPreferences;
import com.transcend.otg.Utils.FileFactory;
import com.transcend.otg.Utils.FileInfoSort;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by wangbojie on 2017/2/13.
 */

public class TabInfoLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {
    private String TAG = TabInfoLoader.class.getSimpleName();
    private ArrayList<FileInfo> mFileList;
    private Context mContext;
    private int mType;
    private int mSortBy;
    private Boolean mSortOrderAsc = false;
    private String mOuterStoragePath;//sdcard or usbotg
    private boolean mIsOtg;
    private DocumentFile dFile = null, rootDFile = null;
    private Uri rootUri = null;
    private Uri baseRootUri;
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
    String[] proj = {DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_MIME_TYPE};


    public TabInfoLoader(Context context, int type, String outer_path, boolean otg) {
        super(context);
        mFileList = new ArrayList<>();
        mType = type;
        mOuterStoragePath = outer_path;
        mIsOtg = otg;
        mContext = context;
        dFile = Constant.mCurrentDocumentFile;
        rootDFile = Constant.mRootDocumentFile;
        rootUri = Constant.rootUri;
        if (rootUri != null)
            baseRootUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri));
    }

    @Override
    public ArrayList<FileInfo> loadInBackground() {
        mSortBy = LocalPreferences.getPref(mContext, LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_DATE);
        mSortOrderAsc = LocalPreferences.getPref(mContext,
                LocalPreferences.BROWSER_SORT_ORDER_PREFIX, Constant.SORT_ORDER_AS) == Constant.SORT_ORDER_AS;
        mFileList.clear();
        switch (mType) {
            case BrowserFragment.LIST_TYPE_IMAGE:
                return mIsOtg ? getSortList(getOtgAllImages(baseRootUri, Constant.STORAGEMODE_OTG)) : getAllImages();
            case BrowserFragment.LIST_TYPE_VIDEO:
                return mIsOtg ? getSortList(getOtgAllVideos(baseRootUri)) : getAllVideos();
            case BrowserFragment.LIST_TYPE_MUSIC:
                return mIsOtg ? getSortList(getOtgAllMusics(baseRootUri)) : getAllMusics();
            case BrowserFragment.LIST_TYPE_DOCUMENT:
                return mIsOtg ? getSortList(getOtgAllDocs(baseRootUri)) : getAllDocs();
            case BrowserFragment.LIST_TYPE_ENCRYPTION:
                return mIsOtg ? getSortList(getOtgAllEncs(baseRootUri)) : getAllEncs();
            case BrowserFragment.LIST_TYPE_FOLDER:
                return mIsOtg ? getSortList(getOtgFileList(baseRootUri)) : getFileList();
            default:
                return null;
        }
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

    private ArrayList<FileInfo> getOtgAllImages(Uri _rootUri, int storage_mode) {

        Cursor imageCursor = mContext.getContentResolver().query(_rootUri, proj, null, null, null);
        if (imageCursor == null) {
            cancelLoad();
            return null;
        }
        int cursor_index_ID = imageCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
        while (imageCursor.moveToNext()) {
            if (!imageCursor.getString(imageCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = imageCursor.getString(imageCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getOtgAllImages(DocumentsContract.buildChildDocumentsUriUsingTree(_rootUri, imageCursor.getString(cursor_index_ID)), storage_mode);
                } else if (type.contains(IMAGE) || type.contains(PNG) || type.contains(JPG)) {
                    FileInfo item = new FileInfo();
                    item.name = imageCursor.getString(imageCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    //item.name = item.name.substring(0, item.name.lastIndexOf("."));
                    item.time = FileInfo.getTime(imageCursor.getLong(imageCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = imageCursor.getLong(imageCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    String[] split = imageCursor.getString(cursor_index_ID).split(":");
                    item.path = mOuterStoragePath + "/" + split[1];
                    item.uri = DocumentsContract.buildDocumentUriUsingTree(_rootUri, imageCursor.getString(cursor_index_ID));
                    item.format_size = Formatter.formatFileSize(mContext, item.size);
                    item.type = Constant.TYPE_PHOTO;
                    item.storagemode = storage_mode;
                    mFileList.add(item);
                }
            }
        }
        imageCursor.close();
        return mFileList;
    }

    private ArrayList<FileInfo> getOtgAllVideos(Uri _rootUri) {

        Cursor videoCursor = mContext.getContentResolver().query(_rootUri, proj, null, null, null);
        if (videoCursor == null) {
            cancelLoad();
            return null;
        }
        int cursor_index_ID = videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
        while (videoCursor.moveToNext()) {
            if (!videoCursor.getString(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = videoCursor.getString(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getOtgAllVideos(DocumentsContract.buildChildDocumentsUriUsingTree(_rootUri, videoCursor.getString(cursor_index_ID)));
                } else if (type.contains(VIDEO)) {
                    FileInfo item = new FileInfo();
                    item.name = videoCursor.getString(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    //item.name = item.name.substring(0, item.name.lastIndexOf("."));
                    item.time = FileInfo.getTime(videoCursor.getLong(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = videoCursor.getLong(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    String[] split = videoCursor.getString(cursor_index_ID).split(":");
                    item.path = mOuterStoragePath + "/" + split[1];
                    item.uri = DocumentsContract.buildDocumentUriUsingTree(_rootUri, videoCursor.getString(cursor_index_ID));
                    item.format_size = Formatter.formatFileSize(mContext, item.size);
                    item.type = Constant.TYPE_VIDEO;
                    item.storagemode = Constant.STORAGEMODE_OTG;
                    mFileList.add(item);
                }
            }
        }
        videoCursor.close();
        return mFileList;
    }

    private ArrayList<FileInfo> getOtgAllMusics(Uri _rootUri) {

        Cursor musicCursor = mContext.getContentResolver().query(_rootUri, proj, null, null, null);
        if (musicCursor == null) {
            cancelLoad();
            return null;
        }
        int cursor_index_ID = musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
        while (musicCursor.moveToNext()) {
            if (!musicCursor.getString(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = musicCursor.getString(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getOtgAllMusics(DocumentsContract.buildChildDocumentsUriUsingTree(_rootUri, musicCursor.getString(cursor_index_ID)));
                } else if (type.contains(AUDIO)) {
                    FileInfo item = new FileInfo();
                    item.name = musicCursor.getString(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    //item.name = item.name.substring(0, item.name.lastIndexOf("."));
                    item.time = FileInfo.getTime(musicCursor.getLong(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = musicCursor.getLong(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    String[] split = musicCursor.getString(cursor_index_ID).split(":");
                    item.path = mOuterStoragePath + "/" + split[1];
                    item.album_id = -1;
                    item.uri = DocumentsContract.buildDocumentUriUsingTree(_rootUri, musicCursor.getString(cursor_index_ID));
                    item.format_size = Formatter.formatFileSize(mContext, item.size);
                    item.type = Constant.TYPE_MUSIC;
                    item.storagemode = Constant.STORAGEMODE_OTG;
                    mFileList.add(item);
                }
            }
        }
        musicCursor.close();
        return mFileList;
    }

    private ArrayList<FileInfo> getOtgAllDocs(Uri _rootUri) {

        Cursor docCursor = mContext.getContentResolver().query(_rootUri, proj, null, null, null);
        if (docCursor == null) {
            cancelLoad();
            return null;
        }
        int cursor_index_ID = docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
        while (docCursor.moveToNext()) {
            if (!docCursor.getString(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = docCursor.getString(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getOtgAllDocs(DocumentsContract.buildChildDocumentsUriUsingTree(_rootUri, docCursor.getString(cursor_index_ID)));
                } else if (type.contains(TEXT) || type.contains(PDF) || type.contains(WORD) || type.contains(PPT) || type.contains(EXCEL)) {
                    FileInfo item = new FileInfo();
                    item.name = docCursor.getString(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    item.time = FileInfo.getTime(docCursor.getLong(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = docCursor.getLong(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    String[] split = docCursor.getString(cursor_index_ID).split(":");
                    item.path = mOuterStoragePath + "/" + split[1];
                    item.uri = DocumentsContract.buildDocumentUriUsingTree(_rootUri, docCursor.getString(cursor_index_ID));
                    item.format_size = Formatter.formatFileSize(mContext, item.size);
                    item.type = Constant.TYPE_DOC;
                    item.storagemode = Constant.STORAGEMODE_OTG;
                    mFileList.add(item);
                }
            }
        }
        docCursor.close();
        return mFileList;
    }

    private ArrayList<FileInfo> getOtgAllEncs(Uri _rootUri) {

        Cursor encCursor = mContext.getContentResolver().query(_rootUri, proj, null, null, null);
        if (encCursor == null) {
            cancelLoad();
            return null;
        }
        int cursor_index_ID = encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
        while (encCursor.moveToNext()) {
            if (!encCursor.getString(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = encCursor.getString(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                String name = encCursor.getString(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                if (type.contains(DIR)) {
                    getOtgAllEncs(DocumentsContract.buildChildDocumentsUriUsingTree(_rootUri, encCursor.getString(cursor_index_ID)));
                } else if (name.endsWith(ENCRYPT)) {
                    FileInfo item = new FileInfo();
                    item.name = name;
                    item.time = FileInfo.getTime(encCursor.getLong(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = encCursor.getLong(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    String[] split = encCursor.getString(cursor_index_ID).split(":");
                    item.path = mOuterStoragePath + "/" + split[1];
                    item.uri = DocumentsContract.buildDocumentUriUsingTree(_rootUri, encCursor.getString(cursor_index_ID));
                    item.format_size = Formatter.formatFileSize(mContext, item.size);
                    item.type = Constant.TYPE_ENCRYPT;
                    item.storagemode = Constant.STORAGEMODE_OTG;
                    mFileList.add(item);
                }
            }
        }
        encCursor.close();

        return mFileList;
    }

    private ArrayList<FileInfo> getSortList(ArrayList<FileInfo> _list) {
        Collections.sort(_list, FileInfoSort.comparator(mContext));
        if (mSortBy != Constant.SORT_BY_SIZE)
            FileFactory.getInstance().addFileTypeSortRule(_list);
        return _list;
    }

    private ArrayList<FileInfo> getOtgFileList(Uri _rootUri) {
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

                FileInfo item = new FileInfo();
                item.name = name;
                item.time = FileInfo.getTime(cursor.getLong(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                if (!type.contains(DIR)) {
                    item.size = cursor.getLong(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.format_size = Formatter.formatFileSize(mContext, item.size);
                }
                String[] split = cursor.getString(cursor_index_ID).split(":");
                item.path = mOuterStoragePath + "/" + split[1];
                item.uri = DocumentsContract.buildDocumentUriUsingTree(_rootUri, cursor.getString(cursor_index_ID));
                item.storagemode = Constant.STORAGEMODE_OTG;
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
        cursor.close();
        return mFileList;
    }

//////local & sd card function start//////

    private ArrayList<FileInfo> getAllImages() {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mOuterStoragePath != null) {
            Uri uriSDKey = Uri.parse(LocalPreferences.getSDKey(mContext));
            Uri sdBaseRootUri = null;
            if (uriSDKey != null && uriSDKey.toString() != "")
                sdBaseRootUri = DocumentsContract.buildChildDocumentsUriUsingTree(uriSDKey, DocumentsContract.getTreeDocumentId(uriSDKey));
            if (sdBaseRootUri != null) {
                return getSortList(getOtgAllImages(sdBaseRootUri, Constant.STORAGEMODE_SD));
            }
        }*/
        try {
            String[] proj = {MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_MODIFIED};

            String orderBy = MediaStore.Images.Media.DATE_MODIFIED;
            if (mSortBy == Constant.SORT_BY_NAME)
                orderBy = MediaStore.Images.Media.DISPLAY_NAME;
            else if (mSortBy == Constant.SORT_BY_SIZE)
                orderBy = MediaStore.Images.Media.SIZE;
            String order = mSortOrderAsc ? " ASC" : " DESC";
            Cursor imagecursor = mContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj,
                    null, null, orderBy + order);
            if (imagecursor != null) {
                while (imagecursor.moveToNext()) {
                    int pathColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    int nameColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                    int timeColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED);
                    int sizeColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.SIZE);

                    Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            imagecursor.getInt(imagecursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)));

                    String picPath = imagecursor.getString(pathColumnIndex);
                    String picName = imagecursor.getString(nameColumnIndex);
                    Long picTime = 1000 * imagecursor.getLong(timeColumnIndex);
                    Long picSize = imagecursor.getLong(sizeColumnIndex);
                    File picFile = new File(picPath);
                    if (picFile.exists()) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = picPath;
                        //picName = picName.substring(0, picName.lastIndexOf("."));
                        fileInfo.name = picName;
                        fileInfo.time = FileInfo.getTime(picTime);
                        fileInfo.type = Constant.TYPE_PHOTO;
                        fileInfo.size = picSize;
                        fileInfo.format_size = Formatter.formatFileSize(mContext, picSize);
                        fileInfo.uri = imageUri;
                        if (mOuterStoragePath == null) {
                            if (picPath.contains(Constant.ROOT_LOCAL)){
                                fileInfo.storagemode = Constant.STORAGEMODE_LOCAL;
                                mFileList.add(fileInfo);
                            }

                        } else {
                            if (picPath.contains(mOuterStoragePath)){
                                fileInfo.storagemode = Constant.STORAGEMODE_SD;
                                mFileList.add(fileInfo);
                            }
                        }
                    }
                }
            }
            imagecursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return mFileList;
        }
        return mFileList;
    }

    private ArrayList<FileInfo> getAllMusics() {
        try {
            String[] proj = {MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.DATE_MODIFIED};
            String select = "(" + MediaStore.Audio.Media.DURATION + " > 10000)";
            String orderBy = MediaStore.Audio.Media.DATE_MODIFIED;
            if (mSortBy == Constant.SORT_BY_NAME)
                orderBy = MediaStore.Audio.Media.DISPLAY_NAME;
            else if (mSortBy == Constant.SORT_BY_SIZE)
                orderBy = MediaStore.Audio.Media.SIZE;
            String order = mSortOrderAsc ? " ASC" : " DESC";
            Cursor musiccursor = mContext.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj,
                    select, null, orderBy + order);
            if (musiccursor != null) {
                while (musiccursor.moveToNext()) {
                    int pathColumnIndex = musiccursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                    int nameColumnIndex = musiccursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                    int timeColumnIndex = musiccursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
                    int sizeColumnIndex = musiccursor.getColumnIndex(MediaStore.Audio.Media.SIZE);

                    String musicPath = musiccursor.getString(pathColumnIndex);
                    String musicName = musiccursor.getString(nameColumnIndex);
                    Long musicTime = 1000 * musiccursor.getLong(timeColumnIndex);
                    Long musicSize = musiccursor.getLong(sizeColumnIndex);

                    long albumId = musiccursor.getInt(musiccursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

                    File musicFile = new File(musicPath);
                    if (musicFile.exists()) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = musicPath;
                        //musicName = musicName.substring(0, musicName.lastIndexOf("."));
                        fileInfo.name = musicName;
                        fileInfo.time = FileInfo.getTime(musicTime);
                        fileInfo.type = Constant.TYPE_MUSIC;
                        fileInfo.album_id = albumId;
                        fileInfo.size = musicSize;
                        fileInfo.format_size = Formatter.formatFileSize(mContext, musicSize);
                        if (mOuterStoragePath == null) {
                            if (musicPath.contains(Constant.ROOT_LOCAL)) {
                                fileInfo.storagemode = Constant.STORAGEMODE_LOCAL;
                                mFileList.add(fileInfo);
                            }
                        } else {
                            if (musicPath.contains(mOuterStoragePath)) {
                                fileInfo.storagemode = Constant.STORAGEMODE_SD;
                                mFileList.add(fileInfo);
                            }
                        }
                    }
                }
            }
            musiccursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return mFileList;
        }
        return mFileList;
    }

    private ArrayList<FileInfo> getAllVideos() {
        try {
            String[] videoTypes = {MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DATE_MODIFIED};
            String orderBy = MediaStore.Video.Media.DATE_MODIFIED;
            if (mSortBy == Constant.SORT_BY_NAME)
                orderBy = MediaStore.Video.Media.DISPLAY_NAME;
            else if (mSortBy == Constant.SORT_BY_SIZE)
                orderBy = MediaStore.Video.Media.SIZE;
            String order = mSortOrderAsc ? " ASC" : " DESC";


            Cursor videocursor = mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    videoTypes, null, null, orderBy + order);

            if (videocursor != null) {
                while (videocursor.moveToNext()) {
                    int pathColumnIndex = videocursor.getColumnIndex(MediaStore.Video.Media.DATA);
                    int nameColumnIndex = videocursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
                    int timeColumnIndex = videocursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED);
                    int sizeColumnIndex = videocursor.getColumnIndex(MediaStore.Video.Media.SIZE);

                    String videoPath = videocursor.getString(pathColumnIndex);
                    String videoName = videocursor.getString(nameColumnIndex);
                    Long videoTime = 1000 * videocursor.getLong(timeColumnIndex);
                    Long videoSize = videocursor.getLong(sizeColumnIndex);

                    File videoFile = new File(videoPath);
                    if (videoFile.exists()) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = videoPath;
                        //videoName = videoName.substring(0, videoName.lastIndexOf("."));
                        fileInfo.name = videoName;
                        fileInfo.time = FileInfo.getTime(videoTime);
                        fileInfo.type = Constant.TYPE_VIDEO;
                        fileInfo.format_size = Formatter.formatFileSize(mContext, videoSize);
                        if (mOuterStoragePath == null) {
                            if (videoPath.contains(Constant.ROOT_LOCAL)) {
                                fileInfo.storagemode = Constant.STORAGEMODE_LOCAL;
                                mFileList.add(fileInfo);
                            }
                        } else {
                            if (videoPath.contains(mOuterStoragePath)) {
                                fileInfo.storagemode = Constant.STORAGEMODE_SD;
                                mFileList.add(fileInfo);
                            }
                        }
                    }
                }
            }
            videocursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return mFileList;
        }
        return mFileList;
    }

    private ArrayList<FileInfo> getAllDocs() {
        boolean isSd =  (mOuterStoragePath != null);
        try {
            String[] proj = {MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DATE_MODIFIED,
                    MediaStore.Files.FileColumns.SIZE};

            String orderBy = MediaStore.Files.FileColumns.DATE_MODIFIED;
            //FileColumns.DISPLAY_NAME could be null, so we can't sort by name in query
            if (mSortBy == Constant.SORT_BY_SIZE)
                orderBy = MediaStore.Files.FileColumns.SIZE;
            String order = mSortOrderAsc ? " ASC" : " DESC";
            String select = "(" + MediaStore.Files.FileColumns.DATA + " LIKE '%.doc'" + " or "
                    + MediaStore.Files.FileColumns.DATA + " LIKE '%.docx'" + " or "
                    + MediaStore.Files.FileColumns.DATA + " LIKE '%.xls'" + " or "
                    + MediaStore.Files.FileColumns.DATA + " LIKE '%.ppt'" + " or "
                    + MediaStore.Files.FileColumns.DATA + " LIKE '%.pdf'" + " or "
                    + MediaStore.Files.FileColumns.DATA + " LIKE '%.txt'" + ")";

            Cursor docscursor = mContext.getContentResolver().query(
                    MediaStore.Files.getContentUri("external"), proj, select, null, orderBy + order);
            if (docscursor != null) {
                while (docscursor.moveToNext()) {
                    int mimeColumnIndex = docscursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE);
                    int pathColumnIndex = docscursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    int timeColumnIndex = docscursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED);
                    int sizeColumnIndex = docscursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);

                    String mimeType = docscursor.getString(mimeColumnIndex);
                    if (mimeType != null && mimeType.contains("directory"))
                        continue;
                    String path = docscursor.getString(pathColumnIndex);
                    if (isSd && path.startsWith(mOuterStoragePath) && !path.contains("/.")) {
                        String name = path.substring(path.lastIndexOf('/')+1);
                        File check_file = new File(path);
                        if (!check_file.exists() || check_file.isDirectory())
                            continue;
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = path;
                        fileInfo.name = name;
                        fileInfo.time = FileInfo.getTime(1000 * docscursor.getLong(timeColumnIndex));
                        fileInfo.size = docscursor.getLong(sizeColumnIndex);
                        fileInfo.format_size = Formatter.formatFileSize(mContext, fileInfo.size);
                        fileInfo.type = Constant.TYPE_DOC;
                        fileInfo.storagemode = Constant.STORAGEMODE_SD;
                        mFileList.add(fileInfo);

                    } else if (!isSd && path.startsWith(Constant.ROOT_LOCAL) && !path.contains("/.")) {
                        String name = path.substring(path.lastIndexOf('/')+1);
                        File check_file = new File(path);
                        if (!check_file.exists() || check_file.isDirectory())
                            continue;
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = path;
                        fileInfo.name = name;
                        fileInfo.time = FileInfo.getTime(1000 * docscursor.getLong(timeColumnIndex));
                        fileInfo.size = docscursor.getLong(sizeColumnIndex);
                        fileInfo.format_size = Formatter.formatFileSize(mContext, fileInfo.size);
                        fileInfo.type = Constant.TYPE_DOC;
                        fileInfo.storagemode = Constant.STORAGEMODE_LOCAL;
                        mFileList.add(fileInfo);
                    }

                    /*File docFile = new File(docPath);
                    if (docFile.exists() && !docFile.isDirectory()) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = docPath;
                        fileInfo.name = docFile.getName();
                        fileInfo.time = FileInfo.getTime(docTime);
                        fileInfo.type = Constant.TYPE_DOC;
                        fileInfo.size = docSize;
                        fileInfo.format_size = Formatter.formatFileSize(mContext, docSize);
                        if (mOuterStoragePath == null) {
                            if (docPath.contains(Constant.ROOT_LOCAL)) {
                                fileInfo.storagemode = Constant.STORAGEMODE_LOCAL;
                                mFileList.add(fileInfo);
                            }
                        } else {
                            if (docPath.contains(mOuterStoragePath)) {
                                fileInfo.storagemode = Constant.STORAGEMODE_SD;
                                mFileList.add(fileInfo);
                            }
                        }
                    }*/
                }
            }
            docscursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return mFileList;
        }
        if (mSortBy == Constant.SORT_BY_NAME)
            return getSortList(mFileList);

        return mFileList;
    }

    private ArrayList<FileInfo> getAllEncs() {
        boolean isSd =  (mOuterStoragePath != null);
        try {
            String[] proj = {
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.DATE_MODIFIED,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.SIZE};
            Uri contextUri = MediaStore.Files.getContentUri("external");

            String orderBy = MediaStore.Files.FileColumns.DATE_MODIFIED;
            if (mSortBy == Constant.SORT_BY_SIZE)
                orderBy = MediaStore.Files.FileColumns.SIZE;
            String order = mSortOrderAsc ? " ASC" : " DESC";
            String select = "(" + MediaStore.Files.FileColumns.DATA + " LIKE '%.enc'" + ")";
            Cursor encCursor = mContext.getContentResolver().query(
                    contextUri, proj,
                    select, null, orderBy + order);
            if (encCursor != null) {
                int mimeColumnIndex = encCursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE);
                int pathColumnIndex = encCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                int timeColumnIndex = encCursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED);
                int sizeColumnIndex = encCursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);
                while (encCursor.moveToNext()) {
                    String mimeType = encCursor.getString(mimeColumnIndex);
                    if (mimeType != null && mimeType.contains("directory"))
                        continue;
                    String path = encCursor.getString(pathColumnIndex);
                    if (isSd && path.startsWith(mOuterStoragePath) && !path.contains("/.")) {
                        String name = path.substring(path.lastIndexOf('/')+1);
                        File check_file = new File(path);
                        if (!check_file.exists() || check_file.isDirectory())
                            continue;
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = path;
                        fileInfo.name = name;
                        fileInfo.time = FileInfo.getTime(1000 * encCursor.getLong(timeColumnIndex));
                        fileInfo.size = encCursor.getLong(sizeColumnIndex);
                        fileInfo.format_size = Formatter.formatFileSize(mContext, fileInfo.size);
                        fileInfo.type = Constant.TYPE_ENCRYPT;
                        fileInfo.storagemode = Constant.STORAGEMODE_SD;
                        mFileList.add(fileInfo);

                    } else if (!isSd && path.startsWith(Constant.ROOT_LOCAL) && !path.contains("/.")) {
                        String name = path.substring(path.lastIndexOf('/')+1);
                        File check_file = new File(path);
                        if (!check_file.exists() || check_file.isDirectory())
                            continue;
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = path;
                        fileInfo.name = name;
                        fileInfo.time = FileInfo.getTime(1000 * encCursor.getLong(timeColumnIndex));
                        fileInfo.size = encCursor.getLong(sizeColumnIndex);
                        fileInfo.format_size = Formatter.formatFileSize(mContext, fileInfo.size);
                        fileInfo.type = Constant.TYPE_ENCRYPT;
                        fileInfo.storagemode = Constant.STORAGEMODE_LOCAL;
                        mFileList.add(fileInfo);
                    }
                }
            }
            encCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return mFileList;
        }
        if (mSortBy == Constant.SORT_BY_NAME)
            return getSortList(mFileList);
        return mFileList;
    }

    //this is slow
    private ArrayList<FileInfo> getFileList_old() {
        String root_path;
        if (mOuterStoragePath == null)
            root_path = Constant.ROOT_LOCAL;
        else
            root_path = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
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
                    if (!path.contains("/.") && path.contains(root_path) && path.lastIndexOf('/') == root_path.length()) {
                        Uri fileUri = ContentUris.withAppendedId(contextUri,
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)));
                        String name = path.substring(path.lastIndexOf('/')+1);
                        String mimeType = cursor.getString(mimeColumnIndex);
                        Long time = 1000 * cursor.getLong(timeColumnIndex);
                        Long size = cursor.getLong(sizeColumnIndex);

                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = path;

                        if (root_path == Constant.ROOT_LOCAL)
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
            return mFileList;
        }
        return mFileList;
    }

    private ArrayList<FileInfo> getFileList() {
        String path;
        if (mOuterStoragePath == null)
            path = Constant.ROOT_LOCAL;
        else
            path = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
        File dir = new File(path);
        if (!dir.exists())
            return mFileList;
        File files[] = dir.listFiles();
        for (File file : files) {
            if (file.isHidden())
                continue;
            FileInfo fileInfo = new FileInfo();
            fileInfo.path = file.getPath();
            fileInfo.name = file.getName();
            fileInfo.time = FileInfo.getTime(file.lastModified());
            fileInfo.type = file.isFile() ? FileInfo.getType(file.getPath()) : Constant.TYPE_DIR;
            if (fileInfo.type != Constant.TYPE_DIR) {
                fileInfo.size = file.length();
                fileInfo.format_size = Formatter.formatFileSize(mContext, fileInfo.size);
            }
            if (mOuterStoragePath == null)
                fileInfo.storagemode = Constant.STORAGEMODE_LOCAL;
            else
                fileInfo.storagemode = Constant.STORAGEMODE_SD;
            mFileList.add(fileInfo);
        }
        Collections.sort(mFileList, FileInfoSort.comparator(mContext));
        //FileFactory.getInstance().addFolderFilterRule(path, mFileList);
        if (mSortBy != Constant.SORT_BY_SIZE)
            FileFactory.getInstance().addFileTypeSortRule(mFileList);

        return mFileList;
    }
//////local & sd card function end//////
}
