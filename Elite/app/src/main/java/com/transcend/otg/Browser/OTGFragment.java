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
        initTabInfos(savedInstanceState);
    }

    private void initTabInfos(Bundle savedInstanceState) {
        TabInfo tab = new TabInfo(
                LIST_TYPE_IMAGE, R.drawable.ic_menu_manage, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_VIDEO, R.drawable.ic_menu_manage, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_MUSIC, R.drawable.ic_menu_manage, savedInstanceState, mContext);
        mTabs.add(tab);

        tab = new TabInfo(
                LIST_TYPE_DOCUMENT, R.drawable.ic_menu_manage, savedInstanceState, mContext);
        mTabs.add(tab);
    }
}
