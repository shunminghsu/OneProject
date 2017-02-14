package com.transcend.otg.Bitmap;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.transcend.otg.MainApplication;
import com.transcend.otg.R;
import com.transcend.otg.Utils.IconUtils;

/**
 * Created by henry_hsu on 2017/2/14.
 */

public class IconHelper {
    private static String TAG = "IconHelper";

    private final Context mContext;

    // Updated when icon size is set.
    private ThumbnailCache mCache;
    private Point mThumbSize;
    // The display mode (MODE_GRID, MODE_LIST, etc).
    private int mMode;
    private boolean mThumbnailsEnabled = true;

    /**
     * @param context
     * @param mode MODE_GRID or MODE_LIST
     */
    public IconHelper(Context context, int mode) {
        mContext = context;
        setViewMode(mode);
        mCache = MainApplication.getThumbnailsCache(context, mThumbSize);
    }

    /**
     * Enables or disables thumbnails. When thumbnails are disabled, mime icons (or custom icons, if
     * specified by the document) are used instead.
     *
     * @param enabled
     */
    public void setThumbnailsEnabled(boolean enabled) {
        mThumbnailsEnabled = enabled;
    }

    /**
     * Sets the current display mode.  This affects the thumbnail sizes that are loaded.
     */
    public void setViewMode(int mode) {
        mMode = mode;
        int thumbSize = getThumbSize(mode);
        mThumbSize = new Point(thumbSize, thumbSize);
        mCache = MainApplication.getThumbnailsCache(mContext, mThumbSize);
    }

    private int getThumbSize(int mode) {
        int thumbSize;
        switch (mode) {
            case 0://MODE_GRID:
                thumbSize = mContext.getResources().getDimensionPixelSize(R.dimen.grid_width);
                break;
            case 1://MODE_LIST:
                thumbSize = mContext.getResources().getDimensionPixelSize(
                        R.dimen.list_item_thumbnail_size);
                break;
            default:
                throw new IllegalArgumentException("Unsupported layout mode: " + mode);
        }
        return thumbSize;
    }

    public Drawable getIcon() {
        return IconUtils.loadIcon(mContext, mMode);
    }
}
