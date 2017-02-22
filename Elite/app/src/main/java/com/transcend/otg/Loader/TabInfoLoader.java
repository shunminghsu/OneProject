package com.transcend.otg.Loader;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.provider.DocumentFile;

import com.transcend.otg.Browser.BrowserFragment;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
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
    private ArrayList<FileInfo> mFileList, mImageList;
    private Context mContext;
    private int mType;
    private String mSDCardPath;
    private boolean mIsOtg;
    private DocumentFile dFile = null, rootDFile = null;
    private Uri rootUri = null;
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

    final String orderBy = DocumentsContract.Document.COLUMN_DISPLAY_NAME;


    public TabInfoLoader(Context context, int type, String sdcard_path, boolean otg) {
        super(context);
        mFileList = new ArrayList<>();
        mType = type;
        mSDCardPath = sdcard_path;
        mIsOtg = otg;
        mContext = context;
        dFile = Constant.pickedDir;
        rootDFile = Constant.rootDir;
        rootUri = Constant.rootUri;
    }

    @Override
    public ArrayList<FileInfo> loadInBackground() {
        mFileList.clear();
        switch (mType) {
            case BrowserFragment.LIST_TYPE_IMAGE:
                return mIsOtg ? getOtgAllImages() : getAllImages();
            case BrowserFragment.LIST_TYPE_VIDEO:
                return mIsOtg ? getOtgAllVideos() : getAllVideos();
            case BrowserFragment.LIST_TYPE_MUSIC:
                return mIsOtg ? getOtgAllMusics() : getAllMusics();
            case BrowserFragment.LIST_TYPE_DOCUMENT:
                return mIsOtg ? getOtgAllDocs() : getAllDocs();
            case BrowserFragment.LIST_TYPE_ENCRYPTION:
                return mIsOtg ? getOtgAllEncs() : getAllEncs();
            case BrowserFragment.LIST_TYPE_FOLDER:
                return mIsOtg ? getOtgFileList() : getFileList();
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

    private ArrayList<FileInfo> getOtgAllImages() {

        Uri rootImageUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri));

        Cursor imageCursor = mContext.getContentResolver().query(rootImageUri, proj, null, null, orderBy + " DESC");
        while (imageCursor.moveToNext()) {
            if (!imageCursor.getString(imageCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = imageCursor.getString(imageCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getOtgAllImagesDIR(DocumentsContract.buildChildDocumentsUriUsingTree(rootImageUri, imageCursor.getString(1)));
                } else if (type.contains(IMAGE) || type.contains(PNG) || type.contains(JPG)) {
                    FileInfo item = new FileInfo();
                    item.name = imageCursor.getString(imageCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    item.time = FileInfo.getTime(imageCursor.getLong(imageCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = imageCursor.getLong(imageCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = imageCursor.getString(imageCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildChildDocumentsUriUsingTree(rootImageUri, imageCursor.getString(1));
                    item.type = FileInfo.TYPE.PHOTO;
                    mFileList.add(item);
                }
            }
        }
        imageCursor.close();
        mImageList = mFileList;
        return mFileList;
    }

    private void getOtgAllImagesDIR(Uri uriDIR) {
        final String orderBy = DocumentsContract.Document.COLUMN_LAST_MODIFIED;
        Cursor imageDIRCursor = mContext.getContentResolver().query(uriDIR, proj, null, null, orderBy + " DESC");
        while (imageDIRCursor.moveToNext()) {
            if (!imageDIRCursor.getString(imageDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = imageDIRCursor.getString(imageDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getOtgAllImagesDIR(DocumentsContract.buildChildDocumentsUriUsingTree(uriDIR, imageDIRCursor.getString(1)));
                } else if (type.contains(IMAGE) || type.contains(PNG) || type.contains(JPG)) {
                    FileInfo item = new FileInfo();
                    item.name = imageDIRCursor.getString(imageDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    item.time = FileInfo.getTime(imageDIRCursor.getLong(imageDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = imageDIRCursor.getLong(imageDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = imageDIRCursor.getString(imageDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildChildDocumentsUriUsingTree(uriDIR, imageDIRCursor.getString(1));
                    item.type = FileInfo.TYPE.PHOTO;
                    mFileList.add(item);
                }
            }
        }
        imageDIRCursor.close();
    }

    private ArrayList<FileInfo> getOtgAllVideos() {
        Uri rootUri2 = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri));

        Cursor videoCursor = mContext.getContentResolver().query(rootUri2, proj, null, null, orderBy + " DESC");
        while (videoCursor.moveToNext()) {
            if (!videoCursor.getString(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = videoCursor.getString(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getOtgAllVideosDIR(DocumentsContract.buildChildDocumentsUriUsingTree(rootUri2, videoCursor.getString(1)));
                } else if (type.contains(VIDEO)) {
                    FileInfo item = new FileInfo();
                    item.name = videoCursor.getString(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    item.time = FileInfo.getTime(videoCursor.getLong(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = videoCursor.getLong(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = videoCursor.getString(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildDocumentUriUsingTree(rootUri2, videoCursor.getString(1));
                    item.type = FileInfo.TYPE.VIDEO;
                    mFileList.add(item);
                }
            }
        }
        videoCursor.close();
        return mFileList;
    }

    private void getOtgAllVideosDIR(Uri uriDIR) {
        Cursor videoDIRCursor = mContext.getContentResolver().query(uriDIR, proj, null, null, orderBy + " DESC");
        while (videoDIRCursor.moveToNext()) {
            if (!videoDIRCursor.getString(videoDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = videoDIRCursor.getString(videoDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getOtgAllVideosDIR(DocumentsContract.buildChildDocumentsUriUsingTree(uriDIR, videoDIRCursor.getString(1)));
                } else if (type.contains(VIDEO)) {
                    FileInfo item = new FileInfo();
                    item.name = videoDIRCursor.getString(videoDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    item.time = FileInfo.getTime(videoDIRCursor.getLong(videoDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = videoDIRCursor.getLong(videoDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = videoDIRCursor.getString(videoDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildDocumentUriUsingTree(uriDIR, videoDIRCursor.getString(1));
                    item.type = FileInfo.TYPE.VIDEO;
                    mFileList.add(item);
                }
            }
        }
        videoDIRCursor.close();
    }

    private ArrayList<FileInfo> getOtgAllMusics() {
        Uri musicUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri));
        Cursor musicCursor = mContext.getContentResolver().query(musicUri, proj, null, null, orderBy + " DESC");
        while (musicCursor.moveToNext()) {
            if (!musicCursor.getString(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = musicCursor.getString(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getOtgAllMusicsDIR(DocumentsContract.buildChildDocumentsUriUsingTree(musicUri, musicCursor.getString(1)));
                } else if (type.contains(AUDIO)) {
                    FileInfo item = new FileInfo();
                    item.name = musicCursor.getString(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    item.time = FileInfo.getTime(musicCursor.getLong(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = musicCursor.getLong(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = musicCursor.getString(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildDocumentUriUsingTree(musicUri, musicCursor.getString(1));
                    item.type = FileInfo.TYPE.MUSIC;
                    mFileList.add(item);
                }
            }
        }
        musicCursor.close();
        return mFileList;
    }

    private void getOtgAllMusicsDIR(Uri uriDIR) {
        Cursor musicDIRCursor = mContext.getContentResolver().query(uriDIR, proj, null, null, orderBy + " DESC");
        while (musicDIRCursor.moveToNext()) {
            if (!musicDIRCursor.getString(musicDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = musicDIRCursor.getString(musicDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getOtgAllMusicsDIR(DocumentsContract.buildChildDocumentsUriUsingTree(uriDIR, musicDIRCursor.getString(1)));
                } else if (type.contains(AUDIO)) {
                    FileInfo item = new FileInfo();
                    item.name = musicDIRCursor.getString(musicDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    item.time = FileInfo.getTime(musicDIRCursor.getLong(musicDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = musicDIRCursor.getLong(musicDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = musicDIRCursor.getString(musicDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildDocumentUriUsingTree(uriDIR, musicDIRCursor.getString(1));
                    item.type = FileInfo.TYPE.MUSIC;
                    mFileList.add(item);
                }
            }

        }
        musicDIRCursor.close();
    }

    private ArrayList<FileInfo> getOtgAllDocs() {
        Uri docUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri));
        Cursor docCursor = mContext.getContentResolver().query(docUri, proj, null, null, orderBy + " DESC");
        while (docCursor.moveToNext()) {
            if (!docCursor.getString(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = docCursor.getString(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getOtgAllDocsDIR(DocumentsContract.buildChildDocumentsUriUsingTree(docUri, docCursor.getString(1)));
                } else if (type.contains(TEXT) || type.contains(PDF) || type.contains(WORD) || type.contains(PPT) || type.contains(EXCEL)) {
                    FileInfo item = new FileInfo();
                    item.name = docCursor.getString(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    item.time = FileInfo.getTime(docCursor.getLong(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = docCursor.getLong(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = docCursor.getString(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildDocumentUriUsingTree(docUri, docCursor.getString(1));
                    item.type = FileInfo.TYPE.FILE;
                    mFileList.add(item);
                }
            }
        }
        docCursor.close();
        return mFileList;
    }

    private void getOtgAllDocsDIR(Uri uriDIR) {
        Cursor docDIRCursor = mContext.getContentResolver().query(uriDIR, proj, null, null, orderBy + " DESC");
        while (docDIRCursor.moveToNext()) {
            if (!docDIRCursor.getString(docDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = docDIRCursor.getString(docDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getOtgAllDocsDIR(DocumentsContract.buildChildDocumentsUriUsingTree(uriDIR, docDIRCursor.getString(1)));
                } else if (type.contains(TEXT) || type.contains(PDF) || type.contains(WORD) || type.contains(PPT) || type.contains(EXCEL)) {
                    FileInfo item = new FileInfo();
                    item.name = docDIRCursor.getString(docDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    item.time = FileInfo.getTime(docDIRCursor.getLong(docDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = docDIRCursor.getLong(docDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = docDIRCursor.getString(docDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildDocumentUriUsingTree(uriDIR, docDIRCursor.getString(1));
                    item.type = FileInfo.TYPE.MUSIC;
                    mFileList.add(item);
                }
            }
        }
        docDIRCursor.close();
    }

    private ArrayList<FileInfo> getOtgAllEncs() {
        Uri encUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri));
        Cursor encCursor = mContext.getContentResolver().query(encUri, proj, null, null, orderBy + " DESC");
        while (encCursor.moveToNext()) {
            if (!encCursor.getString(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = encCursor.getString(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                String name = encCursor.getString(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                if (type.contains(DIR)) {
                    getOtgAllEncsDIR(DocumentsContract.buildChildDocumentsUriUsingTree(encUri, encCursor.getString(1)));
                } else if (name.contains(".enc")) {
                    FileInfo item = new FileInfo();
                    item.name = name;
                    item.time = FileInfo.getTime(encCursor.getLong(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = encCursor.getLong(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = encCursor.getString(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildDocumentUriUsingTree(encUri, encCursor.getString(1));
                    item.type = FileInfo.TYPE.ENCRYPT;
                    mFileList.add(item);
                }
            }
        }
        encCursor.close();
        return mFileList;
    }

    private void getOtgAllEncsDIR(Uri uriDIR) {
        Cursor encDIRCursor = mContext.getContentResolver().query(uriDIR, proj, null, null, orderBy + " DESC");
        while (encDIRCursor.moveToNext()) {
            if (!encDIRCursor.getString(encDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = encDIRCursor.getString(encDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                String name = encDIRCursor.getString(encDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                if (type.contains(DIR)) {
                    getOtgAllEncsDIR(DocumentsContract.buildChildDocumentsUriUsingTree(uriDIR, encDIRCursor.getString(1)));
                } else if (name.contains(".enc")) {
                    FileInfo item = new FileInfo();
                    item.name = name;
                    item.time = FileInfo.getTime(encDIRCursor.getLong(encDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = encDIRCursor.getLong(encDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = encDIRCursor.getString(encDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildDocumentUriUsingTree(uriDIR, encDIRCursor.getString(1));
                    item.type = FileInfo.TYPE.ENCRYPT;
                    mFileList.add(item);
                }
            }
        }
        encDIRCursor.close();
    }

    private ArrayList<FileInfo> getOtgFileList() {
        for (DocumentFile ddFile : dFile.listFiles()) {
            FileInfo tmpFileInfo = new FileInfo();
            tmpFileInfo.checked = false;
            tmpFileInfo.name = ddFile.getName();
            tmpFileInfo.path = ddFile.getUri().getPath();
            tmpFileInfo.time = FileInfo.getTime(ddFile.lastModified());
            tmpFileInfo.uri = ddFile.getUri();
            tmpFileInfo.size = ddFile.length();
            String type = ddFile.getType();
            if (type != null) {
                if (type.contains(IMAGE))
                    tmpFileInfo.type = FileInfo.TYPE.PHOTO;
                else if (type.contains(APPLICATION) || type.contains(TEXT))
                    tmpFileInfo.type = FileInfo.TYPE.FILE;
                else if (type.contains(VIDEO))
                    tmpFileInfo.type = FileInfo.TYPE.VIDEO;
                else if (type.contains(AUDIO))
                    tmpFileInfo.type = FileInfo.TYPE.MUSIC;
                else
                    tmpFileInfo.type = FileInfo.TYPE.DIR;
                if (tmpFileInfo.name.contains(ENCRYPT))
                    tmpFileInfo.type = FileInfo.TYPE.ENCRYPT;
            } else {
                tmpFileInfo.type = FileInfo.TYPE.DIR;
            }
            if (tmpFileInfo.name != null) {
                if (tmpFileInfo.name.substring(0, 1).equals("."))
                    continue;
                mFileList.add(tmpFileInfo);
            }
        }
        Collections.sort(mFileList, FileInfoSort.comparator(mContext));
//            FileFactory.getInstance().addFolderFilterRule("", mFileList);
        FileFactory.getInstance().addFileTypeSortRule(mFileList);
        return mFileList;
    }

//////local & sd card function start//////

    private ArrayList<FileInfo> getAllImages() {
        try {
            String[] proj = {MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_ADDED};

            final String orderBy = MediaStore.Images.Media.DATE_ADDED;
            Cursor imagecursor = mContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj,
                    null, null, orderBy + " DESC");
            if (imagecursor != null) {
                while (imagecursor.moveToNext()) {
                    int pathColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    int nameColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                    int timeColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
                    int sizeColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.SIZE);

                    Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            imagecursor.getInt(imagecursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)));

                    String picPath = imagecursor.getString(pathColumnIndex);
                    String picName = imagecursor.getString(nameColumnIndex);
                    String picTime = imagecursor.getString(timeColumnIndex);
                    String picSize = imagecursor.getString(sizeColumnIndex);
                    File picFile = new File(picPath);
                    if (picFile.exists()) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = picPath;
                        fileInfo.name = picName;
                        fileInfo.time = picTime;
                        fileInfo.type = FileInfo.TYPE.PHOTO;
                        fileInfo.size = Long.valueOf(picSize);
                        fileInfo.uri = imageUri;
                        if (mSDCardPath == null) {
                            if (picPath.contains(Constant.ROOT_LOCAL))
                                mFileList.add(fileInfo);
                        } else {
                            if (picPath.contains(mSDCardPath))
                                mFileList.add(fileInfo);
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
                    MediaStore.Audio.Media.DATE_ADDED};

            final String orderBy = MediaStore.Audio.Media.DATE_ADDED;
            Cursor musiccursor = mContext.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj,
                    null, null, orderBy + " DESC");
            if (musiccursor != null) {
                while (musiccursor.moveToNext()) {
                    int pathColumnIndex = musiccursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                    int nameColumnIndex = musiccursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                    int timeColumnIndex = musiccursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
                    int sizeColumnIndex = musiccursor.getColumnIndex(MediaStore.Audio.Media.SIZE);

                    String musicPath = musiccursor.getString(pathColumnIndex);
                    String musicName = musiccursor.getString(nameColumnIndex);
                    String musicTime = musiccursor.getString(timeColumnIndex);
                    String musicSize = musiccursor.getString(sizeColumnIndex);
                    File musicFile = new File(musicPath);
                    if (musicFile.exists()) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = musicPath;
                        fileInfo.name = musicName;
                        fileInfo.time = musicTime;
                        fileInfo.type = FileInfo.TYPE.MUSIC;
                        fileInfo.size = Long.valueOf(musicSize);
                        if (mSDCardPath == null) {
                            if (musicPath.contains(Constant.ROOT_LOCAL))
                                mFileList.add(fileInfo);
                        } else {
                            if (musicPath.contains(mSDCardPath))
                                mFileList.add(fileInfo);
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
                    MediaStore.Video.Media.DATE_ADDED};
            final String orderBy = MediaStore.Video.Media.DATE_ADDED;
            Cursor videocursor = mContext. getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    videoTypes, null, null, orderBy + " DESC");

            if (videocursor != null) {
                while (videocursor.moveToNext()) {
                    int pathColumnIndex = videocursor.getColumnIndex(MediaStore.Video.Media.DATA);
                    int nameColumnIndex = videocursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
                    int timeColumnIndex = videocursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED);
                    int sizeColumnIndex = videocursor.getColumnIndex(MediaStore.Video.Media.SIZE);

                    String videoPath = videocursor.getString(pathColumnIndex);
                    String videoName = videocursor.getString(nameColumnIndex);
                    String videoTime = videocursor.getString(timeColumnIndex);
                    String videoSize = videocursor.getString(sizeColumnIndex);
                    File videoFile = new File(videoPath);
                    if (videoFile.exists()) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = videoPath;
                        fileInfo.name = videoName;
                        fileInfo.time = videoTime;
                        fileInfo.type = FileInfo.TYPE.VIDEO;
                        fileInfo.size = Long.valueOf(videoSize);
                        if (mSDCardPath == null) {
                            if (videoPath.contains(Constant.ROOT_LOCAL))
                                mFileList.add(fileInfo);
                        } else {
                            if (videoPath.contains(mSDCardPath))
                                mFileList.add(fileInfo);
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
        try {
            String[] proj = {MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.SIZE};

            final String orderBy = MediaStore.Files.FileColumns.DATE_ADDED;

            String select = "(" + MediaStore.Files.FileColumns.DATA + " LIKE '%.doc'" + " or "
                    + MediaStore.Files.FileColumns.DATA + " LIKE '%.docx'" + " or "
                    + MediaStore.Files.FileColumns.DATA + " LIKE '%.xls'" + " or "
                    + MediaStore.Files.FileColumns.DATA + " LIKE '%.ppt'" + " or "
                    + MediaStore.Files.FileColumns.DATA + " LIKE '%.pdf'" + " or "
                    + MediaStore.Files.FileColumns.DATA + " LIKE '%.txt'" + ")";

            Cursor docscursor = mContext.getContentResolver().query(
                    MediaStore.Files.getContentUri("external"), proj, select, null, orderBy + " DESC");
            if (docscursor != null) {
                while (docscursor.moveToNext()) {
                    int pathColumnIndex = docscursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    int timeColumnIndex = docscursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED);
                    int sizeColumnIndex = docscursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);

                    String docPath = docscursor.getString(pathColumnIndex);
                    String docTime = docscursor.getString(timeColumnIndex);
                    String docSize = docscursor.getString(sizeColumnIndex);
                    File docFile = new File(docPath);
                    if (docFile.exists() && !docFile.isDirectory()) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = docPath;
                        fileInfo.name = docFile.getName();
                        fileInfo.time = docTime;
                        fileInfo.type = FileInfo.TYPE.FILE;
                        fileInfo.size = Long.valueOf(docSize);
                        if (mSDCardPath == null) {
                            if (docPath.contains(Constant.ROOT_LOCAL))
                                mFileList.add(fileInfo);
                        } else {
                            if (docPath.contains(mSDCardPath))
                                mFileList.add(fileInfo);
                        }
                    }
                }
            }
            docscursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return mFileList;
        }
        return mFileList;
    }

    private ArrayList<FileInfo> getAllEncs() {

        try {
            String[] proj = {
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.SIZE};
            Uri contextUri = MediaStore.Files.getContentUri("external");

            final String orderBy = MediaStore.Files.FileColumns.DATE_ADDED;
            Cursor encCursor = mContext.getContentResolver().query(
                    contextUri, proj,
                    null, null, orderBy + " DESC");
            if (encCursor != null) {
                int pathColumnIndex = encCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                int timeColumnIndex = encCursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED);
                int sizeColumnIndex = encCursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);
                while (encCursor.moveToNext()) {
                    String path = encCursor.getString(pathColumnIndex);
                    String name = path.substring(path.lastIndexOf('/')+1);
                    String ext = name.substring(name.lastIndexOf('.'));

                    if (!path.contains("/.") && ".enc".equals(ext)) {
                        File file = new File(path);
                        if (file.exists() && !file.isDirectory()) {
                            FileInfo fileInfo = new FileInfo();
                            fileInfo.path = path;
                            fileInfo.name = name;
                            fileInfo.time = encCursor.getString(timeColumnIndex);
                            fileInfo.size = Long.valueOf(encCursor.getString(sizeColumnIndex));
                            fileInfo.type = FileInfo.TYPE.ENCRYPT;
                            mFileList.add(fileInfo);
                        }
                    }
                }
            }
            encCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return mFileList;
        }
        return mFileList;
    }

    private ArrayList<FileInfo> getFileList() {
        String path;
        if (mSDCardPath == null)
            path = Constant.ROOT_LOCAL;
        else
            path = FileFactory.getSdPath(mContext);
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
            fileInfo.type = file.isFile() ? FileInfo.getType(file.getPath()) : FileInfo.TYPE.DIR;
            fileInfo.size = file.length();
            mFileList.add(fileInfo);
        }
        Collections.sort(mFileList, FileInfoSort.comparator(mContext));
        FileFactory.getInstance().addFolderFilterRule(path, mFileList);
        FileFactory.getInstance().addFileTypeSortRule(mFileList);

        return mFileList;
    }
//////local & sd card function end//////

}
