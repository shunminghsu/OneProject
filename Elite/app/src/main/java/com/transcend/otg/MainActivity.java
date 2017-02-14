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
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
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

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.transcend.otg.Browser.BrowserFragment;
import com.transcend.otg.Browser.LocalFragment;
import com.transcend.otg.Browser.NoOtgFragment;
import com.transcend.otg.Browser.NoSdFragment;
import com.transcend.otg.Browser.OTGFragment;
import com.transcend.otg.Browser.SdFragment;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Constant.LoaderID;
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
    private int mLoaderID;
    private FileActionManager mFileActionManager;
    private String mPath;
    private ArrayList<FileInfo> mFileList, mImgFileList, mMusicFileList, mVideoFileList, mDocFileList;
    private Button mLocalButton, mSdButton, mOtgButton;
    private Context mContext;

    //home page
    private LinearLayout home_container;
    private TextView tv_Browser;
    private TextView tv_Backup;

    private static final String ACTION_USB_PERMISSION = "com.transcend.otg.USB_PERMISSION";
    private UsbMassStorageDevice device;
    private FileSystem currentFs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initToolbar();
        initBroadcast();
        initDrawer();
        initButtons();
        initHome();
        initFragment();
        FileFactory.getStoragePath(this);
    }

    private void init() {
        mContext = this;
        mFileActionManager = new FileActionManager(this, FileActionManager.MODE.LOCAL, this);
        mPath = mFileActionManager.getLocalRootPath();
    }

    private void showHomeOrFragment(boolean home) {
        if (home) {
            home_container.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);
            layout_storage.setVisibility(View.GONE);
        } else {
            home_container.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            layout_storage.setVisibility(View.VISIBLE);
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

                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                    if (device != null) {
                        setupDevice();
                    }
                }

            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
//                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//
//                Log.d(TAG, "USB device attached");
//
//                if (device != null) {
//                    discoverDevice();
//                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                Log.d(TAG, "USB device detached");

                // determine if connected device is a mass storage devuce
                if (device != null) {
                    if (MainActivity.this.device != null) {
                        MainActivity.this.device.close();
                    }
                    // check if there are other devices or set action bar title
                    // to no device if not
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
                        switchToFragment(NoSdFragment.class.getName(), false);
                    }
                } else {
                    switchToFragment(NoSdFragment.class.getName(), false);
                }
            } else if (view == mOtgButton) {
                markSelectedBtn(mOtgButton);
                discoverDevice();
            }
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
        // Inflate the menu; this adds items to the action bar if it is present.
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        if (fragment instanceof HomeFragment) {
            layout_storage.setVisibility(View.GONE);
            showSearchIcon(false);
        } else {
            layout_storage.setVisibility(View.VISIBLE);
            showSearchIcon(true);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    public void setDrawerCheckItem(int id) {
        navigationView.setCheckedItem(id);
    }

    private void discoverDevice() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(this);

        if (devices.length == 0) {
            Log.w(TAG, "no device found!");
            switchToFragment(NoOtgFragment.class.getName(), false);
            return;
        }
        device = devices[0];

        UsbDevice usbDevice = (UsbDevice) getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);

        if (!(usbDevice != null && usbManager.hasPermission(usbDevice))) {
            PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                    ACTION_USB_PERMISSION), 0);
            usbManager.requestPermission(device.getUsbDevice(), permissionIntent);
        }
    }

    /**
     * Sets the device up and shows the contents of the root directory.
     */
    private void setupDevice() {

        try {
            device.init();
            Constant.nowMODE = Constant.MODE.OTG;
            Constant.nowDevice = device;
            replaceFragment(otgFragment);
            // we always use the first partition of the device
//            currentFs = device.getPartitions().get(0).getFileSystem();
//            Log.d(TAG, "Capacity: " + currentFs.getCapacity());
//            Log.d(TAG, "Occupied Space: " + currentFs.getOccupiedSpace());
//            Log.d(TAG, "Free Space: " + currentFs.getFreeSpace());
//            Log.d(TAG, "Chunk size: " + currentFs.getChunkSize());
//            UsbFile root = currentFs.getRootDirectory();
//
//            ActionBar actionBar = getSupportActionBar();
//            actionBar.setTitle(currentFs.getVolumeLabel());

        } catch (IOException e) {
            Log.e(TAG, "error setting up device", e);
        }

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
}
