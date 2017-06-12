package com.transcend.otg;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Point;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.transcend.otg.Bitmap.ThumbnailCache;

public class MainApplication extends Application {
    private Point mThumbnailsSize;
    private ThumbnailCache mThumbnails;
    private FirebaseAnalytics mFirebaseAnalytics;

    public static ThumbnailCache getThumbnailsCache(Context context) {
        final MainApplication app = (MainApplication) context.getApplicationContext();
        final ThumbnailCache thumbnails = app.mThumbnails;
        return thumbnails;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final int memoryClassBytes = am.getMemoryClass() * 1024 * 1024;

        mThumbnails = new ThumbnailCache(memoryClassBytes / 4);

    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        if (level >= TRIM_MEMORY_MODERATE) {
            mThumbnails.evictAll();
        } else if (level >= TRIM_MEMORY_BACKGROUND) {
            mThumbnails.trimToSize(mThumbnails.size() / 2);
        }
    }

    synchronized public FirebaseAnalytics getDefaultAnalytics() {
        if (mFirebaseAnalytics == null) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }
        return mFirebaseAnalytics;
    }


//    synchronized public Tracker getDefaultTracker() {
//        if (mTracker == null) {
//            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
//            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
//            mTracker = analytics.newTracker(R.xml.global_tracker);
//        }
//        return mTracker;
//    }
}
