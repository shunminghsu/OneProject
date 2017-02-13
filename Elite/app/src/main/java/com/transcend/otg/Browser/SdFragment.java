package com.transcend.otg.Browser;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.transcend.otg.R;

import java.util.ArrayList;

/**
 * Created by henry_hsu on 2017/2/10.
 */

public class SdFragment extends BrowserFragment {

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
