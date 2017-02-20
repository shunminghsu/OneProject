package com.transcend.otg.Browser;

import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Loader.OTGFileListLoader;
import com.transcend.otg.Loader.TabInfoLoader;
import com.transcend.otg.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/2/2.
 */

public class BrowserFragment extends Fragment implements
        MyPagerAdapter.UpdateCurrentTabListener{

    public BrowserFragment() {
    }

    private String TAG = BrowserFragment.class.getSimpleName();
    protected final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    public TabInfo mCurTab = null;
    private LayoutInflater mInflater;
    public static final int LIST_TYPE_IMAGE = 0;
    public static final int LIST_TYPE_VIDEO = 1;
    public static final int LIST_TYPE_MUSIC = 2;
    public static final int LIST_TYPE_DOCUMENT = 3;
    public static final int LIST_TYPE_ENCRYPTION = 4;
    public static final int LIST_TYPE_FOLDER = 5;
    protected int mCurrentTabPosition = LIST_TYPE_IMAGE;
    private int TAB_LOADER_ID = 168;
    private int OTG_LOADER_ID = 87;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    protected Context mContext;
    protected LoaderManager.LoaderCallbacks<ArrayList<FileInfo>> mCallbacks;
    protected String mOuterStorage;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        CoordinatorLayout root = (CoordinatorLayout) inflater.inflate(R.layout.fragment_browser, container, false);

        mViewPager = (ViewPager) root.findViewById(R.id.viewPager);
        mTabLayout = (TabLayout) root.findViewById(R.id.tabLayout);

        MyPagerAdapter adapter = new MyPagerAdapter(mTabs, mInflater, this);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(adapter);
        mViewPager.setCurrentItem(mCurrentTabPosition);

        mTabLayout.setupWithViewPager(mViewPager);
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            mTabLayout.getTabAt(i).setIcon(mTabs.get(i).IconId);
        }
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mCurTab = mTabs.get(mCurrentTabPosition);
        return root;
    }

    @Override
    public void updateCurrentTab(int position) {
        Log.d("henry","updateCurrentTab "+position);
        TabInfo tab = mTabs.get(position);
        mCurTab = tab;
        mCurrentTabPosition = position;
        if(Constant.nowMODE == Constant.MODE.OTG)
            getLoaderManager().restartLoader(OTG_LOADER_ID, getArguments(), mCallbacks);
        else
            getLoaderManager().restartLoader(TAB_LOADER_ID, getArguments(), mCallbacks);
        // Put things in the correct paused/resumed state.
        //TO-DO
    }

    @Override
    public void onStart() {
        super.onStart();
        if(Constant.nowMODE == Constant.MODE.OTG)
            getLoaderManager().restartLoader(OTG_LOADER_ID, getArguments(), mCallbacks);
        else
            getLoaderManager().restartLoader(TAB_LOADER_ID, getArguments(), mCallbacks);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(Constant.nowMODE == Constant.MODE.OTG)
            getLoaderManager().destroyLoader(OTG_LOADER_ID);
        else
            getLoaderManager().destroyLoader(TAB_LOADER_ID);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (int i=0; i<mTabs.size(); i++) {
            mTabs.get(i).detachView();
        }
    }

    @Override
    public void onDestroy() {
        mTabs.clear();
        super.onDestroy();
    }

    public void onViewModeChanged(int mode) {
        mTabs.get(mCurrentTabPosition).updateLayout(mode);
    }
}
