package com.transcend.otg.Backup;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.transcend.otg.Browser.BrowserFragment;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Dialog.OTGPermissionGuideDialog;
import com.transcend.otg.Loader.FileActionManager;
import com.transcend.otg.Loader.LocalBackuptoOTGLoader;
import com.transcend.otg.Loader.LocalCopytoOTGLoader;
import com.transcend.otg.Loader.TabInfoLoader;
import com.transcend.otg.LocalPreferences;
import com.transcend.otg.R;
import com.transcend.otg.Utils.FileFactory;

import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/3/29.
 */

public class BackupFragment extends Fragment implements android.app.LoaderManager.LoaderCallbacks<Boolean>{

    private static final int RESULT_OK = -1;
    private RadioButton radioButtonOTG, radioButtonSD;
    private Button btnBackup;
    private Context mContext;
    protected LoaderManager.LoaderCallbacks<ArrayList<FileInfo>> mCallbacks;
    private int TAB_LOADER_ID = 119, mOTGDocumentTreeID = 1000;
    private boolean[] bCheckbox;
    private CheckBox cbPhoto, cbVideo, cbMusic, cbDoc;
    private ArrayList<FileInfo> mPhotoList, mVideoList, mMusicList, mDocList;
    private LinearLayout loading_container;
    private FileActionManager mFileActionManager;
    private ArrayList<DocumentFile> destinationDFiles;
    private UsbMassStorageDevice device;
    private DocumentFile rootDir;
    private RelativeLayout root;



