package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Utils.FileFactory;
import com.transcend.otg.Utils.FileInfoSort;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by wangbojie on 2017/3/3.
 */

public class OTGFileListLoader extends AsyncTaskLoader<Boolean> {
    private static final String TAG = OTGFileListLoader.class.getSimpleName();
    private ArrayList<FileInfo> mFileList;
    private DocumentFile dFile;
    private Context mContext;

    String IMAGE = "image";
    String APPLICATION = "application";
    String TEXT = "text";
    String VIDEO = "video";
    String AUDIO = "audio";
    String ENCRYPT = "enc";

    public OTGFileListLoader(Context context, Uri uri, String selectName) {
        super(context);
        mContext = context;
        mFileList = new ArrayList<FileInfo>();
        if (Constant.mCurrentDocumentFileExplore == null) {
            dFile = DocumentFile.fromTreeUri(mContext, uri);
            dFile = dFile.findFile(selectName);
        } else {
            dFile = Constant.mCurrentDocumentFileExplore;
        }

    }

    @Override
    public Boolean loadInBackground() {
        return DocumentFileConvertFileInfo();
    }

    public boolean DocumentFileConvertFileInfo() {
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
