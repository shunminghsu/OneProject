package com.transcend.otg.GoogleAnalytics;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.transcend.otg.MainApplication;

/**
 * Created by wangbojie on 2017/5/17.
 */

public class GoogleAnalyticsFactory {
    private static final String TAG = GoogleAnalyticsFactory.class.getSimpleName();
    private static GoogleAnalyticsFactory mGoogleAnalyticsFactory;
    private static final Object mMute = new Object();
    private static final boolean enableAnalysis = true;
    private Tracker mTracker;

    public static class FRAGMENT {
        public static String HOME = "HomeFragment";
        public static String BROWSER = "BrowserFragment";
        public static String BROWSER_LOCAL = "BrowserLocal";
        public static String BROWSER_SD = "BrowserSD";
        public static String BROWSER_NO_SD = "BrowserNoSD";
        public static String BROWSER_NO_OTG = "BrowserNoOTG";
        public static String BROWSER_OTG = "BrowserOTG";
        public static String BACKUP = "BackupFragment";
        public static String SETTINGS = "SettingFragment";
        public static String HELP = "HelpFragment";
        public static String FEEDBACK = "FeedbackFragment";
        public static String SECURITY = "SecurityFragment";
        public static String BACKUP_SD = "BackuptoSD";
        public static String BACKUP_OTG = "BackuptoOTG";
        public static String FOLDEREXPLORE = "FolderExplore";
        public static String FOLDEREXPLORE_LOCAL = "FolderExploreLocal";
        public static String FOLDEREXPLORE_SD = "FolderExploreSD";
        public static String FOLDEREXPLORE_OTG = "FolderExploreOTG";
        public static String PHOTOACTIIVTY = "PhotoActivity";
    }

    public static class EVENT {
        public static String SEARCH = "Search";
        public static String CHANGEVIEW_GRID = "ChangeViewGrid";
        public static String CHANGEVIEW_LIST = "ChangeViewList";
        public static String SORT_DATE = "SortDate";
        public static String SORT_NAME = "SortName";
        public static String SORT_SIZE = "SortSize";
        public static String NEW_FOLDER = "NewFolder";
        public static String RENAME = "BrowserRename";
        public static String SHARE = "BrowserShare";
        public static String COPY_MOVE = "BrowserCopyorMove";
        public static String ENCRYPT = "BrowserEncrypt";
        public static String DELETE = "BrowserDelete";
        public static String DECRYPT = "BrowserDecrypt";
        public static String INFO = "BrowserFileInfo";
        public static String BACKUP = "Backup";
        public static String FEEDBACK = "Feedback";

    }

    public GoogleAnalyticsFactory(Activity activity) {
        mTracker = ((MainApplication) activity.getApplication()).getDefaultTracker();
    }

    public static GoogleAnalyticsFactory getInstance(Context context) {
        synchronized (mMute) {
            if (mGoogleAnalyticsFactory == null)
                mGoogleAnalyticsFactory = new GoogleAnalyticsFactory((Activity) context);
        }
        return mGoogleAnalyticsFactory;
    }

    public static GoogleAnalyticsFactory getInstance(Activity activity) {
        synchronized (mMute) {
            if (mGoogleAnalyticsFactory == null)
                mGoogleAnalyticsFactory = new GoogleAnalyticsFactory(activity);
        }
        return mGoogleAnalyticsFactory;
    }

    public void sendFragment(String fragment) {
        if (!enableAnalysis || mTracker == null)
            return;
        mTracker.setScreenName(fragment);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void sendEvent(String category, String action) {
        if (!enableAnalysis || mTracker == null)
            return;
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setNonInteraction(true)
                .build());
    }
}
