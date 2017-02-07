package com.transcend.otg.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wangbojie on 2017/2/6.
 */

public class PrefUtils {
    public static void write(Context context, String name, String key, boolean b) {
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean(key, b);
        ed.apply();
    }

    public static void write(Context context, String name, String key, int id) {
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt(key, id);
        ed.apply();
    }

    public static void write(Context context, String name, String key, String data) {
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(key, data);
        ed.apply();
    }

    public static boolean read(Context context, String name, String key, boolean def) {
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return sp.getBoolean(key, def);
    }

    public static int read(Context context, String name, String key, int def) {
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return sp.getInt(key, def);
    }

    public static String read(Context context, String name, String key, String def) {
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return sp.getString(key, def);
    }

    public static void clear(Context context, String name) {
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.clear().apply();
    }
}
