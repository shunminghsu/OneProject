package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import com.transcend.otg.R;

import java.io.File;

/**
 * Created by wangbojie on 2016/6/20.
 */
public class LocalRenameLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = LocalRenameLoader.class.getSimpleName();

    private String mPath;
    private String mName;
    private Context mContext;

    public LocalRenameLoader(Context context, String path, String name) {
        super(context);
        mContext = context;
        mPath = path;
        mName = name;

        File target = new File(mPath);
        File parent = target.getParentFile();
        File rename = new File(parent, mName);
        if (rename.exists()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setIcon(R.mipmap.icon_elite_logo);
            builder.setTitle(mContext.getResources().getString(R.string.app_name));
            String exist = mContext.getResources().getString(R.string.file_exist);
            builder.setMessage(exist);
            builder.setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public Boolean loadInBackground() {
        try {
            return rename();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean rename() throws InterruptedException {
        File target = new File(mPath);
        File parent = target.getParentFile();
        File rename = new File(parent, mName);

        if (rename.exists()) {
            //Log.d("henry","new name file exist");
            return false;
        }
        if (target.exists()) {
            if (target.renameTo(rename)) {
                mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(rename)));
                Thread.sleep(500);
                return true;
            }
        }

        return false;
    }

}
