package com.transcend.otg.Browser;

import android.content.Context;
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
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.LocalPreferences;
import com.transcend.otg.MainActivity;
import com.transcend.otg.R;
import com.transcend.otg.Utils.MediaUtils;


/**
 * Created by henry_hsu on 2017/2/3.
 */

public class TabInfo implements RecyclerViewAdapter.OnRecyclerItemCallbackListener, RecyclerViewAdapter.OnActionModeItemCallbackListener {
    private String TAG = TabInfo.class.getSimpleName();
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
    private IconHelper mIconHelper;
    private OnItemCallbackListener mCallback;

    @Override
    public void onItemClick(int count) {
        mCallback.onItemClick(count);
    }

    @Override
    public void onItemLongClick(int count) {
        mCallback.onItemLongClick(count);
    }

    public interface OnItemCallbackListener {
        void onItemClick(FileInfo file);
        void onItemClick(int count);
        void onItemLongClick(int count);
    }


    public TabInfo(int type, int icon_id, Bundle savedInstanceState, Context context) {
        mType = type;
        IconId = icon_id;
        mSavedInstanceState = savedInstanceState;
        mContext = context;
        mMode = LocalPreferences.getBrowserViewMode(mContext, mType, Constant.ITEM_LIST);
        mCallback = (OnItemCallbackListener) context;
    }

    public View build(LayoutInflater inflater) {
        if (mRootView != null) {
            return mRootView;
        }

        mInflater = inflater;

        mIconHelper = new IconHelper(mContext, mMode);
        mRecyclerAdapter = new RecyclerViewAdapter(this, mIconHelper, mContext);
        mRootView = inflater.inflate(R.layout.pager_layout, null);
        mEmpty = mRootView.findViewById(R.id.empty_view);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);

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
        mIconHelper.setViewMode(mode);
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

    @Override
    public void onRecyclerItemClick(FileInfo file) {
        if(mType == 0){//photo
            Log.d(TAG, "it is image, go to viewer");
        }else if(mType == 4){//encrypted file

        }else if(mType == 5){
            mCallback.onItemClick(file);
        }else{//video / music / document
            if(Constant.nowMODE == Constant.MODE.OTG){
                MediaUtils.executeUri(mContext, file.uri.toString(), mContext.getResources().getString(R.string.openin_title));
            }else{
                MediaUtils.execute(mContext, file.path, mContext.getResources().getString(R.string.openin_title));
            }
        }
    }

    @Override
    public void onRecyclerItemLongClick(FileInfo file) {

    }

    @Override
    public void onRecyclerItemInfoClick(String path) {

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

    public void selectAllFile(){
        mRecyclerAdapter.setAllSelection();
    }

    public void clearAll(){
        mRecyclerAdapter.clearAllSelection();
    }

    public int getItemsCount(){
        return mRecyclerAdapter.getItemCount();
    }

    public boolean getSelectedAllorNot(){
        return mRecyclerAdapter.getSelectedAllorNot();
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