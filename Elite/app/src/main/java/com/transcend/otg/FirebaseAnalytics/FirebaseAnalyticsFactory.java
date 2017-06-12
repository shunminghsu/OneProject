package com.transcend.otg.FirebaseAnalytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.transcend.otg.MainApplication;

/**
 * Created by wangbojie on 2017/5/17.
 */

public class FirebaseAnalyticsFactory {
    private static final String TAG = FirebaseAnalyticsFactory.class.getSimpleName();
    private static FirebaseAnalyticsFactory mFirebaseAnalyticsFactory;
    private static final Object mMute = new Object();
    private static final boolean enableAnalysis = true;
    private FirebaseAnalytics mFirebaseAnalytics;


    public static class FRAGMENT {
        public static String HOME = "Home";
        public static String BROWSER = "Browser";
        public static String BROWSER_LOCAL = "BrLocal";
        public static String BROWSER_SD = "BrSD";
        public static String BROWSER_NO_SD = "BrNoSD";
        public static String BROWSER_NO_OTG = "BrNoOTG";
        public static String BROWSER_OTG = "BrOTG";
        public static String BACKUP = "Backup";
        public static String SETTINGS = "Setting";
        public static String HELP = "Help";
        public static String FEEDBACK = "Feedback";
        public static String SECURITY = "Security";
        public static String BACKUP_SD = "BackupSD";
        public static String BACKUP_OTG = "BackupOTG";
        public static String FOLDEREXPLORE = "FExp";
        public static String FOLDEREXPLORE_LOCAL = "FExpLocal";
        public static String FOLDEREXPLORE_SD = "FExpSD";
        public static String FOLDEREXPLORE_OTG = "FExpOTG";
        public static String PHOTOACTIIVTY = "Photo";
    }

    public static class EVENT {
        public static String SEARCH = "Search";
        public static String CHANGEVIEW_GRID = "CViewGrid";
        public static String CHANGEVIEW_LIST = "CViewList";
        public static String SORT_DATE = "SortDate";
        public static String SORT_NAME = "SortName";
        public static String SORT_SIZE = "SortSize";
        public static String NEW_FOLDER = "NewFolder";
        public static String RENAME = "BRename";
        public static String SHARE = "BShare";
        public static String COPY_MOVE = "BCopyMove";
        public static String ENCRYPT = "BEncrypt";
        public static String DELETE = "BDelete";
        public static String DECRYPT = "BDecrypt";
        public static String INFO = "BInfo";
        public static String BACKUP = "Backup";
        public static String FEEDBACK = "Feedback";
        public static String SECURITY_LOGIN = "SecuLogin";
        public static String SECURITY_REMOVE = "SecuRemove";
        public static String SECURITY_CHANGE = "SecuChange";
    }

    public FirebaseAnalyticsFactory(Activity activity) {
        mFirebaseAnalytics = ((MainApplication) activity.getApplication()).getDefaultAnalytics();
    }

    public static FirebaseAnalyticsFactory getInstance(Context context) {
        synchronized (mMute) {
            if (mFirebaseAnalyticsFactory == null)
                mFirebaseAnalyticsFactory = new FirebaseAnalyticsFactory((Activity) context);
        }
        return mFirebaseAnalyticsFactory;
    }

    public static FirebaseAnalyticsFactory getInstance(Activity activity) {
        synchronized (mMute) {
            if (mFirebaseAnalyticsFactory == null)
                mFirebaseAnalyticsFactory = new FirebaseAnalyticsFactory(activity);
        }
        return mFirebaseAnalyticsFactory;
    }

    public void sendFragment(Activity activity, String fragment) {
        if (!enableAnalysis || mFirebaseAnalytics == null)
            return;
        mFirebaseAnalytics.setCurrentScreen(activity, fragment, fragment);
    }

    public void sendEvent(String fragment, String action) {
        if (!enableAnalysis || mFirebaseAnalytics == null)
            return;
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, action);
        bundle.putString(FirebaseAnalytics.Param.CONTENT, action);
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, fragment);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, fragment + "+" + action);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}
