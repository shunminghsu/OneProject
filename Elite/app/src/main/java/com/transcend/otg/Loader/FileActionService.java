package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.provider.DocumentFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangbojie on 2017/2/7.
 */

public abstract class FileActionService {
    public String TAG = FileActionService.class.getSimpleName();
    protected String mMode;
    protected String mRoot;
    protected String mPath;
    protected int OPEN;
    protected int LIST;
    protected int DOWNLOAD;
    protected int UPLOAD;
    protected int NEWFOLDER;
    protected int RENAME;
    protected int COPY;
    protected int MOVE;
    protected int DELETE;
    protected int SHARE;
    protected int LIST_ALL_TYPE;
    protected int OTGLIST;
    protected int RENAME_OTG;
    protected int DELETE_OTG;
    protected int NEWFOLDER_OTG;
    protected int COPY_OTG;
    protected int COPY_LOCAL_OTG;
    protected int COPY_OTG_LOCAL;
    protected int MOVE_OTG;
    protected int MOVE_LOCAL_OTG;
    protected int MOVE_OTG_LOCAL;
    protected int NEWFOLDER_ENCRYPT;
    protected int COPY_ENCRYPT;
    protected int ENCRYPT;
    protected int DECRYPT;

    public enum FileAction {
        OPEN, LIST, DOWNLOAD, UPLOAD, RENAME, COPY, MOVE, DELETE, NEWFOLDER, SHARE, LIST_ALL_TYPE, OTGLIST, RENAME_OTG, DELETE_OTG,
        NEWFOLDER_OTG, COPY_OTG, COPY_LOCAL_OTG, COPY_OTG_LOCAL, MOVE_OTG, MOVE_LOCAL_OTG, MOVE_OTG_LOCAL, NEWFOLDER_ENCRYPT, COPY_ENCRYPT,
        ENCRYPT, DECRYPT
    }

    public String getMode(Context context){
        return mMode;
    }

    public String getRootPath(Context context){
        return mRoot;
    }

    public void setCurrentPath(String path){
        mPath = path;
    }

//    public void setExternalStorageController(ExternalStorageController controller){
//        mExternalStorageController = controller;
//    }
//
//    protected boolean isWritePermissionRequired(Context context, String path) {
//        if(mExternalStorageController != null)
//            return mExternalStorageController.isWritePermissionRequired(path);
//        return false;
//    }

    public FileAction getFileAction(int action){
        FileAction fileAction = null;
        if(action == OPEN)
            fileAction = FileAction.OPEN;
        else if(action == LIST)
            fileAction = FileAction.LIST;
        else if(action == DOWNLOAD)
            fileAction = FileAction.DOWNLOAD;
        else if(action == UPLOAD)
            fileAction = FileAction.UPLOAD;
        else if(action == RENAME)
            fileAction = FileAction.RENAME;
        else if(action == COPY)
            fileAction = FileAction.COPY;
        else if(action == MOVE)
            fileAction = FileAction.MOVE;
        else if(action == DELETE)
            fileAction = FileAction.DELETE;
        else if(action == NEWFOLDER)
            fileAction = FileAction.NEWFOLDER;
        else if(action == SHARE)
            fileAction = FileAction.SHARE;
        else if(action == LIST_ALL_TYPE)
            fileAction = FileAction.LIST_ALL_TYPE;
        else if(action == OTGLIST)
            fileAction = FileAction.OTGLIST;
        else if(action == RENAME_OTG)
            fileAction = FileAction.RENAME_OTG;
        else if(action == DELETE_OTG)
            fileAction = FileAction.DELETE_OTG;
        else if(action == NEWFOLDER_OTG)
            fileAction = FileAction.NEWFOLDER_OTG;
        else if(action == COPY_OTG)
            fileAction = FileAction.COPY_OTG;
        else if(action == COPY_LOCAL_OTG)
            fileAction = FileAction.COPY_LOCAL_OTG;
        else if(action == COPY_OTG_LOCAL)
            fileAction = FileAction.COPY_OTG_LOCAL;
        else if(action == MOVE_OTG)
            fileAction = FileAction.MOVE_OTG;
        else if(action == MOVE_LOCAL_OTG)
            fileAction = FileAction.MOVE_LOCAL_OTG;
        else if(action == MOVE_OTG_LOCAL)
            fileAction = FileAction.MOVE_OTG_LOCAL;
        else if(action == NEWFOLDER_ENCRYPT)
            fileAction = FileAction.NEWFOLDER_ENCRYPT;
        else if(action == COPY_ENCRYPT)
            fileAction = FileAction.COPY_ENCRYPT;
        else if(action == ENCRYPT)
            fileAction = FileAction.ENCRYPT;
        else if(action == DECRYPT)
            fileAction = FileAction.DECRYPT;
        return fileAction;
    }

