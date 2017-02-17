package com.transcend.otg.Loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.transcend.otg.Browser.BrowserFragment;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Utils.FileFactory;
import com.transcend.otg.Utils.FileInfoSort;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
        Uri rootUri2 = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri));
        String[] proj = {DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                DocumentsContract.Document.COLUMN_MIME_TYPE};

        final String orderBy = DocumentsContract.Document.COLUMN_LAST_MODIFIED;
        Cursor imagecursor = mContext.getContentResolver().query(rootUri2, proj, null, null, orderBy + " DESC");
        while (imagecursor.moveToNext()) {
            String type = imagecursor.getString(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
            if (type.contains(IMAGE)) {
                FileInfo item = new FileInfo();
                item.name = imagecursor.getString(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                item.time = FileInfo.getTime(imagecursor.getLong(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                item.size = imagecursor.getLong(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                item.path = imagecursor.getString(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
//                item.uri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri2, imagecursor.getString(0)).toString();
                item.type = FileInfo.TYPE.PHOTO;
                mFileList.add(item);
            }
        }
        imagecursor.close();
        return mFileList;
    }

    private ArrayList<FileInfo> getAllVideos() {
        Uri rootUri2 = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri));
        String[] proj = {DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                DocumentsContract.Document.COLUMN_MIME_TYPE};

        final String orderBy = DocumentsContract.Document.COLUMN_LAST_MODIFIED;
        Cursor imagecursor = mContext.getContentResolver().query(rootUri2, proj, null, null, orderBy + " DESC");
        while (imagecursor.moveToNext()) {
            String type = imagecursor.getString(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
            if (type.contains(VIDEO)) {
                FileInfo item = new FileInfo();
                item.name = imagecursor.getString(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                item.time = FileInfo.getTime(imagecursor.getLong(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                item.size = imagecursor.getLong(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                item.path = imagecursor.getString(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
//                item.uri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri2, imagecursor.getString(0)).toString();
                item.type = FileInfo.TYPE.VIDEO;
                mFileList.add(item);
            }
        }
        imagecursor.close();
        return mFileList;
    }

    private ArrayList<FileInfo> getAllMusics() {
        Uri rootUri2 = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri));
        String[] proj = {DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                DocumentsContract.Document.COLUMN_MIME_TYPE};

        final String orderBy = DocumentsContract.Document.COLUMN_LAST_MODIFIED;
        Cursor imagecursor = mContext.getContentResolver().query(rootUri2, proj, null, null, orderBy + " DESC");
        while (imagecursor.moveToNext()) {
            String type = imagecursor.getString(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
            if (type.contains(AUDIO)) {
                FileInfo item = new FileInfo();
                item.name = imagecursor.getString(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                item.time = FileInfo.getTime(imagecursor.getLong(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                item.size = imagecursor.getLong(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                item.path = imagecursor.getString(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
//                item.uri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri2, imagecursor.getString(0)).toString();
                item.type = FileInfo.TYPE.MUSIC;
                mFileList.add(item);
            }
        }
        imagecursor.close();
        return mFileList;
    }

    private ArrayList<FileInfo> getAllDocs() {
        Uri rootUri2 = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, DocumentsContract.getTreeDocumentId(rootUri));
        String[] proj = {DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                DocumentsContract.Document.COLUMN_MIME_TYPE};

        final String orderBy = DocumentsContract.Document.COLUMN_LAST_MODIFIED;
        Cursor imagecursor = mContext.getContentResolver().query(rootUri2, proj, null, null, orderBy + " DESC");
        while (imagecursor.moveToNext()) {
            String type = imagecursor.getString(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
            if (type.contains(TEXT) || type.contains(PDF) || type.contains(WORD) || type.contains(PPT) || type.contains(EXCEL)) {
                FileInfo item = new FileInfo();
                item.name = imagecursor.getString(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                item.time = FileInfo.getTime(imagecursor.getLong(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
                item.size = imagecursor.getLong(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                item.path = imagecursor.getString(imagecursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
//                item.uri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri2, imagecursor.getString(0)).toString();
                item.type = FileInfo.TYPE.FILE;
                mFileList.add(item);
            }
        }
        imagecursor.close();
        return mFileList;
    }

    private ArrayList<FileInfo> getFileList() {
        for (DocumentFile ddFile : dFile.listFiles()) {
            FileInfo tmpFileInfo = new FileInfo();
            tmpFileInfo.checked = false;
            tmpFileInfo.name = ddFile.getName();
            tmpFileInfo.path = ddFile.getUri().getPath();
            tmpFileInfo.time = FileInfo.getTime(ddFile.lastModified());
//            tmpFileInfo.uri = ddFile.getUri().toString();
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
