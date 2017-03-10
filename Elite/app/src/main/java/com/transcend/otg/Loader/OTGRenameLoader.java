package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import com.transcend.otg.Constant.ActionParameter;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by wangbojie on 2016/6/8.
 */
public class OTGRenameLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = OTGRenameLoader.class.getSimpleName();
    private Context mContext;
    private ArrayList<DocumentFile> mSelectFiles;
    private String mNewName;

    public OTGRenameLoader(Context context, String name, ArrayList<DocumentFile> dFile) {
        super(context);
        mContext = context;
        mNewName = name;
        mSelectFiles = dFile;
    }

    @Override
    public Boolean loadInBackground() {
        try {
            return rename();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean rename() throws InterruptedException {
        if (mSelectFiles.get(0).exists()) {
            String oldName = mSelectFiles.get(0).getName();
            if(mSelectFiles.get(0).renameTo(mNewName)){
                String newName = mNewName;
                String path = ActionParameter.files.get(0).path;
                path = path.replace(oldName, newName);
                File rename = new File(path);
                mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(rename)));
                Thread.sleep(500);
            }
            return true;
        }
        return false;
    }

    public String getNewName(){
        return mNewName;
    }
}
