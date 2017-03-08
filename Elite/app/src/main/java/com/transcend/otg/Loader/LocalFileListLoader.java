package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Utils.FileInfoSort;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Utils.FileFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by wangbojie on 2017/2/6.
 */

public class LocalFileListLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = LocalFileListLoader.class.getSimpleName();

    private ArrayList<FileInfo> mFileList;
    private String mPath;
    private Context mContext;

    public LocalFileListLoader(Context context, String path) {
        super(context);
        mContext = context;
        mFileList = new ArrayList<FileInfo>();
        mPath = path;
    }

    @Override
    public Boolean loadInBackground() {
        return updateFileList();
    }

    private boolean updateFileList() {
        if (mPath == null)
            return false;
        File dir = new File(mPath);
        if (!dir.exists())
            return false;
        File files[] = dir.listFiles();
        for (File file : files) {
            if (file.isHidden())
                continue;
            FileInfo fileInfo = new FileInfo();
            fileInfo.path = file.getPath();
            fileInfo.name = file.getName();
            fileInfo.time = FileInfo.getTime(file.lastModified());
            fileInfo.type = file.isFile() ? FileInfo.getType(file.getPath()) : Constant.TYPE_DIR;
            fileInfo.size = file.length();
            mFileList.add(fileInfo);
        }
        Collections.sort(mFileList, FileInfoSort.comparator(mContext));
        FileFactory.getInstance().addFolderFilterRule(mPath, mFileList);
        FileFactory.getInstance().addFileTypeSortRule(mFileList);
        Log.w(TAG, "mFileList size: " + mFileList.size());
        return true;
    }

    public String getPath() {
        return mPath;
    }

    public ArrayList<FileInfo> getFileList() {
        return mFileList;
    }

}
