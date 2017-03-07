package com.transcend.otg.Photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;



/**
 * Created by henry_hsu on 2017/3/3.
 */

public class PhotoHelper {
    private final Context mContext;


    public PhotoHelper(Context context) {
        mContext = context;
    }

    public void loadThumbnail(String path, ImageView photoView, ViewGroup loadingView, int width, int height) {

        photoView.setImageDrawable(null);
        final PhotoHelper.LoaderTask task = new PhotoHelper.LoaderTask(path, photoView, loadingView, mContext, width, height);
        photoView.setTag(task);
        task.execute();

    }

    private static class LoaderTask extends AsyncTask<String, Void, Bitmap> {
        private final String mPath;
        private final ViewGroup mLoadingView;
        private final ImageView mPhotoView;
        final private int mPhotoWidth, mPhotoHeight;

        private Context mContext;
        public LoaderTask(String filePath, ImageView photoView, ViewGroup loadingView, Context context,
                          int width, int height) {
            mPath = filePath;
            mLoadingView = loadingView;
            mPhotoView = photoView;
            mContext = context;
            mPhotoWidth = width;
            mPhotoHeight = height;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingView.setVisibility(View.VISIBLE);
            mPhotoView.setVisibility(View.GONE);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            if (isCancelled())
                return null;

            Bitmap result = null;
            result = decodeFullScreenBitmapFromPath(mPath, mPhotoWidth, mPhotoHeight);
            return result;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            if (mPhotoView.getTag() == this && result != null) {
                mPhotoView.setTag(null);
                mPhotoView.setImageBitmap(result);
                mLoadingView.setVisibility(View.GONE);
                mPhotoView.setVisibility(View.VISIBLE);
            }
        }
    }

    public static Bitmap decodeFullScreenBitmapFromPath(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

       // Log.d("henry",path + "\n inSampleSize "+options.inSampleSize);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path, options);
        } catch (OutOfMemoryError e) {
            Log.d("henry", "oom " + e.getMessage());
        }
        return bitmap;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        final int reqArea = reqWidth * reqHeight;
        //final int bmpArea = options.outWidth * options.outHeight;
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) * (halfWidth / inSampleSize) >= reqArea) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;

    }
}
