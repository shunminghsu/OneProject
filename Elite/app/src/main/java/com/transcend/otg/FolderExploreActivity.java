package com.transcend.otg;

import android.app.LoaderManager;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.transcend.otg.Adapter.FolderExploreAdapter;
import com.transcend.otg.Adapter.FolderExploreDropDownAdapter;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Loader.FileActionManager;
import com.transcend.otg.Loader.LocalFileListLoader;
import com.transcend.otg.Loader.OTGFileListLoader;
import com.transcend.otg.Utils.FileFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wangbojie on 2017/3/2.
 */

public class FolderExploreActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Boolean>,
        FolderExploreDropDownAdapter.OnDropdownItemSelectedListener,
        FolderExploreAdapter.OnRecyclerItemCallbackListener {

    private String TAG = FolderExploreActivity.class.getSimpleName();
    public static final int REQUEST_CODE = FolderExploreActivity.class.hashCode() & 0xFFFF;
    private FileActionManager mFileActionManager;
    private FileInfo mFile;
    private String mPath;
    private Uri mUri;
    private DocumentFile mDocumentFile;
    private FileInfo.STORAGEMODE mMode;
    private int mLoaderID;
    private ArrayList<FileInfo> mFileList;
    private DocumentFile mCurrentDocumentFile;
    private FolderExploreDropDownAdapter mDropdownAdapter;
    private AppCompatSpinner mDropdown;
    private FolderExploreAdapter mFolderExploreAdapter;
    private RecyclerView mRecyclerView;
    private TextView mEmptyView;
    private LinearLayout loadingContainer;
    private HashMap<String, DocumentFile> dropDownMapOTG;
    private ArrayList<String> dropDownListOTG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_explore);
        initRecyclerViewAdapter();
        initDropdown();
        initData();

    }

    private void initRecyclerViewAdapter() {
        mEmptyView = (TextView) findViewById(R.id.empty_view);
        loadingContainer = (LinearLayout) findViewById(R.id.loading_container);
        mFolderExploreAdapter = new FolderExploreAdapter(this);
        mFolderExploreAdapter.setOnRecyclerItemCallbackListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.explore_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mFolderExploreAdapter);
    }

    private void initDropdown() {
        mDropdownAdapter = new FolderExploreDropDownAdapter(this, false);
        mDropdownAdapter.setOnDropdownItemSelectedListener(this);
        mDropdown = (AppCompatSpinner) findViewById(R.id.main_dropdown);
        mDropdown.setAdapter(mDropdownAdapter);
        mDropdown.setDropDownVerticalOffset(10);
        resetDropDownMapAndList();
    }

    private void initData() {
        mFileActionManager = new FileActionManager(this, FileActionManager.MODE.LOCAL, this);
        mFile = Constant.mCurrentFile;
        mPath = mFile.path;
        mMode = mFile.storagemode;
        if(mMode == FileInfo.STORAGEMODE.LOCAL || mMode == FileInfo.STORAGEMODE.SD){
            doLoad(mPath);
        }else{
            doLoadOTG(mFile, true);
        }


    }

    private void doLoad(String path) {
        mFileActionManager.checkServiceMode(path);
        mFileActionManager.list(path);
    }

    private void doLoadOTG(FileInfo file, boolean bInitial) {
        mFileActionManager.setMode(FileActionManager.MODE.OTG);
        if(bInitial){
            resetDropDownMapAndList();
            mFileActionManager.otgList(file);
        }else{
            Constant.mCurrentDocumentFile = mCurrentDocumentFile;
            mFileActionManager.otgList(null);
        }

    }

    private void updateScreen() {
        if(mMode == FileInfo.STORAGEMODE.LOCAL || mMode == FileInfo.STORAGEMODE.SD){
            mDropdownAdapter.updateList(mPath);
            mDropdownAdapter.notifyDataSetChanged();
            mFolderExploreAdapter.update(mFileList);
            checkEmpty();
        }else if (mMode == FileInfo.STORAGEMODE.OTG){
            dropDownMapOTG.put(mCurrentDocumentFile.getName(), mCurrentDocumentFile);
            updateDropDownList(mCurrentDocumentFile);
            mDropdownAdapter.updateList(dropDownListOTG);
            mDropdownAdapter.notifyDataSetChanged();
            mFolderExploreAdapter.update(mFileList);
            checkEmpty();
        }
    }

    private void checkEmpty() {
        if (mFileList.isEmpty())
            mEmptyView.setVisibility(View.VISIBLE);
        else
            mEmptyView.setVisibility(View.GONE);
    }

    private void resetDropDownMapAndList() {
        dropDownMapOTG = new HashMap<String, DocumentFile>();
        dropDownListOTG = new ArrayList<String>();

    }

    private void updateDropDownList(DocumentFile mDFile) {
        String fileName = mDFile.getName();
        Uri fileUri = mDFile.getUri();
        if (dropDownListOTG.size() == 0) {//first time, add parent file
            dropDownMapOTG.put(mDFile.getParentFile().getName(), mDFile.getParentFile());
            dropDownListOTG.add(mDFile.getParentFile().getName() + "@" + mDFile.getParentFile().getUri());
            dropDownListOTG.add(fileName + "@" + fileUri.toString());
        } else {//not first time in, should do arrange
            int dSize = dropDownListOTG.size();
            for (int i = 0; i < dSize; i++) {
                String[] compareFileName = dropDownListOTG.get(i).split("@");
                if (compareFileName[0].equals(fileName) && compareFileName[1].equals(fileUri.toString())) {//the same
                    //remove rest dropDownList
                    while (dropDownListOTG.size() > i + 1) {
                        dropDownListOTG.remove(i + 1);
                    }
                    break;
                } else {//not the same
                    if (i == dSize - 1) {//last index, which means there is no same uri
                        dropDownListOTG.add(fileName + "@" + fileUri.toString());
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!isOnTop()) {
            if (mMode == FileInfo.STORAGEMODE.LOCAL || mMode == FileInfo.STORAGEMODE.SD) {
                String parent = new File(mPath).getParent();
                doLoad(parent);
            }else if (mMode == FileInfo.STORAGEMODE.OTG){
                mCurrentDocumentFile = mCurrentDocumentFile.getParentFile();
                doLoadOTG(null, false);
            }
        }else{
            Constant.mCurrentDocumentFile = null;
            this.finish();
        }

    }

    private boolean isOnTop(){
        if (mMode == FileInfo.STORAGEMODE.LOCAL)
            return mPath.equals(Constant.ROOT_LOCAL);
        else if (mMode == FileInfo.STORAGEMODE.SD)
            return mPath.equals(FileFactory.getOuterStoragePath(this, Constant.sd_key_path));
        else if (mMode == FileInfo.STORAGEMODE.OTG){
            return mCurrentDocumentFile.getParentFile() == null;
        }
        return true;
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        loadingContainer.setVisibility(View.VISIBLE);
        mLoaderID = id;
        Loader<Boolean> loader = mFileActionManager.onCreateLoader(id, args);

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean success) {
        mFileActionManager.onLoadFinished(loader, success);
        if (success) {
            if (loader instanceof LocalFileListLoader) {
                mPath = ((LocalFileListLoader) loader).getPath();
                mFileList = ((LocalFileListLoader) loader).getFileList();
                updateScreen();
            }else if (loader instanceof OTGFileListLoader){
                mPath = ((OTGFileListLoader) loader).getPath();
                mFileList = ((OTGFileListLoader) loader).getFileList();
                mCurrentDocumentFile = ((OTGFileListLoader)loader).getCurrentDocumentFile();
                updateScreen();
            }
        }
        loadingContainer.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {

    }

    @Override
    public void onDropdownItemSelected(int position) {
        if (position > 0) {
            if(mMode == FileInfo.STORAGEMODE.LOCAL || mMode == FileInfo.STORAGEMODE.SD){
                String path = mDropdownAdapter.getPath(mMode, position);
                doLoad(path);
            }else if(mMode == FileInfo.STORAGEMODE.OTG){
                String path = mDropdownAdapter.getPath(mMode, position);
                DocumentFile mapDFile = dropDownMapOTG.get(path);
                mCurrentDocumentFile = mapDFile;
                doLoadOTG(null, false);
            }
        }
    }

    @Override
    public void onRecyclerItemClick(int position) {
        if(mFileList.get(position).type == FileInfo.TYPE.DIR){
            if(mMode == FileInfo.STORAGEMODE.LOCAL || mMode == FileInfo.STORAGEMODE.SD)
                doLoad(mFileList.get(position).path);
            else if (mMode == FileInfo.STORAGEMODE.OTG){
                mCurrentDocumentFile = mCurrentDocumentFile.findFile(mFileList.get(position).name);
                doLoadOTG(mFileList.get(position), false);
            }

        }
    }

    @Override
    public void onRecyclerItemLongClick(int position) {

    }

    @Override
    public void onRecyclerItemInfoClick(int position) {

    }
}

