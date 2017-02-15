package com.transcend.otg.Browser;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Loader.TabInfoLoader;
import com.transcend.otg.MainActivity;
import com.transcend.otg.R;

import java.util.ArrayList;

/**
 * Created by henry_hsu on 2017/2/13.
 */

public class LocalFragment extends BrowserFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOuterStorage = null;
        initTabInfos(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Context context = inflater.getContext();
        mCallbacks = new LoaderManager.LoaderCallbacks<ArrayList<FileInfo>>() {
            @Override
            public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
                return new TabInfoLoader(context, mCurrentTabPosition, mOuterStorage);
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
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void initTabInfos(Bundle savedInstanceState) {
        TabInfo tab = new TabInfo(
                LIST_TYPE_IMAGE, R.drawable.test_icon, savedInstanceState, mContext, MainActivity.MODE_LIST);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_VIDEO, R.drawable.test_icon, savedInstanceState, mContext, MainActivity.MODE_LIST);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_MUSIC, R.drawable.test_icon, savedInstanceState, mContext, MainActivity.MODE_LIST);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_DOCUMENT, R.drawable.test_icon, savedInstanceState, mContext, MainActivity.MODE_LIST);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_FOLDER, R.drawable.test_icon, savedInstanceState, mContext, MainActivity.MODE_LIST);
        mTabs.add(tab);
    }
}