    public int getLoaderID(FileAction action){
        int id = -1;
        switch (action) {
            case OPEN:
                id = OPEN;
                break;
            case LIST:
                id = LIST;
                break;
            case DOWNLOAD:
                id = DOWNLOAD;
                break;
            case UPLOAD:
                id = UPLOAD;
                break;
            case RENAME:
                id = RENAME;
                break;
            case COPY:
                id = COPY;
                break;
            case MOVE:
                id = MOVE;
                break;
            case DELETE:
                id = DELETE;
                break;
            case NEWFOLDER:
                id = NEWFOLDER;
                break;
            case SHARE:
                id = SHARE;
                break;
            case LIST_ALL_TYPE:
                id = LIST_ALL_TYPE;
                break;
            case OTGLIST:
                id = OTGLIST;
                break;
            case RENAME_OTG:
                id = RENAME_OTG;
                break;
            case DELETE_OTG:
                id = DELETE_OTG;
                break;
            case NEWFOLDER_OTG:
                id = NEWFOLDER_OTG;
                break;
            case COPY_OTG:
                id = COPY_OTG;
                break;
            case COPY_LOCAL_OTG:
                id = COPY_LOCAL_OTG;
                break;
            case COPY_OTG_LOCAL:
                id = COPY_OTG_LOCAL;
                break;
            case MOVE_OTG:
                id = MOVE_OTG;
                break;
            case MOVE_LOCAL_OTG:
                id = MOVE_LOCAL_OTG;
                break;
            case MOVE_OTG_LOCAL:
                id = MOVE_OTG_LOCAL;
                break;
            case NEWFOLDER_ENCRYPT:
                id = NEWFOLDER_ENCRYPT;
                break;
            case COPY_ENCRYPT:
                id = COPY_ENCRYPT;
                break;
            case ENCRYPT:
                id = ENCRYPT;
                break;
            case DECRYPT:
                id = DECRYPT;
                break;
        }

        return id;
    }

