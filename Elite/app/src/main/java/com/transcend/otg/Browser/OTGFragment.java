package com.transcend.otg.Browser;

import android.os.Bundle;

import com.transcend.otg.R;


/**
 * Created by wangbojie on 2017/2/13.
 */

public class OTGFragment extends BrowserFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSDCardPath = null;
        mIsOtg = true;
        initTabInfos(savedInstanceState);
    }

    private void initTabInfos(Bundle savedInstanceState) {
        TabInfo tab = new TabInfo(
                LIST_TYPE_IMAGE, R.mipmap.test_icon, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_VIDEO, R.mipmap.test_icon, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_MUSIC, R.mipmap.test_icon, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_DOCUMENT, R.mipmap.test_icon, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_ENCRYPTION, R.mipmap.test_icon, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_FOLDER, R.mipmap.test_icon, savedInstanceState, mContext);
        mTabs.add(tab);
    }
}