    public BackupFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        init();
    }

    private void init(){
        mPhotoList = new ArrayList<>();
        mVideoList = new ArrayList<>();
        mMusicList = new ArrayList<>();
        mDocList = new ArrayList<>();
        mFileActionManager = new FileActionManager(mContext, FileActionManager.MODE.LOCAL, this);
        destinationDFiles = new ArrayList<>();

    }



    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if(reqCode == mOTGDocumentTreeID && resCode == RESULT_OK){
            Uri uriTree = data.getData();
            if(checkStorage(uriTree)){
                doOTGBackup();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bCheckbox = new boolean[]{false, false, false, false};
        root = (RelativeLayout) inflater.inflate(R.layout.fragment_backup, container, false);
        loading_container = (LinearLayout) root.findViewById(R.id.loading_container);
        cbPhoto = (CheckBox) root.findViewById(R.id.cb_photo);
        cbVideo = (CheckBox) root.findViewById(R.id.cb_video);
        cbMusic = (CheckBox) root.findViewById(R.id.cb_music);
        cbDoc = (CheckBox) root.findViewById(R.id.cb_doc);

        radioButtonOTG = (RadioButton) root.findViewById(R.id.radiootg);
        radioButtonSD = (RadioButton) root.findViewById(R.id.radiosd);
        btnBackup = (Button) root.findViewById(R.id.btn_backup);
        initListener();

        mCallbacks = new LoaderManager.LoaderCallbacks<ArrayList<FileInfo>>() {
            @Override
            public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
                if(id == TAB_LOADER_ID){
                    if(bCheckbox[0])
                        return new TabInfoLoader(mContext, BrowserFragment.LIST_TYPE_IMAGE, null, false);
                    else if(bCheckbox[1])
                        return new TabInfoLoader(mContext, BrowserFragment.LIST_TYPE_VIDEO, null, false);
                    else if(bCheckbox[2])
                        return new TabInfoLoader(mContext, BrowserFragment.LIST_TYPE_MUSIC, null, false);
                    else if(bCheckbox[3])
                        return new TabInfoLoader(mContext, BrowserFragment.LIST_TYPE_DOCUMENT, null, false);
                }
                return null;
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
                if(loader instanceof TabInfoLoader){
                    if(bCheckbox[0]){
                        bCheckbox[0] = false;
                        mPhotoList = data;
                        getLoaderManager().restartLoader(TAB_LOADER_ID, getArguments(), mCallbacks);
                    }else if(bCheckbox[1]){
                        bCheckbox[1] = false;
                        mVideoList = data;
                        getLoaderManager().restartLoader(TAB_LOADER_ID, getArguments(), mCallbacks);
                    }else if(bCheckbox[2]){
                        bCheckbox[2] = false;
                        mMusicList = data;
                        getLoaderManager().restartLoader(TAB_LOADER_ID, getArguments(), mCallbacks);
                    }else if(bCheckbox[3]){
                        bCheckbox[3] = false;
                        mDocList = data;
                    }
                    if(!bCheckbox[0] && !bCheckbox[1] && !bCheckbox[2] && !bCheckbox[3]){
                        loading_container.setVisibility(View.GONE);
                        doLocalBackuptoOTG(destinationDFiles, "", false);
                    }
                }


            }

            @Override
            public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {}
        };
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return root;
    }

    private void resetAll(){
        cbPhoto.setChecked(false);
        cbVideo.setChecked(false);
        cbMusic.setChecked(false);
        cbDoc.setChecked(false);
        radioButtonOTG.setChecked(false);
        radioButtonSD.setChecked(false);
    }

    private void initListener(){
        radioButtonOTG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonOTG.setChecked(true);
                radioButtonSD.setChecked(false);
            }
        });

        radioButtonSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonOTG.setChecked(false);
                radioButtonSD.setChecked(true);
            }
        });


        btnBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(radioButtonOTG.isChecked()){
                    int result = checkOTGExist();
                    switch (result){
                        case 0:
                            snackBarShow(R.string.no_otg);
                            break;
                        case 1:
                            doOTGBackup();
                            break;
                        case 2:
                            intentDocumentTree();
                            break;
                    }
                }else if(radioButtonSD.isChecked()){

                }


            }
        });
    }

    private void doOTGBackup(){
        loading_container.setVisibility(View.VISIBLE);
        bCheckbox[0] = cbPhoto.isChecked();
        bCheckbox[1] = cbVideo.isChecked();
        bCheckbox[2] = cbMusic.isChecked();
        bCheckbox[3] = cbDoc.isChecked();
        getLoaderManager().restartLoader(TAB_LOADER_ID, getArguments(), mCallbacks);
    }

    private int checkOTGExist(){
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(mContext);
        if(devices.length == 0){
            return 0;
        }
        device = devices[0];
        String otgKey = LocalPreferences.getOTGKey(mContext, device.getUsbDevice().getSerialNumber());
        if(otgKey != "" || otgKey == null){
            Uri uriTree = Uri.parse(otgKey);
            if(checkStorage(uriTree)){
                return 1;
            }
        }else{
            return 2;
        }
        return 0;
    }

    private boolean checkStorage(Uri uri){
        if (!uri.toString().contains("primary")) {
            if (uri != null) {
                if(uri.getPath().toString().split(":").length > 1){
                    snackBarShow(R.string.snackbar_plz_select_top);
                }else{
                    rootDir = DocumentFile.fromTreeUri(mContext, uri);//OTG root path
                    ArrayList<String> sdCardFileName = FileInfo.getSDCardFileName(FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path));
                    boolean bSDCard = false;
                    if(sdCardFileName.size() != 0){
                        bSDCard = FileFactory.getInstance().doFileNameCompare(rootDir.listFiles(), sdCardFileName);
                    }else {
                        bSDCard = false;
                    }
                    if(!bSDCard){
                        mContext.getContentResolver().takePersistableUriPermission(uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        LocalPreferences.setOTGKey(mContext, device.getUsbDevice().getSerialNumber(), uri.toString());
                        Constant.mRootDocumentFile = rootDir;
                        destinationDFiles.add(rootDir);
                        return true;
                    }else{
                        snackBarShow(R.string.snackbar_plz_select_otg);
                    }
                }
            }

        }else {
            snackBarShow(R.string.snackbar_plz_select_otg);
        }
        return false;
    }

    private void snackBarShow(int resId) {
        Snackbar.make(root, resId, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    private void intentDocumentTree() {
        new OTGPermissionGuideDialog(mContext) {
            @Override
            public void onConfirm(Boolean isClick) {
                if (isClick) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(intent, mOTGDocumentTreeID);
                }
            }
        };
    }

    private void doLocalBackuptoOTG(ArrayList<DocumentFile> destinationDFiles, String destinationPath, boolean isSrcSDCard){
        if(isSrcSDCard){

        }else {
            if(mPhotoList.size() != 0){
                mFileActionManager.backupFromLocaltoOTG(mPhotoList, destinationDFiles, "Photo");
            }else if(mVideoList.size() != 0){
                mFileActionManager.backupFromLocaltoOTG(mVideoList, destinationDFiles, "Video");
            }else if(mMusicList.size() != 0){
                mFileActionManager.backupFromLocaltoOTG(mMusicList, destinationDFiles, "Music");
            }else if(mDocList.size() != 0){
                mFileActionManager.backupFromLocaltoOTG(mDocList, destinationDFiles, "Document");
            }

        }
    }


    @Override
    public android.content.Loader<Boolean> onCreateLoader(int id, Bundle args) {
        android.content.Loader<Boolean> loader = mFileActionManager.onCreateLoader(id, args);
        return loader;
    }

    @Override
    public void onLoadFinished(android.content.Loader<Boolean> loader, Boolean success) {
        mFileActionManager.onLoadFinished(loader, success);
        if(success && loader instanceof LocalBackuptoOTGLoader){
            String type = ((LocalBackuptoOTGLoader) loader).getType();
            if(type.equals("Photo")){
                mPhotoList.clear();
                if(mVideoList.size() != 0){
                    mFileActionManager.backupFromLocaltoOTG(mVideoList, destinationDFiles, "Video");
                }else if(mMusicList.size() != 0){
                    mFileActionManager.backupFromLocaltoOTG(mMusicList, destinationDFiles, "Music");
                }else if(mDocList.size() != 0){
                    mFileActionManager.backupFromLocaltoOTG(mDocList, destinationDFiles, "Document");
                }
            }else if(type.equals("Video")){
                mVideoList.clear();
                if(mMusicList.size() != 0){
                    mFileActionManager.backupFromLocaltoOTG(mMusicList, destinationDFiles, "Music");
                }else if(mDocList.size() != 0){
                    mFileActionManager.backupFromLocaltoOTG(mDocList, destinationDFiles, "Document");
                }
            }else if(type.equals("Music")){
                mMusicList.clear();
                if(mDocList.size() != 0){
                    mFileActionManager.backupFromLocaltoOTG(mDocList, destinationDFiles, "Document");
                }
            }else if(type.equals("Document")){
                mDocList.clear();
            }
        }


    }

    @Override
    public void onLoaderReset(android.content.Loader<Boolean> loader) {

    }
}
