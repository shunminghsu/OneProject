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
        LIST_FOLDER = LoaderID.LOCAL_FILE_LIST_ONLYFOLDER;
        LIST_ALL_TYPE = LoaderID.LOCAL_ALL_TYPE_LIST;
        NEWFOLDER = LoaderID.LOCAL_NEW_FOLDER;
        RENAME = LoaderID.LOCAL_FILE_RENAME;
        COPY = LoaderID.LOCAL_COPY;
        MOVE = LoaderID.LOCAL_MOVE;
        DELETE = LoaderID.LOCAL_FILE_DELETE;
        OTGLIST = LoaderID.OTG_FILE_LIST;
        OTGLIST_FOLDER = LoaderID.OTG_FILE_LIST_ONLYFOLDER;
        RENAME_OTG = LoaderID.OTG_RENAME;
        DELETE_OTG = LoaderID.OTG_DELETE;
        NEWFOLDER_OTG = LoaderID.OTG_NEW_FOLDER;
        COPY_OTG = LoaderID.OTG_COPY;
        COPY_LOCAL_OTG = LoaderID.LOCAL_OTG_COPY;
        COPY_OTG_LOCAL = LoaderID.OTG_LOCAL_COPY;
        MOVE_OTG = LoaderID.OTG_MOVE;
        MOVE_LOCAL_OTG = LoaderID.LOCAL_OTG_MOVE;
        MOVE_OTG_LOCAL = LoaderID.OTG_LOCAL_MOVE;
        NEWFOLDER_ENCRYPT = LoaderID.LOCAL_NEW_FOLDER_ENCRYPT;
        COPY_ENCRYPT = LoaderID.LOCAL_COPY_ENCRYPT;
        ENCRYPT = LoaderID.LOCAL_ENCRYPT;
        DECRYPT = LoaderID.LOCAL_DECRYPT;
        NEWFOLDER_ENCRYPT_OTG = LoaderID.OTG_NEW_FOLDER_ENCRYPT;
        COPY_OTG_LOCAL_ENCRYPT = LoaderID.OTG_LOCAL_COPY_ENCRYPT;
        ENCRYPT_OTG = LoaderID.OTG_ENCRYPT;
        COPY_LOCAL_OTG_ENCRYPT = LoaderID.LOCAL_OTG_COPY_ENCRYPT;
        NEWFOLDER_DECRYPT_OTG = LoaderID.OTG_NEW_FOLDER_DECRYPT;
        COPY_OTG_LOCAL_DECRYPT = LoaderID.OTG_LOCAL_COPY_DECRYPT;
        DECRYPT_OTG = LoaderID.OTG_DECRYPT;
        COPY_LOCAL_OTG_DECRYPT = LoaderID.LOCAL_OTG_COPY_DECRYPT;
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
    protected AsyncTaskLoader listFolder(Context context, String path) {
        return new LocalListOnlyFolderLoader(context, path);
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
    protected AsyncTaskLoader otglistFolder(Context context, Uri uri, String selectName) {
        return new OTGFileOnlyFolderLoader(context, uri, selectName);
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
    protected AsyncTaskLoader copyEncrypt(Context context, List<String> sources, String dest) {
        return new LocalEncryptCopyLoader(context, sources, dest);
    }

    @Override
    protected AsyncTaskLoader encrypt(Context context, List<String> list) {
        return new LocalEncryptLoader(context, list);
    }

    @Override
    protected AsyncTaskLoader encryptOTG(Context context, List<String> list) {
        return new OTGEncryptLoader(context, list);
    }

    @Override
    protected AsyncTaskLoader copyLocaltoOTGEncrypt(Context context, List<String> list, ArrayList<DocumentFile> dFiles) {
        return new LocalCopytoOTGEncryptLoader(context, list, dFiles);
    }

    @Override
    protected AsyncTaskLoader newFolderDecryptOTG(Context context, String path) {
        return new OTGDecryptNewFolderLoader(context, path);
    }

    @Override
    protected AsyncTaskLoader copyOTGtoLocalDecrypt(Context context, ArrayList<DocumentFile> dFiles, String dest) {
        return new OTGCopytoLocalDecryptLoader(context, dFiles, dest);
    }

    @Override
    protected AsyncTaskLoader decryptOTG(Context context, List<String> list) {
        return new OTGDecryptLoader(context, list);
    }

    @Override
    protected AsyncTaskLoader copyLocaltoOTGDecrypt(Context context, List<String> list, ArrayList<DocumentFile> dFiles) {
        return new LocalCopytoOTGDecryptLoader(context, list, dFiles);
    }

    @Override
    protected AsyncTaskLoader decrypt(Context context, List<String> list) {
        return new LocalDecryptLoader(context, list);
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
    protected AsyncTaskLoader moveOTG(Context context, ArrayList<DocumentFile> dFiles, ArrayList<DocumentFile> dFiles2) {
        return new OTGMoveLoader(context, dFiles, dFiles2);
    }

    @Override
    protected AsyncTaskLoader moveLocaltoOTG(Context context, List<String> list, ArrayList<DocumentFile> dFiles) {
        return new LocalMovetoOTGLoader(context, list, dFiles);
    }

    @Override
    protected AsyncTaskLoader moveOTGtoLocal(Context context, ArrayList<DocumentFile> dFiles, String dest) {
        return new OTGMovetoLocalLoader(context, dFiles, dest);
    }

    @Override
    protected AsyncTaskLoader move(Context context, List<String> sources, String dest) {
        return new LocalMoveLoader(context, sources, dest);
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
    protected AsyncTaskLoader newFolderEncrypt(Context context, String path) {
        return new LocalEncryptNewFolderLoader(context, path);
    }

    @Override
    protected AsyncTaskLoader newFolderEncryptOTG(Context context, String path) {
        return new OTGEncryptNewFolderLoader(context, path);
    }

    @Override
    protected AsyncTaskLoader copyOTGtoLocalEncrypt(Context context, ArrayList<DocumentFile> dFiles, String dest) {
        return new OTGCopytoLocalEncryptLoader(context, dFiles, dest);
    }

    @Override
    protected AsyncTaskLoader share(Context context, ArrayList<String> paths, String dest) {
        return null;
    }
}
