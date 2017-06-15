package com.transcend.otg.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class LocalPreferences {
    private static final String ELITE_PREFIX = "elite2-";
    private static final String BROWSER_MODE_PREFIX = "browserMode-";
    public static final String BROWSER_SORT_PREFIX = "browserSort-";
    public static final String BROWSER_SORT_ORDER_PREFIX = "browserSortOrder-";
    private static final String OTG_KEY = "otgKey-";
    private static final String SD_KEY = "sdKey-";
    private static final String HAS_SD = "hasSD-";

    public static int getBrowserViewMode(Context context, int type, int default_value) {
        String key = BROWSER_MODE_PREFIX + type;
        return getPrefs(context).getInt(createKey(key), default_value);
    }

    public static void setBrowserViewMode(Context context, int type, int viewMode) {
        String key = BROWSER_MODE_PREFIX + type;
        getPrefs(context).edit().putInt(createKey(key), viewMode).apply();
    }

    public static int getPref(Context context, String key, int default_value) {
        return getPrefs(context).getInt(createKey(key), default_value);
    }

    public static void setPref(Context context, String key, int value) {
        getPrefs(context).edit().putInt(createKey(key), value).apply();
    }

    public static String getOTGKey(Context context, String serialNumber) {
        String key = OTG_KEY + serialNumber;
        return getPrefs(context).getString(createKey(key), "");
    }

    public static void setOTGKey(Context context, String serialNumber, String uri) {
        String key = OTG_KEY + serialNumber;
        getPrefs(context).edit().putString(createKey(key), uri).apply();
    }

    public static String getSDKey(Context context, String uid) {
        String key = SD_KEY + uid;
        return getPrefs(context).getString(createKey(key), "");
    }

    public static void setSDKey(Context context, String uid, String uri) {
        String key = SD_KEY + uid;
        getPrefs(context).edit().putString(createKey(key), uri).apply();
    }

    public static void removeSDKey(Context context, String uid) {
        String key = SD_KEY + uid;
        getPrefs(context).edit().remove(createKey(key)).apply();
    }

    public static void setHasSD(Context context, boolean hasSD) {
        String key = HAS_SD;
        getPrefs(context).edit().putBoolean(createKey(key), hasSD).apply();
    }

    public static boolean getHasSD(Context context) {
        String key = HAS_SD;
        return getPrefs(context).getBoolean(createKey(key), false);
    }

    private static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static String createKey(String key) {
        return ELITE_PREFIX + key;
    }

}
