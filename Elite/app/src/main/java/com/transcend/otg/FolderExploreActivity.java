package com.transcend.otg;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.provider.DocumentFile;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.transcend.otg.Adapter.FolderExploreAdapter;
import com.transcend.otg.Adapter.FolderExploreDropDownAdapter;
import com.transcend.otg.Browser.PagerSwipeRefreshLayout;
import com.transcend.otg.Constant.ActionParameter;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Dialog.LocalDeleteDialog;
import com.transcend.otg.Dialog.LocalNewFolderDialog;
import com.transcend.otg.Dialog.LocalRenameDialog;
import com.transcend.otg.Dialog.OTGDeleteDialog;
import com.transcend.otg.Dialog.OTGRenameDialog;
import com.transcend.otg.Dialog.OTGNewFolderDialog;
import com.transcend.otg.Dialog.SDPermissionGuideDialog;
import com.transcend.otg.Loader.FileActionManager;
import com.transcend.otg.Loader.LocalListLoader;
import com.transcend.otg.Loader.OTGFileLoader;
import com.transcend.otg.Utils.FileFactory;
import com.transcend.otg.Utils.MediaUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wangbojie on 2017/3/2.
 */

public class FolderExploreActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Boolean>,
        FolderExploreDropDownAdapter.OnDropdownItemSelectedListener,
        FolderExploreAdapter.OnRecyclerItemCallbackListener,
        ActionMode.Callback{

    private String TAG = FolderExploreActivity.class.getSimpleName();
    public static final int REQUEST_CODE = FolderExploreActivity.class.hashCode() & 0xFFFF;
    private FileActionManager mFileActionManager;
    private FileInfo mFile;
    private String mPath;
    private Uri mUri;
    private DocumentFile mDocumentFile;
    private int mMode;
    private int mSDDocumentTreeID = 1001, nowAction;
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
    private ActionMode mActionMode;
    private RelativeLayout mActionModeView;
    private TextView mActionModeTitle;
    private FloatingActionButton mFab, mFabExit;
    private PagerSwipeRefreshLayout mSwipeRefreshLayout;
    private CoordinatorLayout mCoordinatorlayout;
    private Toolbar toolbar;
    private static final String ACTION_USB_PERMISSION = "com.transcend.otg.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_explore);
        initToolbar();
        initRecyclerViewAndAdapter();
        initDropdown();
        initBroadcast();
        initData();
        initActionModeView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_folder:
                if(Constant.nowMODE == Constant.MODE.LOCAL)
                    doLocalNewFolder();
                else if(Constant.nowMODE == Constant.MODE.SD){
                    nowAction = R.id.menu_new_folder;
                    doOTGNewFolder(true);
                }else if(Constant.nowMODE == Constant.MODE.OTG)
                    doOTGNewFolder(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar(){
        toolbar = (Toolbar) findViewById(R.id.explore_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initRecyclerViewAndAdapter() {
        mCoordinatorlayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        mSwipeRefreshLayout = (PagerSwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(mActionMode != null)
                    mActionMode.finish();;
                doRefresh();
            }
        });
        mEmptyView = (TextView) findViewById(R.id.empty_view);
        loadingContainer = (LinearLayout) findViewById(R.id.loading_container);
        mFolderExploreAdapter = new FolderExploreAdapter(this);
        mFolderExploreAdapter.setOnRecyclerItemCallbackListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.explore_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mFolderExploreAdapter);
    }

    private void initDropdown() {
        nowAction = -1;
        mDropdownAdapter = new FolderExploreDropDownAdapter(this, false);
        mDropdownAdapter.setOnDropdownItemSelectedListener(this);
        mDropdown = (AppCompatSpinner) findViewById(R.id.main_dropdown);
        mDropdown.setAdapter(mDropdownAdapter);
        mDropdown.setDropDownVerticalOffset(10);
        resetDropDownMapAndList();
    }

    private void initBroadcast(){
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                if(Constant.nowMODE == Constant.MODE.OTG)
                    finish();
            }

        }
    };

    private void initData() {
        mFileActionManager = new FileActionManager(this, FileActionManager.MODE.LOCAL, this);
        mFile = Constant.mCurrentFile;
        mPath = mFile.path;
        mMode = mFile.storagemode;
        if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){
            doLoad(mPath);
        }else{
            doLoadOTG(mFile, true);
        }
    }

    private void initActionModeView() {
        mFab = (FloatingActionButton) findViewById(R.id.explore_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActionMode == null)
                    startActionMode();
                else
                    toggleSelectAll();

            }
        });
        mFabExit = (FloatingActionButton) findViewById(R.id.explore_fab_exit);
        mFabExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mActionModeView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.action_mode_custom, null);
        mActionModeTitle = (TextView) mActionModeView.findViewById(R.id.action_mode_custom_title);
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
            Constant.mCurrentDocumentFileExplore = mCurrentDocumentFile;
            mFileActionManager.otgList(null);
        }

    }

    private void updateScreen() {
        if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){
            mDropdownAdapter.updateList(mPath);
            mDropdownAdapter.notifyDataSetChanged();
            mFolderExploreAdapter.update(mFileList);
            checkEmpty();
        }else if (mMode == Constant.STORAGEMODE_OTG){
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

    private void snackBarShow(int resId) {
        Snackbar.make(mCoordinatorlayout, resId, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    private boolean checkSDWritePermission(){
        String sdKey = LocalPreferences.getSDKey(this);
        if(sdKey != ""){
            Uri uriSDKey = Uri.parse(sdKey);
            Constant.mSDCurrentDocumentFile = Constant.mSDRootDocumentFile = DocumentFile.fromTreeUri(this, uriSDKey);
            return true;
        }else{
            intentDocumentTreeSD();
            return false;
        }
    }

    private void intentDocumentTreeSD() {
        new SDPermissionGuideDialog(this) {
            @Override
            public void onConfirm(Boolean isClick) {
                if (isClick) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(intent, mSDDocumentTreeID);
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constant.mCurrentDocumentFileExplore = null;
        unregisterReceiver(usbReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBroadcast();
        if(mActionMode != null)
            mActionMode.finish();
    }

    @Override
    public void onBackPressed() {
        if (!isOnTop()) {
            if (mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD) {
                String parent = new File(mPath).getParent();
                doLoad(parent);
            }else if (mMode == Constant.STORAGEMODE_OTG){
                mCurrentDocumentFile = mCurrentDocumentFile.getParentFile();
                doLoadOTG(null, false);
            }
        }else{
            Constant.mCurrentDocumentFileExplore = null;
            this.finish();
        }

    }

    private boolean isOnTop(){
        if (mMode == Constant.STORAGEMODE_LOCAL)
            return mPath.equals(Constant.ROOT_LOCAL);
        else if (mMode == Constant.STORAGEMODE_SD)
            return mPath.equals(FileFactory.getOuterStoragePath(this, Constant.sd_key_path));
        else if (mMode == Constant.STORAGEMODE_OTG){
            return mCurrentDocumentFile.getParentFile() == null;
        }
        return true;
    }

    private void doRefresh(){
        if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){
            doLoad(mPath);
        }else if(mMode == Constant.STORAGEMODE_OTG){
            doLoadOTG(null, false);
        }
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        loadingContainer.setVisibility(View.VISIBLE);
        Loader<Boolean> loader = mFileActionManager.onCreateLoader(id, args);

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean success) {
        mFileActionManager.onLoadFinished(loader, success);
        if (success) {
            if (loader instanceof LocalListLoader) {
                mPath = ((LocalListLoader) loader).getPath();
                mFileList = ((LocalListLoader) loader).getFileList();
                updateScreen();
            }else if (loader instanceof OTGFileLoader){
                mPath = ((OTGFileLoader) loader).getPath();
                mFileList = ((OTGFileLoader) loader).getFileList();
                Constant.mCurrentDocumentFileExplore = mCurrentDocumentFile = ((OTGFileLoader)loader).getCurrentDocumentFile();
                updateScreen();
            }else{
                doRefresh();
            }
            if(mActionMode != null){
                mActionMode.finish();
            }

        }
        mSwipeRefreshLayout.setRefreshing(false);
        loadingContainer.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {

    }

    @Override
    public void onDropdownItemSelected(int position) {
        if (position > 0) {
            if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){
                String path = mDropdownAdapter.getPath(mMode, position);
                doLoad(path);
            }else if(mMode == Constant.STORAGEMODE_OTG){
                String path = mDropdownAdapter.getPath(mMode, position);
                DocumentFile mapDFile = dropDownMapOTG.get(path);
                mCurrentDocumentFile = mapDFile;
                doLoadOTG(null, false);
            }
        }
    }

    @Override
    public void onRecyclerItemClick(int position) {
        int type = mFileList.get(position).type;
        FileInfo file = mFileList.get(position);
        if(mActionMode == null){
            switch (type){
                case Constant.TYPE_DIR:
                    if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD)
                        doLoad(mFileList.get(position).path);
                    else if (mMode == Constant.STORAGEMODE_OTG){
                        mCurrentDocumentFile = mCurrentDocumentFile.findFile(mFileList.get(position).name);
                        doLoadOTG(mFileList.get(position), false);
                    }
                    break;
                case Constant.TYPE_PHOTO:
                    if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){

                    }else if (mMode == Constant.STORAGEMODE_OTG){

                    }
                    break;
                case Constant.TYPE_VIDEO:
                    if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){
                        MediaUtils.execute(this, file.path, getResources().getString(R.string.openin_title));
                    }else if (mMode == Constant.STORAGEMODE_OTG){
                        MediaUtils.executeUri(this, file.uri.toString(), getResources().getString(R.string.openin_title));
                    }
                    break;
                case Constant.TYPE_MUSIC:
                    if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){
                        MediaUtils.execute(this, file.path, getResources().getString(R.string.openin_title));
                    }else if (mMode == Constant.STORAGEMODE_OTG){
                        MediaUtils.executeUri(this, file.uri.toString(), getResources().getString(R.string.openin_title));
                    }
                    break;
                case Constant.TYPE_DOC:
                    if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){
                        MediaUtils.execute(this, file.path, getResources().getString(R.string.openin_title));
                    }else if (mMode == Constant.STORAGEMODE_OTG){
                        MediaUtils.executeUri(this, file.uri.toString(), getResources().getString(R.string.openin_title));
                    }
                    break;
                case Constant.TYPE_ENCRYPT:
                    if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){

                    }else if (mMode == Constant.STORAGEMODE_OTG){

                    }
                    break;
                case Constant.TYPE_OTHER_FILE:
                    if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){
                        MediaUtils.execute(this, file.path, getResources().getString(R.string.openin_title));
                    }else if (mMode == Constant.STORAGEMODE_OTG){
                        MediaUtils.executeUri(this, file.uri.toString(), getResources().getString(R.string.openin_title));
                    }
                    break;
            }
        }else{
            selectAtPosition(position);
        }

    }

    @Override
    public void onRecyclerItemLongClick(int position) {
        if (mActionMode == null) {
            startActionMode();
            selectAtPosition(position);
        }
    }

    @Override
    public void onRecyclerItemInfoClick(int position) {

    }

    private void startActionMode() {
        if (mActionMode == null)
            startSupportActionMode(this);
    }

    private void selectAtPosition(int position) {
        boolean checked = mFileList.get(position).checked;
        mFileList.get(position).checked = !checked;
        mFolderExploreAdapter.notifyItemChanged(position);
        int count = mFolderExploreAdapter.getItemSelectedCount();
        boolean selectAll = (count == mFileList.size());
        updateActionModeTitle(count);
        toggleActionModeAction(count);
        toggleFabSelectAll(selectAll);
    }

    private void toggleSelectAll() {
        boolean selectAll = mFolderExploreAdapter.getSelectedAllorNot();
        if (selectAll)
            mFolderExploreAdapter.clearAllSelection();
        else
            mFolderExploreAdapter.setAllSelection();
        selectAll = !selectAll;
        updateActionModeTitle(mFolderExploreAdapter.getItemSelectedCount());
        toggleActionModeAction(mFolderExploreAdapter.getItemSelectedCount());
        toggleFabSelectAll(selectAll);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        initView(mode);
        initMenu(menu);
        updateActionModeTitle(0);
        toggleActionModeAction(0);
        toggleFabSelectAll(false);
        return true;
    }

    public void updateActionModeTitle(int count) {
        String format = getResources().getString(R.string.conj_selected);
        mActionModeTitle.setText(String.format(format, count));
    }

    private void toggleFabSelectAll(boolean selectAll) {
        int resId = selectAll
                ? R.drawable.ic_menu_manage
                : R.drawable.ic_menu_camera;
        mFab.setImageResource(resId);
        mFab.setVisibility(View.VISIBLE);
    }

    private void toggleActionModeAction(int count) {
        boolean visible = false;
        if (count == 0) {
            mActionMode.getMenu().findItem(R.id.action_rename).setVisible(visible);
            mActionMode.getMenu().findItem(R.id.action_share).setVisible(visible);
            mActionMode.getMenu().findItem(R.id.action_copy).setVisible(visible);
            mActionMode.getMenu().findItem(R.id.action_move).setVisible(visible);
            mActionMode.getMenu().findItem(R.id.action_delete).setVisible(visible);
            mActionMode.getMenu().findItem(R.id.action_new_folder).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_encrypt).setVisible(visible);
        } else if (count == 1) {
            mActionMode.getMenu().findItem(R.id.action_rename).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_share).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_copy).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_move).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_delete).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_new_folder).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_encrypt).setVisible(!visible);
        } else if (count > 1) {
            mActionMode.getMenu().findItem(R.id.action_rename).setVisible(visible);
            mActionMode.getMenu().findItem(R.id.action_share).setVisible(visible);
            mActionMode.getMenu().findItem(R.id.action_copy).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_move).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_delete).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_new_folder).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_encrypt).setVisible(!visible);
        }
    }

    private void initView(ActionMode mode) {
        mActionMode = mode;
        mActionMode.setCustomView(mActionModeView);
    }

    private void initMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fab_editor, menu);
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rename:
                if (Constant.nowMODE == Constant.MODE.LOCAL) {
                    doLocalRename();
                }else if(Constant.nowMODE == Constant.MODE.SD) {
                    nowAction = R.id.action_rename;
                    doOTGRename(true);
                }else if (Constant.nowMODE == Constant.MODE.OTG) {
                    doOTGRename(false);
                }
                break;
            case R.id.action_delete:
                if (Constant.nowMODE == Constant.MODE.LOCAL) {
                    doLocalDelete();
                }else if(Constant.nowMODE == Constant.MODE.SD){
                    nowAction = R.id.action_delete;
                    doOTGDelete(true);
                }else if(Constant.nowMODE == Constant.MODE.OTG){
                    doOTGDelete(false);
                }
                break;
            case R.id.action_share:
                if(Constant.nowMODE == Constant.MODE.LOCAL || Constant.nowMODE == Constant.MODE.SD){
                    doLocalShare();
                }else if(Constant.nowMODE == Constant.MODE.OTG){
                    doOTGShare();
                }
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mFolderExploreAdapter.clearAllSelection();
        toggleFabSelectAll(false);
        mActionMode = null;
    }

    private void doLocalNewFolder(){
        List<String> folderNames = new ArrayList<String>();
        ArrayList<FileInfo> allFiles = mFolderExploreAdapter.getAllFiles();
        for (FileInfo file : allFiles) {
            if (file.type == Constant.TYPE_DIR)
                folderNames.add(file.name.toLowerCase());
        }
        new LocalNewFolderDialog(this, folderNames) {
            @Override
            public void onConfirm(String newName) {
                String path = mPath;
                StringBuilder builder = new StringBuilder(path);
                if (!path.endsWith("/"))
                    builder.append("/");
                builder.append(newName);
                String newFolderPath = builder.toString();
                mFileActionManager.newFolder(newFolderPath);
            }
        };
    }

    private void doOTGNewFolder(final boolean bSDCard) {
        List<String> folderNames = new ArrayList<String>();
        ArrayList<FileInfo> allFiles = mFolderExploreAdapter.getAllFiles();
        for (FileInfo file : allFiles) {
            if (file.type == Constant.TYPE_DIR)
                folderNames.add(file.name.toLowerCase());
        }
        ActionParameter.path = mPath;
        new OTGNewFolderDialog(this, folderNames, true) {
            @Override
            public void onConfirm(String newName, ArrayList<DocumentFile> mDFiles) {
                if(bSDCard){
                    if(checkSDWritePermission()){
                        ActionParameter.name = newName;
                        mFileActionManager.newFolderOTG(newName, mDFiles);
                    }else{
                        ActionParameter.name = newName;
                    }

                }else{
                    mFileActionManager.newFolderOTG(newName, mDFiles);
                }
            }
        };
    }

    private void doLocalRename(){
        List<String> names = new ArrayList<String>();
        ArrayList<FileInfo> allFiles = mFolderExploreAdapter.getAllFiles();
        FileInfo target = new FileInfo();
        for (FileInfo file : allFiles) {
            if (file.checked)
                target = file;
            else
                names.add(file.name.toLowerCase());
        }
        final String path = target.path;
        final String name = target.name;
        boolean ignoreType = (target.type == Constant.TYPE_DIR);
        new LocalRenameDialog(this,ignoreType, name, names) {
            @Override
            public void onConfirm(String newName) {
                if (newName.equals(name))
                    return;
                mFileActionManager.rename(path, newName);
            }
        };
    }

    private void doOTGRename(final boolean bSDCard) {
        final ArrayList<FileInfo> selectedFiles = mFolderExploreAdapter.getSelectedFiles();
        new OTGRenameDialog(this, selectedFiles, !bSDCard, true) {
            @Override
            public void onConfirm(String newName, String oldName, ArrayList<DocumentFile> selectedDocumentFile) {
                if (newName.equals(oldName))
                    return;
                if(bSDCard){
                    if(checkSDWritePermission()){
                        ActionParameter.name = newName;
                        ActionParameter.files = selectedFiles;
                        mFileActionManager.renameOTG(newName, selectedDocumentFile);
                    }else{
                        ActionParameter.name = newName;
                        ActionParameter.files = selectedFiles;
                    }
                }else{
                    mFileActionManager.renameOTG(newName, selectedDocumentFile);
                }

            }
        };
    }

    private void doLocalDelete(){
        ArrayList<FileInfo> selectedFiles = mFolderExploreAdapter.getSelectedFiles();
        new LocalDeleteDialog(this, selectedFiles) {
            @Override
            public void onConfirm(ArrayList<FileInfo> selectedFiles) {
                mFileActionManager.delete(selectedFiles);
            }
        };
    }

    private void doOTGDelete(final boolean bSDCard){
        final ArrayList<FileInfo> selectedFiles = mFolderExploreAdapter.getSelectedFiles();
        new OTGDeleteDialog(this, selectedFiles, !bSDCard, true) {
            @Override
            public void onConfirm(ArrayList<DocumentFile> selectedDocumentFile) {
                if(bSDCard){
                    if(checkSDWritePermission()){
                        mFileActionManager.deleteOTG(selectedDocumentFile);
                    }else{
                        ActionParameter.files = selectedFiles;
                    }
                }else{
                    mFileActionManager.deleteOTG(selectedDocumentFile);
                }
            }
        };
    }

    private void doLocalShare() {
        String selectPath = mFolderExploreAdapter.getSelectedFiles().get(0).path;
        boolean shareSuccess = MediaUtils.localShare(this, selectPath);
        if(!shareSuccess)
            snackBarShow(R.string.snackbar_not_support_share);
        mActionMode.finish();
    }

    private void doOTGShare(){
        ArrayList<FileInfo> selectFiles = mFolderExploreAdapter.getSelectedFiles();
        ArrayList<DocumentFile> selectDFiles = FileFactory.findDocumentFilefromName(selectFiles, true);
        boolean shareSuccess = MediaUtils.otgShare(this, selectDFiles.get(0));
        if(!shareSuccess)
            snackBarShow(R.string.snackbar_not_support_share);
        mActionMode.finish();
    }

}

