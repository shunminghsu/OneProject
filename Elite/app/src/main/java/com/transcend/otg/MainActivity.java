package com.transcend.otg;

import android.app.LoaderManager;
import android.app.PendingIntent;
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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.transcend.otg.Browser.BrowserFragment;
import com.transcend.otg.Browser.LocalFragment;
import com.transcend.otg.Browser.NoOtgFragment;
import com.transcend.otg.Browser.NoSdFragment;
import com.transcend.otg.Browser.OTGFragment;
import com.transcend.otg.Browser.SdFragment;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Constant.LoaderID;
import com.transcend.otg.Dialog.OTGFileActionGuideDialog;
import com.transcend.otg.Home.HomeFragment;
import com.transcend.otg.Loader.FileActionManager;
import com.transcend.otg.Loader.LocalFileListLoader;
import com.transcend.otg.Loader.LocalTypeListLoader;
import com.transcend.otg.Utils.FileFactory;
import com.transcend.otg.Utils.Pref;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Boolean> {

    private String TAG = MainActivity.class.getSimpleName();
    public static final String BACK_STACK_PREFS = ":elite:prefs";
    private SearchView mSearchView;
    private MenuItem mSearchMenuItem;
    private boolean mSearchMenuItemExpanded = false;
    private boolean mShowSearchIcon = false;
    private SearchResults mSearchResultsFragment;

    private Toolbar toolbar;
    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private LinearLayout container, layout_storage;
    private SdFragment sdFragment;
    private OTGFragment otgFragment;
    private LocalFragment localFragment;
    private int mLoaderID, mOTGDocumentTreeID = 1000;
    private FileActionManager mFileActionManager;
    private String mPath;
    private ArrayList<FileInfo> mFileList, mImgFileList, mMusicFileList, mVideoFileList, mDocFileList;
    private Button mLocalButton, mSdButton, mOtgButton;
    private Context mContext;

    //home page
    private LinearLayout home_container;
    private TextView tv_Browser;
    private TextView tv_Backup;

    //USB
    private Toast mToast;
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
        initBroadcast();
        initDrawer();
        initImageLoader();
        initButtons();
        initHome();
        initFragment();
        FileFactory.getStoragePath(this);
    }

    private void init() {
        mContext = this;
        mFileActionManager = new FileActionManager(this, FileActionManager.MODE.LOCAL, this);
        mPath = mFileActionManager.getLocalRootPath();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenW = displaymetrics.widthPixels;
    }

    private void showHomeOrFragment(boolean home) {
        if (home) {
            home_container.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);
            layout_storage.setVisibility(View.GONE);
            showSearchIcon(false);
        } else {
            home_container.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            layout_storage.setVisibility(View.VISIBLE);
            showSearchIcon(getBrowserFragment() != null);
        }
    }
    private void initHome() {
        home_container = (LinearLayout) findViewById(R.id.home_page);
        tv_Browser = (TextView) findViewById(R.id.home_browser);
        tv_Browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDrawerCheckItem(R.id.nav_browser);
                showHomeOrFragment(false);
                markSelectedBtn(mLocalButton);
                replaceFragment(localFragment);
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
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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

                if (device != null) {


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

    private void initImageLoader() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(options)
                .build();
        ImageLoader.getInstance().init(config);
    }

    private void initFragment() {
        layout_storage = (LinearLayout) findViewById(R.id.layout_storage);
        container = (LinearLayout) findViewById(R.id.fragment_container);
        sdFragment = new SdFragment();
        localFragment = new LocalFragment();
        otgFragment = new OTGFragment();
    }


    private void markSelectedBtn(Button selected) {
        mLocalButton.setTextColor(getResources().getColor(R.color.colorBlack));
        mSdButton.setTextColor(getResources().getColor(R.color.colorBlack));
        mOtgButton.setTextColor(getResources().getColor(R.color.colorBlack));
        selected.setTextColor(getResources().getColor(R.color.colorPrimary));
    }
    class ButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view == mLocalButton) {
                Constant.nowMODE = Constant.MODE.LOCAL;
                markSelectedBtn(mLocalButton);
                replaceFragment(localFragment);
            } else if (view == mSdButton) {
                Constant.nowMODE = Constant.MODE.SD;
                markSelectedBtn(mSdButton);
                String sdpath = FileFactory.getSdPath(mContext);
                if (sdpath != null) {
                    if (FileFactory.getMountedState(mContext, sdpath)) {
                        replaceFragment(sdFragment);
                    } else {
                        showSearchIcon(false);
                        switchToFragment(NoSdFragment.class.getName(), false);
                    }
                } else {
                    showSearchIcon(false);
                    switchToFragment(NoSdFragment.class.getName(), false);
                }
            } else if (view == mOtgButton) {
                markSelectedBtn(mOtgButton);
                discoverDevice();
            }
        }
    }

    private void discoverDevice() {
        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(mContext);

        if (devices.length == 0) {
            Log.w(TAG, "no device found!");
            showSearchIcon(false);
            switchToFragment(NoOtgFragment.class.getName(), false);
            return;
        }
        device = devices[0];
        intentDocumentTree();
//        UsbDevice usbDevice = (UsbDevice) getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
//
//        if (!(usbDevice != null && usbManager.hasPermission(usbDevice))) {
//            PendingIntent permissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(
//                    ACTION_USB_PERMISSION), 0);
//            usbManager.requestPermission(device.getUsbDevice(), permissionIntent);
//        }
    }

    private void initButtons() {
        mLocalButton = (Button) findViewById(R.id.btn_local);
        mSdButton = (Button) findViewById(R.id.btn_sd);
        mOtgButton = (Button) findViewById(R.id.btn_otg);

        ButtonClickListener listener = new ButtonClickListener();
        mLocalButton.setOnClickListener(listener);
        mSdButton.setOnClickListener(listener);
        mOtgButton.setOnClickListener(listener);

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
        mSearchMenuItem.setVisible(mShowSearchIcon);
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
                        return true;
                    }
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        if (mSearchMenuItemExpanded) {
                            revertToInitialFragment();
                            layout_storage.setVisibility(View.VISIBLE);
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

        final MenuItem sort = menu.findItem(R.id.menu_sort);
        final MenuItem grid = menu.findItem(R.id.menu_grid);
        final MenuItem list = menu.findItem(R.id.menu_list);

        BrowserFragment fragment = getBrowserFragment();
        if (fragment != null && fragment.mCurTab != null) {
            grid.setVisible(fragment.mCurTab.mMode == Constant.ITEM_LIST);
            list.setVisible(fragment.mCurTab.mMode == Constant.ITEM_GRID);
            sort.setVisible(true);
        } else {
            grid.setVisible(false);
            list.setVisible(false);
            sort.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_create_dir:
                //showCreateDirectoryDialog();
                return true;
            case R.id.menu_sort_name:
                //setUserSortOrder(State.SORT_ORDER_DISPLAY_NAME);
                return true;
            case R.id.menu_sort_date:
                //setUserSortOrder(State.SORT_ORDER_LAST_MODIFIED);
                return true;
            case R.id.menu_sort_size:
                //setUserSortOrder(State.SORT_ORDER_SIZE);
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

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void replaceFragment(Fragment fragment) {
        if (fragment instanceof BrowserFragment) {
            showSearchIcon(true);
        } else {
            showSearchIcon(false);
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
            /*if (loader instanceof LocalFileListLoader) {
                mPath = ((LocalFileListLoader) loader).getPath();
                mFileList = ((LocalFileListLoader) loader).getFileList();
                browserFragment.setFileList(mFileList);
                replaceFragment(browserFragment);
                //TO DO
            }else if (loader instanceof LocalTypeListLoader){
                mImgFileList = ((LocalTypeListLoader) loader).getImageList();
                mMusicFileList = ((LocalTypeListLoader) loader).getMusicList();
                mVideoFileList = ((LocalTypeListLoader) loader).getVideoList();
                mDocFileList = ((LocalTypeListLoader) loader).getDocList();
                browserFragment.setImgFileList(mImgFileList);
                browserFragment.setMusicFileList(mMusicFileList);
                browserFragment.setVideoFileList(mVideoFileList);
                browserFragment.setDocFileList(mDocFileList);
                replaceFragment(browserFragment);
            }*/
        }

    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {

    }

    private Fragment switchToFragment(String fragmentName, boolean addToBackStack) {
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

    private void showSearchIcon(boolean show) {
        mShowSearchIcon = show;
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

    private void setViewMode(int mode) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            ((BrowserFragment) fragment).onViewModeChanged(mode);
        }
    }

    private void toast(int resId) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(this, resId, Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.show();
    }

    private void intentDocumentTree() {
        new OTGFileActionGuideDialog(this) {
            @Override
            public void onConfirm(Boolean isClick) {
                if (isClick) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(intent, mOTGDocumentTreeID);
                } else{
                    //                    toggleDrawerCheckedItem();
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

        }
    }

    private boolean checkStorage(Uri uri){
        if (!uri.toString().contains("primary")) {
            if (uri != null) {
                if(uri.getPath().toString().split(":").length > 1){
                    toast(R.string.toast_plz_select_top);
                    intentDocumentTree();
                }else{
                    rootDir = DocumentFile.fromTreeUri(this, uri);//OTG root path
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    Constant.pickedDir = Constant.rootDir = otgDir = rootDir;
                    Constant.nowMODE = Constant.MODE.OTG;
                    Constant.rootUri = uri;
                    return true;
                }

            }

        }else {
            toast(R.string.toast_plz_select_otg);
            intentDocumentTree();
        }
        return false;
    }
}
