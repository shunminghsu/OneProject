package com.transcend.otg.Constant;

import android.os.Environment;

import com.github.mjdev.libaums.UsbMassStorageDevice;

/**
 * Created by wangbojie on 2017/2/6.
 */

public class Constant {
    public static enum MODE {
        LOCAL, SD, OTG, ARRANGE, DROPBOX, GOOGLEDRIVE, SSD
    }
    public static MODE nowMODE;
    public static UsbMassStorageDevice nowDevice;
    public static final String ROOT_LOCAL = Environment.getExternalStorageDirectory().getAbsolutePath();
}
