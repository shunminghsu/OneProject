package com.transcend.otg.Loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.transcend.otg.Browser.BrowserFragment;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Utils.FileFactory;
import com.transcend.otg.Utils.FileInfoSort;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by wangbojie on 2017/2/13.
 */

public class OTGFileListLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {
    private String TAG = OTGFileListLoader.class.getSimpleName();
    private ArrayList<FileInfo> mFileList;
    private Context mContext;
    private int mType;
    private DocumentFile dFile = null, rootDFile = null;
    private Uri rootUri = null;
    String APPLICATION = "application";
    String IMAGE = "image";
    String WORD = "msword";
    String PDF = "pdf";
    String PPT = "powerpoint";
    String EXCEL = "excel";
    String TEXT = "text";
    String VIDEO = "video";
    String AUDIO = "audio";
    String ENCRYPT = "";
    String DIR = "directory";
    String PNG = "png";
    String JPG = "jpg";
    String ENC = "enc";
    String[] proj = {DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_MIME_TYPE};

    final String orderBy = DocumentsContract.Document.COLUMN_DISPLAY_NAME;


    public OTGFileListLoader(Context context, int type) {
        super(context);
        mFileList = new ArrayList<>();
        mType = type;
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
                return getAllImages();
            case BrowserFragment.LIST_TYPE_VIDEO:
                return getAllVideos();
            case BrowserFragment.LIST_TYPE_MUSIC:
                return getAllMusics();
            case BrowserFragment.LIST_TYPE_DOCUMENT:
                return getAllDocs();
            case BrowserFragment.LIST_TYPE_ENCRYPTION:
                return getAllEncs();
            case BrowserFragment.LIST_TYPE_FOLDER:
                return getFileList();
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

    private ArrayList<FileInfo> getAllImages() {

        Uri rootImageUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri));

