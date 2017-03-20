package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

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
        NEWFOLDER = LoaderID.LOCAL_NEW_FOLDER;
        RENAME = LoaderID.LOCAL_FILE_RENAME;
        COPY = LoaderID.LOCAL_COPY;
//        MOVE = LoaderID.LOCAL_FILE_MOVE;
        DELETE = LoaderID.LOCAL_FILE_DELETE;
        OTGLIST = LoaderID.OTG_FILE_LIST;
        RENAME_OTG = LoaderID.OTG_RENAME;
        DELETE_OTG = LoaderID.OTG_DELETE;
        NEWFOLDER_OTG = LoaderID.OTG_NEW_FOLDER;
        COPY_OTG = LoaderID.OTG_COPY;
        COPY_LOCAL_OTG = LoaderID.LOCAL_OTG_COPY;
        COPY_OTG_LOCAL = LoaderID.OTG_LOCAL_COPY;

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
        return new LocalListLoader(context, path);
    }

    @Override
    protected AsyncTaskLoader listAllType(Context context) {
        return new LocalTypeListLoader(context);
    }

    @Override
    protected AsyncTaskLoader otglist(Context context, Uri uri, String selectName) {
        return new OTGFileLoader(context, uri, selectName);
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
        return new LocalRenameLoader(context, path, name);
    }

    @Override
    protected AsyncTaskLoader renameOTG(Context context, String name, ArrayList<DocumentFile> dFiles) {
        return new OTGRenameLoader(context, name, dFiles);
    }

    @Override
    protected AsyncTaskLoader copy(Context context, List<String> sources, String dest) {
        return new LocalCopyLoader(context, sources, dest);
    }

    @Override
    protected AsyncTaskLoader copyOTG(Context context, ArrayList<DocumentFile> dFiles, ArrayList<DocumentFile> dFiles2) {
        return new OTGCopyLoader(context, dFiles, dFiles2);
    }

    @Override
    protected AsyncTaskLoader copyLocaltoOTG(Context context, List<String> list, ArrayList<DocumentFile> dFiles) {
        return new LocalCopytoOTGLoader(context, list, dFiles);
    }

    @Override
    protected AsyncTaskLoader copyOTGtoLocal(Context context, ArrayList<DocumentFile> dFiles, String dest) {
        return new OTGCopytoLocalLoader(context, dFiles, dest);
    }

    @Override
    protected AsyncTaskLoader move(Context context, List<String> list, String dest) {
        return null;
    }

    @Override
    protected AsyncTaskLoader delete(Context context, List<String> list) {
        return new LocalDeleteLoader(context, list);
    }

    @Override
    protected AsyncTaskLoader deleteOTG(Context context, ArrayList<DocumentFile> dFiles) {
        return new OTGDeleteLoader(context, dFiles);
    }

    @Override
    protected AsyncTaskLoader newFolder(Context context, String path) {
        return new LocalNewFolderLoader(context, path);
    }

    @Override
    protected AsyncTaskLoader newFolderOTG(Context context, String name, ArrayList<DocumentFile> dFiles) {
        return new OTGNewFolderLoader(context, name, dFiles);
    }

    @Override
    protected AsyncTaskLoader share(Context context, ArrayList<String> paths, String dest) {
        return null;
    }
}
