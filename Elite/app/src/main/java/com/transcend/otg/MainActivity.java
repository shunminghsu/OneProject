package com.transcend.otg;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.Context;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mjdev.libaums.UsbMassStorageDevice;
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
import com.transcend.otg.Dialog.LocalDeleteDialog;
import com.transcend.otg.Dialog.LocalNewFolderDialog;
import com.transcend.otg.Dialog.LocalRenameDialog;
import com.transcend.otg.Dialog.OTGDeleteDialog;
import com.transcend.otg.Dialog.OTGFileRenameDialog;
import com.transcend.otg.Dialog.OTGNewFolderDialog;
import com.transcend.otg.Dialog.OTGPermissionGuideDialog;
import com.transcend.otg.Dialog.SDPermissionGuideDialog;
import com.transcend.otg.Loader.FileActionManager;
import com.transcend.otg.Loader.OTGRenameLoader;
import com.transcend.otg.Utils.FileFactory;
import com.transcend.otg.Utils.MediaUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Boolean>,
        ActionMode.Callback,
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
    private LinearLayout container, layout_storage;
    private RelativeLayout main_relativeLayout;
    private SdFragment sdFragment;
    private OTGFragment otgFragment;
    private LocalFragment localFragment;
    private int mLoaderID, mOTGDocumentTreeID = 1000, mSDDocumentTreeID = 1001;
    private FileActionManager mFileActionManager;
    private String mPath;
    private Button mLocalButton, mSdButton, mOtgButton;
    private Context mContext;
    private FloatingActionButton mFab;
    private ActionMode mActionMode;
    private RelativeLayout mActionModeView;
    private TextView mActionModeTitle;
    private int nowAction;

    //home page
    private LinearLayout home_container;
    private TextView tv_Browser;
    private TextView tv_Backup;

    //USB
    private DocumentFile rootDir, otgDir;
    private static final String ACTION_USB_PERMISSION = "com.transcend.otg.USB_PERMISSION";
    private UsbMassStorageDevice device;

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
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenW = displaymetrics.widthPixels;
        nowAction = -1;
    }

    private void showHomeOrFragment(boolean home) {
        if (home) {
            home_container.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);
            layout_storage.setVisibility(View.GONE);
            mFab.setVisibility(View.GONE);
            invalidateOptionsMenu(); //all menu item should be disabled, and the empty menu will be hided after invalidated
        } else {
            home_container.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            layout_storage.setVisibility(View.VISIBLE);
            mFab.setVisibility(View.VISIBLE);
            replaceFragment(localFragment);
            invalidateOptionsMenu();
        }
    }
    private void initHome() {
        home_container = (LinearLayout) findViewById(R.id.home_page);
        setDrawerCheckItem(R.id.nav_home);
        tv_Browser = (TextView) findViewById(R.id.home_browser);
        tv_Browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

            }
        });
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
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
    }

    private void initFragment() {
        layout_storage = (LinearLayout) findViewById(R.id.layout_storage);
        container = (LinearLayout) findViewById(R.id.fragment_container);
        sdFragment = new SdFragment();
        localFragment = new LocalFragment();
        otgFragment = new OTGFragment();
    }

    private void initActionModeView() {
        mActionModeView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.action_mode_custom, null);
        mActionModeTitle = (TextView) mActionModeView.findViewById(R.id.action_mode_custom_title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBroadcast();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(usbReceiver);
    }

    private void markSelectedBtn(Button selected) {
        mLocalButton.setTextColor(getResources().getColor(R.color.colorBlack));
        mSdButton.setTextColor(getResources().getColor(R.color.colorBlack));
        mOtgButton.setTextColor(getResources().getColor(R.color.colorBlack));
        selected.setTextColor(getResources().getColor(R.color.colorPrimary));
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
                ? R.drawable.ic_menu_manage
                : R.drawable.ic_menu_camera;
        mFab.setImageResource(resId);
        mFab.setVisibility(View.VISIBLE);
    }

    private void toggleActionModeAction(int count) {
        boolean visible = false;
        int position = getBrowserFragment().getCurrentTabPosition();
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
        if(position != 5)
            mActionMode.getMenu().findItem(R.id.action_new_folder).setVisible(visible);
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        boolean isEmpty = getBrowserFragment().getSelectedFiles().size() == 0;
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
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        getBrowserFragment().clearAllSelect();
        toggleFabSelectAll(false);
        Constant.mActionMode = mActionMode = null;
    }

    @Override
    public void onItemClick(FileInfo file) {
        if(file.type == Constant.TYPE_DIR)
            startFolderExploreActivity(file);
    }

    @Override
    public void onItemClick(int count) {
        updateActionModeTitle(count);
        toggleActionModeAction(count);
        int totalCount = getBrowserFragment().getItemsCount();
        if(totalCount == count)
            toggleFabSelectAll(true);
        else
            toggleFabSelectAll(false);
    }

    @Override
    public void onItemLongClick(int count) {
        startActionMode();
        updateActionModeTitle(count);
        toggleActionModeAction(count);
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
                        //showSearchIcon(false);
                        switchToFragment(NoSdFragment.class.getName(), false);
                    }
                } else {
                    //showSearchIcon(false);
                    if(mActionMode != null)
                        mActionMode.finish();
                    switchToFragment(NoSdFragment.class.getName(), false);
                }
            } else if (view == mOtgButton) {
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
        boolean b_SelectAll = getBrowserFragment().getSelectedAllorNot();
        if(b_SelectAll){
            getBrowserFragment().clearAllSelect();
            updateActionModeTitle(0);
            toggleActionModeAction(0);
        }else{
            getBrowserFragment().selectAll();
            updateActionModeTitle(getBrowserFragment().getItemsCount());
            toggleActionModeAction(getBrowserFragment().getItemsCount());
        }

        toggleFabSelectAll(!b_SelectAll);
    }

    private void discoverDevice() {
        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(mContext);

        if (devices.length == 0) {
            Log.w(TAG, "no device found!");
            //showSearchIcon(false);
            Constant.mCurrentDocumentFile = Constant.mRootDocumentFile = null;
            Constant.rootUri = null;
            switchToFragment(NoOtgFragment.class.getName(), false);
            return;
        }
        device = devices[0];
        String otgKey = LocalPreferences.getOTGKey(this, device.getUsbDevice().getSerialNumber());
        if(otgKey != "" || otgKey == null){
            Uri uriTree = Uri.parse(otgKey);
            if(checkStorage(uriTree)){
                replaceFragment(otgFragment);
            }
        }else{
            intentDocumentTree();
        }
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

    private void initButtons() {
        mLocalButton = (Button) findViewById(R.id.btn_local);
        mSdButton = (Button) findViewById(R.id.btn_sd);
        mOtgButton = (Button) findViewById(R.id.btn_otg);

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
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mSearchMenuItem = menu.findItem(R.id.search);
        //mSearchMenuItem.setVisible(mShowSearchIcon);
        mSearchView = (SearchView) mSearchMenuItem.getActionView();
        initSearch(mSearchView);

        if (mSearchResultsFragment != null) {
            mSearchResultsFragment.setSearchView(mSearchView);
        }

        MenuItemCompat.setOnActionExpandListener(mSearchMenuItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        switchToSearchResultsFragmentIfNeeded();
                        layout_storage.setVisibility(View.GONE);
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        EditText et = ((EditText)mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));
                        if (mSearchResultsFragment != null)
                            mSearchResultsFragment.onExpand(et);

                        return true;
                    }
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        if (mSearchMenuItemExpanded) {
                            revertToInitialFragment();
                            layout_storage.setVisibility(View.VISIBLE);
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) { // called every time the menu opens
        super.onPrepareOptionsMenu(menu);

        final MenuItem search = menu.findItem(R.id.search);
        final MenuItem sort = menu.findItem(R.id.menu_sort);
        final MenuItem sort_order = menu.findItem(R.id.menu_sort_order);
        final MenuItem grid = menu.findItem(R.id.menu_grid);
        final MenuItem list = menu.findItem(R.id.menu_list);
        final MenuItem newFolder = menu.findItem(R.id.menu_new_folder);

        BrowserFragment fragment = getBrowserFragment();
        if (fragment != null && fragment.mCurTab != null) {
            grid.setVisible(fragment.mCurTab.mMode == Constant.ITEM_LIST);
            list.setVisible(fragment.mCurTab.mMode == Constant.ITEM_GRID);
            sort.setVisible(true);
            sort_order.setVisible(true);
            search.setVisible(true);
            if(fragment.getCurrentTabPosition() == 5)
                newFolder.setVisible(true);
            else
                newFolder.setVisible(false);
        } else {
            grid.setVisible(false);
            list.setVisible(false);
            sort.setVisible(false);
            sort_order.setVisible(false);
            search.setVisible(false);
            newFolder.setVisible(false);
        }

        final MenuItem menu_sort_name = menu.findItem(R.id.menu_sort_name);
        final MenuItem menu_sort_date = menu.findItem(R.id.menu_sort_date);
        final MenuItem menu_sort_size = menu.findItem(R.id.menu_sort_size);
        int sort_value = LocalPreferences.getPref(this, LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_DATE);
        menu_sort_name.setChecked(sort_value == Constant.SORT_BY_NAME);
        menu_sort_date.setChecked(sort_value == Constant.SORT_BY_DATE);
        menu_sort_size.setChecked(sort_value == Constant.SORT_BY_SIZE);

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
            case R.id.menu_sort_name:
                setSortBy(Constant.SORT_BY_NAME);
                return true;
            case R.id.menu_sort_date:
                setSortBy(Constant.SORT_BY_DATE);
                return true;
            case R.id.menu_sort_size:
                setSortBy(Constant.SORT_BY_SIZE);
                return true;
            case R.id.menu_sort_order_as:
                setSortOrder(Constant.SORT_ORDER_AS);
                return true;
            case R.id.menu_sort_order_des:
                setSortOrder(Constant.SORT_ORDER_DES);
                return true;
            case R.id.menu_grid:
                setViewMode(Constant.ITEM_GRID);
                return true;
            case R.id.menu_list:
                setViewMode(Constant.ITEM_LIST);
                return true;
            case R.id.menu_paste_from_clipboard:
               // DirectoryFragment dir = getDirectoryFragment();
                //if (dir != null) {
                //    dir.pasteFromClipboard();
                //}
                return true;
            case R.id.menu_settings:
                //final Intent intent = new Intent();
                //startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            showHomeOrFragment(true);
        } else if (id == R.id.nav_browser) {
            //doLoad(Pref.getMainPageLocation(this));
            showHomeOrFragment(false);

        } else if (id == R.id.nav_backup) {

        } else if (id == R.id.nav_help){

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
            //showSearchIcon(true);
        } else {
            mFab.setVisibility(View.GONE);
            //showSearchIcon(false);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
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
            if(mActionMode != null){
                mActionMode.finish();
                Constant.mActionMode = mActionMode = null;
            }
            getBrowserFragment().restartLoaderforCurrentTab();
        }else{
            snackBarShow(R.string.fail);
        }

    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {

    }

    private Fragment switchToFragment(String fragmentName, boolean addToBackStack) {
        mFab.setVisibility(View.GONE);
        Fragment f = Fragment.instantiate(this, fragmentName);
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, f);

        if (addToBackStack) {
            transaction.addToBackStack(BACK_STACK_PREFS);
        }
        transaction.commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
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
        mSearchResultsFragment.setSearchView(mSearchView);
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

    /*private void showSearchIcon(boolean show) {
        mShowSearchIcon = show;
        invalidateOptionsMenu();
    }*/

    private BrowserFragment getBrowserFragment() {
        if (container != null && container.getVisibility() == View.GONE)
            return null;
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment == null)
            return null;
        return fragment instanceof BrowserFragment? (BrowserFragment)fragment : null;
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

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if(reqCode == mOTGDocumentTreeID && resCode == RESULT_OK){
            Uri uriTree = data.getData();
            if(checkStorage(uriTree)){
                replaceFragment(otgFragment);
            }
        }else if(reqCode == mSDDocumentTreeID && resCode == RESULT_OK){
            Uri uriTree = data.getData();
            if(checkSD(uriTree)){
                doAction();
            }
        }
    }

    private boolean checkSD(Uri uri){
        if (!uri.toString().contains("primary")) {
            if (uri != null) {
                if(uri.getPath().toString().split(":").length > 1){
                    snackBarShow(R.string.snackbar_plz_select_top);
                    intentDocumentTreeSD();
                }else{
                    rootDir = DocumentFile.fromTreeUri(this, uri);//sd root path
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    LocalPreferences.setSDKey(this, uri.toString());
                    Constant.mSDCurrentDocumentFile = Constant.mSDRootDocumentFile = rootDir;
                    ArrayList<DocumentFile> tmpDFiles = new ArrayList<>();
                    tmpDFiles.add(rootDir);
                    ActionParameter.dFiles = tmpDFiles;
                    return true;
                }
            }

        }else {
            snackBarShow(R.string.snackbar_plz_select_sd);
            intentDocumentTreeSD();
        }
        return false;
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
                    Constant.mCurrentDocumentFile = Constant.mRootDocumentFile = otgDir = rootDir;
                    Constant.nowMODE = Constant.MODE.OTG;
                    Constant.rootUri = uri;
                    return true;
                }

            }

        }else {
            snackBarShow(R.string.snackbar_plz_select_otg);
            intentDocumentTree();
        }
        return false;
    }

    private void doAction(){
        switch (nowAction){
            case R.id.menu_new_folder:
                mFileActionManager.newFolderOTG(ActionParameter.name, ActionParameter.dFiles);
                break;
            case R.id.action_delete:
                String sdPathDelete = FileFactory.getOuterStoragePath(this, Constant.sd_key_path);
                ActionParameter.dFiles = FileFactory.findDocumentFilefromPath(ActionParameter.files, sdPathDelete, false);
                mFileActionManager.deleteOTG(ActionParameter.dFiles);
                break;
            case R.id.action_rename:
                String sdPathRename = FileFactory.getOuterStoragePath(this, Constant.sd_key_path);
                ActionParameter.dFiles = FileFactory.findDocumentFilefromPath(ActionParameter.files, sdPathRename, false);
                mFileActionManager.renameOTG(ActionParameter.name, ActionParameter.dFiles);
                break;
            default:
                break;
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
        new OTGNewFolderDialog(this, folderNames) {
            @Override
            public void onConfirm(String newName, ArrayList<DocumentFile> mDFiles) {
                if(bSDCard){
                    if(checkSDWritePermission()){
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
        ArrayList<FileInfo> allFiles = getBrowserFragment().getAllFiles();
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
        boolean fromName = false;
        int postion = getBrowserFragment().getCurrentTabPosition();
        if(postion == 5)
            fromName = true;
        final ArrayList<FileInfo> selectedFiles = getBrowserFragment().getSelectedFiles();
        new OTGFileRenameDialog(this, selectedFiles, fromName, false) {
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
        ArrayList<FileInfo> selectedFiles = getBrowserFragment().getSelectedFiles();
        new LocalDeleteDialog(this, selectedFiles) {
            @Override
            public void onConfirm(ArrayList<FileInfo> selectedFiles) {
                mFileActionManager.delete(selectedFiles);
            }
        };
    }

    private void doOTGDelete(final boolean bSDCard){
        boolean fromName = false;
        int postion = getBrowserFragment().getCurrentTabPosition();
        if(postion == 5)
            fromName = true;
        final ArrayList<FileInfo> selectedFiles = getBrowserFragment().getSelectedFiles();
        new OTGDeleteDialog(this, selectedFiles, fromName, false) {
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
        String selectPath = getBrowserFragment().getSelectedFiles().get(0).path;
        boolean shareSuccess = MediaUtils.localShare(this, selectPath);
        if(!shareSuccess)
            snackBarShow(R.string.snackbar_not_support_share);
        mActionMode.finish();
        Constant.mActionMode = null;

    }

    private void doOTGShare(){
        int position = getBrowserFragment().getCurrentTabPosition();
        ArrayList<FileInfo> selectFiles = getBrowserFragment().getSelectedFiles();
        ArrayList<DocumentFile> selectDFiles = new ArrayList<>();
        if(position == 5){
            selectDFiles = FileFactory.findDocumentFilefromName(selectFiles, false);
        }else{
            selectDFiles = FileFactory.findDocumentFilefromPath(selectFiles, false);
        }
        boolean shareSuccess = MediaUtils.otgShare(this, selectDFiles.get(0));
        if(!shareSuccess)
            snackBarShow(R.string.snackbar_not_support_share);
        mActionMode.finish();
        Constant.mActionMode = null;
    }
}
