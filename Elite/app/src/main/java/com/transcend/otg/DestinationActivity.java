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
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.transcend.otg.Adapter.FolderExploreAdapter;
import com.transcend.otg.Adapter.FolderExploreDropDownAdapter;
import com.transcend.otg.Browser.NoOtgFragment;
import com.transcend.otg.Browser.NoSdFragment;
import com.transcend.otg.Browser.PagerSwipeRefreshLayout;
import com.transcend.otg.Constant.ActionParameter;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Dialog.LocalNewFolderDialog;
import com.transcend.otg.Dialog.OTGNewFolderDialog;
import com.transcend.otg.Dialog.OTGPermissionGuideDialog;
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
 * Created by wangbojie on 2017/3/15.
 */

public class DestinationActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Boolean>,
        FolderExploreDropDownAdapter.OnDropdownItemSelectedListener,
        FolderExploreAdapter.OnRecyclerItemCallbackListener{

    private String TAG = DestinationActivity.class.getSimpleName();
    public static final int REQUEST_CODE = DestinationActivity.class.hashCode() & 0xFFFF;
    private Toolbar toolbar;
    private CoordinatorLayout mCoordinatorlayout;
    private PagerSwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmptyView;
    private LinearLayout loadingContainer;
    private FolderExploreAdapter mFolderExploreAdapter;
    private RecyclerView mRecyclerView;
    private FolderExploreDropDownAdapter mDropdownAdapter;
    private AppCompatSpinner mDropdown;
    private HashMap<String, DocumentFile> dropDownMapOTG;
    private ArrayList<String> dropDownListOTG;
    private FileActionManager mFileActionManager;
    private FileInfo mFile;
    private String mPath;
    private DocumentFile mCurrentDocumentFile;
    private ArrayList<FileInfo> mFileList;
    private Constant.MODE nowMode;
    private FloatingActionButton mFab, mFabExit;
    private static final String ACTION_USB_PERMISSION = "com.transcend.otg.USB_PERMISSION";
    private Button mLocalButton, mSdButton, mOtgButton, mCheckSDButton, mCheckOTGButton;
    private Context mContext;
    private RelativeLayout mNoSDLayout, mNoOTGLayout;
    private UsbMassStorageDevice device;
    private int mOTGDocumentTreeID = 1000, mSDDocumentTreeID = 1001;
    private DocumentFile rootDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);
        init();
        initToolbar();
        initButtons();
        initRecyclerViewAndAdapter();
        initDropdown();
        initBroadcast();
        initData();

    }

    private void init(){
        mContext = this;
        mNoSDLayout = (RelativeLayout) findViewById(R.id.no_sd_layout);
        mNoOTGLayout = (RelativeLayout) findViewById(R.id.no_otg_layout);
        Constant.Activity = 2;
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initButtons() {
        mLocalButton = (Button) findViewById(R.id.btn_local);
        mSdButton = (Button) findViewById(R.id.btn_sd);
        mOtgButton = (Button) findViewById(R.id.btn_otg);

        DestinationActivity.ButtonClickListener listener = new DestinationActivity.ButtonClickListener();
        mLocalButton.setOnClickListener(listener);
        mSdButton.setOnClickListener(listener);
        mOtgButton.setOnClickListener(listener);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(listener);
        mFabExit = (FloatingActionButton) findViewById(R.id.fab_exit);
        mFabExit.setOnClickListener(listener);
        mCheckSDButton = (Button) findViewById(R.id.check_sdbtn);
        mCheckSDButton.setOnClickListener(listener);
        mCheckOTGButton = (Button) findViewById(R.id.check_otgbtn);
        mCheckOTGButton.setOnClickListener(listener);
    }

    class ButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view == mLocalButton) {
                nowMode = Constant.MODE.LOCAL;
                markSelectedBtn(mLocalButton);
                mPath = Constant.ROOT_LOCAL;
                doLoad(mPath);
            } else if (view == mSdButton) {
                nowMode = Constant.MODE.SD;
                markSelectedBtn(mSdButton);
                String sdpath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
                if (sdpath != null) {
                    if (FileFactory.getMountedState(mContext, sdpath)) {
                        mPath = sdpath;
                        doLoad(mPath);
                    } else {
                        markSelectView(mNoSDLayout);
                    }
                } else {
                    markSelectView(mNoSDLayout);
                }
            } else if (view == mOtgButton) {
                markSelectedBtn(mOtgButton);
                discoverDevice();
            } else if (view == mFabExit){
                finish();
            } else if (view == mCheckSDButton){

            } else if (view == mCheckOTGButton){

            }
        }
    }

    private void initRecyclerViewAndAdapter() {
        mCoordinatorlayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        mSwipeRefreshLayout = (PagerSwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });
        mEmptyView = (TextView) findViewById(R.id.empty_view);
        loadingContainer = (LinearLayout) findViewById(R.id.loading_container);
        mFolderExploreAdapter = new FolderExploreAdapter(this);
        mFolderExploreAdapter.setOnRecyclerItemCallbackListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
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

    private void initBroadcast(){
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                    intentDocumentTree();
                }

            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {

            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (device != null && Constant.nowMODE == Constant.MODE.OTG) {
                    discoverDevice();
                }
            }

        }
    };

    private void discoverDevice() {
        UsbManager usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(this);

        if (devices.length == 0) {
            Log.w(TAG, "no device found!");
            markSelectView(mNoOTGLayout);
            return;
        }
        device = devices[0];
        String otgKey = LocalPreferences.getOTGKey(this, device.getUsbDevice().getSerialNumber());
        if(otgKey != "" || otgKey == null){
            Uri uriTree = Uri.parse(otgKey);
            if(checkStorage(uriTree)) {
                doLoadOTG(true);
            }
        }else{
            intentDocumentTree();
        }
    }

    private boolean checkStorage(Uri uri){
        if (!uri.toString().contains("primary")) {
            if (uri != null) {
                if(uri.getPath().toString().split(":").length > 1){
                    snackBarShow(R.string.snackbar_plz_select_top);
                    intentDocumentTree();
                }else{
                    rootDir = DocumentFile.fromTreeUri(this, uri);//OTG root path
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    LocalPreferences.setOTGKey(this, device.getUsbDevice().getSerialNumber(), uri.toString());
                    Constant.mCurrentDocumentFileDestination = rootDir;
                    nowMode = Constant.MODE.OTG;
                    return true;
                }

            }

        }else {
            snackBarShow(R.string.snackbar_plz_select_otg);
            intentDocumentTree();
        }
        return false;
    }

    private void intentDocumentTree() {
        new OTGPermissionGuideDialog(this) {
            @Override
            public void onConfirm(Boolean isClick) {
                if (isClick) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(intent, mOTGDocumentTreeID);
                }
            }
        };
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

    private void resetDropDownMapAndList() {
        dropDownMapOTG = new HashMap<String, DocumentFile>();
        dropDownListOTG = new ArrayList<String>();

    }

    private void initData() {
        mFileActionManager = new FileActionManager(this, FileActionManager.MODE.LOCAL, this);
        if(Constant.nowMODE == Constant.MODE.LOCAL){
            nowMode = Constant.MODE.LOCAL;
            markSelectedBtn(mLocalButton);
            mPath = Constant.ROOT_LOCAL;
            doLoad(mPath);
        }else if(Constant.nowMODE == Constant.MODE.SD){
            nowMode = Constant.MODE.SD;
            markSelectedBtn(mSdButton);
            mPath = FileFactory.getOuterStoragePath(this, Constant.sd_key_path);
            doLoad(mPath);
        }else if(Constant.nowMODE == Constant.MODE.OTG){
            nowMode = Constant.MODE.OTG;
            markSelectedBtn(mOtgButton);
            discoverDevice();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_destination, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_new_folder:
                if(nowMode == Constant.MODE.LOCAL)
                    doLocalNewFolder();
                else if(nowMode == Constant.MODE.SD){
//                    nowAction = R.id.menu_new_folder;
                    doOTGNewFolder(true);
                }else if(nowMode == Constant.MODE.OTG)
                    doOTGNewFolder(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doLoad(String path) {
        mFileActionManager.checkServiceMode(path);
        mFileActionManager.list(path);
    }

    private void doLoadOTG(boolean bInitial) {
        mFileActionManager.setMode(FileActionManager.MODE.OTG);
        if(bInitial){
            resetDropDownMapAndList();
            Constant.mCurrentDocumentFileDestination = rootDir;
            mFileActionManager.otgList(null);
        }else{
            Constant.mCurrentDocumentFileDestination = mCurrentDocumentFile;
            mFileActionManager.otgList(null);
        }

    }
    private void updateScreen() {
        if(nowMode == Constant.MODE.LOCAL || nowMode == Constant.MODE.SD){
            mDropdownAdapter.updateList(mPath);
            mDropdownAdapter.notifyDataSetChanged();
            mFolderExploreAdapter.update(mFileList);
            checkEmpty();
        }else if (nowMode == Constant.MODE.OTG){
            dropDownMapOTG.put(mCurrentDocumentFile.getName(), mCurrentDocumentFile);
            updateDropDownList(mCurrentDocumentFile);
            mDropdownAdapter.updateList(dropDownListOTG);
            mDropdownAdapter.notifyDataSetChanged();
            mFolderExploreAdapter.update(mFileList);
            checkEmpty();
        }
    }

    private void markSelectedBtn(Button selected) {
        mLocalButton.setTextColor(getResources().getColor(R.color.colorBlack));
        mSdButton.setTextColor(getResources().getColor(R.color.colorBlack));
        mOtgButton.setTextColor(getResources().getColor(R.color.colorBlack));
        selected.setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    private void markSelectView(View view){
        mRecyclerView.setVisibility(View.GONE);
        mNoSDLayout.setVisibility(View.GONE);
        mNoOTGLayout.setVisibility(View.GONE);
        view.setVisibility(View.VISIBLE);
        if(view == mNoSDLayout || view == mNoOTGLayout){
            mEmptyView.setVisibility(View.GONE);
            mFab.setVisibility(View.GONE);
            resetDropDownMapAndList();
            mDropdownAdapter.resetList();
            mDropdownAdapter.notifyDataSetChanged();
        }
        else
            mFab.setVisibility(View.VISIBLE);

    }

    private void checkEmpty() {
        if (mFileList.isEmpty())
            mEmptyView.setVisibility(View.VISIBLE);
        else
            mEmptyView.setVisibility(View.GONE);
    }

    private void updateDropDownList(DocumentFile mDFile) {
        String fileName = mDFile.getName();
        Uri fileUri = mDFile.getUri();
        if (dropDownListOTG.size() == 0) {
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

    private boolean isOnTop(){
        if (nowMode == Constant.MODE.LOCAL)
            return mPath.equals(Constant.ROOT_LOCAL);
        else if (nowMode == Constant.MODE.SD)
            return mPath.equals(FileFactory.getOuterStoragePath(this, Constant.sd_key_path));
        else if (nowMode == Constant.MODE.OTG){
            return mCurrentDocumentFile.getParentFile() == null;
        }
        return true;
    }

    private void doRefresh(){
        if(nowMode == Constant.MODE.LOCAL){
            doLoad(mPath);
        }else if(nowMode == Constant.MODE.SD){
            String sdPath = FileFactory.getOuterStoragePath(this, Constant.sd_key_path);
            if (sdPath != null) {
                doLoad(mPath);
            }else{
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }else if(nowMode == Constant.MODE.OTG){
            doLoadOTG(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (!isOnTop()) {
            if (nowMode == Constant.MODE.LOCAL || nowMode == Constant.MODE.SD) {
                String parent = new File(mPath).getParent();
                doLoad(parent);
            }else if (nowMode == Constant.MODE.OTG){
                mCurrentDocumentFile = mCurrentDocumentFile.getParentFile();
                doLoadOTG(false);
            }
        }else{
            Constant.mCurrentDocumentFileDestination = null;
            this.finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constant.Activity = 2;
        initBroadcast();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constant.mCurrentDocumentFileDestination = null;
        unregisterReceiver(usbReceiver);
    }

    @Override
    public void onDropdownItemSelected(int position) {
        if (position > 0) {
            if(nowMode == Constant.MODE.LOCAL || nowMode == Constant.MODE.SD){
                String path = mDropdownAdapter.getPath(position);
                doLoad(path);
            }else if(nowMode == Constant.MODE.OTG){
                String path = mDropdownAdapter.getPath(position);
                DocumentFile mapDFile = dropDownMapOTG.get(path);
                mCurrentDocumentFile = mapDFile;
                doLoadOTG(false);
            }
        }
    }

    @Override
    public void onRecyclerItemClick(int position) {
        int type = mFileList.get(position).type;
        FileInfo file = mFileList.get(position);
        switch (type){
            case Constant.TYPE_DIR:
                if(nowMode == Constant.MODE.LOCAL || nowMode == Constant.MODE.SD)
                    doLoad(mFileList.get(position).path);
                else if (nowMode == Constant.MODE.OTG){
                    mCurrentDocumentFile = mCurrentDocumentFile.findFile(mFileList.get(position).name);
                    doLoadOTG(false);
                }
                break;
        }
    }

    @Override
    public void onRecyclerItemLongClick(int position) {

    }

    @Override
    public void onRecyclerItemInfoClick(int position) {

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
                Constant.mCurrentDocumentFileDestination = mCurrentDocumentFile = ((OTGFileLoader)loader).getCurrentDocumentFile();
                updateScreen();
            }else{
                doRefresh();
            }
        }
        markSelectView(mRecyclerView);
        mSwipeRefreshLayout.setRefreshing(false);
        loadingContainer.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {

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
        new OTGNewFolderDialog(this, folderNames, Constant.Activity) {
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


}
