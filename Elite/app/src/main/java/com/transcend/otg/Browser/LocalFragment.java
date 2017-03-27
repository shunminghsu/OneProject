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
                LIST_TYPE_IMAGE, R.mipmap.ic_browser_filetype_image_gray, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_VIDEO, R.mipmap.ic_browser_filetype_video_gray, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_MUSIC, R.mipmap.ic_browser_filetype_music_gray, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_DOCUMENT, R.mipmap.ic_browser_filetype_document_gray, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_ENCRYPTION, R.mipmap.ic_browser_filetype_encryption_gray, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_FOLDER, R.mipmap.ic_browser_filetype_all_gray, savedInstanceState, mContext);
        mTabs.add(tab);
    }
}
