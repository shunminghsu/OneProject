package com.transcend.otg.Loader;

import android.app.Activity;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.transcend.otg.Constant.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangbojie on 2017/2/7.
 */

public class FileActionManager {
    private String TAG = FileActionManager.class.getSimpleName();
    private Context mContext;
    private RelativeLayout mProgressLayout;
    private FileActionService mFileActionService;
    private Map<MODE, FileActionService> mFileActionServicePool;
    private MODE mMode;
    private android.app.LoaderManager.LoaderCallbacks<Boolean> mCallbacks;

    public enum MODE {
        LOCAL, SD, OTG
    }

    public FileActionManager(Context context, MODE mode, android.app.LoaderManager.LoaderCallbacks<Boolean> callbacks) {
        this(context, mode, callbacks, null);
    }

    public FileActionManager(Context context, MODE mode, android.app.LoaderManager.LoaderCallbacks<Boolean> callbacks, RelativeLayout progressLayout) {
        mContext = context;
        mMode = mode;
        mCallbacks = callbacks;
        mProgressLayout = progressLayout;
        setMode(mode);
    }

    public void setMode(MODE mode) {
        if (mFileActionService != null && mMode == mode)
            return;

        if (null == mFileActionServicePool)
            mFileActionServicePool = new HashMap<>();

        FileActionService service = mFileActionServicePool.get(mode);
        if (null == service) {
            switch (mode) {
                case LOCAL:
                    service = new LocalActionService();
                    break;
                case SD:

                    break;
                case OTG:

                    break;
            }
            mFileActionServicePool.put(mode, service);
        }

        mMode = mode;
        mFileActionService = service;
    }

    public void checkServiceMode(String path) {
        if (path.startsWith("/storage")) {
            setMode(MODE.LOCAL);
        }
    }


    public String getLocalRootPath(){
        String root = Constant.ROOT_LOCAL;
        return root;
    }

    public void list(String path) {
        createLoader(FileActionService.FileAction.LIST, null, path, null);
        Log.w(TAG, "doLoad: " + path);
    }

    public void listAllType(){
        createLoader(FileActionService.FileAction.LIST_ALL_TYPE, null, null, null);
    }

    private void createLoader(FileActionService.FileAction mode, String name, String dest, ArrayList<String> paths) {
        int id = mFileActionService.getLoaderID(mode);
        Bundle args = new Bundle();
        if (name != null)
            args.putString("name", name);
        if (dest != null)
            args.putString("path", dest);
        if (paths != null)
            args.putStringArrayList("paths", paths);
        if (mode != null)
            args.putInt("actionMode", id);

        ((Activity) mContext).getLoaderManager().restartLoader(id, args, mCallbacks).forceLoad();
    }

    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        Loader<Boolean> loader = null;
        if (mFileActionService != null) {
            int mode = args.getInt("actionMode");
            FileActionService.FileAction action = mFileActionService.getFileAction(mode);
            if (action != null) {
                Log.d(TAG, "action : " + action);
                loader = mFileActionService.onCreateLoader(mContext, action, args);
                if (loader != null && mProgressLayout != null) {
                    switch (action) {
                        case LIST:
                        case RENAME:
                        case DELETE:
                        case CreateFOLDER:
                            mProgressLayout.setVisibility(View.VISIBLE);
                            break;
                        default:
                            mProgressLayout.setVisibility(View.INVISIBLE);
                            break;
                    }
                }
            }
        }

        return loader;
    }

    public void onLoadFinished(Loader<Boolean> loader, Boolean success) {
        Log.w(TAG, "onLoaderFinished: " + loader.getClass().getSimpleName() + " " + success);
        if (mFileActionService != null) {
            mFileActionService.onLoadFinished(mContext, loader, success);
        }
    }

    public void onLoaderReset(Loader<Boolean> loader) {
        Log.w(TAG, "onLoaderReset: " + loader.getClass().getSimpleName());
    }
}