        Cursor imageCursor = mContext.getContentResolver().query(rootImageUri, proj, null, null, orderBy + " DESC");
        while (imageCursor.moveToNext()) {
            if (!imageCursor.getString(imageCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = imageCursor.getString(imageCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getAllImagesDIR(DocumentsContract.buildChildDocumentsUriUsingTree(rootImageUri, imageCursor.getString(1)));
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
        return mFileList;
    }

    private void getAllImagesDIR(Uri uriDIR) {
        final String orderBy = DocumentsContract.Document.COLUMN_LAST_MODIFIED;
        Cursor imageDIRCursor = mContext.getContentResolver().query(uriDIR, proj, null, null, orderBy + " DESC");
        while (imageDIRCursor.moveToNext()) {
            if (!imageDIRCursor.getString(imageDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = imageDIRCursor.getString(imageDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getAllImagesDIR(DocumentsContract.buildChildDocumentsUriUsingTree(uriDIR, imageDIRCursor.getString(1)));
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

    private ArrayList<FileInfo> getAllVideos() {
        Uri rootUri2 = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri));

        Cursor videoCursor = mContext.getContentResolver().query(rootUri2, proj, null, null, orderBy + " DESC");
        while (videoCursor.moveToNext()) {
            if (!videoCursor.getString(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = videoCursor.getString(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getAllVideosDIR(DocumentsContract.buildChildDocumentsUriUsingTree(rootUri2, videoCursor.getString(1)));
                } else if (type.contains(VIDEO)) {
                    FileInfo item = new FileInfo();
                    item.name = videoCursor.getString(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    item.time = FileInfo.getTime(videoCursor.getLong(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = videoCursor.getLong(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = videoCursor.getString(videoCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri2, videoCursor.getString(1));
                    item.type = FileInfo.TYPE.VIDEO;
                    mFileList.add(item);
                }
            }
        }
        videoCursor.close();
        return mFileList;
    }

    private void getAllVideosDIR(Uri uriDIR) {
        Cursor videoDIRCursor = mContext.getContentResolver().query(uriDIR, proj, null, null, orderBy + " DESC");
        while (videoDIRCursor.moveToNext()) {
            if (!videoDIRCursor.getString(videoDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = videoDIRCursor.getString(videoDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getAllVideosDIR(DocumentsContract.buildChildDocumentsUriUsingTree(uriDIR, videoDIRCursor.getString(1)));
                } else if (type.contains(VIDEO)) {
                    FileInfo item = new FileInfo();
                    item.name = videoDIRCursor.getString(videoDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    item.time = FileInfo.getTime(videoDIRCursor.getLong(videoDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = videoDIRCursor.getLong(videoDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = videoDIRCursor.getString(videoDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildChildDocumentsUriUsingTree(uriDIR, videoDIRCursor.getString(1));
                    item.type = FileInfo.TYPE.VIDEO;
                    mFileList.add(item);
                }
            }
        }
        videoDIRCursor.close();
    }

    private ArrayList<FileInfo> getAllMusics() {
        Uri musicUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri));
        Cursor musicCursor = mContext.getContentResolver().query(musicUri, proj, null, null, orderBy + " DESC");
        while (musicCursor.moveToNext()) {
            if (!musicCursor.getString(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = musicCursor.getString(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getAllMusicsDIR(DocumentsContract.buildChildDocumentsUriUsingTree(musicUri, musicCursor.getString(1)));
                } else if (type.contains(AUDIO)) {
                    FileInfo item = new FileInfo();
                    item.name = musicCursor.getString(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    item.time = FileInfo.getTime(musicCursor.getLong(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = musicCursor.getLong(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = musicCursor.getString(musicCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildChildDocumentsUriUsingTree(musicUri, musicCursor.getString(1));
                    item.type = FileInfo.TYPE.MUSIC;
                    mFileList.add(item);
                }
            }
        }
        musicCursor.close();
        return mFileList;
    }

    private void getAllMusicsDIR(Uri uriDIR) {
        Cursor musicDIRCursor = mContext.getContentResolver().query(uriDIR, proj, null, null, orderBy + " DESC");
        while (musicDIRCursor.moveToNext()) {
            if (!musicDIRCursor.getString(musicDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = musicDIRCursor.getString(musicDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getAllMusicsDIR(DocumentsContract.buildChildDocumentsUriUsingTree(uriDIR, musicDIRCursor.getString(1)));
                } else if (type.contains(AUDIO)) {
                    FileInfo item = new FileInfo();
                    item.name = musicDIRCursor.getString(musicDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    item.time = FileInfo.getTime(musicDIRCursor.getLong(musicDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = musicDIRCursor.getLong(musicDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = musicDIRCursor.getString(musicDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildChildDocumentsUriUsingTree(uriDIR, musicDIRCursor.getString(1));
                    item.type = FileInfo.TYPE.MUSIC;
                    mFileList.add(item);
                }
            }

        }
        musicDIRCursor.close();
    }

    private ArrayList<FileInfo> getAllDocs() {
        Uri docUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri));
        Cursor docCursor = mContext.getContentResolver().query(docUri, proj, null, null, orderBy + " DESC");
        while (docCursor.moveToNext()) {
            if (!docCursor.getString(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = docCursor.getString(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getAllDocsDIR(DocumentsContract.buildChildDocumentsUriUsingTree(docUri, docCursor.getString(1)));
                } else if (type.contains(TEXT) || type.contains(PDF) || type.contains(WORD) || type.contains(PPT) || type.contains(EXCEL)) {
                    FileInfo item = new FileInfo();
                    item.name = docCursor.getString(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    item.time = FileInfo.getTime(docCursor.getLong(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = docCursor.getLong(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = docCursor.getString(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildChildDocumentsUriUsingTree(docUri, docCursor.getString(1));
                    item.type = FileInfo.TYPE.FILE;
                    mFileList.add(item);
                }
            }
        }
        docCursor.close();
        return mFileList;
    }

    private void getAllDocsDIR(Uri uriDIR) {
        Cursor docDIRCursor = mContext.getContentResolver().query(uriDIR, proj, null, null, orderBy + " DESC");
        while (docDIRCursor.moveToNext()) {
            if (!docDIRCursor.getString(docDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = docDIRCursor.getString(docDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (type.contains(DIR)) {
                    getAllDocsDIR(DocumentsContract.buildChildDocumentsUriUsingTree(uriDIR, docDIRCursor.getString(1)));
                } else if (type.contains(TEXT) || type.contains(PDF) || type.contains(WORD) || type.contains(PPT) || type.contains(EXCEL)) {
                    FileInfo item = new FileInfo();
                    item.name = docDIRCursor.getString(docDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                    item.time = FileInfo.getTime(docDIRCursor.getLong(docDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = docDIRCursor.getLong(docDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = docDIRCursor.getString(docDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildChildDocumentsUriUsingTree(uriDIR, docDIRCursor.getString(1));
                    item.type = FileInfo.TYPE.MUSIC;
                    mFileList.add(item);
                }
            }
        }
        docDIRCursor.close();
    }

    private ArrayList<FileInfo> getAllEncs() {
        Uri encUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri));
        Cursor encCursor = mContext.getContentResolver().query(encUri, proj, null, null, orderBy + " DESC");
        while (encCursor.moveToNext()) {
            if (!encCursor.getString(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = encCursor.getString(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                String name = encCursor.getString(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                if (type.contains(DIR)) {
                    getAllEncsDIR(DocumentsContract.buildChildDocumentsUriUsingTree(encUri, encCursor.getString(1)));
                } else if (name.contains(".enc")) {
                    FileInfo item = new FileInfo();
                    item.name = name;
                    item.time = FileInfo.getTime(encCursor.getLong(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = encCursor.getLong(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = encCursor.getString(encCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildChildDocumentsUriUsingTree(encUri, encCursor.getString(1));
                    item.type = FileInfo.TYPE.ENCRYPT;
                    mFileList.add(item);
                }
            }
        }
        encCursor.close();
        return mFileList;
    }

    private void getAllEncsDIR(Uri uriDIR) {
        Cursor encDIRCursor = mContext.getContentResolver().query(uriDIR, proj, null, null, orderBy + " DESC");
        while (encDIRCursor.moveToNext()) {
            if (!encDIRCursor.getString(encDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)).substring(0, 1).equals(".")) {
                String type = encDIRCursor.getString(encDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                String name = encDIRCursor.getString(encDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                if (type.contains(DIR)) {
                    getAllEncsDIR(DocumentsContract.buildChildDocumentsUriUsingTree(uriDIR, encDIRCursor.getString(1)));
                } else if (name.contains(".enc")) {
                    FileInfo item = new FileInfo();
                    item.name = name;
                    item.time = FileInfo.getTime(encDIRCursor.getLong(encDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                    item.size = encDIRCursor.getLong(encDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                    item.path = encDIRCursor.getString(encDIRCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                    item.uri = DocumentsContract.buildChildDocumentsUriUsingTree(uriDIR, encDIRCursor.getString(1));
                    item.type = FileInfo.TYPE.ENCRYPT;
                    mFileList.add(item);
                }
            }
        }
        encDIRCursor.close();
    }

    private ArrayList<FileInfo> getFileList() {
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


}
