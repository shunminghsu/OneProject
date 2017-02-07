package com.transcend.otg.Constant;

import android.os.Environment;

/**
 * Created by wangbojie on 2017/2/6.
 */

public class Constant {
    public static enum MODE {
        LOCAL, OTG, ARRANGE, DROPBOX, GOOGLEDRIVE, SSD
    }

    public static final String ROOT_LOCAL = Environment.getExternalStorageDirectory().getAbsolutePath();
}
