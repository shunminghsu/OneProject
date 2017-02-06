package com.transcend.otg.Browser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.transcend.otg.R;

/**
 * Created by henry_hsu on 2017/2/3.
 */

public class TabInfo {
    public View mRootView;
    public LayoutInflater mInflater;
    public final int mType;
    public int IconId;
    private View mLoadingContainer;
    private View mListContainer;
    private ViewGroup mPinnedHeader;
    private final Bundle mSavedInstanceState;

    public TabInfo(int type, int icon_id, Bundle savedInstanceState) {
        mType = type;
        IconId = icon_id;
        mSavedInstanceState = savedInstanceState;
    }

    public View build(LayoutInflater inflater) {
        if (mRootView != null) {
            return mRootView;
        }

        mInflater = inflater;

        mRootView = inflater.inflate(R.layout.pager_child_layout_1, null);

        mLoadingContainer = mRootView.findViewById(R.id.loading_container);
        mLoadingContainer.setVisibility(View.VISIBLE);
        mListContainer = mRootView.findViewById(R.id.list_container);
        if (mListContainer != null) {
            View emptyView = mListContainer.findViewById(R.id.empty);
            ListView lv = (ListView) mListContainer.findViewById(R.id.list);
            if (emptyView != null) {
                lv.setEmptyView(emptyView);
            }
            //lv.setOnItemClickListener(this);
            //lv.setSaveEnabled(true);
            //lv.setItemsCanFocus(true);
            //lv.setTextFilterEnabled(true);
        }
        return mRootView;
    }

    public void detachView() {
        if (mRootView != null) {
            ViewGroup group = (ViewGroup)mRootView.getParent();
            if (group != null) {
                group.removeView(mRootView);
            }
        }
    }
}