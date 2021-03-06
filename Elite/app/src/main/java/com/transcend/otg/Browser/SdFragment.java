package com.transcend.otg.Browser;


import android.os.Bundle;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.R;
import com.transcend.otg.Utils.FileFactory;


/**
 * Created by henry_hsu on 2017/2/10.
 */

public class SdFragment extends BrowserFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOuterStoragePath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
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
