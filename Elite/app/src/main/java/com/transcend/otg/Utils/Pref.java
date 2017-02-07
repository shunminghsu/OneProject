package com.transcend.otg.Utils;

import android.content.Context;
import android.os.Environment;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.R;

/**
 * Created by wangbojie on 2017/2/6.
 */

public class Pref {
    public enum Sort {
        TYPE,
        DATE,
        REVERSEDATE,
        NAME,
        REVERSENAME
    }

    public static void setFileSortType(Context context, Sort sort) {
        String name = context.getResources().getString(R.string.pref_name);
        String key = context.getResources().getString(R.string.pref_file_sort_type);
        PrefUtils.write(context, name, key, sort.ordinal());
    }

    public static Sort getFileSortType(Context context) {
        String name = context.getResources().getString(R.string.pref_name);
        String key = context.getResources().getString(R.string.pref_file_sort_type);
        int def = Sort.TYPE.ordinal();
        return Sort.values()[PrefUtils.read(context, name, key, def)];
    }

    public static String getMainPageLocation(Context context) {
        String name = context.getResources().getString(R.string.pref_name);
        String key = context.getResources().getString(R.string.pref_mainpage_location);
        String def = getDefaultMainPageLocation(context);
        return PrefUtils.read(context, name, key, def);
    }

    private static String getDefaultMainPageLocation(Context context) {
        StringBuffer buf = new StringBuffer();
        buf.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        return buf.toString();
    }

//    public static String getSDLocation(Context context) {
//        String name = context.getResources().getString(R.string.pref_name);
//        String key = context.getResources().getString(R.string.pref_sd_location);
//        String def = getDefaultSDLocation(context);
//        return PrefUtils.read(context, name, key, def);
//    }
//
//    private static String getDefaultSDLocation(Context context) {
//        StringBuffer buf = new StringBuffer();
//        buf.append(Constant.ROOT_SD);
//        return buf.toString();
//    }

//    public static String getCameraLocation(Context context) {
//        String name = context.getResources().getString(R.string.pref_name);
//        String key = context.getResources().getString(R.string.pref_camera_location);
//        String def = getDefaultCameraLocation(context);
//        return PrefUtils.read(context, name, key, def);
//    }

//    private static String getDefaultCameraLocation(Context context) {
//        StringBuffer buf = new StringBuffer();
//        buf.append(Constant.ROOT_CAMERA);
//        return buf.toString();
//    }
//
//    public static String getDownloadLocation(Context context) {
//        String name = context.getResources().getString(R.string.pref_name);
//        String key = context.getResources().getString(R.string.pref_download_location);
//        String def = getDefaultDownloadLocation(context);
//        return PrefUtils.read(context, name, key, def);
//    }
//
//    private static String getDefaultDownloadLocation(Context context) {
//        StringBuffer buf = new StringBuffer();
//        buf.append(Constant.ROOT_DOWNLOAD);
//        return buf.toString();
//    }
//
//    public static String getPicturesLocation(Context context) {
//        String name = context.getResources().getString(R.string.pref_name);
//        String key = context.getResources().getString(R.string.pref_pictures_location);
//        String def = getDefaultPicturesLocation(context);
//        return PrefUtils.read(context, name, key, def);
//    }
//
//    private static String getDefaultPicturesLocation(Context context) {
//        StringBuffer buf = new StringBuffer();
//        buf.append(Constant.ROOT_PICTURES);
//        return buf.toString();
//    }
//
//    public static String getMoviesLocation(Context context) {
//        String name = context.getResources().getString(R.string.pref_name);
//        String key = context.getResources().getString(R.string.pref_movies_location);
//        String def = getDefaultMoviesLocation(context);
//        return PrefUtils.read(context, name, key, def);
//    }
//
//    private static String getDefaultMoviesLocation(Context context) {
//        StringBuffer buf = new StringBuffer();
//        buf.append(Constant.ROOT_MOVIES);
//        return buf.toString();
//    }
//
//    public static String getMusicLocation(Context context) {
//        String name = context.getResources().getString(R.string.pref_name);
//        String key = context.getResources().getString(R.string.pref_musics_location);
//        String def = getDefaultMusicLocation(context);
//        return PrefUtils.read(context, name, key, def);
//    }
//
//    private static String getDefaultMusicLocation(Context context) {
//        StringBuffer buf = new StringBuffer();
//        buf.append(Constant.ROOT_MUSIC);
//        return buf.toString();
//    }

//    public static FileManageRecyclerAdapter.LayoutType getFileViewType(Context context) {
//        String name = context.getResources().getString(R.string.pref_name);
//        String key = context.getResources().getString(R.string.pref_file_view_type);
//        int def = FileManageRecyclerAdapter.LayoutType.LIST.ordinal();
//        return FileManageRecyclerAdapter.LayoutType.values()[PrefUtils.read(context, name, key, def)];
//    }
//
//    public static void setFileViewType(Context context, FileManageRecyclerAdapter.LayoutType type) {
//        String name = context.getResources().getString(R.string.pref_name);
//        String key = context.getResources().getString(R.string.pref_file_view_type);
//        PrefUtils.write(context, name, key, type.ordinal());
//    }
}
