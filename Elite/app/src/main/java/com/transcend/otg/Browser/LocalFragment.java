package com.transcend.otg.Browser;

import android.os.Bundle;

import com.transcend.otg.R;


/**
 * Created by henry_hsu on 2017/2/13.
 */

public class LocalFragment extends BrowserFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTabInfos(savedInstanceState);
    }

    private void initTabInfos(Bundle savedInstanceState) {
        TabInfo tab = new TabInfo(
                LIST_TYPE_IMAGE, R.drawable.ic_browser_filetype_image, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_VIDEO, R.drawable.ic_browser_filetype_video, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_MUSIC, R.drawable.ic_browser_filetype_music, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_DOCUMENT, R.drawable.ic_browser_filetype_document, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_ENCRYPTION, R.drawable.ic_browser_filetype_encryption, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_FOLDER, R.drawable.ic_browser_filetype_all, savedInstanceState, mContext);
        mTabs.add(tab);
    }
}
