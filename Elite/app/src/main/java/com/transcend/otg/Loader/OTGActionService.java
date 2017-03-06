package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.support.v4.provider.DocumentFile;

import com.transcend.otg.Constant.LoaderID;

import java.util.ArrayList;
import java.util.List;



/**
 * Created by wangbojie on 2017/3/3.
 */

public class OTGActionService extends LocalActionService {

    public OTGActionService() {
        OTGLIST = LoaderID.OTG_FILE_LIST;

//        UPLOAD = LoaderID.LOCAL_FILE_UPLOAD;
//        CreateFOLDER = LoaderID.LOCAL_NEW_FOLDER;
//        RENAME = LoaderID.LOCAL_FILE_RENAME;
//        COPY = LoaderID.LOCAL_FILE_COPY;
//        MOVE = LoaderID.LOCAL_FILE_MOVE;
//        DELETE = LoaderID.LOCAL_FILE_DELETE;
//        SHARE = LoaderID.LOCAL_FILE_SHARE;
//        mMode = NASApp.MODE_STG;
//        mRoot = NASApp.ROOT_STG;
//        mPath = NASApp.ROOT_STG;
    }

    @Override
    public void onLoadFinished(Context context, Loader<Boolean> loader, Boolean success) {

    }
}