    public Loader<Boolean> onCreateLoader(Context context, FileAction id, Bundle args){
        ArrayList<String> paths = args.getStringArrayList("paths");
        String path = args.getString("path");
        String name = args.getString("name");
        Uri uri = args.getParcelable("uri");
        ArrayList<DocumentFile> dFiles = (ArrayList<DocumentFile>) args.getSerializable("dFile");
        ArrayList<DocumentFile> dFiles2 = (ArrayList<DocumentFile>) args.getSerializable("dFile2");
        switch (id) {
            case LIST:
                return list(context, path);
            case UPLOAD:
                return upload(context, paths, path);
            case DOWNLOAD:
                return download(context, paths, path);
            case RENAME:
                return rename(context, path, name);
            case COPY:
                return copy(context, paths, path);
            case MOVE:
                return move(context, paths, path);
            case DELETE:
                return delete(context, paths);
            case NEWFOLDER:
                return newFolder(context, path);
            case SHARE:
                return share(context, paths, path);
            case LIST_ALL_TYPE:
                return listAllType(context);
            case OTGLIST:
                return otglist(context, uri, name);
            case RENAME_OTG:
                return renameOTG(context, name, dFiles);
            case DELETE_OTG:
                return deleteOTG(context, dFiles);
            case NEWFOLDER_OTG:
                return newFolderOTG(context, name, dFiles);
            case COPY_OTG:
                return copyOTG(context, dFiles, dFiles2);
            case COPY_LOCAL_OTG:
                return copyLocaltoOTG(context, paths, dFiles);
            case COPY_OTG_LOCAL:
                return copyOTGtoLocal(context, dFiles, path);
            case MOVE_OTG:
                return moveOTG(context, dFiles, dFiles2);
            case MOVE_LOCAL_OTG:
                return moveLocaltoOTG(context, paths, dFiles);
            case MOVE_OTG_LOCAL:
                return moveOTGtoLocal(context, dFiles, path);
            case NEWFOLDER_ENCRYPT:
                return newFolderEncrypt(context, path);
            case COPY_ENCRYPT:
                return copyEncrypt(context, paths, path);
            case ENCRYPT:
                return encrypt(context, paths);
            case DECRYPT:
                return decrypt(context, paths);
        }

        return null;
    }

    public abstract void onLoadFinished(Context context, Loader<Boolean> loader, Boolean success);

    protected abstract AsyncTaskLoader open(Context context, String path);

    protected abstract AsyncTaskLoader list(Context context, String path);

    protected abstract AsyncTaskLoader download(Context context, List<String> list, String dest);

    protected abstract AsyncTaskLoader upload(Context context, List<String> list, String dest);

    protected abstract AsyncTaskLoader rename(Context context, String path, String name);

    protected abstract AsyncTaskLoader copy(Context context, List<String> list, String dest);

    protected abstract AsyncTaskLoader move(Context context, List<String> list, String dest);

    protected abstract AsyncTaskLoader delete(Context context, List<String> list);

    protected abstract AsyncTaskLoader newFolder(Context context, String path);

    protected abstract AsyncTaskLoader share(Context context, ArrayList<String> paths, String dest);

    protected abstract AsyncTaskLoader listAllType(Context context);

    protected abstract AsyncTaskLoader otglist(Context context, Uri uri, String na);

    protected abstract AsyncTaskLoader renameOTG(Context context, String name, ArrayList<DocumentFile> dFiles);

    protected abstract AsyncTaskLoader deleteOTG(Context context, ArrayList<DocumentFile> dFiles);

    protected abstract AsyncTaskLoader newFolderOTG(Context context, String name, ArrayList<DocumentFile> dFiles);

    protected abstract AsyncTaskLoader copyOTG(Context context, ArrayList<DocumentFile> dFiles, ArrayList<DocumentFile> dFiles2);

    protected abstract AsyncTaskLoader copyLocaltoOTG(Context context, List<String> list, ArrayList<DocumentFile> dFiles);

    protected abstract AsyncTaskLoader copyOTGtoLocal(Context context, ArrayList<DocumentFile> dFiles, String dest);

    protected abstract AsyncTaskLoader moveOTG(Context context, ArrayList<DocumentFile> dFiles, ArrayList<DocumentFile> dFiles2);

    protected abstract AsyncTaskLoader moveLocaltoOTG(Context context, List<String> list, ArrayList<DocumentFile> dFiles);

    protected abstract AsyncTaskLoader moveOTGtoLocal(Context context, ArrayList<DocumentFile> dFiles, String dest);

    protected abstract AsyncTaskLoader newFolderEncrypt(Context context, String path);

    protected abstract AsyncTaskLoader copyEncrypt(Context context, List<String> list, String dest);

    protected abstract AsyncTaskLoader encrypt(Context context, List<String> list);

    protected abstract AsyncTaskLoader decrypt(Context context, List<String> list);
}
