package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.v4.provider.DocumentFile;

import java.net.MalformedURLException;
import java.util.List;

/**
 * Created by wangbojie on 2016/3/8.
 */
public class OTGDeleteLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = OTGDeleteLoader.class.getSimpleName();
    private Context mContext;
    private List<DocumentFile> mDeleteDFiles;

    public OTGDeleteLoader(Context context, List<DocumentFile> deleteDFiles) {
        super(context);
        mContext = context;
        mDeleteDFiles = deleteDFiles;
    }

    @Override
    public Boolean loadInBackground() {
        try {
            return delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean delete() throws MalformedURLException {
        for (DocumentFile file : mDeleteDFiles) {
            file.delete();
        }
        return true;
    }
}
