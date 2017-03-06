package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Constant.LoaderID;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangbojie on 2017/2/7.
 */

public class LocalActionService extends FileActionService{

    public LocalActionService(){
        TAG = LocalActionService.class.getSimpleName();
        LIST = LoaderID.LOCAL_FILE_LIST;
        LIST_ALL_TYPE = LoaderID.LOCAL_ALL_TYPE_LIST;
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

    @Override
    protected AsyncTaskLoader open(Context context, String path) {
        return null;
    }

    @Override
    protected AsyncTaskLoader list(Context context, String path) {
        return new LocalFileListLoader(context, path);
    }

    @Override
    protected AsyncTaskLoader listAllType(Context context) {
        return new LocalTypeListLoader(context);
    }

    @Override
    protected AsyncTaskLoader otglist(Context context, Uri uri, String selectName) {
        return new OTGFileListLoader(context, uri, selectName);
    }

    @Override
    protected AsyncTaskLoader download(Context context, List<String> list, String dest) {
        return null;
    }

    @Override
    protected AsyncTaskLoader upload(Context context, List<String> list, String dest) {
        return null;
    }

    @Override
    protected AsyncTaskLoader rename(Context context, String path, String name) {
        return null;
    }

    @Override
    protected AsyncTaskLoader copy(Context context, List<String> list, String dest) {
        return null;
    }

    @Override
    protected AsyncTaskLoader move(Context context, List<String> list, String dest) {
        return null;
    }

    @Override
    protected AsyncTaskLoader delete(Context context, List<String> list) {
        return null;
    }

    @Override
    protected AsyncTaskLoader createFolder(Context context, String path) {
        return null;
    }

    @Override
    protected AsyncTaskLoader share(Context context, ArrayList<String> paths, String dest) {
        return null;
    }
}
