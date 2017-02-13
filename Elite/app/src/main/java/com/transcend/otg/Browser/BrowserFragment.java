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
import android.view.View;
import android.view.ViewGroup;

import com.transcend.otg.Constant.FileInfo;
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
    protected TabInfo mCurTab = null;
    private LayoutInflater mInflater;
    public static final int LIST_TYPE_IMAGE = 0;
    public static final int LIST_TYPE_VIDEO = 1;
    public static final int LIST_TYPE_MUSIC = 2;
    public static final int LIST_TYPE_DOCUMENT = 3;
    static final int LIST_TYPE_FOLDER = 5;
    private int mCurrentTabPosition = LIST_TYPE_IMAGE;
    private int TAB_LOADER_ID = 168;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    protected Context mContext;
    private LoaderManager.LoaderCallbacks<ArrayList<FileInfo>> mCallbacks;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Context context = inflater.getContext();
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

        mCallbacks = new LoaderManager.LoaderCallbacks<ArrayList<FileInfo>>() {
            @Override
            public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
                Log.d("henry","onCreateLoader "+id);
                ///Loader<Boolean> loader = mFileActionManager.onCreateLoader(id, args);
                return new TabInfoLoader(context, mCurrentTabPosition);
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
                mTabs.get(mCurrentTabPosition).getAdapter().update(data);
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {
                //mAdapter.update(null);
            }
        };
        return root;
    }

    @Override
    public void updateCurrentTab(int position) {
        Log.d("henry","updateCurrentTab "+position);
        TabInfo tab = mTabs.get(position);
        mCurTab = tab;
        mCurrentTabPosition = position;
        getLoaderManager().restartLoader(TAB_LOADER_ID, getArguments(), mCallbacks);
        // Put things in the correct paused/resumed state.
        //TO-DO
    }

    @Override
    public void onStart() {
        super.onStart();
        getLoaderManager().restartLoader(TAB_LOADER_ID, getArguments(), mCallbacks);
    }

    @Override
    public void onStop() {
        super.onStop();
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



}