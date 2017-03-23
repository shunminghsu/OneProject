package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * Created by wangbojie on 2016/6/20.
 */
public class LocalEncryptNewFolderLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = LocalEncryptNewFolderLoader.class.getSimpleName();
    private Context mContext;
    private String mPath;

    public LocalEncryptNewFolderLoader(Context context, String path) {
        super(context);
        mContext = context;
        mPath = path;
    }

    @Override
    public Boolean loadInBackground() {
        try {
            return createNewFolder();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean createNewFolder() throws InterruptedException {
        File dir = new File(mPath);
        if (!dir.exists()) {
            boolean b = dir.mkdirs();
            if(b){
                mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dir)));
            }
            return true;
        }
        return false;
    }

}
