package com.transcend.otg.Loader;

import android.app.Activity;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Utils.FileFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangbojie on 2017/2/7.
 */

public class FileActionManager {
    private String TAG = FileActionManager.class.getSimpleName();
    private Context mContext;
    private RelativeLayout mProgressLayout;
    private FileActionService mFileActionService;
    private Map<MODE, FileActionService> mFileActionServicePool;
    private MODE mMode;
    private android.app.LoaderManager.LoaderCallbacks<Boolean> mCallbacks;

    public enum MODE {
        LOCAL, SD, OTG
    }

    public FileActionManager(Context context, MODE mode, android.app.LoaderManager.LoaderCallbacks<Boolean> callbacks) {
        this(context, mode, callbacks, null);
    }

    public FileActionManager(Context context, MODE mode, android.app.LoaderManager.LoaderCallbacks<Boolean> callbacks, RelativeLayout progressLayout) {
        mContext = context;
        mMode = mode;
        mCallbacks = callbacks;
        mProgressLayout = progressLayout;
        setMode(mode);
    }

    public void setMode(MODE mode) {
        if (mFileActionService != null && mMode == mode)
            return;

        if (null == mFileActionServicePool)
            mFileActionServicePool = new HashMap<>();

        FileActionService service = mFileActionServicePool.get(mode);
        if (null == service) {
            switch (mode) {
                case LOCAL:
                    service = new LocalActionService();
                    break;
                case SD:
                    service = new SDActionService();
                    break;
                case OTG:
                    service = new OTGActionService();
                    break;
            }
            mFileActionServicePool.put(mode, service);
        }

        mMode = mode;
        mFileActionService = service;
    }

