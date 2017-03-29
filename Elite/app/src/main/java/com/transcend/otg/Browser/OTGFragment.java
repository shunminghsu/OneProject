package com.transcend.otg.Browser;

import android.os.Build;
import android.os.Bundle;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.R;
import com.transcend.otg.Utils.FileFactory;

import java.io.File;


/**
 * Created by wangbojie on 2017/2/13.
 */

public class OTGFragment extends BrowserFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOuterStoragePath = FileFactory.getOTGStoragePath(mContext, Constant.otg_key_path);
        mIsOtg = true;
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
