package com.transcend.otg.Photo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.R;

import java.util.ArrayList;

/**
 * Created by henry_hsu on 2017/3/3.
 */

public class PhotoActivity extends AppCompatActivity {
    private PhotoHelper mPhotoHelper;
    private ViewPager mPager;
    private ArrayList<FileInfo> mPhotoList;
    CustomPagerAdapter mAdapter;
    private int mPosition;
    int mScreenW, mScreenH;
    private Toolbar toolbar;
    private ActionMenuView mActionMenuView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.photo_layout);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenW = displaymetrics.widthPixels;
        mScreenH = displaymetrics.heightPixels;



        mPhotoHelper = new PhotoHelper(this);
        mPhotoList = getIntent().getParcelableArrayListExtra("photo_list");
        mPosition = getIntent().getIntExtra("list_index", 0);
        initPager();
        initToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo, mActionMenuView.getMenu());
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) { // called every time the menu opens
        super.onPrepareOptionsMenu(menu);


        return true;
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mActionMenuView = (ActionMenuView) toolbar.findViewById(R.id.amvMenu);
        mActionMenuView.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onOptionsItemSelected(menuItem);
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initPager() {
        mAdapter = new CustomPagerAdapter(this);
        mPager = (ViewPager)findViewById(R.id.photo_view_pager);
        mPager.setPageTransformer(true, new DepthPageTransformer());
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(mPosition);
        mPager.setOffscreenPageLimit(0);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public class CustomPagerAdapter extends PagerAdapter {

        Context mContext;
        LayoutInflater mLayoutInflater;

        public CustomPagerAdapter(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mPhotoList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.photo_pager_item, container, false);
            String size = ((TextView) itemView.findViewById(R.id.size)).getText().toString();
            String location = ((TextView) itemView.findViewById(R.id.location)).getText().toString();
            String time = ((TextView) itemView.findViewById(R.id.time)).getText().toString();
            String path = ((TextView) itemView.findViewById(R.id.path)).getText().toString();

            ImageView imageView = (ImageView) itemView.findViewById(R.id.photo);
            ViewGroup loading = (ViewGroup) itemView.findViewById(R.id.loading);
            mPhotoHelper.loadThumbnail(mPhotoList.get(position).path, imageView, loading, mScreenW, mScreenH);

            ((TextView) itemView.findViewById(R.id.name)).setText(mPhotoList.get(position).name);
            ((TextView) itemView.findViewById(R.id.size)).setText(size+": "+mPhotoList.get(position).format_size);
            ((TextView) itemView.findViewById(R.id.location)).setText(location+": ");
            ((TextView) itemView.findViewById(R.id.time)).setText(time+": "+mPhotoList.get(position).time);
            ((TextView) itemView.findViewById(R.id.path)).setText(path+": "+mPhotoList.get(position).path);

            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((RelativeLayout) object);
        }
    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            float pageWidthTimesPosition = pageWidth * position;
            View photo = view.findViewById(R.id.photo);
            View info = view.findViewById(R.id.info);
            View name = view.findViewById(R.id.name);
            View size = view.findViewById(R.id.size);
            View location = view.findViewById(R.id.location);
            View time = view.findViewById(R.id.time);
            View path = view.findViewById(R.id.path);

            if (position < -1 || position > 1) {
                // The page is not visible. This is a good place to stop
                // any potential work / animations you may have running.
                //view.setAlpha(0);

            } else if (position == 0) {
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else {
                if (position < 0) {
                    info.setTranslationX(pageWidthTimesPosition*2f);
                    photo.setTranslationX(-pageWidthTimesPosition / 2f);

                    photo.setScaleX(1);
                    photo.setScaleY(1);
                } else {
                    name.setTranslationX(pageWidthTimesPosition*0.8f);
                    size.setTranslationX(pageWidthTimesPosition*1.1f);
                    location.setTranslationX(pageWidthTimesPosition*1.4f);
                    time.setTranslationX(pageWidthTimesPosition*1.7f);
                    path.setTranslationX(pageWidthTimesPosition*2f);
                    photo.setTranslationX(-pageWidthTimesPosition / 2f);

                    // Scale the page down (between MIN_SCALE and 1)
                    float scaleFactor = MIN_SCALE
                            + (1 - MIN_SCALE) * (1 - Math.abs(position));
                    photo.setScaleX(scaleFactor);
                    photo.setScaleY(scaleFactor);
                }
            }
        }
    }
}