    public void checkServiceMode(String path) {
        if (path.contains(Constant.ROOT_LOCAL)) {
            setMode(MODE.LOCAL);
        } else if (path.contains(FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path))) {
            setMode(MODE.SD);
        }
    }


    public String getLocalRootPath() {
        String root = Constant.ROOT_LOCAL;
        return root;
    }

    public void list(String path) {
        createLoader(FileActionService.FileAction.LIST, null, path, null, null, null, null);
    }

    public void listFolder(String path) {
        createLoader(FileActionService.FileAction.LIST_FOLDER, null, path, null, null, null, null);
    }

    public void otgList(FileInfo file) {
        if (file != null)
            createLoader(FileActionService.FileAction.OTGLIST, file.name, null, null, file, null, null);
        else
            createLoader(FileActionService.FileAction.OTGLIST, null, null, null, file, null, null);
    }

    public void otgListFolder(FileInfo file) {
        if (file != null)
            createLoader(FileActionService.FileAction.OTGLIST_FOLDER, file.name, null, null, file, null, null);
        else
            createLoader(FileActionService.FileAction.OTGLIST_FOLDER, null, null, null, file, null, null);
    }

    public void rename(String path, String newName) {
        createLoader(FileActionService.FileAction.RENAME, newName, path, null, null, null, null);
    }

    public void renameOTG(String newName, ArrayList<DocumentFile> dFile){
        createLoader(FileActionService.FileAction.RENAME_OTG, newName, null, null, null, dFile, null);
    }

    public void delete(ArrayList<FileInfo> selectFiles) {
        ArrayList<String> paths = new ArrayList<>();
        for (FileInfo info : selectFiles) {
            paths.add(info.path);
        }
        createLoader(FileActionService.FileAction.DELETE, null, null, paths, null, null, null);
    }

    public void deleteOTG(ArrayList<DocumentFile> selectDFiles) {
        createLoader(FileActionService.FileAction.DELETE_OTG, null, null, null, null, selectDFiles, null);
    }

    public void newFolder(String path){
        createLoader(FileActionService.FileAction.NEWFOLDER, null, path, null, null, null, null);
    }

    public void newFolderOTG(String name, ArrayList<DocumentFile> dFile){
        createLoader(FileActionService.FileAction.NEWFOLDER_OTG, name, null, null, null, dFile, null);
    }

    public void listAllType() {
        createLoader(FileActionService.FileAction.LIST_ALL_TYPE, null, null, null, null, null, null);
    }

    public void copy(ArrayList<FileInfo> selectedFiles, String destinationPath){
        ArrayList<String> paths = new ArrayList<>();
        for (FileInfo info : selectedFiles) {
            paths.add(info.path);
        }
        createLoader(FileActionService.FileAction.COPY, null, destinationPath, paths, null, null, null);
    }

    public void copyFromLocaltoOTG(ArrayList<FileInfo> selectedFiles, ArrayList<DocumentFile> destinationDFiles){
        ArrayList<String> paths = new ArrayList<>();
        for (FileInfo info : selectedFiles) {
            paths.add(info.path);
        }
        createLoader(FileActionService.FileAction.COPY_LOCAL_OTG, null, null, paths, null, destinationDFiles, null);
    }

    public void copyFromLocaltoOTGEncrypt(ArrayList<FileInfo> selectedFiles, ArrayList<DocumentFile> destinationDFiles){
        ArrayList<String> paths = new ArrayList<>();
        for (FileInfo info : selectedFiles) {
            paths.add(info.path);
        }
        createLoader(FileActionService.FileAction.COPY_LOCAL_OTG_ENCRYPT, null, null, paths, null, destinationDFiles, null);
    }

    public void copyFromLocaltoOTGDecrypt(ArrayList<FileInfo> selectedFiles, ArrayList<DocumentFile> destinationDFiles){
        ArrayList<String> paths = new ArrayList<>();
        for (FileInfo info : selectedFiles) {
            paths.add(info.path);
        }
        createLoader(FileActionService.FileAction.COPY_LOCAL_OTG_DECRYPT, null, null, paths, null, destinationDFiles, null);
    }

    public void copyOTG(ArrayList<DocumentFile> srcDFiles, ArrayList<DocumentFile> destinationDFiles){
        createLoader(FileActionService.FileAction.COPY_OTG, null, null, null, null, srcDFiles, destinationDFiles);
    }

    public void copyOTGtoLocal(ArrayList<DocumentFile> srcDFiles, String desinationPath){
        createLoader(FileActionService.FileAction.COPY_OTG_LOCAL, null, desinationPath, null, null, srcDFiles, null);
    }

    public void move(ArrayList<FileInfo> selectedFiles, String destinationPath){
        ArrayList<String> paths = new ArrayList<>();
        for (FileInfo info : selectedFiles) {
            paths.add(info.path);
        }
        createLoader(FileActionService.FileAction.MOVE, null, destinationPath, paths, null, null, null);
    }

    public void moveOTG(ArrayList<DocumentFile> srcDFiles, ArrayList<DocumentFile> destinationDFiles){
        createLoader(FileActionService.FileAction.MOVE_OTG, null, null, null, null, srcDFiles, destinationDFiles);
    }

    public void moveFromLocaltoOTG(ArrayList<FileInfo> selectedFiles, ArrayList<DocumentFile> destinationDFiles){
        ArrayList<String> paths = new ArrayList<>();
        for (FileInfo info : selectedFiles) {
            paths.add(info.path);
        }
        createLoader(FileActionService.FileAction.MOVE_LOCAL_OTG, null, null, paths, null, destinationDFiles, null);
    }

    public void moveOTGtoLocal(ArrayList<DocumentFile> srcDFiles, String desinationPath){
        createLoader(FileActionService.FileAction.MOVE_OTG_LOCAL, null, desinationPath, null, null, srcDFiles, null);
    }

    public void newFolderEncrypt(String path){
        createLoader(FileActionService.FileAction.NEWFOLDER_ENCRYPT, null, path, null, null, null, null);
    }

    public void copyEncrypt(ArrayList<FileInfo> selectedFiles, String destinationPath){
        ArrayList<String> paths = new ArrayList<>();
        for (FileInfo info : selectedFiles) {
            paths.add(info.path);
        }
        createLoader(FileActionService.FileAction.COPY_ENCRYPT, null, destinationPath, paths, null, null, null);
    }

    public void encrypt(ArrayList<String> encryptList){
        createLoader(FileActionService.FileAction.ENCRYPT, null, null, encryptList, null, null, null);
    }

    public void decrypt(ArrayList<String> decryptList){
        createLoader(FileActionService.FileAction.DECRYPT, null, null, decryptList, null, null, null);
    }

    public void decryptOTG(ArrayList<String> decryptList){
        createLoader(FileActionService.FileAction.DECRYPT_OTG, null, null, decryptList, null, null, null);
    }

    public void newFolderEncryptOTG(String path){
        createLoader(FileActionService.FileAction.NEWFOLDER_ENCRYPT_OTG, null, path, null, null, null, null);
    }

    public void newFolderDecryptOTG(String path){
        createLoader(FileActionService.FileAction.NEWFOLDER_DECRYPT_OTG, null, path, null, null, null, null);
    }

    public void copyOTGtoLocalEncrypt(ArrayList<DocumentFile> srcDFiles, String desinationPath){
        createLoader(FileActionService.FileAction.COPY_OTG_LOCAL_ENCRYPT, null, desinationPath, null, null, srcDFiles, null);
    }

    public void copyOTGtoLocalDecrypt(ArrayList<DocumentFile> srcDFiles, String desinationPath){
        createLoader(FileActionService.FileAction.COPY_OTG_LOCAL_DECRYPT, null, desinationPath, null, null, srcDFiles, null);
    }

    public void encryptOTG(ArrayList<String> encryptList){
        createLoader(FileActionService.FileAction.ENCRYPT_OTG, null, null, encryptList, null, null, null);
    }

    private void createLoader(FileActionService.FileAction mode, String name, String dest, ArrayList<String> paths, FileInfo file, ArrayList<DocumentFile> dFiles, ArrayList<DocumentFile> dFiles2) {
        int id = mFileActionService.getLoaderID(mode);
        Bundle args = new Bundle();
        if (name != null)
            args.putString("name", name);
        if (dest != null)
            args.putString("path", dest);
        if (paths != null)
            args.putStringArrayList("paths", paths);
        if (mode != null)
            args.putInt("actionMode", id);
        if (file != null)
            args.putParcelable("uri", file.uri);
        if(dFiles != null)
            args.putSerializable("dFile", dFiles);
        if(dFiles2 != null)
            args.putSerializable("dFile2", dFiles2);

        ((Activity) mContext).getLoaderManager().restartLoader(id, args, mCallbacks).forceLoad();
    }

    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        Loader<Boolean> loader = null;
        if (mFileActionService != null) {
            int mode = args.getInt("actionMode");
            FileActionService.FileAction action = mFileActionService.getFileAction(mode);
            if (action != null) {
                Log.d(TAG, "action : " + action);
                loader = mFileActionService.onCreateLoader(mContext, action, args);
                if (loader != null && mProgressLayout != null) {
                    switch (action) {
                        case LIST:
                        case RENAME:
                        case DELETE:
                        case NEWFOLDER:
                            mProgressLayout.setVisibility(View.VISIBLE);
                            break;
                        default:
                            mProgressLayout.setVisibility(View.INVISIBLE);
                            break;
                    }
                }
            }
        }

        return loader;
    }

    public void onLoadFinished(Loader<Boolean> loader, Boolean success) {
        Log.w(TAG, "onLoaderFinished: " + loader.getClass().getSimpleName() + " " + success);
        if (mFileActionService != null) {
            mFileActionService.onLoadFinished(mContext, loader, success);
        }
    }

    public void onLoaderReset(Loader<Boolean> loader) {
        Log.w(TAG, "onLoaderReset: " + loader.getClass().getSimpleName());
    }
}
