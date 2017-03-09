package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.transcend.otg.Constant.FileInfo;

import java.util.ArrayList;

/**
 * Created by wangbojie on 2016/6/8.
 */
public class OTGRenameLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = OTGRenameLoader.class.getSimpleName();
    private Context mContext;
    private ArrayList<DocumentFile> mSelectFiles;
    private String mNewNmae;

    public OTGRenameLoader(Context context, String name, ArrayList<DocumentFile> dFile) {
        super(context);
        mContext = context;
        mNewNmae = name;
        mSelectFiles = dFile;
    }

    @Override
    public Boolean loadInBackground() {
        return rename();
    }

    private boolean rename() {

        if (mSelectFiles.get(0).exists())
            return mSelectFiles.get(0).renameTo(mNewNmae);
        return false;
    }
}
