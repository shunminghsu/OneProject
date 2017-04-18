package com.transcend.otg;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.provider.DocumentFile;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.transcend.otg.Adapter.DestinationAdapter;
import com.transcend.otg.Adapter.FolderExploreDropDownAdapter;
import com.transcend.otg.Bitmap.IconHelper;
import com.transcend.otg.Browser.BrowserFragment;
import com.transcend.otg.Browser.PagerSwipeRefreshLayout;
import com.transcend.otg.Constant.ActionParameter;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Dialog.LocalNewFolderDialog;
import com.transcend.otg.Dialog.OTGNewFolderDialog;
import com.transcend.otg.Dialog.OTGPermissionGuideDialog;
import com.transcend.otg.Dialog.PreGuideDialog;
import com.transcend.otg.Dialog.SDPermissionGuideDialog;
import com.transcend.otg.Loader.FileActionManager;
import com.transcend.otg.Loader.LocalListOnlyFolderLoader;
import com.transcend.otg.Loader.OTGFileOnlyFolderLoader;
import com.transcend.otg.Utils.FileFactory;

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
        DestinationAdapter.OnRecyclerItemCallbackListener{

    private String TAG = DestinationActivity.class.getSimpleName();
    public static final int REQUEST_CODE = DestinationActivity.class.hashCode() & 0xFFFF;
    private Toolbar toolbar;
    private CoordinatorLayout mCoordinatorlayout;
    private PagerSwipeRefreshLayout mSwipeRefreshLayout;
    private View mEmptyView;
   // private LinearLayout loadingContainer;
    private DestinationAdapter mDestinationAdapter;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayout;
    private IconHelper mIconHelper;
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
    private TextView mLocalButton, mSdButton, mOtgButton, mLastSelected;
    private Context mContext;
    private RelativeLayout mNoSDLayout, mNoOTGLayout;
    private UsbMassStorageDevice device;
    private int mOTGDocumentTreeID = 1000, mSDDocumentTreeID = 1001;
    private DocumentFile rootDir;
    private int actionId;
    private int mScreenW;
    private int mLayoutMode;
    private int mOriginalSortValue;
    private int mOriginalSortOrderValue;
    private int nowAction;


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
        mOriginalSortValue = LocalPreferences.getPref(mContext, LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_DATE);
        mOriginalSortOrderValue = LocalPreferences.getPref(mContext, LocalPreferences.BROWSER_SORT_ORDER_PREFIX, Constant.SORT_ORDER_DES);
        LocalPreferences.setPref(mContext, LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_NAME);
        LocalPreferences.setPref(mContext, LocalPreferences.BROWSER_SORT_ORDER_PREFIX, Constant.SORT_ORDER_AS);
    }

    private void init(){
        mContext = this;
        mNoSDLayout = (RelativeLayout) findViewById(R.id.no_sd_layout);
        mNoOTGLayout = (RelativeLayout) findViewById(R.id.no_otg_layout);
        Constant.Activity = 2;
        actionId = -1;
        nowAction = -1;
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenW = displaymetrics.widthPixels;
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_navigation_arrow_white);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initButtons() {
        mLocalButton = (TextView) findViewById(R.id.btn_local);
        mSdButton = (TextView) findViewById(R.id.btn_sd);
        mOtgButton = (TextView) findViewById(R.id.btn_otg);
        DestinationActivity.ButtonClickListener listener = new DestinationActivity.ButtonClickListener();
        mLocalButton.setOnClickListener(listener);
        String sdpath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
        if (sdpath == null || !FileFactory.getMountedState(mContext, sdpath)) {
            mSdButton.setTextColor(getResources().getColor(R.color.colorGray));
        } else {
            mSdButton.setOnClickListener(listener);
        }
        if (!initOTGButton()) {
            mOtgButton.setTextColor(getResources().getColor(R.color.colorGray));
        } else {
            mOtgButton.setOnClickListener(listener);
        }
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(listener);
        mFabExit = (FloatingActionButton) findViewById(R.id.fab_exit);
        mFabExit.setOnClickListener(listener);
        //mCheckSDButton = (Button) findViewById(R.id.check_sdbtn);
        //mCheckSDButton.setOnClickListener(listener);
        //mCheckOTGButton = (Button) findViewById(R.id.check_otgbtn);
        //mCheckOTGButton.setOnClickListener(listener);
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
            } else if (view == mFab){
                popupConfirmDialog();
            //} else if (view == mCheckSDButton){

            //} else if (view == mCheckOTGButton){
            //    markSelectedBtn(mOtgButton);
            //    discoverDevice();
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
        mEmptyView = findViewById(R.id.empty_view);
        //loadingContainer = (LinearLayout) findViewById(R.id.loading_container);
        mLayoutMode = LocalPreferences.getBrowserViewMode(this, BrowserFragment.LIST_TYPE_FOLDER, Constant.ITEM_LIST);
        mIconHelper = new IconHelper(this, mLayoutMode);
        mDestinationAdapter = new DestinationAdapter(this, mIconHelper);
        mDestinationAdapter.setOnRecyclerItemCallbackListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        int ColumnCount = calculateColumnCount(mLayoutMode);
        mLayout = new GridLayoutManager(this, ColumnCount);
        mLayout.setSpanCount(ColumnCount);
        mRecyclerView.setLayoutManager(mLayout);
        mRecyclerView.setAdapter(mDestinationAdapter);
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

                    preGuideDialog("otg");
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

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if(reqCode == mOTGDocumentTreeID && resCode == RESULT_OK){
            Uri uriTree = data.getData();
            if(checkStorage(uriTree, true)){
                doLoadOTG(true);
            }else{
                nowMode = Constant.MODE.LOCAL;
                markSelectedBtn(mLocalButton);
                mPath = Constant.ROOT_LOCAL;
                doLoad(mPath);
            }
        }else if(reqCode == mSDDocumentTreeID && resCode == RESULT_OK){
            Uri uriTree = data.getData();
            if(checkSD(uriTree)){
                doAction();
            }
        }
    }

    private boolean initOTGButton(){
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(this);
        UsbMassStorageDevice mDevice;
        if(devices.length > 0){
            mDevice = devices[0];
            String otgKey = LocalPreferences.getOTGKey(this, mDevice.getUsbDevice().getSerialNumber());
            if(otgKey != "")
                return true;
        }

        return false;
    }

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
        if(otgKey != ""){
            Uri uriTree = Uri.parse(otgKey);
            if(checkStorage(uriTree, false)) {
                doLoadOTG(true);
            }else{
                nowMode = Constant.MODE.LOCAL;
                markSelectedBtn(mLocalButton);
                mPath = Constant.ROOT_LOCAL;
                doLoad(mPath);
            }
        }else{
            preGuideDialog("otg");
        }
    }

    private void doAction(){
        switch (nowAction) {
            case R.id.menu_new_folder:
                String sdPath = FileFactory.getOuterStoragePath(this, Constant.sd_key_path);
                FileInfo tmpFile = new FileInfo();
                tmpFile.path = ActionParameter.path;
                ArrayList<FileInfo> tmpFileList = new ArrayList<>();
                tmpFileList.add(tmpFile);
                ActionParameter.dFiles = FileFactory.findDocumentFilefromPathSD(tmpFileList, sdPath, Constant.Activity);
                mFileActionManager.newFolderOTG(ActionParameter.name, ActionParameter.dFiles);
                break;
        }
    }

    private boolean checkSD(Uri uri){
        if (!uri.toString().contains("primary")) {
            if (uri != null) {
                if(uri.getPath().toString().split(":").length > 1){
                    snackBarShow(R.string.snackbar_plz_select_top);
                }else{
                    rootDir = DocumentFile.fromTreeUri(this, uri);//sd root path
                    ArrayList<String> sdCardFileName = FileInfo.getSDCardFileName(FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path));
                    boolean bSDCard = FileFactory.getInstance().doFileNameCompare(rootDir.listFiles(), sdCardFileName);
                    if(bSDCard){
                        getContentResolver().takePersistableUriPermission(uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        String uid = FileFactory.getSDCardUniqueId();
                        LocalPreferences.setSDKey(this, uid, uri.toString());
                        Constant.mCurrentDocumentFileDestination = Constant.mSDCurrentDocumentFile = Constant.mSDRootDocumentFile = rootDir;
                        return true;
                    }else{
                        snackBarShow(R.string.snackbar_plz_select_sd);
                    }
                }
            }

        }else {
            snackBarShow(R.string.snackbar_plz_select_sd);
        }
        return false;
    }

    private boolean checkStorage(Uri uri, boolean b_needCheckSD){
        if (!uri.toString().contains("primary")) {
            if(uri.getPath().toString().split(":").length > 1){
                snackBarShow(R.string.snackbar_plz_select_top);
            }else{
                rootDir = DocumentFile.fromTreeUri(this, uri);//OTG root path
                boolean bSDCard = false;
                if(b_needCheckSD){
                    ArrayList<String> sdCardFileName = FileInfo.getSDCardFileName(FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path));
                    if(sdCardFileName.size() != 0){
                        bSDCard = FileFactory.getInstance().doFileNameCompare(rootDir.listFiles(), sdCardFileName);
                    }else {
                        bSDCard = false;
                    }
                }

                if(!bSDCard){
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    LocalPreferences.setOTGKey(this, device.getUsbDevice().getSerialNumber(), uri.toString());
                    Constant.mCurrentDocumentFileDestination = rootDir;
                    nowMode = Constant.MODE.OTG;
                    return true;
                }else{
                    snackBarShow(R.string.snackbar_plz_select_otg);
                }
            }
        }else {
            snackBarShow(R.string.snackbar_plz_select_otg);
        }
        return false;
    }

    private void preGuideDialog(String type) {
        new PreGuideDialog(this, type){
            @Override
            public void onConfirm(String type) {
                if(type.equals("otg"))
                    intentDocumentTree();
                else if(type.equals("sd"))
                    intentDocumentTreeSD();
            }
        };
    }

    private void intentDocumentTree() {
        new OTGPermissionGuideDialog(this) {
            @Override
            public void onConfirm(Boolean isClick) {
                if (isClick) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(intent, mOTGDocumentTreeID);
                }else{
                    nowMode = Constant.MODE.LOCAL;
                    markSelectedBtn(mLocalButton);
                    mPath = Constant.ROOT_LOCAL;
                    doLoad(mPath);
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
        String uid = FileFactory.getSDCardUniqueId();
        String sdKey = LocalPreferences.getSDKey(mContext, uid);
        if(sdKey != ""){
            Uri uriSDKey = Uri.parse(sdKey);
            Constant.mSDCurrentDocumentFile = Constant.mSDRootDocumentFile = DocumentFile.fromTreeUri(this, uriSDKey);
            return true;
        }else{
            preGuideDialog("sd");
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
            mLastSelected = mLocalButton;
            markSelectedBtn(mLocalButton);
            mPath = Constant.ROOT_LOCAL;
            doLoad(mPath);
        }else if(Constant.nowMODE == Constant.MODE.SD){
            nowMode = Constant.MODE.SD;
            mLastSelected = mSdButton;
            markSelectedBtn(mSdButton);
            mPath = FileFactory.getOuterStoragePath(this, Constant.sd_key_path);
            doLoad(mPath);
        }else if(Constant.nowMODE == Constant.MODE.OTG){
            nowMode = Constant.MODE.OTG;
            mLastSelected = mOtgButton;
            markSelectedBtn(mOtgButton);
            discoverDevice();
        }
        Bundle args = getIntent().getExtras();
        actionId = args.getInt("actionId");
        if(actionId == R.id.action_copy)
            mFab.setImageResource(R.mipmap.ic_copy_white);
        else if(actionId == R.id.action_move)
            mFab.setImageResource(R.mipmap.ic_move_white);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_destination, menu);
        MenuItem customMenu = menu.findItem(R.id.menu_new_folder);
        customMenu.setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_new_folder:
                if(nowMode == Constant.MODE.LOCAL)
                    doLocalNewFolder();
                else if(nowMode == Constant.MODE.SD){
                    nowAction = R.id.menu_new_folder;
                    doOTGNewFolder(true);
                }else if(nowMode == Constant.MODE.OTG)
                    doOTGNewFolder(false);
                return true;
            case R.id.menu_grid:
                updateLayout(Constant.ITEM_GRID);
                return true;
            case R.id.menu_list:
                updateLayout(Constant.ITEM_LIST);
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doLoad(String path) {
        mFileActionManager.checkServiceMode(path);
        mFileActionManager.listFolder(path);
    }

    private void doLoadOTG(boolean bInitial) {
        mFileActionManager.setMode(FileActionManager.MODE.OTG);
        if(bInitial){
            resetDropDownMapAndList();
            Constant.mCurrentDocumentFileDestination = rootDir;
            mFileActionManager.otgListFolder(null);
        }else{
            Constant.mCurrentDocumentFileDestination = mCurrentDocumentFile;
            mFileActionManager.otgListFolder(null);
        }

    }
    private void updateScreen() {
        if(nowMode == Constant.MODE.LOCAL || nowMode == Constant.MODE.SD){
            mDropdownAdapter.updateList(mPath);
            mDropdownAdapter.notifyDataSetChanged();
            mDestinationAdapter.update(mFileList);
            checkEmpty();
        }else if (nowMode == Constant.MODE.OTG){
            dropDownMapOTG.put(mCurrentDocumentFile.getName(), mCurrentDocumentFile);
            updateDropDownList(mCurrentDocumentFile);
            mDropdownAdapter.updateList(dropDownListOTG);
            mDropdownAdapter.notifyDataSetChanged();
            mDestinationAdapter.update(mFileList);
            checkEmpty();
        }
    }

    private void popupConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getHintResId(actionId));
        builder.setMessage(mPath);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.confirm, null);
        builder.setCancelable(true);
        final AlertDialog dialog = builder.show();
        Button bnPos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        bnPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                backToPreviousActivity();
            }
        });
    }

    private void backToPreviousActivity(){
        ArrayList<DocumentFile> destinationDFiles = new ArrayList<>();
        if(nowMode == Constant.MODE.OTG){
            destinationDFiles.add(mCurrentDocumentFile);
            Constant.destinationDFile = destinationDFiles;
        }
        Bundle args = new Bundle();
        args.putInt("actionId", actionId);
        args.putString("path", mPath);
        args.putSerializable("destinationMode", nowMode);
        Intent intent = new Intent();
        intent.putExtras(args);
        setResult(RESULT_OK, intent);
        finish();
    }

    private int getHintResId(int actionId) {
        switch (actionId){
            case R.id.action_copy:
                return R.string.title_copy_to;
            case R.id.action_move:
                return  R.string.title_move_to;
            default:
                return 0;
        }
    }

    private void markSelectedBtn(TextView selected) {
        //mLocalButton.setTextColor(getResources().getColor(R.color.colorBlack));
        //mSdButton.setTextColor(getResources().getColor(R.color.colorBlack));
        //mOtgButton.setTextColor(getResources().getColor(R.color.colorBlack));
        mLastSelected.setTextColor(getResources().getColor(R.color.colorBlack));
        selected.setTextColor(getResources().getColor(R.color.colorPrimary));

        mLocalButton.setSelected(false);
        mSdButton.setSelected(false);
        mOtgButton.setSelected(false);
        selected.setSelected(true);
        mLastSelected = selected;
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
        LocalPreferences.setPref(mContext, LocalPreferences.BROWSER_SORT_PREFIX, mOriginalSortValue);
        LocalPreferences.setPref(mContext, LocalPreferences.BROWSER_SORT_ORDER_PREFIX, mOriginalSortOrderValue);
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
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        //loadingContainer.setVisibility(View.VISIBLE);
        Loader<Boolean> loader = mFileActionManager.onCreateLoader(id, args);

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean success) {
        mFileActionManager.onLoadFinished(loader, success);
        if (success) {
            if (loader instanceof LocalListOnlyFolderLoader) {
                mPath = ((LocalListOnlyFolderLoader) loader).getPath();
                mFileList = ((LocalListOnlyFolderLoader) loader).getFileList();
                updateScreen();
            }else if (loader instanceof OTGFileOnlyFolderLoader){
                mPath = ((OTGFileOnlyFolderLoader) loader).getPath();
                mFileList = ((OTGFileOnlyFolderLoader) loader).getFileList();
                Constant.mCurrentDocumentFileDestination = mCurrentDocumentFile = ((OTGFileOnlyFolderLoader)loader).getCurrentDocumentFile();
                updateScreen();
            }else{
                doRefresh();
            }
        }
        markSelectView(mRecyclerView);
        mSwipeRefreshLayout.setRefreshing(false);
        //loadingContainer.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {

    }

    private void doLocalNewFolder(){
        List<String> folderNames = new ArrayList<String>();
        ArrayList<FileInfo> allFiles = mDestinationAdapter.getAllFiles();
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
        ArrayList<FileInfo> allFiles = mDestinationAdapter.getAllFiles();
        for (FileInfo file : allFiles) {
            if (file.type == Constant.TYPE_DIR)
                folderNames.add(file.name.toLowerCase());
        }
        ActionParameter.path = mPath;
        new OTGNewFolderDialog(this, folderNames, Constant.Activity, bSDCard) {
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

    public void updateLayout(int mode) {
        int count = calculateColumnCount(mode);
        if (mLayout != null) {
            mLayout.setSpanCount(count);
        }
        mIconHelper.setViewMode(mode);
        mRecyclerView.requestLayout();
        //to make thing simple, dont save ViewMode value in destinationActivity
        //so MainActivity will not be effected
        //LocalPreferences.setBrowserViewMode(this, BrowserFragment.LIST_TYPE_FOLDER, mode);
        mLayoutMode = mode;
    }

    private int calculateColumnCount(int mode) {
        if (mode == Constant.ITEM_LIST) {
            // List mode is a "grid" with 1 column.
            return 1;
        }
        int cellWidth = getResources().getDimensionPixelSize(R.dimen.grid_width);
        int viewPadding = 0;
        int cellMargin = 0;
        int columnCount = Math.max(2,
                (mScreenW - viewPadding) / (cellWidth + cellMargin));

        return columnCount;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenW = displaymetrics.widthPixels;
        updateLayout(mLayoutMode);
    }
}
