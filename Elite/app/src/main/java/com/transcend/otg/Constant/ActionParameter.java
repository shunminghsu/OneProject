package com.transcend.otg.Constant;

import android.support.v4.provider.DocumentFile;

import com.transcend.otg.Loader.FileActionService;

import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/3/9.
 */

public class ActionParameter {
    public static FileActionService.FileAction mode = null;
    public static String name = null;
    public static String path = null;
    public static ArrayList<String> paths = null;
    public static ArrayList<FileInfo> files = null;
    public static ArrayList<DocumentFile> dFiles = null;
}
