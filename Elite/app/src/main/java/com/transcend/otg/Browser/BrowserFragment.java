package com.transcend.otg.Browser;

import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Loader.TabInfoLoader;
import com.transcend.otg.R;

import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/2/2.
 */

public class BrowserFragment extends Fragment {

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

    private PagerSwipeRefreshLayout mSwipeRefreshLayout;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    protected Context mContext;
    protected LoaderManager.LoaderCallbacks<ArrayList<FileInfo>> mCallbacks;
    protected String mOuterStoragePath = null;
    protected boolean mIsOtg = false;


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
        mCallbacks = new LoaderManager.LoaderCallbacks<ArrayList<FileInfo>>() {
            @Override
            public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
                return new TabInfoLoader(context, mCurrentTabPosition, mOuterStoragePath, mIsOtg);
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
                mTabs.get(mCurrentTabPosition).getAdapter().update(data);
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {
                //mAdapter.update(null);
            }
        };
        mInflater = inflater;
        CoordinatorLayout root = (CoordinatorLayout) inflater.inflate(R.layout.fragment_browser, container, false);

        mSwipeRefreshLayout = (PagerSwipeRefreshLayout) root.findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        mViewPager = (ViewPager) root.findViewById(R.id.viewPager);
        mTabLayout = (TabLayout) root.findViewById(R.id.tabLayout);

        MyPagerAdapter adapter = new MyPagerAdapter(mTabs, mInflater);
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Constant.mActionMode != null)
                    Constant.mActionMode.finish();
                getLoaderManager().restartLoader(TAB_LOADER_ID, getArguments(), mCallbacks);
            }
        });
    }

    public int getItemsCount(){
       return mTabs.get(mCurrentTabPosition).getItemsCount();
    }

    public boolean getSelectedAllorNot(){
        return mTabs.get(mCurrentTabPosition).getSelectedAllorNot();
    }

    public void clearAllSelect(){
        mTabs.get(mCurrentTabPosition).clearAll();
    }

    public void selectAll(){
        mTabs.get(mCurrentTabPosition).selectAllFile();
    }

    public ArrayList<FileInfo> getSelectedFiles(){
        return mTabs.get(mCurrentTabPosition).getSelectedFile();
    }

    public ArrayList<FileInfo> getAllFiles(){
        return mTabs.get(mCurrentTabPosition).getAllFiles();
    }

    public void updateCurrentTab(int position) {
        boolean needLoad = (mCurrentTabPosition != position);
        //fix bugs: do twice loader when switch storage type
        //In some case, ex: switch from Local to SD, onStart() will be called, and
        //then do restartLoader, so we don't need to do restartLoader again
        TabInfo tab = mTabs.get(position);
        mCurTab = tab;
        mCurrentTabPosition = position;
        if(needLoad)
            getLoaderManager().restartLoader(TAB_LOADER_ID, getArguments(), mCallbacks);
        // Put things in the correct paused/resumed state.
        //TO-DO
    }

    public int getCurrentTabPosition(){
        return mCurrentTabPosition;
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

    public void onViewModeChanged(int mode) {
        mTabs.get(mCurrentTabPosition).updateLayout(mode);
    }

    public void restartLoaderforCurrentTab() {
        getLoaderManager().restartLoader(TAB_LOADER_ID, getArguments(), mCallbacks);
    }

    class MyPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
        private ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
        private LayoutInflater mInflater;


        MyPagerAdapter(ArrayList<TabInfo> tabs, LayoutInflater inflater) {
            mTabs = tabs;
            mInflater = inflater;
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            TabInfo tab = mTabs.get(position);
            View root = tab.build(mInflater);
            container.addView(root);

            return root;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            if(Constant.mActionMode != null)
               Constant.mActionMode.finish();
            updateCurrentTab(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            enableDisableSwipeRefresh( state == ViewPager.SCROLL_STATE_IDLE );
        }
    }

    private void enableDisableSwipeRefresh(boolean enable) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setEnabled(enable);
        }
    }

}
