package com.transcend.otg.Browser;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.transcend.otg.Bitmap.IconHelper;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.LocalPreferences;
import com.transcend.otg.MainActivity;
import com.transcend.otg.Photo.PhotoActivity;
import com.transcend.otg.R;
import com.transcend.otg.Task.ComputeFilsNumberTask;
import com.transcend.otg.Task.ComputeFilsTotalSizeTask;
import com.transcend.otg.Utils.MediaUtils;

import java.util.ArrayList;


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
    private int mMode;
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
        setFileTypeEmptyBackground(mType);
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
    public void onRecyclerItemClick(FileInfo file, int position) {
        switch (file.type) {
            case Constant.TYPE_PHOTO:
                startPhotoSingleView(getAdapter().getList(), position);
                break;
            case Constant.TYPE_MUSIC:
            case Constant.TYPE_VIDEO:
            case Constant.TYPE_DOC:
            case Constant.TYPE_OTHER_FILE:
                if (file.storagemode == Constant.STORAGEMODE_OTG)
                    MediaUtils.executeUri(mContext, file.uri.toString(), mContext.getResources().getString(R.string.openin_title));
                else
                    MediaUtils.execute(mContext, file.path, mContext.getResources().getString(R.string.openin_title));
                break;
            case Constant.TYPE_ENCRYPT:
                mCallback.onItemClick(file);
                break;
            case Constant.TYPE_DIR:
                mCallback.onItemClick(file);
                break;
        }
    }

    @Override
    public void onRecyclerItemInfoClick(FileInfo fileInfo) {
        createInfoDialog(mContext, fileInfo, MainActivity.mScreenW);
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

    public ArrayList<FileInfo> getSelectedFile(){
        return mRecyclerAdapter.getSelectedFiles();
    }

    public ArrayList<FileInfo> getAllFiles(){
        return mRecyclerAdapter.getAllFiles();
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

    private void startPhotoSingleView(ArrayList<FileInfo> list, int position) {
        Intent intent = new Intent(mContext, PhotoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int newListPosition = 0;
        ArrayList<FileInfo> photoList = new ArrayList<FileInfo>();
        for (int i=0;i<list.size();i++) {
            if (list.get(i).type == Constant.TYPE_PHOTO)
                photoList.add(list.get(i));
            if (i == position)
                newListPosition = photoList.size() - 1;
        }

        intent.putParcelableArrayListExtra("photo_list", photoList);
        intent.putExtra("list_index", newListPosition);
        mContext.startActivity(intent);
    }

    private void createInfoDialog(Context context, FileInfo fileInfo, int dialog_size) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View mInfoDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_info, null);
        ((TextView) mInfoDialogView.findViewById(R.id.name)).setText(fileInfo.name);
        ((TextView) mInfoDialogView.findViewById(R.id.type)).setText(getFileTypeString(context, fileInfo.type));
        if (fileInfo.format_size == null) {
            ((TextView) mInfoDialogView.findViewById(R.id.size)).setText(Formatter.formatFileSize(context, fileInfo.size));
        } else {
            ((TextView) mInfoDialogView.findViewById(R.id.size)).setText(fileInfo.format_size);
        }
        ((TextView) mInfoDialogView.findViewById(R.id.modify_time)).setText(fileInfo.time);
        ((TextView) mInfoDialogView.findViewById(R.id.path)).setText(fileInfo.path);
        if (fileInfo.type == Constant.TYPE_DIR) {
            mInfoDialogView.findViewById(R.id.file_number_title).setVisibility(View.VISIBLE);
            TextView fileNumView = (TextView) mInfoDialogView.findViewById(R.id.file_number);
            fileNumView.setVisibility(View.VISIBLE);
            fileNumView.setText(context.getResources().getString(R.string.info_file_number_computing));
            new ComputeFilsNumberTask(context, fileInfo, fileNumView).execute();
            new ComputeFilsTotalSizeTask(context, fileInfo, (TextView) mInfoDialogView.findViewById(R.id.size)).execute();
        }

        builder.setView(mInfoDialogView);
        builder.setTitle(context.getResources().getString(R.string.info_title));
        builder.setIcon(R.mipmap.ic_info_gray);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(dialog_size, dialog_size*6/5);
    }

    private String getFileTypeString(Context context, int type) {
        switch (type) {
            case Constant.TYPE_PHOTO:
                return context.getResources().getString(R.string.info_image);
            case Constant.TYPE_MUSIC:
                return context.getResources().getString(R.string.info_music);
            case Constant.TYPE_VIDEO:
                return context.getResources().getString(R.string.info_video);
            case Constant.TYPE_DOC:
                return context.getResources().getString(R.string.info_document);
            case Constant.TYPE_ENCRYPT:
                return context.getResources().getString(R.string.info_enc);
            case Constant.TYPE_DIR:
                return context.getResources().getString(R.string.info_folder);
            default: //Constant.TYPE_OTHER_FILE:
                return context.getResources().getString(R.string.info_other);
        }
    }

    private void setFileTypeEmptyBackground(int type) {
        switch (type) {
            case BrowserFragment.LIST_TYPE_IMAGE:
                mEmpty.setBackgroundResource(R.drawable.img_empty_image);
                break;
            case BrowserFragment.LIST_TYPE_VIDEO:
                mEmpty.setBackgroundResource(R.drawable.img_empty_video);
                break;
            case BrowserFragment.LIST_TYPE_MUSIC:
                mEmpty.setBackgroundResource(R.drawable.img_empty_music);
                break;
            case BrowserFragment.LIST_TYPE_DOCUMENT:
                mEmpty.setBackgroundResource(R.drawable.img_empty_file);
                break;
            case BrowserFragment.LIST_TYPE_ENCRYPTION:
                mEmpty.setBackgroundResource(R.drawable.img_empty_encryption);
                break;
            case BrowserFragment.LIST_TYPE_FOLDER:
                mEmpty.setBackgroundResource(R.drawable.img_empty_directory);
        }
    }
}