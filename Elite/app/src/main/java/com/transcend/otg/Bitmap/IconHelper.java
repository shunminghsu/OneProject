package com.transcend.otg.Bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.os.OperationCanceledException;
import android.util.Log;
import android.widget.ImageView;

import com.transcend.otg.Constant.Constant;
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

    private int mMode;
    private static final int THUMBNAIL_TYPE_IMAGE = 0;
    private static final int THUMBNAIL_TYPE_VIDEO = 1;
    private static final int THUMBNAIL_TYPE_MUSIC = 2;
    private static final int THUMBNAIL_TYPE_FILE = 3;
    private static final int THUMBNAIL_TYPE_FOLDER = 4;

    /**
     * @param context
     * @param mode MODE_GRID or MODE_LIST
     */
    public IconHelper(Context context, int mode) {
        mContext = context;
        setViewMode(mode);
        mCache = MainApplication.getThumbnailsCache(context);
    }

    private String createKey(String path) {
        return path+mThumbSize;
    }

    /**
     * Sets the current display mode.  This affects the thumbnail sizes that are loaded.
     */
    public void setViewMode(int mode) {
        mMode = mode;
        int thumbSize = getThumbSize(mode);
        mThumbSize = new Point(thumbSize, thumbSize);
    }

    private int getThumbSize(int mode) {
        int thumbSize;
        switch (mode) {
            case Constant.ITEM_GRID:
                thumbSize = mContext.getResources().getDimensionPixelSize(R.dimen.grid_width);
                break;
            case Constant.ITEM_LIST:
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

    public Drawable getIconMime(int type) {
        switch (type) {
            case THUMBNAIL_TYPE_IMAGE:
                return IconUtils.loadImageTypeIcon(mContext, mMode==Constant.ITEM_GRID);
            case THUMBNAIL_TYPE_VIDEO:
                return IconUtils.loadVideoTypeIcon(mContext, mMode==Constant.ITEM_GRID);
            case THUMBNAIL_TYPE_MUSIC:
                return IconUtils.loadMusicTypeIcon(mContext, mMode==Constant.ITEM_GRID);
            case THUMBNAIL_TYPE_FILE:
                return IconUtils.loadFileTypeIcon(mContext, mMode==Constant.ITEM_GRID);
            case THUMBNAIL_TYPE_FOLDER:
                return IconUtils.loadFolderTypeIcon(mContext, mMode==Constant.ITEM_GRID);
            default:
                return IconUtils.loadImageTypeIcon(mContext, mMode==Constant.ITEM_GRID);
        }
    }

    public void loadThumbnail(String path, int thumbnailType,
                              ImageView iconThumb, ImageView iconMime) {
        boolean cacheHit = false;
        boolean showThumbnail = true;
        if (showThumbnail) {
            final Bitmap cachedResult = mCache.get(createKey(path));
            if (cachedResult != null) {
                iconThumb.setImageBitmap(cachedResult);
                cacheHit = true;
            } else {
                iconThumb.setImageDrawable(null);
                final LoaderTask task = new LoaderTask(path, iconMime, iconThumb, thumbnailType, mThumbSize, mContext);
                iconThumb.setTag(task);
                task.execute();
            }
        }

        //final Drawable icon = getDocumentIcon(mContext, docAuthority,
        //        DocumentsContract.getDocumentId(uri), mimeType, docIcon);

        final Drawable icon = getIconMime(thumbnailType);
        if (cacheHit) {
            iconMime.setImageDrawable(null);
            iconMime.setAlpha(0f);
            iconThumb.setAlpha(1f);
        } else {
            // Add a mime icon if the thumbnail is being loaded in the background.
            iconThumb.setImageDrawable(null);
            iconMime.setImageDrawable(icon);
            iconMime.setAlpha(1f);
            iconThumb.setAlpha(0f);
        }
    }

    private static class LoaderTask
            extends AsyncTask<String, Void, Bitmap> {
            //implements Preemptable {
        private final String mPath;
        private final ImageView mIconMime;
        private final ImageView mIconThumb;
        private final Point mThumbSize;
        private final int mThumbType;
        //private final CancellationSignal mSignal;

        private Context mContext;
        public LoaderTask(String filePath, ImageView iconMime, ImageView iconThumb,
                          int thumbType, Point thumbSize, Context context) {
            mPath = filePath;
            mIconMime = iconMime;
            mIconThumb = iconThumb;
            mThumbSize = thumbSize;
            mThumbType = thumbType;
            //mSignal = new CancellationSignal();
            mContext = context;
            //Log.d(TAG, "Starting icon loader task for " + mPath);
        }
/*
        @Override
        public void preempt() {
            if (DEBUG) Log.d(TAG, "Icon loader task for " + mUri + " was cancelled.");
            cancel(false);
            mSignal.cancel();
        }*/

        @Override
        protected Bitmap doInBackground(String... params) {
            if (isCancelled())
                return null;

            //final Context context = mIconThumb.getContext();
            Bitmap result = null;
            try {
                if (mThumbType == THUMBNAIL_TYPE_IMAGE)
                    result = IconUtils.decodeSampledBitmapFromPath(mPath, mThumbSize.x, mThumbSize.y);
                else if (mThumbType == THUMBNAIL_TYPE_VIDEO)
                    result = ThumbnailUtils.createVideoThumbnail(mPath, MediaStore.Video.Thumbnails.MINI_KIND);
                else {
                    //music
                }
                if (result != null) {
                    final ThumbnailCache thumbs = MainApplication.getThumbnailsCache(mContext);
                    thumbs.put(mPath+mThumbSize, result);
                }
            } catch (Exception e) {
                if (!(e instanceof OperationCanceledException)) {
                    Log.d(TAG, "Failed to load thumbnail for " + mPath + ": " + e);
                }
            } finally {
                //ContentProviderClient.releaseQuietly(client);
            }
            return result;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            //Log.d(TAG, "Loader task for " + mPath + " completed");
            if (mIconThumb.getTag() == this && result != null) {
                mIconThumb.setTag(null);
                mIconThumb.setImageBitmap(result);

                float alpha = mIconMime.getAlpha();
                mIconMime.animate().alpha(0f).start();
                mIconThumb.setAlpha(0f);
                mIconThumb.animate().alpha(alpha).start();
            }
        }
    }
}
