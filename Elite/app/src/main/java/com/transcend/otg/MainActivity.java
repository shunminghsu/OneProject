package com.transcend.otg;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.transcend.otg.Browser.BrowserFragment;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Constant.LoaderID;
import com.transcend.otg.Home.HomeFragment;
import com.transcend.otg.Loader.FileActionManager;
import com.transcend.otg.Loader.LocalFileListLoader;
import com.transcend.otg.Utils.Pref;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Boolean> {

    private String TAG = MainActivity.class.getSimpleName();
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private LinearLayout container, layout_storage;
    private HomeFragment homeFragment;
    private BrowserFragment browserFragment;
    private int mLoaderID;
    private FileActionManager mFileActionManager;
    private String mPath;
    private ArrayList<FileInfo> mFileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initToolbar();
        initDrawer();
        initFragment();
        replaceFragment(homeFragment);
    }

    private void init() {
        mFileActionManager = new FileActionManager(this, FileActionManager.MODE.LOCAL, this);
        mPath = mFileActionManager.getLocalRootPath();
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
        homeFragment = new HomeFragment();
        browserFragment = new BrowserFragment();
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
        return true;
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
            replaceFragment(homeFragment);
        } else if (id == R.id.nav_browser) {
            doLoad(Pref.getMainPageLocation(this));
            replaceFragment(browserFragment);
        } else if (id == R.id.nav_backup) {

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void replaceFragment(Fragment fragment) {
        if (fragment instanceof HomeFragment) {
            layout_storage.setVisibility(View.GONE);
        } else {
            layout_storage.setVisibility(View.GONE);
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
        mFileActionManager.list(path);
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
            if (loader instanceof LocalFileListLoader) {
                mPath = ((LocalFileListLoader) loader).getPath();
                mFileList = ((LocalFileListLoader) loader).getFileList();
                //TO DO
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {

    }
}
