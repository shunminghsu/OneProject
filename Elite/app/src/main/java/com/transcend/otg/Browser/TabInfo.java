package com.transcend.otg.Browser;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transcend.otg.Bitmap.IconHelper;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.LocalPreferences;
import com.transcend.otg.MainActivity;
import com.transcend.otg.R;

/**
 * Created by henry_hsu on 2017/2/3.
 */

public class TabInfo {
    public View mRootView;
    public LayoutInflater mInflater;
    public final int mType;
    public int IconId;
    private View mEmpty;
    private View mLoadingContainer;
    private View mListContainer;
    private ViewGroup mPinnedHeader;
    private final Bundle mSavedInstanceState;

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mRecyclerAdapter;
    private Context mContext;
    private GridLayoutManager mLayout;
    private int mColumnCount = 1;  // This will get updated when layout changes.
    public int mMode;

    IconHelper mIconHelper;

    public TabInfo(int type, int icon_id, Bundle savedInstanceState, Context context) {
        mType = type;
        IconId = icon_id;
        mSavedInstanceState = savedInstanceState;
        mContext = context;
        mMode = LocalPreferences.getBrowserViewMode(mContext, mType, Constant.ITEM_LIST);
    }

    public View build(LayoutInflater inflater) {
        if (mRootView != null) {
            return mRootView;
        }

        mInflater = inflater;

        mIconHelper = new IconHelper(mContext, 0);
        mRecyclerAdapter = new RecyclerViewAdapter(this, mIconHelper);
        mRootView = inflater.inflate(R.layout.pager_layout, null);

        mEmpty = mRootView.findViewById(R.id.empty_view);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mLayout = new GridLayoutManager(mContext, mColumnCount);
        mLayout.setSpanSizeLookup(new SpanSizeLookup(mLayout.getSpanCount()));

        mColumnCount = calculateColumnCount(mMode);
        mLayout.setSpanCount(mColumnCount);
        mRecyclerView.setLayoutManager(mLayout);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mLoadingContainer = mRootView.findViewById(R.id.loading_container);
        mListContainer = mRootView.findViewById(R.id.list_container);
        if (mListContainer != null) {

            //lv.setOnItemClickListener(this);
            //lv.setSaveEnabled(true);
            //lv.setItemsCanFocus(true);
            //lv.setTextFilterEnabled(true);
        }
        return mRootView;
    }

    public void updateLayout(int mode) {
        mMode = mode;
        mColumnCount = calculateColumnCount(mode);
        if (mLayout != null) {
            mLayout.setSpanCount(mColumnCount);
        }

        mRecyclerView.requestLayout();
        //mRecyclerView.setAdapter(mRecyclerAdapter);
        //mSelectionManager.handleLayoutChanged();  // RecyclerView doesn't do this for us
        //mIconHelper.setViewMode(mode);
        LocalPreferences.setBrowserViewMode(mContext, mType, mode);
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

    public RecyclerViewAdapter getAdapter() {
        return mRecyclerAdapter;
    }

    public void showLoadingResult(boolean empty) {
        mLoadingContainer.setVisibility(View.GONE);
        if (empty) {
            mListContainer.setVisibility(View.GONE);
            mEmpty.setVisibility(View.VISIBLE);
        } else {
            mListContainer.setVisibility(View.VISIBLE);
            mEmpty.setVisibility(View.GONE);
        }
    }

    private int calculateColumnCount(int mode) {
        if (mode == Constant.ITEM_LIST) {
            // List mode is a "grid" with 1 column.
            return 1;
        }

        int cellWidth = mContext.getResources().getDimensionPixelSize(R.dimen.grid_width);
        //int cellMargin = 2 * mContext.getResources().getDimensionPixelSize(R.dimen.grid_item_margin);
        //int viewPadding = mRecView.getPaddingLeft() + mRecView.getPaddingRight();
        int viewPadding = 0;
        int cellMargin = 0;

        // RecyclerView sometimes gets a width of 0 (see b/27150284).  Clamp so that we always lay
        // out the grid with at least 2 columns.
        int columnCount = Math.max(2,
                (MainActivity.mScreenW - viewPadding) / (cellWidth + cellMargin));

        return columnCount;
    }
}