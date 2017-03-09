package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.v4.provider.DocumentFile;

import java.util.ArrayList;

/**
 * Created by wangbojie on 2016/6/8.
 */
public class OTGNewFolderLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = OTGNewFolderLoader.class.getSimpleName();

    private String mName;
    private DocumentFile mDocumentFile;
    public OTGNewFolderLoader(Context context, String name, ArrayList<DocumentFile> documentFiles) {
        super(context);
        mName = name;
        mDocumentFile = documentFiles.get(0);
    }

    @Override
    public Boolean loadInBackground() {
        return createNewFolder();
    }

    private boolean createNewFolder() {

        if (mDocumentFile.findFile(mName) == null) {
            mDocumentFile.createDirectory(mName);
            return true;
        }
        return false;
    }
}
