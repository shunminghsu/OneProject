package com.transcend.otg.Constant;

import android.net.Uri;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;

import com.github.mjdev.libaums.UsbMassStorageDevice;

/**
 * Created by wangbojie on 2017/2/6.
 */

public class Constant {
    public static enum MODE {
        LOCAL, SD, OTG, ARRANGE, DROPBOX, GOOGLEDRIVE, SSD
    }
    public static MODE nowMODE;
    public static DocumentFile pickedDir, rootDir;
    public static Uri rootUri;
    public static final String ROOT_LOCAL = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static final int ITEM_LIST = 0;
    public static final int ITEM_GRID = 1;
    public static final int ITEM_FOOTER = 2;

    public static final int SORT_BY_DATE = 0;
    public static final int SORT_BY_NAME = 1;
    public static final int SORT_BY_SIZE = 2;
    public static final int SORT_ORDER_AS = 0;
    public static final int SORT_ORDER_DES = 1;
}
