package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;
import android.text.format.Formatter;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.LocalPreferences;
import com.transcend.otg.Utils.FileFactory;
import com.transcend.otg.Utils.FileInfoSort;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by wangbojie on 2017/3/3.
 */

public class OTGFileLoader extends AsyncTaskLoader<Boolean> {
    private static final String TAG = OTGFileLoader.class.getSimpleName();
    private ArrayList<FileInfo> mFileList;
    private DocumentFile dFile;
    private Context mContext;

    String APPLICATION = "application";
    String IMAGE = "image";
    String WORD = "word";
    String PDF = "pdf";
    String PPT = "powerpoint";
    String EXCEL = "excel";
    String TEXT = "text";
    String VIDEO = "video";
    String AUDIO = "audio";
    String ENCRYPT = ".enc";
    String DIR = "directory";
    String PNG = "png";
    String JPG = "jpg";
    String[] proj = {DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_MIME_TYPE};

    public OTGFileLoader(Context context, Uri uri, String selectName) {
        super(context);
        mContext = context;
        mFileList = new ArrayList<FileInfo>();


        if (Constant.Activity == 0) {
            dFile = DocumentFile.fromTreeUri(mContext, uri);
            dFile = dFile.findFile(selectName);
        } else if(Constant.Activity == 1){
            if(Constant.mCurrentDocumentFileExplore == null){
                dFile = DocumentFile.fromTreeUri(mContext, uri);
                dFile = dFile.findFile(selectName);
            }else
                dFile = Constant.mCurrentDocumentFileExplore;
        } else if(Constant.Activity == 2)
            dFile = Constant.mCurrentDocumentFileDestination;
    }

    @Override
    public Boolean loadInBackground() {
        Uri baseRootUri = DocumentsContract.buildChildDocumentsUriUsingTree(dFile.getUri(), DocumentsContract.getDocumentId(dFile.getUri()));
        if (getOtgFileList(baseRootUri)) {
            SortList(mFileList);
            return true;
        }
        return false;
    }

    /*public boolean DocumentFileConvertFileInfo() {
        try {
            for (DocumentFile ddFile : dFile.listFiles()) {
                FileInfo tmpFileInfo = new FileInfo();
                tmpFileInfo.checked = false;
                tmpFileInfo.name = ddFile.getName();
                tmpFileInfo.path = ddFile.getUri().getPath();
                tmpFileInfo.time = FileInfo.getTime(ddFile.lastModified());
                tmpFileInfo.uri = ddFile.getUri();
                tmpFileInfo.size = ddFile.length();
                tmpFileInfo.storagemode = Constant.STORAGEMODE_OTG;
                String type = ddFile.getType();
                if (type != null) {
                    if (type.contains(IMAGE))
                        tmpFileInfo.type = Constant.TYPE_PHOTO;
                    else if (type.contains(APPLICATION) || type.contains(TEXT))
                        tmpFileInfo.type = Constant.TYPE_DOC;
                    else if (type.contains(VIDEO))
                        tmpFileInfo.type = Constant.TYPE_VIDEO;
                    else if (type.contains(AUDIO))
                        tmpFileInfo.type = Constant.TYPE_MUSIC;
                    else
                        tmpFileInfo.type = Constant.TYPE_DIR;
                    if (tmpFileInfo.name.contains(ENCRYPT))
                        tmpFileInfo.type = Constant.TYPE_ENCRYPT;
                } else {
                    tmpFileInfo.type = Constant.TYPE_DIR;
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
            return true;
        } catch (Exception e) {
            return false;
        }

    }*/

    private boolean getOtgFileList(Uri _rootUri) {

        Cursor cursor = mContext.getContentResolver().query(_rootUri, proj, null, null, null);
        if (cursor == null) {
            cancelLoad();
            return false;
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
                item.path = FileFactory.getOTGStoragePath(mContext, Constant.otg_key_path) + "/" + split[1];
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
                } else if (type.contains(DIR)) {
                    item.type = Constant.TYPE_DIR;
                } else if (name.contains(ENCRYPT)) {
                    item.type = Constant.TYPE_ENCRYPT;
                } else {
                    item.type = Constant.TYPE_OTHER_FILE;
                }

                mFileList.add(item);
            }
        }
        cursor.close();
        return true;
    }

    private void SortList(ArrayList<FileInfo> _list) {
        Collections.sort(_list, FileInfoSort.comparator(mContext));
        int sort_by = LocalPreferences.getPref(mContext, LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_DATE);
        if (sort_by != Constant.SORT_BY_SIZE)
            FileFactory.getInstance().addFileTypeSortRule(_list);
    }

    public String getPath() {
        return dFile.getName();
    }

    public ArrayList<FileInfo> getFileList() {
        return mFileList;
    }

    public DocumentFile getCurrentDocumentFile() {
        return dFile;
    }
}
