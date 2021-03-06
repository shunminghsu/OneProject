package com.transcend.otg.Setting;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.R;
import com.transcend.otg.Utils.FileFactory;
import com.transcend.otg.Utils.FileInfoSort;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by wangbojie on 2017/3/26.
 */

public class SettingFragment extends Fragment {

    private LinearLayout layoutCapacity, layoutCleanCache, layoutAbout, layoutRate;
    private CoordinatorLayout root;
    private File fileCache;
    private Context mContext;
    private TextView tvCleancacheaction;
    private long totalSize;
    private ProgressBar pbCleancache;

    public SettingFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        totalSize = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = (CoordinatorLayout) inflater.inflate(R.layout.fragment_setting, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        LayoutClickListener listener = new LayoutClickListener();
        layoutCapacity = (LinearLayout) root.findViewById(R.id.layout_capacity);
        layoutCleanCache = (LinearLayout) root.findViewById(R.id.layout_cleancache);
        layoutAbout = (LinearLayout) root.findViewById(R.id.layout_about);
        layoutRate = (LinearLayout) root.findViewById(R.id.layout_rate);
        layoutCapacity.setOnClickListener(listener);
        layoutCleanCache.setOnClickListener(listener);
        layoutAbout.setOnClickListener(listener);
        layoutRate.setOnClickListener(listener);
        tvCleancacheaction = (TextView) root.findViewById(R.id.cleancacheaction);
        pbCleancache = (ProgressBar) root.findViewById(R.id.cleancacheprogress);
        fileCache = new File(Constant.ROOT_CACHE);
        getCacheSize();

        return root;
    }

    private void setProgressBarVisibility(boolean visible){
        if(visible){
            tvCleancacheaction.setVisibility(View.GONE);
            pbCleancache.setVisibility(View.VISIBLE);
        }else {
            tvCleancacheaction.setVisibility(View.VISIBLE);
            pbCleancache.setVisibility(View.GONE);
        }
    }

    private boolean getCacheSize() {
        setProgressBarVisibility(true);
        File[] mFiles = fileCache.listFiles();
        totalSize = 0;
        for (File file : mFiles) {
            if (file.isDirectory())
                getCacheSizeDirectory(file);
            else
                totalSize += file.length();
        }
        tvCleancacheaction.setText(Formatter.formatFileSize(mContext, totalSize));
        setProgressBarVisibility(false);
        return true;
    }

    private void getCacheSizeDirectory(File dir) {
        for (File target : dir.listFiles()) {
            if (target.isDirectory())
                getCacheSizeDirectory(target);
            else
                totalSize += target.length();
        }
    }

    private void clearCache(){
        setProgressBarVisibility(true);
        File[] files = fileCache.listFiles();
        if(delete(files)){
            getCacheSize();
        }else {
            snackBarShow(R.string.fail);
        }
        setProgressBarVisibility(false);
    }

    private boolean delete(File[] mFiles) {
        for (File file : mFiles) {
            if (file.isDirectory())
                deleteDirectory(file);
            else
                file.delete();
        }

        return true;
    }

    private void deleteDirectory(File dir) {
        for (File target : dir.listFiles()) {
            if (target.isDirectory())
                deleteDirectory(target);
            else
                target.delete();
        }
        dir.delete();
    }

    private void snackBarShow(int resId) {
        Snackbar.make(root, resId, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    private void gotoAboutActivity() {
        Intent intent = new Intent(getActivity(), AboutActivity.class);
        getActivity().startActivity(intent);
    }

    private void gotoCapacityActivity(){
        Intent intent = new Intent(getActivity(), CapacityActivity.class);
        getActivity().startActivity(intent);
    }

    private void gotoRate(){
        final String appPackageName = "com.transcend.otg"; // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }

    }

    class LayoutClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v == layoutCapacity) {
                gotoCapacityActivity();
            } else if (v == layoutCleanCache) {
                clearCache();
            } else if (v == layoutAbout) {
                gotoAboutActivity();
            } else if (v == layoutRate)
                gotoRate();
        }
    }





}
