package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.io.File;

/**
 * Created by wangbojie on 2016/6/20.
 */
public class LocalNewFolderLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = LocalNewFolderLoader.class.getSimpleName();

    private String mPath;

    public LocalNewFolderLoader(Context context, String path) {
        super(context);
        mPath = path;
    }

    @Override
    public Boolean loadInBackground() {
        return createNewFolder();
    }

    private boolean createNewFolder() {
        File dir = new File(mPath);
        if (!dir.exists()) {
            boolean b = dir.mkdirs();
            return true;
        }
        return false;
    }

}
