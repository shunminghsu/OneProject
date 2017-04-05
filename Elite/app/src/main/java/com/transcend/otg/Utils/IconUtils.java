/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.transcend.otg.Utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import com.transcend.otg.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class IconUtils {
    public static Bitmap loadAlbumThumbnail(String path) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        byte[] artBytes =  mmr.getEmbeddedPicture();
        if (artBytes != null) {
            return BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
        }

        return null;
    }

    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    public static Bitmap loadAlbumThumbnail(Context context, long album_id) {
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, sBitmapOptions);
            } catch (FileNotFoundException ex) {
                //Log.d("henry", ex.getMessage());
                // The album art thumbnail does not actually exist. Maybe the user deleted it, or
                // maybe it never existed to begin with.
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                }
            }
        }
        return null;
    }

    public static Drawable loadPackageIcon(Context context, String authority, int icon) {
        if (icon != 0) {
            if (authority != null) {
                final PackageManager pm = context.getPackageManager();
                final ProviderInfo info = pm.resolveContentProvider(authority, 0);
                if (info != null) {
                    return pm.getDrawable(info.packageName, icon, info.applicationInfo);
                }
            } else {
                return context.getDrawable(icon);
            }
        }
        return null;
    }

    public static Drawable loadIcon(Context context, int mode) {
        return context.getDrawable(R.drawable.ic_menu_camera);
    }

    public static Drawable loadImageTypeIcon(Context context, boolean large) {
        return large ? context.getDrawable(R.drawable.ic_image_l) : context.getDrawable(R.mipmap.ic_image_s);
    }
    public static Drawable loadVideoTypeIcon(Context context, boolean large) {
        return large ? context.getDrawable(R.drawable.ic_video_l) : context.getDrawable(R.mipmap.ic_video_s);
    }
    public static Drawable loadMusicTypeIcon(Context context, boolean large) {
        return large ? context.getDrawable(R.drawable.ic_music_l) : context.getDrawable(R.mipmap.ic_music_s);
    }
    public static Drawable loadFileTypeIcon(Context context, boolean large) {
        return large ? context.getDrawable(R.drawable.ic_file_l) : context.getDrawable(R.mipmap.ic_file_s);
    }
    public static Drawable loadEncryptTypeIcon(Context context, boolean large) {
        return large ? context.getDrawable(R.drawable.ic_encrypt_gray_l) : context.getDrawable(R.mipmap.ic_encrypt_s);
    }
    public static Drawable loadFolderTypeIcon(Context context, boolean large) {
        return large ? context.getDrawable(R.drawable.ic_folder_l) : context.getDrawable(R.mipmap.ic_folder_s);
    }

    public static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;

    }
}
