package com.transcend.otg.Photo;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;


/**
 * Created by henry_hsu on 2017/3/3.
 */

public class PhotoHelper {
    private final Context mContext;
    private static final String TAG = "PhotoHelper";

    public PhotoHelper(Context context) {
        mContext = context;
    }

    public void loadThumbnail(FileInfo fileInfo, ImageView photoView, ViewGroup loadingView, int width, int height) {
        boolean isOtg = fileInfo.type == Constant.STORAGEMODE_OTG;
        photoView.setImageDrawable(null);
        final PhotoHelper.LoaderTask task = new PhotoHelper.LoaderTask(fileInfo.path, fileInfo.uri, photoView, loadingView, mContext, width, height, isOtg);
        photoView.setTag(task);
        task.execute();

    }

    private static class LoaderTask extends AsyncTask<String, Void, Bitmap> {
        private final Uri mUri;
        private final String mPath;
        private boolean mOtgFile;
        private final ViewGroup mLoadingView;
        private final ImageView mPhotoView;
        final private int mPhotoWidth, mPhotoHeight;
        private final CancellationSignal mSignal;
        private Point mThumbSize;
        private Context mContext;
        public LoaderTask(String path, Uri uri, ImageView photoView, ViewGroup loadingView, Context context,
                          int width, int height, boolean isOtg) {
            mPath = path;
            mUri = uri;
            mOtgFile = isOtg;
            mLoadingView = loadingView;
            mPhotoView = photoView;
            mContext = context;
            mPhotoWidth = width;
            mPhotoHeight = height;
            mThumbSize = new Point(width, height);
            mSignal = new CancellationSignal();
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
            final ContentResolver resolver = mContext.getContentResolver();
            Bitmap result = null;
            if (mOtgFile && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                try {
                    result = getBitmapFromUri(mContext, mUri);
                } catch (IOException e) {
                    Log.d(TAG, e.getMessage());
                }
                if (result == null)
                    result = DocumentsContract.getDocumentThumbnail(resolver, mUri, mThumbSize, mSignal);
            } else {
                File f = new File(mPath);
                if (f.exists())
                    result = decodeFullScreenBitmapFromPath(mPath, mPhotoWidth, mPhotoHeight);
                else
                    result = DocumentsContract.getDocumentThumbnail(resolver, mUri, mThumbSize, mSignal);
            }
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

    private static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
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
            Log.d(TAG, "oom " + e.getMessage());
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
