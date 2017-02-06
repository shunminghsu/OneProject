package com.transcend.otg.Browser;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.transcend.otg.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangbojie on 2017/2/2.
 */

public class BrowserFragment extends Fragment implements MyPagerAdapter.UpdateCurrentTabListener{

    public BrowserFragment() {    }

    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    private int mNumTabs;
    TabInfo mCurTab = null;
    private LayoutInflater mInflater;
    static final int LIST_TYPE_IMAGE = 1;
    static final int LIST_TYPE_VIDEO = 2;
    static final int LIST_TYPE_MUSIC = 3;
    static final int LIST_TYPE_DOCUMENT = 4;
    static final int LIST_TYPE_FOLDER = 5;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private Context mContext;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        mContext = getActivity();

        initTabInfos(savedInstanceState);
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

        mTabLayout.setupWithViewPager(mViewPager);
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            mTabLayout.getTabAt(i).setIcon(mTabs.get(i).IconId);
        }

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return root;
    }

    @Override
    public void updateCurrentTab(int position) {
        TabInfo tab = mTabs.get(position);
        mCurTab = tab;

        // Put things in the correct paused/resumed state.
        //TO-DO
    }

    private void initTabInfos(Bundle savedInstanceState) {
        TabInfo tab = new TabInfo(
                LIST_TYPE_IMAGE, R.drawable.test_icon, savedInstanceState);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_VIDEO, R.drawable.test_icon, savedInstanceState);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_MUSIC, R.drawable.test_icon, savedInstanceState);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_DOCUMENT, R.drawable.test_icon, savedInstanceState);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_FOLDER, R.drawable.test_icon, savedInstanceState);
        mTabs.add(tab);

        mNumTabs = mTabs.size();
    }
}
