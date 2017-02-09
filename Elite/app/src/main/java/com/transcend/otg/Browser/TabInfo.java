package com.transcend.otg.Browser;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.R;

import java.util.ArrayList;

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

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mRecyclerAdapter;
    private Context mContext;
    private ArrayList<FileInfo> mFileList;
    private static final int GRID_PORTRAIT = 3;
    private static final int GRID_LANDSCAPE = 5;

    public TabInfo(int type, int icon_id, Bundle savedInstanceState, Context context, ArrayList<FileInfo> fileList) {
        mType = type;
        IconId = icon_id;
        mSavedInstanceState = savedInstanceState;
        mContext = context;
        mFileList = fileList;
    }

    public View build(LayoutInflater inflater) {
        if (mRootView != null) {
            return mRootView;
        }

        mInflater = inflater;

        mRecyclerAdapter = new RecyclerViewAdapter(mFileList);
        mRootView = inflater.inflate(R.layout.pager_layout, null);

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        updateListView(false);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mLoadingContainer = mRootView.findViewById(R.id.loading_container);
        mLoadingContainer.setVisibility(View.GONE);
        mListContainer = mRootView.findViewById(R.id.list_container);
        if (mListContainer != null) {

            //lv.setOnItemClickListener(this);
            //lv.setSaveEnabled(true);
            //lv.setItemsCanFocus(true);
            //lv.setTextFilterEnabled(true);
        }
        return mRootView;
    }

    private void updateListView(boolean update) {
        LinearLayoutManager list = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(list);
        if (update) {
            mRecyclerView.getRecycledViewPool().clear();
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    private void updateGridView(boolean update) {
        int orientation = mContext.getResources().getConfiguration().orientation;
        int spanCount = (orientation == Configuration.ORIENTATION_PORTRAIT)
                ? GRID_PORTRAIT : GRID_LANDSCAPE;
        GridLayoutManager grid = new GridLayoutManager(mContext, spanCount);
        grid.setSpanSizeLookup(new SpanSizeLookup(grid.getSpanCount()));
        mRecyclerView.setLayoutManager(grid);
        if (update) {
            mRecyclerView.getRecycledViewPool().clear();
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    public void detachView() {
        if (mRootView != null) {
            ViewGroup group = (ViewGroup)mRootView.getParent();
            if (group != null) {
                group.removeView(mRootView);
            }
        }
    }

    private class SpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

        private int spanSize;

        public SpanSizeLookup(int spanCount) {
            spanSize = spanCount;
        }

        @Override
        public int getSpanSize(int position) {
            return mRecyclerAdapter.isFooter(position) ? spanSize : 1;
        }
    }
}