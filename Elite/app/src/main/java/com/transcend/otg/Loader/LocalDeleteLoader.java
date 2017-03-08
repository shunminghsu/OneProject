package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.List;

/**
 * Created by wangbojie on 2017/3/7.
 */

public class LocalDeleteLoader extends AsyncTaskLoader<Boolean> {
    private static final String TAG = LocalDeleteLoader.class.getSimpleName();
    private Context mContext;
    private List<String> mPaths;

    public LocalDeleteLoader(Context context, List<String> paths) {
        super(context);
        mContext = context;
        mPaths = paths;
    }

    @Override
    public Boolean loadInBackground() {
        return delete();
    }

    private boolean delete() {
        for (String path : mPaths) {
            File target = new File(path);
            if (target.isDirectory())
                deleteDirectory(target);
            else
                target.delete();
        }

        return true;
    }

    private void deleteDirectory(File dir) {
        for (File target : dir.listFiles()) {
            if (target.isDirectory())
                deleteDirectory(target);
            else
                target.delete();
        }
        dir.delete();
    }
}
