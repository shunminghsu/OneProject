package com.transcend.otg;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class LocalPreferences {
    private static final String ELITE_PREFIX = "elite2-";
    private static final String BROWSER_MODE_PREFIX = "browserMode-";
    private static final String OTG_KEY = "otgKey-";

    public static int getBrowserViewMode(Context context, int type, int default_value) {
        String key = BROWSER_MODE_PREFIX + type;
        return getPrefs(context).getInt(createKey(key), default_value);
    }

    public static void setBrowserViewMode(Context context, int type, int viewMode) {
        String key = BROWSER_MODE_PREFIX + type;
        getPrefs(context).edit().putInt(createKey(key), viewMode).apply();
    }

    public static String getOTGKey(Context context, String serialNumber) {
        String key = OTG_KEY + serialNumber;
        return getPrefs(context).getString(createKey(key), "");
    }

    public static void setOTGKey(Context context, String serialNumber, String uri) {
        String key = OTG_KEY + serialNumber;
        getPrefs(context).edit().putString(createKey(key), uri).apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static String createKey(String key) {
        return ELITE_PREFIX + key;
    }

}
