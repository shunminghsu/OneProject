package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.text.format.Formatter;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Utils.FileFactory;
import com.transcend.otg.Utils.FileInfoSort;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by wangbojie on 2017/2/6.
 */

public class LocalListOnlyFolderLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = LocalListOnlyFolderLoader.class.getSimpleName();

    private ArrayList<FileInfo> mFileList;
    private String mPath;
    private Boolean mIsLocal;
    private Context mContext;

    public LocalListOnlyFolderLoader(Context context, String path) {
        super(context);
        mContext = context;
        mFileList = new ArrayList<FileInfo>();
        mPath = path;
    }

    @Override
    public Boolean loadInBackground() {
        if (mPath != null)
            mIsLocal = mPath.startsWith(Constant.ROOT_LOCAL);
        else
            return false;
        return updateFileList();
    }

    private boolean updateFileList() {
        File dir = new File(mPath);
        if (!dir.exists())
            return false;
        File files[] = dir.listFiles();
        for (File file : files) {
            if (file.isHidden())
                continue;
            if(file.isDirectory()){
                FileInfo fileInfo = new FileInfo();
                fileInfo.path = file.getPath();
                fileInfo.name = file.getName();
                fileInfo.time = FileFactory.getTime(file.lastModified());
                fileInfo.type = Constant.TYPE_DIR;
                fileInfo.size = file.length();
                fileInfo.format_size = Formatter.formatFileSize(mContext, fileInfo.size);
                if (mIsLocal)
                    fileInfo.storagemode = Constant.STORAGEMODE_LOCAL;
                else
                    fileInfo.storagemode = Constant.STORAGEMODE_SD;
                mFileList.add(fileInfo);
            }
        }
        Collections.sort(mFileList, FileInfoSort.comparator_destination());
        //FileFactory.getInstance().addFolderFilterRule(path, mFileList);
        //FileFactory.getInstance().addFileTypeSortRule(mFileList);
        return true;
    }

    public String getPath() {
        return mPath;
    }

    public ArrayList<FileInfo> getFileList() {
        return mFileList;
    }

}
