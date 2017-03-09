package com.transcend.otg.Photo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.transcend.otg.Constant.Constant;
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
    private boolean mHideAllUI = false;
    PhotoClickListener mPhotoClickListener;
    private static final int EDIT_REQUEST_CODE = 101;

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
        if (displaymetrics.widthPixels < displaymetrics.heightPixels) {
            findViewById(R.id.view_for_ui).setVisibility(View.GONE);
        } else {
            findViewById(R.id.view_for_ui).setVisibility(View.VISIBLE);
        }


        mPhotoHelper = new PhotoHelper(this);
        mPhotoList = getIntent().getParcelableArrayListExtra("photo_list");
        mPosition = getIntent().getIntExtra("list_index", 0);
        initPager();
        initToolbar();
        mPhotoClickListener = new PhotoClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            findViewById(R.id.view_for_ui).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.view_for_ui).setVisibility(View.GONE);
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                Intent share_intent = new Intent(Intent.ACTION_SEND);
                share_intent.putExtra(Intent.EXTRA_STREAM, mPhotoList.get(mPager.getCurrentItem()).uri);
                share_intent.setType("image/*");
                share_intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(share_intent, EDIT_REQUEST_CODE);
                //startActivity(Intent.createChooser(share_intent, null));
                return true;
            case R.id.edit:
                Intent edit_intent = new Intent(Intent.ACTION_EDIT);
                edit_intent.setDataAndType(mPhotoList.get(mPager.getCurrentItem()).uri, "image/*");
                edit_intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(edit_intent, null));
                //startActivityForResult(intent, EDIT_REQUEST_CODE);
                return true;
            case R.id.action_delete:
                return true;
            case R.id.set_photo_as:
                Intent setas_intent = new Intent(Intent.ACTION_ATTACH_DATA);
                setas_intent.setDataAndType(mPhotoList.get(mPager.getCurrentItem()).uri, "image/*");
                setas_intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(setas_intent, getString(R.string.set_photo_as)));
                return true;
            case R.id.action_rename:
                return true;
            case R.id.action_copy:
                return true;
            case R.id.action_move:
                return true;
            case R.id.action_encrypt:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

    private class PhotoClickListener implements TouchImageView.onPhotoClickListener {

        @Override
        public void onPhotoClick() {
            SparseArray<ViewGroup> photoInfo = mAdapter.getPhotoInfo();

            if (mHideAllUI) {
                toolbar.setVisibility(View.VISIBLE);
                for (int i=0;i<photoInfo.size();i++) {
                    photoInfo.get(photoInfo.keyAt(i)).setVisibility(View.VISIBLE);
                }
                mHideAllUI = false;
            } else {
                toolbar.setVisibility(View.GONE);
                for (int i=0;i<photoInfo.size();i++) {
                    photoInfo.get(photoInfo.keyAt(i)).setVisibility(View.GONE);
                }
                mHideAllUI = true;
            }
        }
    }

    public class CustomPagerAdapter extends PagerAdapter {
        private SparseArray<ViewGroup> photoInfo = new SparseArray<ViewGroup>();
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

            TouchImageView imageView = (TouchImageView) itemView.findViewById(R.id.photo);
            imageView.setPhotoClickListener(mPhotoClickListener);
            ViewGroup loading = (ViewGroup) itemView.findViewById(R.id.loading);
            mPhotoHelper.loadThumbnail(mPhotoList.get(position).uri, imageView, loading, mScreenW, mScreenH);

            ViewGroup viewGroup = (ViewGroup)itemView.findViewById(R.id.info);
            viewGroup.setVisibility(mHideAllUI ? View.GONE : View.VISIBLE);
            photoInfo.put(position, viewGroup);

            ((TextView) itemView.findViewById(R.id.name)).setText(mPhotoList.get(position).name);
            ((TextView) itemView.findViewById(R.id.size)).setText(size+": "+mPhotoList.get(position).format_size);

            if (mPhotoList.get(position).storagemode == Constant.STORAGEMODE_LOCAL)
                ((TextView) itemView.findViewById(R.id.location)).setText(getResources().getString(R.string.photo_location_phone));
            else if (mPhotoList.get(position).storagemode == Constant.STORAGEMODE_SD)
                ((TextView) itemView.findViewById(R.id.location)).setText(getResources().getString(R.string.photo_location_sd));
            else
                ((TextView) itemView.findViewById(R.id.location)).setText(getResources().getString(R.string.photo_location_otg));

            ((TextView) itemView.findViewById(R.id.time)).setText(time+": "+mPhotoList.get(position).time);
            ((TextView) itemView.findViewById(R.id.path)).setText(path+": "+mPhotoList.get(position).path);

            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            photoInfo.remove(position);
            container.removeView((RelativeLayout) object);
        }

        public SparseArray<ViewGroup> getPhotoInfo() {
            return photoInfo;
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
