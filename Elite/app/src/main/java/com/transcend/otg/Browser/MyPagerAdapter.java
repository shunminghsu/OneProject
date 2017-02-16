package com.transcend.otg.Browser;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by henry_hsu on 2017/2/3.
 */


class MyPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
    private ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    private LayoutInflater mInflater;

    interface UpdateCurrentTabListener {
        void updateCurrentTab(int cur_pos);
    }
    private UpdateCurrentTabListener mListener;

    MyPagerAdapter(ArrayList<TabInfo> tabs, LayoutInflater inflater, UpdateCurrentTabListener listener) {
        mTabs = tabs;
        mInflater = inflater;
        mListener = listener;
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
        //root.setTag(R.id.name, tab);
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
        mListener.updateCurrentTab(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            //mListener.updateCurrentTab(mCurPos);
        }
    }
}