package com.transcend.otg.Browser;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Loader.OTGFileListLoader;
import com.transcend.otg.MainActivity;
import com.transcend.otg.R;

import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/2/13.
 */

public class OTGFragment extends BrowserFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTabInfos(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Context context = inflater.getContext();
        mCallbacks = new LoaderManager.LoaderCallbacks<ArrayList<FileInfo>>() {
            @Override
            public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
                return new OTGFileListLoader(context, mCurrentTabPosition);
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
                LIST_TYPE_IMAGE, R.drawable.ic_menu_manage, savedInstanceState, mContext, MainActivity.MODE_LIST);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_VIDEO, R.drawable.ic_menu_manage, savedInstanceState, mContext, MainActivity.MODE_LIST);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_MUSIC, R.drawable.ic_menu_manage, savedInstanceState, mContext, MainActivity.MODE_LIST);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_DOCUMENT, R.drawable.ic_menu_manage, savedInstanceState, mContext, MainActivity.MODE_LIST);
        mTabs.add(tab);
    }
}
