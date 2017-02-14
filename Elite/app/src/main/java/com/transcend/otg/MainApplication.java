package com.transcend.otg;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Point;

import com.transcend.otg.Bitmap.ThumbnailCache;

public class MainApplication extends Application {
    private Point mThumbnailsSize;
    private ThumbnailCache mThumbnails;


    public static ThumbnailCache getThumbnailsCache(Context context, Point size) {
        final MainApplication app = (MainApplication) context.getApplicationContext();
        final ThumbnailCache thumbnails = app.mThumbnails;
        if (!size.equals(app.mThumbnailsSize)) {
            thumbnails.evictAll();
            app.mThumbnailsSize = size;
        }
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
}
