package com.transcend.otg.Constant;

import android.net.Uri;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;
import android.support.v7.view.ActionMode;

import com.github.mjdev.libaums.UsbMassStorageDevice;

import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/2/6.
 */

public class Constant {
    public static enum MODE {
        LOCAL, SD, OTG, ARRANGE, DROPBOX, GOOGLEDRIVE, SSD
    }
    public static MODE nowMODE;
    public static DocumentFile mCurrentDocumentFile, mRootDocumentFile, mSDCurrentDocumentFile, mSDRootDocumentFile;
    public static Uri rootUri;
    public static final String ROOT_LOCAL = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String ROOT_CACHE;
    public static ActionMode mActionMode;
    public static FileInfo mCurrentFile;
    public static DocumentFile mCurrentDocumentFileExplore, mCurrentDocumentFileDestination;

    public static final int ITEM_LIST = 0;
    public static final int ITEM_GRID = 1;
    public static final int ITEM_FOOTER = 2;

    public static final int SORT_BY_DATE = 0;
    public static final int SORT_BY_NAME = 1;
    public static final int SORT_BY_SIZE = 2;
    public static final int SORT_ORDER_AS = 0;
    public static final int SORT_ORDER_DES = 1;

    public static final String sd_key_path = "sd";
    public static final String otg_key_path = "usb";

    public static final int TYPE_DIR = 0;
    public static final int TYPE_PHOTO = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_MUSIC = 3;
    public static final int TYPE_DOC = 4;
    public static final int TYPE_ENCRYPT = 5;
    public static final int TYPE_OTHER_FILE = 6;

    public static final int STORAGEMODE_LOCAL = 0;
    public static final int STORAGEMODE_SD = 1;
    public static final int STORAGEMODE_OTG = 2;

    public static int Activity = 0;//Main = 0, FolderExplore = 1, Destination = 2
    public static ArrayList<DocumentFile> destinationDFile;
    public static final int SECURITY_DEVICE_EMPTY = -1;
    public static final int SECURITY_DISABLE = 0;
    public static final int SECURITY_LOCK = 1;
    public static final int SECURITY_UNLOCK = 2;
}
