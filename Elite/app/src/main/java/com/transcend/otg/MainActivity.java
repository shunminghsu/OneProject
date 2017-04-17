package com.transcend.otg;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.provider.DocumentFile;
import android.support.v7.view.ActionMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.transcend.otg.Backup.BackupFragment;
import com.transcend.otg.Browser.BrowserFragment;
import com.transcend.otg.Browser.LocalFragment;
import com.transcend.otg.Browser.NoOtgFragment;
import com.transcend.otg.Browser.NoSdFragment;
import com.transcend.otg.Browser.OTGFragment;
import com.transcend.otg.Browser.SdFragment;
import com.transcend.otg.Browser.TabInfo;
import com.transcend.otg.Constant.ActionParameter;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Dialog.AskExitDialog;
import com.transcend.otg.Dialog.LocalDecryptDialog;
import com.transcend.otg.Dialog.LocalDeleteDialog;
import com.transcend.otg.Dialog.LocalEncryptDialog;
import com.transcend.otg.Dialog.LocalNewFolderDialog;
import com.transcend.otg.Dialog.LocalRenameDialog;
import com.transcend.otg.Dialog.OTGDecryptDialog;
import com.transcend.otg.Dialog.OTGDeleteDialog;
import com.transcend.otg.Dialog.OTGEncryptDialog;
import com.transcend.otg.Dialog.OTGRenameDialog;
import com.transcend.otg.Dialog.OTGNewFolderDialog;
import com.transcend.otg.Dialog.OTGPermissionGuideDialog;
import com.transcend.otg.Dialog.PreGuideDialog;
import com.transcend.otg.Dialog.SDDecryptDialog;
import com.transcend.otg.Dialog.SDPermissionGuideDialog;
import com.transcend.otg.Feedback.FeedbackFragment;
import com.transcend.otg.Help.HelpFragment;
import com.transcend.otg.Loader.FileActionManager;
import com.transcend.otg.Loader.LocalEncryptCopyLoader;
import com.transcend.otg.Loader.LocalEncryptNewFolderLoader;
import com.transcend.otg.Loader.OTGCopytoLocalDecryptLoader;
import com.transcend.otg.Loader.OTGCopytoLocalEncryptLoader;
import com.transcend.otg.Loader.OTGDecryptLoader;
import com.transcend.otg.Loader.OTGDecryptNewFolderLoader;
import com.transcend.otg.Loader.OTGEncryptLoader;
import com.transcend.otg.Loader.OTGEncryptNewFolderLoader;
import com.transcend.otg.Loader.SDCopytoLocalDecryptLoader;
import com.transcend.otg.Loader.SDDecryptLoader;
import com.transcend.otg.Loader.SDDecryptNewFolderLoader;
import com.transcend.otg.Loader.SDEncryptCopyLoader;
import com.transcend.otg.Loader.SDEncryptLoader;
import com.transcend.otg.Loader.SDEncryptNewFolderLoader;
import com.transcend.otg.Setting.SettingFragment;
import com.transcend.otg.Utils.DecryptUtils;
import com.transcend.otg.Utils.EncryptUtils;
import com.transcend.otg.Utils.FileFactory;
import com.transcend.otg.Utils.MediaUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Boolean>,
        ActionMode.Callback,
        NoOtgFragment.OnRefreshClickedListener, NoSdFragment.OnRefreshClickedListener,
        TabInfo.OnItemCallbackListener{

    private String TAG = MainActivity.class.getSimpleName();
    public static final String BACK_STACK_PREFS = ":elite:prefs";
    private SearchView mSearchView;
    private MenuItem mSearchMenuItem;
    private boolean mSearchMenuItemExpanded = false;
    //private boolean mShowSearchIcon = false;
    private SearchResults mSearchResultsFragment;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private LinearLayout container, layout_storage, loading_container;
    private RelativeLayout main_relativeLayout;
    private SdFragment sdFragment;
    private OTGFragment otgFragment;
    private LocalFragment localFragment;
    private FeedbackFragment feedbackFragment;
    private HelpFragment helpFragment;
    private SettingFragment settingFragment;
    private BackupFragment backupFragment;
    private int mLoaderID, mOTGDocumentTreeID = 1000, mSDDocumentTreeID = 1001;
    private FileActionManager mFileActionManager;
    private String mPath;
    private TextView mLocalButton, mSdButton, mOtgButton;
    private Context mContext;
    private FloatingActionButton mFab;
    private ActionMode mActionMode;
    private RelativeLayout mActionModeView;
    public TextView mActionModeTitle, mToolbarTitle;
    private int nowAction;
    private Calendar calendar;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");

    //home page
    private LinearLayout home_container;
    private TextView tv_Browser;
    private TextView tv_Backup;

    //USB
    private DocumentFile rootDir, otgDir;
    private static final String ACTION_USB_PERMISSION = "com.transcend.otg.USB_PERMISSION";
    private UsbMassStorageDevice device;

    //Menu
    private MenuItem.OnMenuItemClickListener mCustomMenuItemClicked;
    private boolean mShowCustomMenuIcon = false;

    private LayoutTransition mTransitioner;
    public static int mScreenW;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initToolbar();
        initDrawer();
        initButtons();
        initHome();
        initFragment();
        initActionModeView();
        FileFactory.getStoragePath(this);
    }

    private void init() {
        mContext = this;
        main_relativeLayout = (RelativeLayout) findViewById(R.id.main_relativelayout);
        mFileActionManager = new FileActionManager(this, FileActionManager.MODE.LOCAL, this);
        mPath = mFileActionManager.getLocalRootPath();
        loading_container = (LinearLayout) findViewById(R.id.loading_container);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenW = displaymetrics.widthPixels;
        nowAction = -1;
        Constant.ROOT_CACHE = getCacheDir().getAbsolutePath();
    }

    private void initHome() {
        home_container = (LinearLayout) findViewById(R.id.home_page);
        setDrawerCheckItem(R.id.nav_home);
        mToolbarTitle.setText(getResources().getString(R.string.drawer_home));
        tv_Browser = (TextView) findViewById(R.id.home_browser);
        tv_Browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mToolbarTitle.setText(getResources().getString(R.string.drawer_browser));
                setDrawerCheckItem(R.id.nav_browser);
                showHomeOrFragment(false);
                markSelectedBtn(mLocalButton);
                replaceFragment(localFragment);
                Constant.nowMODE = Constant.MODE.LOCAL;
            }
        });

        tv_Backup = (TextView) findViewById(R.id.home_backup);
        tv_Backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mToolbarTitle.setText(getResources().getString(R.string.drawer_backup));
                setDrawerCheckItem(R.id.nav_backup);
                showFragment(backupFragment);
            }
        });
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mCustomMenuItemClicked = new CustomMenuItemClicked();
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
                if (getBrowserFragment() == null)
                    return;
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                Log.d(TAG, "USB device detached");

                if (device != null && Constant.nowMODE == Constant.MODE.OTG) {
                    discoverDevice();
                }
            }

        }
    };

    private void initDrawer() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
        if (navigationMenuView != null) {
            navigationMenuView.setVerticalScrollBarEnabled(false);
        }
    }

    private void initFragment() {
        layout_storage = (LinearLayout) findViewById(R.id.layout_storage);
        container = (LinearLayout) findViewById(R.id.fragment_container);
        sdFragment = new SdFragment();
        localFragment = new LocalFragment();
        otgFragment = new OTGFragment();
        helpFragment = new HelpFragment();
        feedbackFragment = new FeedbackFragment();
        settingFragment = new SettingFragment();
        backupFragment = new BackupFragment();
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {

                    @Override
                    public void onBackStackChanged() {
                        if (getBrowserFragment() != null && mFab.getVisibility() != View.VISIBLE)
                            mFab.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void initActionModeView() {
        mActionModeView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.action_mode_custom, null);
        mActionModeTitle = (TextView) mActionModeView.findViewById(R.id.action_mode_custom_title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkGridOrListForFolderPage();
        Constant.Activity = 0;
        initBroadcast();
        if(mActionMode != null)
            mActionMode.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(usbReceiver);
    }

    public void showHomeOrFragment(boolean home) {
        if (home) {
            home_container.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);
            layout_storage.setVisibility(View.GONE);
            mFab.setVisibility(View.GONE);
            showCustomMenuIcon(false);
            //invalidateOptionsMenu(); //all menu item should be disabled, and the empty menu will be hided after invalidated
        } else {
            home_container.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            layout_storage.setVisibility(View.VISIBLE);
            mFab.setVisibility(View.VISIBLE);
            //showCustomMenuIcon(getBrowserFragment() == null ? false : true);
            //invalidateOptionsMenu();
        }
    }

    private void showFragment(Fragment fragment){
        home_container.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);
        layout_storage.setVisibility(View.GONE);
        mFab.setVisibility(View.INVISIBLE);
        replaceFragment(fragment);
//        invalidateOptionsMenu();
    }

    private void markSelectedBtn(TextView selected) {
        mLocalButton.setTextColor(getResources().getColor(R.color.colorBlack));
        mSdButton.setTextColor(getResources().getColor(R.color.colorBlack));
        mOtgButton.setTextColor(getResources().getColor(R.color.colorBlack));
        selected.setTextColor(getResources().getColor(R.color.colorPrimary));
        mLocalButton.setSelected(false);
        mSdButton.setSelected(false);
        mOtgButton.setSelected(false);
        selected.setSelected(true);
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

    private void initView(ActionMode mode) {
        Constant.mActionMode = mActionMode = mode;
        mActionMode.setCustomView(mActionModeView);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void initMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fab_editor, menu);
    }

    public void updateActionModeTitle(int count) {
        String format = getResources().getString(R.string.conj_selected);
        mActionModeTitle.setText(String.format(format, count));
    }

    private void toggleFabSelectAll(boolean selectAll) {
        int resId = selectAll
                ? R.mipmap.ic_floating_browser_cancelselect
                : R.mipmap.ic_floating_browser_selectall;
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
            mActionMode.getMenu().findItem(R.id.action_encrypt).setVisible(visible);
        } else if (count == 1) {
            mActionMode.getMenu().findItem(R.id.action_rename).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_share).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_copy).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_move).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_delete).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_encrypt).setVisible(!visible);
        } else if (count > 1) {
            mActionMode.getMenu().findItem(R.id.action_rename).setVisible(visible);
            mActionMode.getMenu().findItem(R.id.action_share).setVisible(visible);
            mActionMode.getMenu().findItem(R.id.action_copy).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_move).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_delete).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_encrypt).setVisible(!visible);
        }
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if(getActionSeletedFiles().size() > 0){
            FileInfo fileInfo = getActionSeletedFiles().get(0);
            boolean isFromSearch = getBrowserFragment() == null;
            boolean isMultiStorageSelected = false;
            if (isFromSearch) {
                for (FileInfo f : getActionSeletedFiles()) {
                    if (f.storagemode != fileInfo.storagemode) {
                        isMultiStorageSelected = true;
                        break;
                    }
                }
            }

            switch (item.getItemId()) {
                case R.id.action_rename:
                    if (fileInfo.storagemode == Constant.STORAGEMODE_LOCAL) {
                        doLocalRename();
                    }else if(fileInfo.storagemode == Constant.STORAGEMODE_SD) {
                        nowAction = R.id.action_rename;
                        doOTGRename(true);
                    }else if (fileInfo.storagemode == Constant.STORAGEMODE_OTG) {
                        doOTGRename(false);
                    }
                    break;
                case R.id.action_delete:
                    if (isMultiStorageSelected)
                        return false;
                    if (fileInfo.storagemode == Constant.STORAGEMODE_LOCAL) {
                        doLocalDelete();
                    }else if(fileInfo.storagemode == Constant.STORAGEMODE_SD) {
                        nowAction = R.id.action_delete;
                        doOTGDelete(true);
                    }else if (fileInfo.storagemode == Constant.STORAGEMODE_OTG) {
                        doOTGDelete(false);
                    }
                    break;
                case R.id.action_share:
                    if(fileInfo.storagemode == Constant.STORAGEMODE_LOCAL || fileInfo.storagemode == Constant.STORAGEMODE_SD){
                        doLocalShare();
                    }else if(fileInfo.storagemode == Constant.STORAGEMODE_OTG){
                        doOTGShare();
                    }
                    break;
                case R.id.action_copy:
                    if (isMultiStorageSelected)
                        return false;
                    startDestinationActivity(R.id.action_copy);
                    break;
                case R.id.action_move:
                    if (isMultiStorageSelected)
                        return false;
                    startDestinationActivity(R.id.action_move);
                    break;
                case R.id.action_encrypt:
                    if (isMultiStorageSelected)
                        return false;
                    if (fileInfo.storagemode == Constant.STORAGEMODE_LOCAL) {
                        doLocalEncryptDialog();
                    }else if(fileInfo.storagemode == Constant.STORAGEMODE_SD) {
                        nowAction = R.id.action_encrypt;
                        doSDEncryptDialog();
                    }else if (fileInfo.storagemode == Constant.STORAGEMODE_OTG) {
                        doOTGEncryptDialog();
                    }
                    break;
            }
        }

        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        if (getBrowserFragment() == null) { //for search page
            ((SearchResults) getFragment()).actionFinish();
        } else {
            getBrowserFragment().clearAllSelect();
        }
        toggleFabSelectAll(false);
        Constant.mActionMode = mActionMode = null;
        mFab.setImageResource(R.mipmap.ic_floating_browser_intoaction);
    }

    @Override
    public void onItemClick(FileInfo file) {
        if(file.type == Constant.TYPE_DIR)
            startFolderExploreActivity(file);
        else if(file.type == Constant.TYPE_ENCRYPT){
            if(Constant.nowMODE == Constant.MODE.LOCAL){
                doLocalDecryptDialog(file);
            }else if(Constant.nowMODE == Constant.MODE.OTG){
                doOTGDecryptDialog(file);
            }else if(Constant.nowMODE == Constant.MODE.SD){
                doSDDecryptDialog(file);
            }
        }

    }

    @Override
    public void onItemClick(int count) {
        updateActionModeTitle(count);
        toggleActionModeAction(count);
//        int totalCount = getBrowserFragment().getItemsCount();
//        if(totalCount == count)
//            toggleFabSelectAll(true);
//        else
//            toggleFabSelectAll(false);
    }

    @Override
    public void onItemLongClick(int count) {
        startActionMode();
        updateActionModeTitle(count);
        toggleActionModeAction(count);
    }

    @Override
    public void onOtgRefreshClick() {
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(mContext);

        if (devices.length > 0) {
            device = devices[0];
            String otgKey = LocalPreferences.getOTGKey(this, device.getUsbDevice().getSerialNumber());
            if(otgKey != ""){
                Uri uriTree = Uri.parse(otgKey);
                if(checkStorage(uriTree, false)){
                    replaceFragment(otgFragment);
                }
            }else{
                preGuideDialog("otg");
            }
        }
    }

    @Override
    public void onSdRefreshClick() {
        String sdpath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
        if (sdpath != null) {
            if (FileFactory.getMountedState(mContext, sdpath)) {
                replaceFragment(sdFragment);
            }
        }
    }

    class ButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view == mLocalButton) {
                if(mActionMode != null)
                    mActionMode.finish();
                Constant.nowMODE = Constant.MODE.LOCAL;
                markSelectedBtn(mLocalButton);
                replaceFragment(localFragment);
            } else if (view == mSdButton) {
                if(mActionMode != null)
                    mActionMode.finish();
                Constant.nowMODE = Constant.MODE.SD;
                markSelectedBtn(mSdButton);
                String sdpath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
                if (sdpath != null) {
                    if (FileFactory.getMountedState(mContext, sdpath)) {
                        replaceFragment(sdFragment);
                    } else {
                        switchToFragment(NoSdFragment.class.getName(), false);
                    }
                } else {
                    if(mActionMode != null)
                        mActionMode.finish();
                    switchToFragment(NoSdFragment.class.getName(), false);
                }
            } else if (view == mOtgButton) {
                if(mActionMode != null)
                    mActionMode.finish();
                markSelectedBtn(mOtgButton);
                discoverDevice();
            } else if (view == mFab)
                if (mActionMode == null)
                    startActionMode();
                else
                    toggleSelectAll();
        }
    }

    private void startActionMode() {
        if (mActionMode == null)
            startSupportActionMode(this);
    }

    private void toggleSelectAll() {
        boolean b_SelectAll;
        if (getBrowserFragment() == null) { //for search page
            if (((SearchResults) getFragment()).getItemsCount() == 0)
                return;
            b_SelectAll = ((SearchResults) getFragment()).getSelectedAllorNot();
        } else {
            b_SelectAll = getBrowserFragment().getSelectedAllorNot();
        }
        if(b_SelectAll){
            clearActionAllSelected();
            updateActionModeTitle(0);
            toggleActionModeAction(0);
        }else{
            if (getBrowserFragment() == null) { //for search page
                ((SearchResults) getFragment()).selectAll();
            } else {
                getBrowserFragment().selectAll();
            }
            updateActionModeTitle(getActionSelectedCount());
            toggleActionModeAction(getActionSelectedCount());
        }

        toggleFabSelectAll(!b_SelectAll);
    }

    private void discoverDevice() {
        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(mContext);

        if (devices.length == 0) {
            Log.w(TAG, "no device found!");
            Constant.mCurrentDocumentFile = Constant.mRootDocumentFile = null;
            Constant.rootUri = null;
            switchToFragment(NoOtgFragment.class.getName(), false);
            return;
        }
        device = devices[0];
        String otgKey = LocalPreferences.getOTGKey(this, device.getUsbDevice().getSerialNumber());
        if(otgKey != ""){
            Uri uriTree = Uri.parse(otgKey);
            if(checkStorage(uriTree, false)){
                replaceFragment(otgFragment);
            }
        }else{
            preGuideDialog("otg");
        }
    }

    private boolean checkSDWritePermission(){
        String sdId = FileFactory.getSDCardUniqueId();

        String sdKey = LocalPreferences.getSDKey(this, sdId);
        if(sdKey != ""){
            Uri uriSDKey = Uri.parse(sdKey);
            Constant.mSDCurrentDocumentFile = Constant.mSDRootDocumentFile = DocumentFile.fromTreeUri(this, uriSDKey);
            return true;
        }else{
            preGuideDialog("sd");
            return false;
        }
    }

    private void initButtons() {
        mLocalButton = (TextView) findViewById(R.id.btn_local);
        mSdButton = (TextView) findViewById(R.id.btn_sd);
        mOtgButton = (TextView) findViewById(R.id.btn_otg);

        ButtonClickListener listener = new ButtonClickListener();
        mLocalButton.setOnClickListener(listener);
        mSdButton.setOnClickListener(listener);
        mOtgButton.setOnClickListener(listener);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(listener);

        mLocalButton.setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else {
            new AskExitDialog(this) {
                @Override
                public void onConfirm(boolean bExit) {
                    if(bExit)
                        finish();
                }
            };
//            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_custom_main, menu);
        MenuItem customMenu = menu.findItem(R.id.more);
        customMenu.setOnMenuItemClickListener(mCustomMenuItemClicked);
        customMenu.setVisible(mShowCustomMenuIcon);

        mSearchMenuItem = menu.findItem(R.id.search);
        //mSearchMenuItem.setVisible(mShowSearchIcon);
        mSearchView = (SearchView) mSearchMenuItem.getActionView();
        initSearch(mSearchView);
        EditText searchEditText = ((EditText)mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));
        if (mSearchResultsFragment != null) {
            mSearchResultsFragment.setSearchView(mSearchView, searchEditText);
        }

        MenuItemCompat.setOnActionExpandListener(mSearchMenuItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        switchToSearchResultsFragmentIfNeeded();
                        layout_storage.setVisibility(View.GONE);
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        return true;
                    }
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        if (mSearchMenuItemExpanded) {
                            revertToInitialFragment();
                            layout_storage.setVisibility(View.VISIBLE);
                            showCustomMenuIcon(true);
                            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        }
                        return true;
                    }
                });

        if (mSearchMenuItemExpanded) {
            mSearchMenuItem.expandActionView();
        }

        return true;
    }

    class CustomMenuItemClicked implements MenuItem.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Menu menu = item.getSubMenu();

            final MenuItem search = menu.findItem(R.id.search);
            final MenuItem sort = menu.findItem(R.id.menu_easy_sort);

            final MenuItem grid = menu.findItem(R.id.menu_grid);
            final MenuItem list = menu.findItem(R.id.menu_list);
            final MenuItem newFolder = menu.findItem(R.id.menu_new_folder);

            BrowserFragment fragment = getBrowserFragment();
            if (fragment != null && fragment.mCurTab != null) {
                int layout_mode = LocalPreferences.getBrowserViewMode(mContext,
                        fragment.mCurTab.mType, Constant.ITEM_LIST);
                grid.setVisible(layout_mode == Constant.ITEM_LIST);
                list.setVisible(layout_mode == Constant.ITEM_GRID);
                sort.setVisible(true);
                search.setVisible(true);
                if (fragment.getCurrentTabPosition() == BrowserFragment.LIST_TYPE_FOLDER)
                    newFolder.setVisible(true);
                else
                    newFolder.setVisible(false);
            } else {
                grid.setVisible(false);
                list.setVisible(false);
                sort.setVisible(false);
                search.setVisible(false);
                newFolder.setVisible(false);
            }

            return false;
        }
    }
    /*@Override Since we suing custom menu, this fun won't be called
    public boolean onPrepareOptionsMenu(Menu menu) { // called every time the menu opens
        super.onPrepareOptionsMenu(menu);

        final MenuItem search = menu.findItem(R.id.search);
        final MenuItem sort = menu.findItem(R.id.menu_easy_sort);

        final MenuItem grid = menu.findItem(R.id.menu_grid);
        final MenuItem list = menu.findItem(R.id.menu_list);
        final MenuItem newFolder = menu.findItem(R.id.menu_new_folder);

        BrowserFragment fragment = getBrowserFragment();
        if (fragment != null && fragment.mCurTab != null) {
            int layout_mode = LocalPreferences.getBrowserViewMode(this,
                    fragment.mCurTab.mType, Constant.ITEM_LIST);
            grid.setVisible(layout_mode == Constant.ITEM_LIST);
            list.setVisible(layout_mode == Constant.ITEM_GRID);
            sort.setVisible(true);
            search.setVisible(true);
            if(fragment.getCurrentTabPosition() == 5)
                newFolder.setVisible(true);
            else
                newFolder.setVisible(false);
        } else {
            grid.setVisible(false);
            list.setVisible(false);
            sort.setVisible(false);
            search.setVisible(false);
            newFolder.setVisible(false);
        }


        return true;
    }*/

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
            case R.id.menu_easy_sort:
                createPopupWindow(toolbar, this);
                return true;
            case R.id.menu_grid:
                setViewMode(Constant.ITEM_GRID);
                return true;
            case R.id.menu_list:
                setViewMode(Constant.ITEM_LIST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            mToolbarTitle.setText(getResources().getString(R.string.drawer_home));
            showHomeOrFragment(true);
        } else if (id == R.id.nav_browser) {
            mToolbarTitle.setText(getResources().getString(R.string.drawer_browser));
            showHomeOrFragment(false);
            markSelectedBtn(mLocalButton);
            replaceFragment(localFragment);
            Constant.nowMODE = Constant.MODE.LOCAL;
        } else if (id == R.id.nav_backup) {
            mToolbarTitle.setText(getResources().getString(R.string.drawer_backup));
            showFragment(backupFragment);
        } else if (id == R.id.nav_help){
            mToolbarTitle.setText(getResources().getString(R.string.drawer_help));
            showFragment(helpFragment);
        } else if(id == R.id.nav_feedback){
            mToolbarTitle.setText(getResources().getString(R.string.drawer_feedback));
            showFragment(feedbackFragment);
        } else if(id == R.id.nav_setting){
            mToolbarTitle.setText(getResources().getString(R.string.drawer_setting));
            showFragment(settingFragment);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void startDestinationActivity(int actionId){
        Intent intent = new Intent();
        Bundle args = new Bundle();
        args.putInt("actionId", actionId);
        intent.putExtras(args);
        intent.setClass(MainActivity.this, DestinationActivity.class);
        startActivityForResult(intent, DestinationActivity.REQUEST_CODE);
    }

    public void startFolderExploreActivity(FileInfo file){
//        Bundle args = new Bundle();
//        args.putSerializable("file", file);
        Constant.mCurrentFile = file;
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, FolderExploreActivity.class);
//        intent.putExtras(args);
        startActivityForResult(intent, FolderExploreActivity.REQUEST_CODE);
    }

    public void replaceFragment(Fragment fragment) {
        if (fragment instanceof BrowserFragment) {
            mFab.setVisibility(View.VISIBLE);
            showCustomMenuIcon(true);
        } else {
            mFab.setVisibility(View.GONE);
            showCustomMenuIcon(false);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.appear, 0);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    public void setDrawerCheckItem(int id) {
        navigationView.setCheckedItem(id);
    }

    private void doLoad(String path) {
        mFileActionManager.checkServiceMode(path);
        mFileActionManager.listAllType();
    }

    private Fragment switchToFragment(String fragmentName, boolean addToBackStack) {
        showCustomMenuIcon(false);
        mFab.setVisibility(View.GONE);
        Fragment f = Fragment.instantiate(this, fragmentName);
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, f);

        if (addToBackStack) {
            transaction.addToBackStack(BACK_STACK_PREFS);
        }
        transaction.commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();

        if (f instanceof NoOtgFragment) {
            ((NoOtgFragment) f).setOtgRefreshClickedListener(this);
        } else if (f instanceof NoSdFragment) {
            ((NoSdFragment) f).setSdRefreshClickedListener(this);
        }
        return f;
    }

    private void switchToSearchResultsFragmentIfNeeded() {
        if (mSearchResultsFragment != null) {
            return;
        }
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (current != null && current instanceof SearchResults) {
            mSearchResultsFragment = (SearchResults) current;
        } else {
            mSearchResultsFragment = (SearchResults) switchToFragment(
                    SearchResults.class.getName(), true);
        }
        EditText searchEditText = ((EditText)mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));
        mSearchResultsFragment.setSearchView(mSearchView, searchEditText);
        mSearchMenuItemExpanded = true;
    }

    private void revertToInitialFragment() {
        mSearchResultsFragment = null;
        mSearchMenuItemExpanded = false;
        getSupportFragmentManager().popBackStackImmediate(BACK_STACK_PREFS,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if (mSearchMenuItem != null) {
            mSearchMenuItem.collapseActionView();
        }
    }

    private void initSearch(SearchView searchview) {
        (searchview.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setBackgroundResource(R.drawable.search_background);
        ((EditText)searchview.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setTextColor(Color.BLACK);
        searchview.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);// close soft keyboard
                        switchToSearchResultsFragmentIfNeeded();
                        return mSearchResultsFragment.onQueryTextSubmit(query);
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (mSearchResultsFragment == null) {
                            return false;
                        }
                        return mSearchResultsFragment.onQueryTextChange(newText);
                    }
                });
    }

    private void showCustomMenuIcon(boolean show) {
        mShowCustomMenuIcon = show;
        invalidateOptionsMenu();
    }

    private BrowserFragment getBrowserFragment() {
        if (container != null && container.getVisibility() == View.GONE)
            return null;
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment == null)
            return null;
        return fragment instanceof BrowserFragment? (BrowserFragment)fragment : null;
    }

    private Fragment getFragment() {
        if (container != null && container.getVisibility() == View.GONE)
            return null;
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment == null)
            return null;
        return fragment;
    }

    private void setViewMode(int mode) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            ((BrowserFragment) fragment).onViewModeChanged(mode);
        }
    }

    private void setSortBy(int sort_by) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            LocalPreferences.setPref(mContext, LocalPreferences.BROWSER_SORT_PREFIX, sort_by);
            ((BrowserFragment) fragment).restartLoaderforCurrentTab();
        }
    }

    private void setSortOrder(int sort_order) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            LocalPreferences.setPref(mContext, LocalPreferences.BROWSER_SORT_ORDER_PREFIX, sort_order);
            ((BrowserFragment) fragment).restartLoaderforCurrentTab();
        }
    }

    private void snackBarShow(int resId) {
        Snackbar.make(main_relativeLayout, resId, Snackbar.LENGTH_LONG).setAction("Action", null).show();
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
                    Constant.nowMODE = Constant.MODE.LOCAL;
                    markSelectedBtn(mLocalButton);
                    replaceFragment(localFragment);
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

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if(reqCode == mOTGDocumentTreeID && resCode == RESULT_OK){
            Uri uriTree = data.getData();
            if(checkStorage(uriTree, true)){
                replaceFragment(otgFragment);
            }
        }else if(reqCode == mSDDocumentTreeID && resCode == RESULT_OK){
            Uri uriTree = data.getData();
            if(checkSD(uriTree)){
                doAction();
            }
        }else if(reqCode == DestinationActivity.REQUEST_CODE && resCode == RESULT_OK){
            Bundle bundle = data.getExtras();
            int actionId = bundle.getInt("actionId");
            String destinationPath = bundle.getString("path");
            Constant.MODE actionMode = (Constant.MODE) bundle.getSerializable("destinationMode");
            ArrayList<DocumentFile> destinationDFiles = Constant.destinationDFile;
            doDestinationAction(actionId, destinationPath, actionMode, destinationDFiles);
        }
    }

    private ArrayList<FileInfo> createListFileInfoFromPath(String path){
        ArrayList<FileInfo> tmpDesFiles = new ArrayList<>();
        FileInfo file = new FileInfo();
        file.path = path;
        tmpDesFiles.add(file);
        return tmpDesFiles;
    }

    private void doDestinationAction(int actionId, String destinationPath, Constant.MODE actionMode, ArrayList<DocumentFile> destinationDFiles){
        Constant.Activity = 0;
        ArrayList<FileInfo> mSelectedFiles = getActionSeletedFiles();
        if(actionMode == Constant.MODE.LOCAL){
            if(Constant.nowMODE == Constant.MODE.LOCAL){//Local -> Local
                doLocalCopyorMove(actionId, mSelectedFiles, destinationPath);
            }else if(Constant.nowMODE == Constant.MODE.SD){//SD -> Local
                if(actionId == R.id.action_copy)
                    doLocalCopyorMove(actionId, mSelectedFiles, destinationPath);//copy doesnt need permission
                else {
                    if(checkSDWritePermission()){
                        doOTGCopyorMovetoLocal(actionId, mSelectedFiles, destinationPath, true);
                    }else {
                        nowAction = actionId;
                        ActionParameter.path = destinationPath;
                        ActionParameter.files = mSelectedFiles;
                        ActionParameter.dFiles = destinationDFiles;
                    }
                }
            }else if(Constant.nowMODE == Constant.MODE.OTG){//OTG -> Local
                doOTGCopyorMovetoLocal(actionId, mSelectedFiles, destinationPath, false);
            }
        }else if(actionMode == Constant.MODE.SD){
            if(Constant.nowMODE == Constant.MODE.LOCAL){//Local -> SD
                if(checkSDWritePermission()){
                    doLocalCopyorMovetoOTG(actionId, mSelectedFiles, destinationDFiles, destinationPath, true);
                }else {
                    nowAction = actionId;
                    ActionParameter.path = destinationPath;
                    ActionParameter.files = mSelectedFiles;
                    ActionParameter.dFiles = destinationDFiles;
                }
            }else if(Constant.nowMODE == Constant.MODE.SD){//SD -> SD
                if(checkSDWritePermission()){
                    doLocalCopyorMovetoOTG(actionId, mSelectedFiles, destinationDFiles, destinationPath, true);
                }else {
                    nowAction = actionId;
                    ActionParameter.path = destinationPath;
                    ActionParameter.files = mSelectedFiles;
                    ActionParameter.dFiles = destinationDFiles;
                }
            }else if(Constant.nowMODE == Constant.MODE.OTG){//OTG -> SD
                if(checkSDWritePermission()){
                    doOTGCopyorMove(actionId, mSelectedFiles, destinationDFiles, destinationPath, true);
                }else {
                    nowAction = actionId;
                    ActionParameter.path = destinationPath;
                    ActionParameter.files = mSelectedFiles;
                    ActionParameter.dFiles = destinationDFiles;
                }
            }
        }else if(actionMode == Constant.MODE.OTG){
            if(Constant.nowMODE == Constant.MODE.LOCAL){//Local -> OTG
                doLocalCopyorMovetoOTG(actionId, mSelectedFiles, destinationDFiles, destinationPath, false);
            }else if(Constant.nowMODE == Constant.MODE.SD){//SD -> OTG
                if(actionId == R.id.action_copy)
                    doLocalCopyorMovetoOTG(actionId, mSelectedFiles, destinationDFiles, destinationPath, false);
                else{
                    if(checkSDWritePermission()){
                        doSDMovetoOTG(actionId, mSelectedFiles, destinationDFiles, destinationPath, true);
                    }else{
                        nowAction = actionId;
                        ActionParameter.path = destinationPath;
                        ActionParameter.files = mSelectedFiles;
                        ActionParameter.dFiles = destinationDFiles;
                    }
                }
            }else if(Constant.nowMODE == Constant.MODE.OTG){//OTG -> OTG
                doOTGCopyorMove(actionId, mSelectedFiles, destinationDFiles, destinationPath, false);
            }
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
                        Constant.mSDCurrentDocumentFile = Constant.mSDRootDocumentFile = rootDir;
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
                    Constant.mCurrentDocumentFile = Constant.mRootDocumentFile = otgDir = rootDir;
                    Constant.nowMODE = Constant.MODE.OTG;
                    Constant.rootUri = uri;
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

    private void doAction(){
        switch (nowAction){
            case R.id.menu_new_folder:
                ArrayList<DocumentFile> tmpDFiles = new ArrayList<>();
                tmpDFiles.add(rootDir);
                ActionParameter.dFiles = tmpDFiles;
                ActionParameter.path = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
                mFileActionManager.newFolderOTG(ActionParameter.name, ActionParameter.dFiles);
                break;
            case R.id.action_delete:
                String sdPathDelete = FileFactory.getOuterStoragePath(this, Constant.sd_key_path);
                ActionParameter.dFiles = FileFactory.findDocumentFilefromPathSD(ActionParameter.files, sdPathDelete, Constant.Activity);
                mFileActionManager.deleteOTG(ActionParameter.dFiles);
                break;
            case R.id.action_rename:
                String sdPathRename = FileFactory.getOuterStoragePath(this, Constant.sd_key_path);
                ActionParameter.dFiles = FileFactory.findDocumentFilefromPathSD(ActionParameter.files, sdPathRename, Constant.Activity);
                mFileActionManager.renameOTG(ActionParameter.name, ActionParameter.dFiles);
                break;
            case R.id.action_copy:
                if(Constant.nowMODE == Constant.MODE.LOCAL){//Local -> SD
                    doLocalCopyorMovetoOTG(nowAction, ActionParameter.files, ActionParameter.dFiles, ActionParameter.path, true);
                }else if(Constant.nowMODE == Constant.MODE.SD){//SD -> SD
                    doLocalCopyorMovetoOTG(nowAction, ActionParameter.files, ActionParameter.dFiles, ActionParameter.path, true);
                }else if(Constant.nowMODE == Constant.MODE.OTG){//OTG -> SD
                    doOTGCopyorMove(nowAction, ActionParameter.files, ActionParameter.dFiles, ActionParameter.path, true);
                }
                break;
            case R.id.action_move:
                if(Constant.nowMODE == Constant.MODE.LOCAL){//Local -> SD
                    doLocalCopyorMovetoOTG(nowAction, ActionParameter.files, ActionParameter.dFiles, ActionParameter.path, true);
                }else if(Constant.nowMODE == Constant.MODE.SD){//SD -> Local or SD -> SD or SD -> OTG
                    String otgPath = FileFactory.getOTGStoragePath(this, Constant.otg_key_path);
                    if(otgPath.contains(ActionParameter.path))
                        doSDMovetoOTG(nowAction, ActionParameter.files, ActionParameter.dFiles, ActionParameter.path, true);
                    else
                        doOTGCopyorMovetoLocal(nowAction, ActionParameter.files, ActionParameter.path, true);
//                    doLocalCopyorMovetoOTG(nowAction, ActionParameter.files, ActionParameter.dFiles, ActionParameter.path, true);
                }else if(Constant.nowMODE == Constant.MODE.OTG){// OTG -> SD
                    doOTGCopyorMove(nowAction, ActionParameter.files, ActionParameter.dFiles, ActionParameter.path, true);
                }
                break;
            case R.id.action_encrypt:
                doSDEncryptNewFolder();
                break;
            default:
                break;
        }
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        mLoaderID = id;
        Loader<Boolean> loader = mFileActionManager.onCreateLoader(id, args);

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean success) {
        mFileActionManager.onLoadFinished(loader, success);
        if (success) {
            if (loader instanceof LocalEncryptNewFolderLoader) {
                doLocalEncryptCopy();
            } else if (loader instanceof LocalEncryptCopyLoader){
                doLocalEncrypt();
            }else if(loader instanceof OTGEncryptNewFolderLoader){
                doOTGEncryptCopy();
            }else if(loader instanceof OTGCopytoLocalEncryptLoader){
                doOTGEncrypt();
            }else if(loader instanceof OTGEncryptLoader){
                doLocalCopytoOTGEncrypt(false);
            }else if(loader instanceof OTGDecryptNewFolderLoader){
                doOTGDecryptCopy();
            }else if(loader instanceof OTGCopytoLocalDecryptLoader){
                doOTGDecrypt();
            }else if(loader instanceof OTGDecryptLoader){
                doLocalCopytoOTGDecrypt(false);
            }else if(loader instanceof SDEncryptNewFolderLoader){
                doSDEncryptCopy();
            }else if(loader instanceof SDEncryptCopyLoader){
                doSDEncrypt();
            }else if(loader instanceof SDEncryptLoader){
                doLocalCopytoOTGEncrypt(true);
            }else if(loader instanceof SDDecryptNewFolderLoader){
                doSDDecryptCopy();
            }else if(loader instanceof SDCopytoLocalDecryptLoader){
                doSDDecrypt();
            }else if(loader instanceof SDDecryptLoader){
                doLocalCopytoOTGDecrypt(true);
            }

            else{
                if(mActionMode != null){
                    mActionMode.finish();
                    Constant.mActionMode = mActionMode = null;
                }
                if(getBrowserFragment() !=null)
                    getBrowserFragment().restartLoaderforCurrentTab();
            }

        }else{
            snackBarShow(R.string.fail);
        }
        if(loading_container != null)
            loading_container.setVisibility(View.GONE);

    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {}

    private void doOTGEncryptDialog() {
        ArrayList<FileInfo> selectedFiles = getActionSeletedFiles();
        EncryptUtils.clearAllEncryptUtils();
        new OTGEncryptDialog(this, selectedFiles) {
            @Override
            public void onConfirm(String newName, String password, ArrayList<DocumentFile> mSelectedDFiles) {
                if (getBrowserFragment() != null) {
                    int tabPostiion = getBrowserFragment().getCurrentTabPosition();
                    if (tabPostiion == BrowserFragment.LIST_TYPE_FOLDER) {
                        DocumentFile child = mSelectedDFiles.get(0).getParentFile();
                        EncryptUtils.setAfterEncryptDFile(child);
                    }
                }
                EncryptUtils.setSelectedDocumentFile(mSelectedDFiles);
                EncryptUtils.setEncryptFileName(newName);
                EncryptUtils.setPassword(password);
                doOTGEncryptNewFolder();
                if(mActionMode != null)
                    mActionMode.finish();
            }
        };
    }

    private void doOTGEncryptNewFolder(){
        calendar = Calendar.getInstance();
        String currentDateTimeString = sdf.format(calendar.getTime());
        String folderName = Constant.ROOT_CACHE + File.separator + currentDateTimeString;
        EncryptUtils.setBeforeEncryptPath(folderName);
        mFileActionManager.newFolderEncryptOTG(folderName);
    }

    private void doOTGEncryptCopy(){
        ArrayList<DocumentFile> selectedDFiles = EncryptUtils.getSelectedDocumentFile();
        mFileActionManager.copyOTGtoLocalEncrypt(selectedDFiles, EncryptUtils.getBeforeEncryptPath());
    }

    private void doOTGEncrypt(){
        String password = EncryptUtils.getPassword();
        String beforeEncryptPath = EncryptUtils.getBeforeEncryptPath();
        String afterEncryptPath = EncryptUtils.getBeforeEncryptPath() + File.separator + EncryptUtils.getEncryptFileName();
        EncryptUtils.setAfterEncryptPath(afterEncryptPath);
        ArrayList<String> encryptList = new ArrayList<>();
        encryptList.add(beforeEncryptPath);
        encryptList.add(afterEncryptPath);
        encryptList.add(password);
        mFileActionManager.encryptOTG(encryptList);
    }

    private void doLocalCopytoOTGEncrypt(boolean isSrcSDCard){
        if(isSrcSDCard){
            String uid = FileFactory.getSDCardUniqueId();
            String sdKey = LocalPreferences.getSDKey(mContext, uid);
            if(sdKey != ""){
                Uri uriSDKey = Uri.parse(sdKey);
                DocumentFile tmpDFile = DocumentFile.fromTreeUri(mContext, uriSDKey);
                Constant.mSDCurrentDocumentFile = tmpDFile;
                String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);

                String getLocalEncryptFilePath = EncryptUtils.getAfterEncryptPath();
                FileInfo tmpFile = new FileInfo();
                tmpFile.path = getLocalEncryptFilePath + getResources().getString(R.string.encrypt_subfilename);
                ArrayList<FileInfo> selectedFiles = new ArrayList<>();
                selectedFiles.add(tmpFile);
                String copyToSDPath = EncryptUtils.getCopyToSDPath() != "" ? EncryptUtils.getCopyToSDPath() : sdPath;
                ArrayList<FileInfo> files = createListFileInfoFromPath(copyToSDPath);
                ArrayList<DocumentFile> destinationDFiles = FileFactory.findDocumentFilefromPathSD(files, sdPath, Constant.Activity);
                mFileActionManager.copyFromLocaltoOTGEncrypt(selectedFiles, destinationDFiles, copyToSDPath);
            }
        }else {
            String getLocalEncryptFilePath = EncryptUtils.getAfterEncryptPath();
            FileInfo tmpFile = new FileInfo();
            tmpFile.path = getLocalEncryptFilePath + getResources().getString(R.string.encrypt_subfilename);
            ArrayList<FileInfo> selectedFiles = new ArrayList<>();
            selectedFiles.add(tmpFile);
            DocumentFile tmpDFile = EncryptUtils.getAfterEncryptDFile();
            if(tmpDFile == null)
                tmpDFile = Constant.mRootDocumentFile;
            ArrayList<DocumentFile> destinationDFiles = new ArrayList<>();
            destinationDFiles.add(tmpDFile);
            mFileActionManager.copyFromLocaltoOTGEncrypt(selectedFiles, destinationDFiles, "");
        }
    }

    private void doSDEncryptDialog(){
        EncryptUtils.clearAllEncryptUtils();
        new LocalEncryptDialog(this) {
            @Override
            public void onConfirm(String newName, String password) {
                ArrayList<FileInfo> selectedFiles = getActionSeletedFiles();
                if (getBrowserFragment() != null) {
                    int tabPostiion = getBrowserFragment().getCurrentTabPosition();
                    if (tabPostiion == BrowserFragment.LIST_TYPE_FOLDER) {
                        File child = new File(selectedFiles.get(0).path);
                        EncryptUtils.setCopyToSDPath(child.getParent());
                    }
                }
                EncryptUtils.setSelectLocalFile(selectedFiles);
                EncryptUtils.setEncryptFileName(newName);
                EncryptUtils.setPassword(password);
                if(checkSDWritePermission()){
                    doSDEncryptNewFolder();
                }
                if(mActionMode != null)
                    mActionMode.finish();
            }
        };
    }

    private void doSDEncryptNewFolder(){
        calendar = Calendar.getInstance();
        String currentDateTimeString = sdf.format(calendar.getTime());
        String folderName = Constant.ROOT_CACHE + File.separator + currentDateTimeString;
        EncryptUtils.setBeforeEncryptPath(folderName);
        mFileActionManager.newFolderEncryptSD(folderName);
    }

    private void doSDEncryptCopy(){
        ArrayList<FileInfo> selectedFiles = EncryptUtils.getSelectLocalFile();
        mFileActionManager.copyEncryptSD(selectedFiles, EncryptUtils.getBeforeEncryptPath());
    }

    private void doSDEncrypt(){
        String password = EncryptUtils.getPassword();
        String beforeEncryptPath = EncryptUtils.getBeforeEncryptPath();
        String afterEncryptPath = EncryptUtils.getBeforeEncryptPath() + File.separator + EncryptUtils.getEncryptFileName();
        EncryptUtils.setAfterEncryptPath(afterEncryptPath);
        ArrayList<String> encryptList = new ArrayList<>();
        encryptList.add(beforeEncryptPath);
        encryptList.add(afterEncryptPath);
        encryptList.add(password);
        mFileActionManager.encryptSD(encryptList);
    }

    private void doLocalEncryptDialog() {
        EncryptUtils.clearAllEncryptUtils();
        new LocalEncryptDialog(this) {
            @Override
            public void onConfirm(String newName, String password) {
                ArrayList<FileInfo> selectedFiles = getActionSeletedFiles();
                if (getBrowserFragment() != null) {
                    int tabPostiion = getBrowserFragment().getCurrentTabPosition();
                    if (tabPostiion == BrowserFragment.LIST_TYPE_FOLDER) {
                        File child = new File(selectedFiles.get(0).path);
                        EncryptUtils.setAfterEncryptPath(child.getParent() + File.separator + newName);
                    }
                }
                EncryptUtils.setSelectLocalFile(selectedFiles);
                EncryptUtils.setEncryptFileName(newName);
                EncryptUtils.setPassword(password);
                doLocalEncryptNewFolder();
                if(mActionMode != null)
                    mActionMode.finish();
            }
        };
    }

    private void doLocalEncryptNewFolder(){
        calendar = Calendar.getInstance();
        String currentDateTimeString = sdf.format(calendar.getTime());
        String folderName = Constant.ROOT_CACHE + File.separator + currentDateTimeString;
        EncryptUtils.setBeforeEncryptPath(folderName);
        mFileActionManager.newFolderEncrypt(folderName);
    }

    private void doLocalEncryptCopy(){
        ArrayList<FileInfo> selectedFiles = EncryptUtils.getSelectLocalFile();
        mFileActionManager.copyEncrypt(selectedFiles, EncryptUtils.getBeforeEncryptPath());
    }

    private void doLocalEncrypt(){
        String password = EncryptUtils.getPassword();
        String beforeEncryptPath = EncryptUtils.getBeforeEncryptPath();
        String afterEncryptPath = EncryptUtils.getAfterEncryptPath();
        if(afterEncryptPath.equals("")||afterEncryptPath.equals(null))
            afterEncryptPath = Constant.ROOT_LOCAL + File.separator + EncryptUtils.getEncryptFileName();
        ArrayList<String> encryptList = new ArrayList<>();
        encryptList.add(beforeEncryptPath);
        encryptList.add(afterEncryptPath);
        encryptList.add(password);
        mFileActionManager.encrypt(encryptList);
    }

    private void doLocalDecryptDialog(FileInfo selectedFile){
        ArrayList<String> folderNames = new ArrayList<String>();
        ArrayList<FileInfo> allFiles = getBrowserFragment().getAllFiles();
        for (FileInfo file : allFiles) {
            if (file.type == Constant.TYPE_DIR)
                folderNames.add(file.name.toLowerCase());
        }
        DecryptUtils.clearAllDecryptUtils();
        new LocalDecryptDialog(this, folderNames, selectedFile.path) {
            @Override
            public void onConfirm(String newFolderpath, String password, String filePath) {
                doLocalDecrypt(newFolderpath, password, filePath);
            }
        };
    }

    private void doLocalDecrypt(String decryptPath, String password, String encryptPath){
        ArrayList<String> decryptList = new ArrayList<>();
        decryptList.add(decryptPath);
        decryptList.add(password);
        decryptList.add(encryptPath);
        mFileActionManager.decrypt(decryptList);
    }

    private void doSDDecryptDialog(FileInfo clickFile){
        final int tabPostiion = getBrowserFragment().getCurrentTabPosition();
        ArrayList<String> folderNames = new ArrayList<String>();
        ArrayList<FileInfo> allFiles = getBrowserFragment().getAllFiles();
        for (FileInfo file : allFiles) {
            if (file.type == Constant.TYPE_DIR)
                folderNames.add(file.name.toLowerCase());
        }
        ArrayList<FileInfo> selectedFile = new ArrayList<>();
        selectedFile.add(clickFile);
        DecryptUtils.clearAllDecryptUtils();
        new SDDecryptDialog(this, folderNames, selectedFile){
            @Override
            public void onConfirm(String newFolderName, String password, ArrayList<DocumentFile> selectedDFiles) {
                if(tabPostiion == 5){
                    DocumentFile child = selectedDFiles.get(0).getParentFile();
                    DecryptUtils.setAfterDecryptDFile(child);
                }
                DecryptUtils.setSelectedDocumentFile(selectedDFiles);
                DecryptUtils.setDecryptFileName(newFolderName);
                DecryptUtils.setPassword(password);
                doSDDecryptNewFolder();
            }
        };
    }

    private void doSDDecryptNewFolder(){
        calendar = Calendar.getInstance();
        String currentDateTimeString = sdf.format(calendar.getTime());
        String folderName = Constant.ROOT_CACHE + File.separator + currentDateTimeString;
        DecryptUtils.setBeforeDecryptPath(folderName);
        mFileActionManager.newFolderDecryptSD(folderName);
    }

    private void doSDDecryptCopy(){
        ArrayList<DocumentFile> selectedDFiles = DecryptUtils.getSelectedDocumentFile();
        mFileActionManager.copySDtoLocalDecrypt(selectedDFiles, DecryptUtils.getBeforeDecryptPath());
    }

    private void doSDDecrypt(){
        String password = DecryptUtils.getPassword();
        String beforeEncryptPath = DecryptUtils.getBeforeDecryptPath() + File.separator + DecryptUtils.getSelectedDocumentFile().get(0).getName();
        String afterDecryptPath = DecryptUtils.getBeforeDecryptPath() + File.separator + DecryptUtils.getDecryptFileName();
        DecryptUtils.setAfterDecryptPath(afterDecryptPath);
        ArrayList<String> decryptList = new ArrayList<>();
        decryptList.add(afterDecryptPath);
        decryptList.add(password);
        decryptList.add(beforeEncryptPath);
        mFileActionManager.decryptSD(decryptList);
    }

    private void doOTGDecryptDialog(FileInfo clickFile){
        final int tabPostiion = getBrowserFragment().getCurrentTabPosition();
        ArrayList<String> folderNames = new ArrayList<String>();
        ArrayList<FileInfo> allFiles = getBrowserFragment().getAllFiles();
        for (FileInfo file : allFiles) {
            if (file.type == Constant.TYPE_DIR)
                folderNames.add(file.name.toLowerCase());
        }
        ArrayList<FileInfo> selectedFile = new ArrayList<>();
        selectedFile.add(clickFile);
        DecryptUtils.clearAllDecryptUtils();
        new OTGDecryptDialog(this, folderNames, selectedFile){
            @Override
            public void onConfirm(String newFolderName, String password, ArrayList<DocumentFile> selectedDFiles) {
                if(tabPostiion == 5){
                    DocumentFile child = selectedDFiles.get(0).getParentFile();
                    DecryptUtils.setAfterDecryptDFile(child);
                }
                DecryptUtils.setSelectedDocumentFile(selectedDFiles);
                DecryptUtils.setDecryptFileName(newFolderName);
                DecryptUtils.setPassword(password);
                doOTGDecryptNewFolder();
            }
        };
    }

    private void doOTGDecryptNewFolder(){
        calendar = Calendar.getInstance();
        String currentDateTimeString = sdf.format(calendar.getTime());
        String folderName = Constant.ROOT_CACHE + File.separator + currentDateTimeString;
        DecryptUtils.setBeforeDecryptPath(folderName);
        mFileActionManager.newFolderDecryptOTG(folderName);
    }

    private void doOTGDecryptCopy(){
        ArrayList<DocumentFile> selectedDFiles = DecryptUtils.getSelectedDocumentFile();
        mFileActionManager.copyOTGtoLocalDecrypt(selectedDFiles, DecryptUtils.getBeforeDecryptPath());
    }

    private void doOTGDecrypt(){
        String password = DecryptUtils.getPassword();
        String beforeEncryptPath = DecryptUtils.getBeforeDecryptPath() + File.separator + DecryptUtils.getSelectedDocumentFile().get(0).getName();
        String afterDecryptPath = DecryptUtils.getBeforeDecryptPath() + File.separator + DecryptUtils.getDecryptFileName();
        DecryptUtils.setAfterDecryptPath(afterDecryptPath);
        ArrayList<String> decryptList = new ArrayList<>();
        decryptList.add(afterDecryptPath);
        decryptList.add(password);
        decryptList.add(beforeEncryptPath);
        mFileActionManager.decryptOTG(decryptList);
    }

    private void doLocalCopytoOTGDecrypt(boolean isSrcSDCard){
        if(isSrcSDCard){
            String uid = FileFactory.getSDCardUniqueId();
            String sdKey = LocalPreferences.getSDKey(mContext, uid);
            if(sdKey != ""){
                String getLocalDecryptFilePath = DecryptUtils.getAfterDecryptPath();
                FileInfo tmpFile = new FileInfo();
                tmpFile.path = getLocalDecryptFilePath;
                ArrayList<FileInfo> selectedFiles = new ArrayList<>();
                selectedFiles.add(tmpFile);
                DocumentFile tmpDFile = DecryptUtils.getSelectedDocumentFile().get(0).getParentFile();
                ArrayList<DocumentFile> destinationDFiles = new ArrayList<>();
                destinationDFiles.add(tmpDFile);
                mFileActionManager.copyFromLocaltoOTGDecrypt(selectedFiles, destinationDFiles);
            }
        }else {
            String getLocalDecryptFilePath = DecryptUtils.getAfterDecryptPath();
            FileInfo tmpFile = new FileInfo();
            tmpFile.path = getLocalDecryptFilePath;
            ArrayList<FileInfo> selectedFiles = new ArrayList<>();
            selectedFiles.add(tmpFile);
            DocumentFile tmpDFile = DecryptUtils.getAfterDecryptDFile();
            if(tmpDFile == null)
                tmpDFile = Constant.mRootDocumentFile;
            ArrayList<DocumentFile> destinationDFiles = new ArrayList<>();
            destinationDFiles.add(tmpDFile);
            mFileActionManager.copyFromLocaltoOTGDecrypt(selectedFiles, destinationDFiles);
        }
    }

    private void doLocalNewFolder(){
        List<String> folderNames = new ArrayList<String>();
        ArrayList<FileInfo> allFiles = getBrowserFragment().getAllFiles();
        for (FileInfo file : allFiles) {
            if (file.type == Constant.TYPE_DIR)
                folderNames.add(file.name.toLowerCase());
        }
        new LocalNewFolderDialog(this, folderNames) {
            @Override
            public void onConfirm(String newName) {
                String path = Constant.ROOT_LOCAL;
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
        ArrayList<FileInfo> allFiles = getBrowserFragment().getAllFiles();
        for (FileInfo file : allFiles) {
            if (file.type == Constant.TYPE_DIR)
                folderNames.add(file.name.toLowerCase());
        }
        new OTGNewFolderDialog(this, folderNames, Constant.Activity, false) {
            @Override
            public void onConfirm(String newName, ArrayList<DocumentFile> mDFiles) {
                if(bSDCard){
                    if(checkSDWritePermission()){
                        ActionParameter.path = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
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
        FileInfo target = getActionSeletedFiles().get(0);
        final String path = target.path;
        final String name = target.name;
        boolean ignoreType = (target.type == Constant.TYPE_DIR);
        new LocalRenameDialog(this,ignoreType, name) {
            @Override
            public void onConfirm(String newName) {
                if (newName.equals(name))
                    return;
                mFileActionManager.rename(path, newName);
            }
        };
    }

    private void doOTGRename(final boolean bSDCard) {
        boolean fromName = false;
        if (getBrowserFragment() == null) {

        } else {
            int postion = getBrowserFragment().getCurrentTabPosition();
            if (postion == BrowserFragment.LIST_TYPE_FOLDER)
                fromName = true;
        }
        final ArrayList<FileInfo> selectedFiles = getActionSeletedFiles();
        new OTGRenameDialog(this, selectedFiles, fromName, false) {
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
        ArrayList<FileInfo> selectedFiles = getActionSeletedFiles();
        new LocalDeleteDialog(this, selectedFiles) {
            @Override
            public void onConfirm(ArrayList<FileInfo> selectedFiles) {
                loading_container.setVisibility(View.VISIBLE);
                mFileActionManager.delete(selectedFiles);
            }
        };
    }

    private void doOTGDelete(final boolean bSDCard){
        boolean fromName = false;
        if (getBrowserFragment() == null) {
            //from search page
        } else {
            int postion = getBrowserFragment().getCurrentTabPosition();
            if (postion == BrowserFragment.LIST_TYPE_FOLDER)
                fromName = true;
        }
        final ArrayList<FileInfo> selectedFiles = getActionSeletedFiles();
        new OTGDeleteDialog(this, selectedFiles, fromName, false) {
            @Override
            public void onConfirm(ArrayList<DocumentFile> selectedDocumentFile) {
                if(bSDCard){
                    if(checkSDWritePermission()){
                        loading_container.setVisibility(View.VISIBLE);
                        mFileActionManager.deleteOTG(selectedDocumentFile);
                    }else{
                        ActionParameter.files = selectedFiles;
                    }
                }else{
                    loading_container.setVisibility(View.VISIBLE);
                    mFileActionManager.deleteOTG(selectedDocumentFile);
                }
            }
        };
    }

    private void doLocalCopyorMove(int actionId, ArrayList<FileInfo> selectedFiles, String destinationPath) {
        for (FileInfo fileInfo : selectedFiles) {
            if(fileInfo.type == Constant.TYPE_DIR){
                if (destinationPath.startsWith(fileInfo.path)) {
                    snackBarShow(R.string.select_folder_error);
                    return;
                }
            }
        }
        if(actionId == R.id.action_copy)
            mFileActionManager.copy(selectedFiles, destinationPath);
        else if(actionId == R.id.action_move)
            mFileActionManager.move(selectedFiles, destinationPath);
    }

    private void doLocalCopyorMovetoOTG(int actionId, ArrayList<FileInfo> selectedFiles, ArrayList<DocumentFile> destinationDFiles, String destinationPath, boolean isSrcSDCard){
        if(isSrcSDCard){
            String uid = FileFactory.getSDCardUniqueId();
            String sdKey = LocalPreferences.getSDKey(mContext, uid);
            if(sdKey != ""){
                Uri uriSDKey = Uri.parse(sdKey);
                DocumentFile tmpDFile = DocumentFile.fromTreeUri(mContext, uriSDKey);
                Constant.mSDCurrentDocumentFile = tmpDFile;
                String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
                ArrayList<FileInfo> files = createListFileInfoFromPath(destinationPath);
                ArrayList<DocumentFile> destDFiles = FileFactory.findDocumentFilefromPathSD(files, sdPath, Constant.Activity);
                if(actionId == R.id.action_copy)
                    mFileActionManager.copyFromLocaltoOTG(selectedFiles, destDFiles, destinationPath);
                else if(actionId == R.id.action_move)
                    mFileActionManager.moveFromLocaltoOTG(selectedFiles, destDFiles, destinationPath);
            }
        }else {
            if(actionId == R.id.action_copy)
                mFileActionManager.copyFromLocaltoOTG(selectedFiles, destinationDFiles, "");
            else if(actionId == R.id.action_move)
                mFileActionManager.moveFromLocaltoOTG(selectedFiles, destinationDFiles, "");
        }
    }

    private void doSDMovetoOTG(int actionId, ArrayList<FileInfo> selectedFiles, ArrayList<DocumentFile> destinationDFiles, String destinationPath, boolean isSrcSDCard){
        String uid = FileFactory.getSDCardUniqueId();
        String sdKey = LocalPreferences.getSDKey(mContext, uid);
        Uri uriSDKey = Uri.parse(sdKey);
        DocumentFile tmpDFile = DocumentFile.fromTreeUri(mContext, uriSDKey);
        Constant.mSDCurrentDocumentFile = tmpDFile;
        String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
        ArrayList<FileInfo> files = createListFileInfoFromPath(destinationPath);
        ArrayList<DocumentFile> srcDFiles = FileFactory.findDocumentFilefromPathSD(selectedFiles, sdPath, Constant.Activity);
        mFileActionManager.moveOTG(srcDFiles, destinationDFiles, destinationPath);

    }


    private void doOTGCopyorMove(int actionId, ArrayList<FileInfo> selectedFiles, ArrayList<DocumentFile> destinationDFiles, String destinationPath, boolean isSrcSDCard) {
        if(isSrcSDCard){
            String uid = FileFactory.getSDCardUniqueId();
            String sdKey = LocalPreferences.getSDKey(mContext, uid);
            if(sdKey != ""){
                Uri uriSDKey = Uri.parse(sdKey);
                DocumentFile tmpDFile = DocumentFile.fromTreeUri(mContext, uriSDKey);
                Constant.mSDCurrentDocumentFile = tmpDFile;
                String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
                ArrayList<FileInfo> files = createListFileInfoFromPath(destinationPath);
                ArrayList<DocumentFile> destDFiles = FileFactory.findDocumentFilefromPathSD(files, sdPath, Constant.Activity);
                String otgPath = FileFactory.getOTGStoragePath(mContext, Constant.otg_key_path);
                ArrayList<DocumentFile> srcDFiles = FileFactory.findDocumentFilefromPathOTG(selectedFiles, otgPath, Constant.Activity);
                if(actionId == R.id.action_copy)
                    mFileActionManager.copyOTG(srcDFiles, destDFiles, destinationPath);
                else if(actionId == R.id.action_move)
                    mFileActionManager.moveOTG(srcDFiles, destDFiles, destinationPath);
            }
        }else{
            String otgPath = FileFactory.getOTGStoragePath(mContext, Constant.otg_key_path);
            ArrayList<DocumentFile> srcDFiles = FileFactory.findDocumentFilefromPathOTG(selectedFiles, otgPath, Constant.Activity);
            for(DocumentFile dFile : srcDFiles){
                if(dFile.isDirectory()){
                    DocumentFile tmpDFile = destinationDFiles.get(0);
                    while (tmpDFile != null){
                        if (dFile.getUri().toString().equals(tmpDFile.getUri().toString())){
                            snackBarShow(R.string.select_folder_error);
                            return;
                        }else{
                            tmpDFile = tmpDFile.getParentFile();
                        }
                    }
                }
            }
            if(actionId == R.id.action_copy)
                mFileActionManager.copyOTG(srcDFiles, destinationDFiles, "");
            else if(actionId == R.id.action_move)
                mFileActionManager.moveOTG(srcDFiles, destinationDFiles, "");

        }
    }

    private void doOTGCopyorMovetoLocal(int actionId, ArrayList<FileInfo> selectedFiles, String destinationPath, boolean isDesSDCard){
        if(isDesSDCard){
            String uid = FileFactory.getSDCardUniqueId();
            String sdKey = LocalPreferences.getSDKey(mContext, uid);
            if(sdKey != ""){
                Uri uriSDKey = Uri.parse(sdKey);
                DocumentFile tmpDFile = DocumentFile.fromTreeUri(mContext, uriSDKey);
                Constant.mSDCurrentDocumentFile = tmpDFile;
                String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
                ArrayList<DocumentFile> srcDFiles = FileFactory.findDocumentFilefromPathSD(selectedFiles, sdPath, Constant.Activity);
                if(actionId == R.id.action_copy)
                    mFileActionManager.copyOTGtoLocal(srcDFiles, destinationPath);
                else if(actionId == R.id.action_move)
                    mFileActionManager.moveOTGtoLocal(srcDFiles, destinationPath);
            }

        }else {
            String otgPath = FileFactory.getOTGStoragePath(mContext, Constant.otg_key_path);
            ArrayList<DocumentFile> srcDFiles = FileFactory.findDocumentFilefromPathOTG(selectedFiles, otgPath, Constant.Activity);
            if(actionId == R.id.action_copy)
                mFileActionManager.copyOTGtoLocal(srcDFiles, destinationPath);
            else if(actionId == R.id.action_move)
                mFileActionManager.moveOTGtoLocal(srcDFiles, destinationPath);
        }
    }

    private void doLocalShare() {
        String selectPath = getActionSeletedFiles().get(0).path;
        boolean shareSuccess = MediaUtils.localShare(this, selectPath);
        if(!shareSuccess)
            snackBarShow(R.string.snackbar_not_support_share);
        mActionMode.finish();
        Constant.mActionMode = null;

    }

    private void doOTGShare(){
        ArrayList<FileInfo> selectFiles = getActionSeletedFiles();
        ArrayList<DocumentFile> selectDFiles;
        if(getBrowserFragment() != null && getBrowserFragment().getCurrentTabPosition() == BrowserFragment.LIST_TYPE_FOLDER){
            selectDFiles = FileFactory.findDocumentFilefromName(selectFiles, Constant.Activity);
        }else{
            String otgPath = FileFactory.getOTGStoragePath(this, Constant.otg_key_path);
            selectDFiles = FileFactory.findDocumentFilefromPathOTG(selectFiles, otgPath, Constant.Activity);
        }
        boolean shareSuccess = MediaUtils.otgShare(this, selectDFiles.get(0));
        if(!shareSuccess)
            snackBarShow(R.string.snackbar_not_support_share);
        mActionMode.finish();
        Constant.mActionMode = null;
    }

    private void checkGridOrListForFolderPage() {
        BrowserFragment fragment = getBrowserFragment();
        if (fragment != null && fragment.mCurTab != null && fragment.mCurTab.mType == BrowserFragment.LIST_TYPE_FOLDER) {
            int layout_mode = LocalPreferences.getBrowserViewMode(this, fragment.mCurTab.mType, Constant.ITEM_LIST);
            setViewMode(layout_mode);
        }
    }

    private final View.OnClickListener mOnSortClickListener = new View.OnClickListener() {

        public void onClick(View v) {
            int sort_by = LocalPreferences.getPref(mContext, LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_DATE);
            if (v.getTag().equals("date") && sort_by != Constant.SORT_BY_DATE) {
                v.getRootView().findViewById(R.id.arrow_sort_date).setVisibility(View.VISIBLE);
                v.getRootView().findViewById(R.id.arrow_sort_name).setVisibility(View.INVISIBLE);
                v.getRootView().findViewById(R.id.arrow_sort_size).setVisibility(View.INVISIBLE);
                setSortBy(Constant.SORT_BY_DATE);
            } else if (v.getTag().equals("name") && sort_by != Constant.SORT_BY_NAME) {
                v.getRootView().findViewById(R.id.arrow_sort_date).setVisibility(View.INVISIBLE);
                v.getRootView().findViewById(R.id.arrow_sort_name).setVisibility(View.VISIBLE);
                v.getRootView().findViewById(R.id.arrow_sort_size).setVisibility(View.INVISIBLE);
                setSortBy(Constant.SORT_BY_NAME);
            } else if (v.getTag().equals("size") && sort_by != Constant.SORT_BY_SIZE) {
                v.getRootView().findViewById(R.id.arrow_sort_date).setVisibility(View.INVISIBLE);
                v.getRootView().findViewById(R.id.arrow_sort_name).setVisibility(View.INVISIBLE);
                v.getRootView().findViewById(R.id.arrow_sort_size).setVisibility(View.VISIBLE);
                setSortBy(Constant.SORT_BY_SIZE);
            } else {
                boolean sortOrderAsc = LocalPreferences.getPref(mContext,
                        LocalPreferences.BROWSER_SORT_ORDER_PREFIX, Constant.SORT_ORDER_AS) == Constant.SORT_ORDER_AS;
                setSortOrder(sortOrderAsc ? Constant.SORT_ORDER_DES : Constant.SORT_ORDER_AS);
                updateSortArrow(v.getRootView(), mContext);
            }
        }
    };

    private void createPopupWindow(View toolBarView, final Context context) {
        int xy[] = {0,0};
        toolBarView.getLocationOnScreen(xy);

        View layout = getLayoutInflater().inflate(R.layout.easy_sort_layout, null, false);
        PopupWindow easySortView = new PopupWindow(layout, toolBarView.getWidth(), toolBarView.getHeight(), true);
        easySortView.setBackgroundDrawable(new BitmapDrawable());
        easySortView.setOutsideTouchable(true);
        View rootView = ((ViewGroup) (getWindow().getDecorView().findViewById(android.R.id.content))).getChildAt(0);
        easySortView.setAnimationStyle(R.style.PopupWindowAnimation);
        easySortView.showAtLocation(rootView, Gravity.LEFT|Gravity.TOP, xy[0], xy[1]);

        RadioButton b_date = (RadioButton) layout.findViewById(R.id.btn_sort_date);
        RadioButton b_name = (RadioButton) layout.findViewById(R.id.btn_sort_name);
        RadioButton b_size = (RadioButton) layout.findViewById(R.id.btn_sort_size);
        int sort_by = LocalPreferences.getPref(context, LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_DATE);
        switch (sort_by) {
            case Constant.SORT_BY_DATE:
                b_date.setChecked(true);
                layout.findViewById(R.id.arrow_sort_date).setVisibility(View.VISIBLE);
                break;
            case Constant.SORT_BY_NAME:
                b_name.setChecked(true);
                layout.findViewById(R.id.arrow_sort_name).setVisibility(View.VISIBLE);
                break;
            case Constant.SORT_BY_SIZE:
                b_size.setChecked(true);
                layout.findViewById(R.id.arrow_sort_size).setVisibility(View.VISIBLE);
                break;
        }
        updateSortArrow(layout, mContext);
        b_date.setOnClickListener(mOnSortClickListener);
        b_name.setOnClickListener(mOnSortClickListener);
        b_size.setOnClickListener(mOnSortClickListener);
    }

    private void updateSortArrow (final View layout, Context context) {
        String arrow = getSortArrow(context);
        ((TextView) layout.findViewById(R.id.arrow_sort_date)).setText(arrow);
        ((TextView) layout.findViewById(R.id.arrow_sort_name)).setText(arrow);
        ((TextView) layout.findViewById(R.id.arrow_sort_size)).setText(arrow);
    }

    private String getSortArrow(Context context) {
        int order = LocalPreferences.getPref(context, LocalPreferences.BROWSER_SORT_ORDER_PREFIX, Constant.SORT_ORDER_AS);
        if (order == Constant.SORT_ORDER_AS) {
            return context.getResources().getString(R.string.top_arrow);
        } else {
            return context.getResources().getString(R.string.bottom_arrow);
        }
    }

    private ArrayList<FileInfo> getActionSeletedFiles() {
        if (getBrowserFragment() == null) { //for search page
            return ((SearchResults) getFragment()).getSelectedFiles();
        } else {
            return getBrowserFragment().getSelectedFiles();
        }
    }

    private void clearActionAllSelected() {
        if (getBrowserFragment() == null) { //for search page
            ((SearchResults) getFragment()).clearAll();
        } else {
            getBrowserFragment().clearAllSelect();
        }
    }

    private int getActionSelectedCount() {
        if (getBrowserFragment() == null) { //for search page
            return ((SearchResults) getFragment()).getItemsCount();
        } else {
            return getBrowserFragment().getItemsCount();
        }
    }
}
