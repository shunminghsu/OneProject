package com.transcend.otg.Photo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.provider.DocumentFile;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.DestinationActivity;
import com.transcend.otg.Dialog.LocalDeleteDialog;
import com.transcend.otg.Dialog.LocalRenameDialog;
import com.transcend.otg.Dialog.SDPermissionGuideDialog;
import com.transcend.otg.LocalPreferences;
import com.transcend.otg.MainActivity;
import com.transcend.otg.R;
import com.transcend.otg.Utils.FileFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by henry_hsu on 2017/3/3.
 */

public class PhotoActivity extends AppCompatActivity {
    private Context mContext;
    private PhotoHelper mPhotoHelper;
    private ViewPager mPager;
    private ArrayList<FileInfo> mPhotoList;
    CustomPagerAdapter mAdapter;
    private int mPosition;
    int mScreenW, mScreenH;
    private Toolbar toolbar;
    //private ActionMenuView mActionMenuView;
    private ViewGroup mBottomToolbar, mBottomToolbarBackground;
    private ImageView mInfoView, mDeleteView, mShareView;
    private BottomToolBarClickListener mBottomToolBarClickListener;
    private boolean mHideAllUI = false;
    PhotoClickListener mPhotoClickListener;
    private TextView mToolBarTitle;
    private RelativeLayout mRootLayout;
    private static final int SD_PERMISSION_REQUEST_CODE = 2017;
    private static final int EDIT_REQUEST_CODE = 101;
    private static final int ACTION_RESULT_DEFAUT = 0;
    private static final int ACTION_RESULT_FILE_EXIST = 1;
    private static final int ACTION_RESULT_NEED_PERMISSION = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
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
        mPhotoClickListener = new PhotoClickListener();
        mRootLayout = (RelativeLayout) findViewById(R.id.main_relativelayout);
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
        getMenuInflater().inflate(R.menu.menu_photo, menu);
        //getMenuInflater().inflate(R.menu.menu_photo, mActionMenuView.getMenu());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final FileInfo fileinfo = mPhotoList.get(mPager.getCurrentItem());
        switch (item.getItemId()) {
            case R.id.share:
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) &&
                        fileinfo.storagemode == Constant.STORAGEMODE_OTG) {

                } else {
                    Intent share_intent = new Intent(Intent.ACTION_SEND);
                    share_intent.putExtra(Intent.EXTRA_STREAM, fileinfo.uri);
                    share_intent.setType("image/jpeg");
                    share_intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(share_intent, EDIT_REQUEST_CODE);
                }
                return true;
            case R.id.edit:
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) &&
                        fileinfo.storagemode == Constant.STORAGEMODE_OTG) {

                } else {
                    Intent edit_intent = new Intent(Intent.ACTION_EDIT);
                    edit_intent.setDataAndType(fileinfo.uri, "image/*");
                    edit_intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(edit_intent, null));
                    //startActivityForResult(intent, EDIT_REQUEST_CODE);
                }
                return true;
            case R.id.delete:
                ArrayList<FileInfo> deleteOneFiles = new ArrayList<FileInfo>();
                deleteOneFiles.add(fileinfo);
                new LocalDeleteDialog(this, deleteOneFiles) {
                    @Override
                    public void onConfirm(ArrayList<FileInfo> selectedFiles) {
                        new DeleteTask(fileinfo).execute();
                    }
                };
                return true;
            case R.id.set_photo_as:
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) &&
                        fileinfo.storagemode == Constant.STORAGEMODE_OTG) {

                } else {
                    Intent setas_intent = new Intent(Intent.ACTION_ATTACH_DATA);
                    setas_intent.setDataAndType(fileinfo.uri, "image/*");
                    setas_intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(setas_intent, getString(R.string.set_photo_as)));
                }
                return true;
            case R.id.action_rename:
                new LocalRenameDialog(this,false, fileinfo.name) {
                    @Override
                    public void onConfirm(String newName) {
                        if (newName.equals(fileinfo.name))
                            return;
                        new RenameTask(fileinfo).execute(newName);
                    }
                };
                return true;
            case R.id.action_copy:
                startDestinationActivity(R.id.action_copy);
                return true;
            case R.id.action_move:
                startDestinationActivity(R.id.action_move);
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
        if(requestCode == SD_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK){
            Uri uriTree = data.getData();
            if(checkSD(uriTree)){
                createDialog(mContext, "get permission success");
            } else {
                createDialog(mContext, "get permission fail");
            }
        } else if(requestCode == DestinationActivity.REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            int actionId = bundle.getInt("actionId");
            String destinationPath = bundle.getString("path");
            Constant.MODE actionMode = (Constant.MODE) bundle.getSerializable("destinationMode");
            ArrayList<DocumentFile> destinationDFiles = Constant.destinationDFile;

            FileInfo fileInfo = mPhotoList.get(mPosition);

            boolean isMove = actionId == R.id.action_move;

            if (actionMode == Constant.MODE.LOCAL) {//destination is local
                new CopyTask(fileInfo, Constant.STORAGEMODE_LOCAL, destinationPath, isMove).execute();
            } else if (actionMode == Constant.MODE.SD) {

                new CopyTask(fileInfo, Constant.STORAGEMODE_SD, destinationPath, isMove).execute();
            } else if (actionMode == Constant.MODE.OTG) {
                new CopyTask(fileInfo, Constant.STORAGEMODE_OTG, null, isMove).execute(destinationDFiles.get(0));
            }

        }
    }

    private void initToolbar() {
        mBottomToolbarBackground = (ViewGroup) findViewById(R.id.bottom_toolbar_background);
        mBottomToolbar = (ViewGroup) findViewById(R.id.bottom_toolbar);
        mInfoView = (ImageView) findViewById(R.id.information);
        mDeleteView = (ImageView) findViewById(R.id.delete);
        mShareView = (ImageView) findViewById(R.id.share);

        mBottomToolBarClickListener = new BottomToolBarClickListener();
        mInfoView.setOnClickListener(mBottomToolBarClickListener);
        mDeleteView.setOnClickListener(mBottomToolBarClickListener);
        mShareView.setOnClickListener(mBottomToolBarClickListener);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        /*mActionMenuView = (ActionMenuView) toolbar.findViewById(R.id.amvMenu);
        mActionMenuView.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onOptionsItemSelected(menuItem);
            }
        });*/
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolBarTitle = (TextView) findViewById(R.id.toolbar_title);
        FileInfo fileInfo = mPhotoList.get(mPosition);
        mToolBarTitle.setText(fileInfo.name.substring(0, fileInfo.name.lastIndexOf(".")));
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
                mPosition = position;
                FileInfo fileInfo = mPhotoList.get(position);
                mToolBarTitle.setText(fileInfo.name.substring(0, fileInfo.name.lastIndexOf(".")));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private class PhotoClickListener implements TouchImageView.onPhotoClickListener {

        @Override
        public void onPhotoClick() {
            //SparseArray<ViewGroup> photoInfo = mAdapter.getPhotoInfo();

            if (mHideAllUI) {
                mBottomToolbarBackground.setVisibility(View.VISIBLE);
                mBottomToolbar.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);
                //for (int i=0;i<photoInfo.size();i++) {
                //    photoInfo.get(photoInfo.keyAt(i)).setVisibility(View.VISIBLE);
               // }
                mHideAllUI = false;
            } else {
                mBottomToolbarBackground.setVisibility(View.GONE);
                mBottomToolbar.setVisibility(View.GONE);
                toolbar.setVisibility(View.GONE);
                //for (int i=0;i<photoInfo.size();i++) {
                //    photoInfo.get(photoInfo.keyAt(i)).setVisibility(View.GONE);
                //}
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
            mPhotoHelper.loadThumbnail(mPhotoList.get(position), imageView, loading, mScreenW, mScreenH);

            ViewGroup viewGroup = (ViewGroup)itemView.findViewById(R.id.info);
            //viewGroup.setVisibility(mHideAllUI ? View.GONE : View.VISIBLE);
            viewGroup.setVisibility(View.GONE);//current UI don't show info
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

    private class DeleteTask extends AsyncTask<Void, Void, Boolean> {
        private final FileInfo mFileInfo;
        int result_code = ACTION_RESULT_DEFAUT;

        public DeleteTask(FileInfo fileInfo) {
            mFileInfo = fileInfo;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (mFileInfo.storagemode != Constant.STORAGEMODE_LOCAL) {
                if (mFileInfo.storagemode == Constant.STORAGEMODE_SD && !hasSDPermission()) {
                    result_code = ACTION_RESULT_NEED_PERMISSION;
                    return false;
                }
                DocumentFile dfile = FileFactory.findDocumentFilefromName(mContext, mFileInfo);
                return dfile.delete();
            } else {
                File f = new File(mFileInfo.path);
                return f.delete();
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                mPhotoList.remove(mFileInfo);
                if (mPhotoList.size() == 0) {
                    finish();
                } else {
                    mAdapter = null;
                    mAdapter = new CustomPagerAdapter(mContext);
                    mPager.setAdapter(mAdapter);
                    mPosition = mPosition - 1;
                    if (mPosition < 0)
                        mPosition = 0;
                    mPager.setCurrentItem(mPosition);
                    FileInfo fileInfo = mPhotoList.get(mPosition);
                    mToolBarTitle.setText(fileInfo.name.substring(0, fileInfo.name.lastIndexOf(".")));
                }
            } else {
                if (result_code == ACTION_RESULT_NEED_PERMISSION) {
                    intentDocumentTreeSD();
                } else {
                    createDialog(mContext, "Delete Fail");
                }
            }
        }
    }

    private class RenameTask extends AsyncTask<String, Void, Boolean> {
        private final FileInfo mFileInfo;
        int result_code = ACTION_RESULT_DEFAUT;
        String mimeType;//be used for sendBroadcast Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
        private String mNewDisplayName;
        File mNewFile;//be used for sendBroadcast Intent.ACTION_MEDIA_SCANNER_SCAN_FILE

        public RenameTask(FileInfo fileInfo) {
            mFileInfo = fileInfo;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(String... params) {
            mNewDisplayName = params[0];
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) &&
                    mFileInfo.storagemode == Constant.STORAGEMODE_OTG) {
                DocumentFile dfile = FileFactory.findDocumentFilefromName(mContext, mFileInfo);
                if (dfile.getParentFile().findFile(mNewDisplayName) != null) {
                    result_code = ACTION_RESULT_FILE_EXIST;
                    return false;
                }
                return dfile.renameTo(mNewDisplayName);
            } else if (mFileInfo.storagemode == Constant.STORAGEMODE_SD || mFileInfo.storagemode == Constant.STORAGEMODE_OTG) {
                if (mFileInfo.storagemode == Constant.STORAGEMODE_SD && !hasSDPermission()) {
                    result_code = ACTION_RESULT_NEED_PERMISSION;
                    return false;
                }
                File f = new File(mFileInfo.path);
                File parent = f.getParentFile();
                File f2 = new File(parent, mNewDisplayName);
                mNewFile = f2;
                DocumentFile dfile = FileFactory.findDocumentFilefromName(mContext, mFileInfo);
                if (dfile.getParentFile().findFile(mNewDisplayName) != null) {
                    result_code = ACTION_RESULT_FILE_EXIST;
                    return false;
                }
                mimeType = dfile.getType();
                return dfile.renameTo(mNewDisplayName);
            } else {
                File f = new File(mFileInfo.path);
                File parent = f.getParentFile();
                File f2 = new File(parent, mNewDisplayName);
                mNewFile = f2;
                if (f2.exists()) {
                    result_code = ACTION_RESULT_FILE_EXIST;
                    return false;
                }
                return f.renameTo(f2);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                //SparseArray<ViewGroup> photoInfo = mAdapter.getPhotoInfo();
                //TextView photoName = (TextView) photoInfo.get(mPosition).findViewById(R.id.name);
                TextView photoName = mToolBarTitle;
                //TextView photoPath = (TextView) photoInfo.get(mPosition).findViewById(R.id.path);
                if (mFileInfo.storagemode == Constant.STORAGEMODE_OTG) {
                    String newPath = mFileInfo.path.replace(mFileInfo.name, mNewDisplayName);
                    photoName.setText(mNewDisplayName.substring(0, mNewDisplayName.lastIndexOf(".")));
                    mFileInfo.name = mNewDisplayName;
                    mFileInfo.path = newPath;
                    //photoPath.setText(newPath);
                } else if (mFileInfo.storagemode == Constant.STORAGEMODE_SD) {
                    MediaScannerConnection.scanFile(mContext, new String[]{mNewFile.getPath()}, new String[]{mimeType}, null);
                    photoName.setText(mNewFile.getName().substring(0, mNewFile.getName().lastIndexOf(".")));
                    mFileInfo.name = mNewFile.getName();
                    mFileInfo.path = mNewFile.getPath();
                    //photoPath.setText(mNewFile.getPath());
                } else {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mNewFile)));
                    photoName.setText(mNewFile.getName().substring(0, mNewFile.getName().lastIndexOf(".")));
                    mFileInfo.name = mNewFile.getName();
                    mFileInfo.path = mNewFile.getPath();
                    //photoPath.setText(mNewFile.getPath());
                }
            } else {
                if (result_code == ACTION_RESULT_NEED_PERMISSION) {
                    intentDocumentTreeSD();
                } else {
                    createDialog(mContext, result_code == ACTION_RESULT_FILE_EXIST ? "File exist" : "Fail");
                }
            }
        }
    }

    private class CopyTask extends AsyncTask<DocumentFile, String, Boolean> {
        FileInfo mSource;
        DocumentFile desDfile;//for sendBroadcast Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
        int mDesStorageMode;
        int result_code = ACTION_RESULT_DEFAUT;
        String mDesDirPath;
        File mNewFile;//for sendBroadcast Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
        boolean mDeleteSource;//use for move action

        CopyTask(FileInfo source, int des_storage_mode, String des_dir_path, boolean deleteSource) {
            mSource = source;
            mDesStorageMode = des_storage_mode;
            mDesDirPath = des_dir_path;
            mDeleteSource = deleteSource;
        }

        @Override
        protected Boolean doInBackground(DocumentFile... params) {
            String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
            if (mSource.storagemode == Constant.STORAGEMODE_LOCAL &&
                    mDesStorageMode == Constant.STORAGEMODE_LOCAL) {//local to local

                File parent = new File(mDesDirPath);
                File f2 = new File(parent, mSource.name);
                if (f2.exists()) {
                    result_code = 1;
                    return false;
                }
                mNewFile = f2;
                return copyFile(new File(mSource.path), mNewFile);
            } else if (mSource.storagemode == Constant.STORAGEMODE_LOCAL &&
                    mDesStorageMode == Constant.STORAGEMODE_SD) {//local to sd
                if (!hasSDPermission()) {
                    result_code = ACTION_RESULT_NEED_PERMISSION;
                    return false;
                }
                DocumentFile destDDir = findDocumentFilefromPath(mDesDirPath, sdPath, Constant.mSDRootDocumentFile);

                if (destDDir.findFile(mSource.name) != null) {
                    result_code = 1;
                    return false;
                }
                File parent = new File(mDesDirPath);
                File f2 = new File(parent, mSource.name);
                mNewFile = f2;
                desDfile = destDDir.createFile("image", mSource.name);
                return copydFile(new File(mSource.path), desDfile);
            } else if (mSource.storagemode == Constant.STORAGEMODE_LOCAL &&
                    mDesStorageMode == Constant.STORAGEMODE_OTG) {//local to otg

                DocumentFile destDDir = params[0];
                if (destDDir.findFile(mSource.name) != null) {
                    result_code = 1;
                    return false;
                }
                return copydFile(new File(mSource.path), destDDir.createFile("image", mSource.name));
            } else if (mSource.storagemode == Constant.STORAGEMODE_SD &&
                    mDesStorageMode == Constant.STORAGEMODE_LOCAL) {//sd to local
                if (mDeleteSource && !hasSDPermission()) {
                    result_code = ACTION_RESULT_NEED_PERMISSION;
                    return false;
                }
                DocumentFile sourceDfile = FileFactory.findDocumentFilefromName(mContext, mSource);

                File parent = new File(mDesDirPath);
                File f2 = new File(parent, mSource.name);
                if (f2.exists()) {
                    result_code = 1;
                    return false;
                }
                mNewFile = f2;
                return copydFile(sourceDfile, mNewFile);
            } else if (mSource.storagemode == Constant.STORAGEMODE_SD &&
                    mDesStorageMode == Constant.STORAGEMODE_SD) {//sd to sd
                if (!hasSDPermission()) {
                    result_code = ACTION_RESULT_NEED_PERMISSION;
                    return false;
                }
                DocumentFile sourceDfile = FileFactory.findDocumentFilefromName(mContext, mSource);
                DocumentFile destDDir = findDocumentFilefromPath(mDesDirPath, sdPath, Constant.mSDRootDocumentFile);
                if (destDDir.findFile(mSource.name) != null) {
                    result_code = 1;
                    return false;
                }
                File parent = new File(mDesDirPath);
                File f2 = new File(parent, mSource.name);
                mNewFile = f2;
                return copydFile(sourceDfile, destDDir.createFile("image", mSource.name));
            } else if (mSource.storagemode == Constant.STORAGEMODE_SD &&
                    mDesStorageMode == Constant.STORAGEMODE_OTG) {//sd to otg
                if (mDeleteSource && !hasSDPermission()) {
                    result_code = ACTION_RESULT_NEED_PERMISSION;
                    return false;
                }
                DocumentFile sourceDfile = FileFactory.findDocumentFilefromName(mContext, mSource);
                DocumentFile destDDir = params[0];
                if (destDDir.findFile(mSource.name) != null) {
                    result_code = 1;
                    return false;
                }
                return copydFile(sourceDfile, destDDir.createFile("image", mSource.name));
            } else if (mSource.storagemode == Constant.STORAGEMODE_OTG &&
                    mDesStorageMode == Constant.STORAGEMODE_LOCAL) {//otg to local

                DocumentFile sourceDfile = FileFactory.findDocumentFilefromName(mContext, mSource);

                File parent = new File(mDesDirPath);
                File f2 = new File(parent, mSource.name);
                if (f2.exists()) {
                    result_code = 1;
                    return false;
                }
                mNewFile = f2;
                return copydFile(sourceDfile, mNewFile);
            } else if (mSource.storagemode == Constant.STORAGEMODE_OTG &&
                    mDesStorageMode == Constant.STORAGEMODE_SD) {//otg to sd
                if (!hasSDPermission()) {
                    result_code = ACTION_RESULT_NEED_PERMISSION;
                    return false;
                }
                DocumentFile sourceDfile = FileFactory.findDocumentFilefromName(mContext, mSource);
                DocumentFile destDDir = findDocumentFilefromPath(mDesDirPath, sdPath, Constant.mSDRootDocumentFile);

                if (destDDir.findFile(mSource.name) != null) {
                    result_code = 1;
                    return false;
                }
                File parent = new File(mDesDirPath);
                File f2 = new File(parent, mSource.name);
                mNewFile = f2;
                return copydFile(sourceDfile, destDDir.createFile("image", mSource.name));
            } else if (mSource.storagemode == Constant.STORAGEMODE_OTG &&
                    mDesStorageMode == Constant.STORAGEMODE_OTG) {//otg to otg

                DocumentFile sourceDfile = FileFactory.findDocumentFilefromName(mContext, mSource);
                DocumentFile destDDir = params[0];

                if (destDDir.findFile(mSource.name) != null) {
                    result_code = 1;
                    return false;
                }
                return copydFile(sourceDfile, destDDir.createFile("image", mSource.name));
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                if (mDesStorageMode == Constant.STORAGEMODE_OTG) {
                    //createFailDialog(mContext, "OTG");
                } else if (mDesStorageMode == Constant.STORAGEMODE_SD) {
                    MediaScannerConnection.scanFile(mContext, new String[]{mNewFile.getPath()}, new String[]{desDfile.getType()}, null);
                    //createFailDialog(mContext, mNewFile.getPath()+" "+desDfile.getType());
                } else if (mDesStorageMode == Constant.STORAGEMODE_LOCAL) {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mNewFile)));
                    //createFailDialog(mContext,Uri.fromFile(mNewFile).toString());
                }
                if (mDeleteSource) {
                    if (mSource.storagemode == Constant.STORAGEMODE_LOCAL) {
                        File f = new File(mSource.path);
                        f.delete();
                    } else if (mSource.storagemode == Constant.STORAGEMODE_SD) {
                        if (Constant.mSDRootDocumentFile != null) {
                            DocumentFile df = FileFactory.findDocumentFilefromName(mContext, mSource);
                            df.delete();
                        }
                    } else {
                        DocumentFile df = FileFactory.findDocumentFilefromName(mContext, mSource);
                        df.delete();
                        //mSource.path = mNewFile.getPath();
                    }
                    mPhotoList.remove(mSource);
                    mAdapter = null;
                    mAdapter = new CustomPagerAdapter(mContext);
                    mPager.setAdapter(mAdapter);
                    mPosition = mPosition - 1;
                    if (mPosition < 0)
                        mPosition = 0;
                    mPager.setCurrentItem(mPosition);
                    FileInfo fileInfo = mPhotoList.get(mPosition);
                    mToolBarTitle.setText(fileInfo.name.substring(0, fileInfo.name.lastIndexOf(".")));
                }
                createDialog(mContext, "Action finish");
            } else {
                if (result_code == ACTION_RESULT_NEED_PERMISSION) {
                    intentDocumentTreeSD();
                } else {
                    createDialog(mContext, result_code == 1 ? "File exist" : "Fail");
                }
            }
        }

        private boolean copydFile(DocumentFile source, DocumentFile destination) {
            if (source == null)
                return false;
            InputStream in = null;
            OutputStream out = null;
            try {
                in = getContentResolver().openInputStream(source.getUri());
                out = getContentResolver().openOutputStream(destination.getUri());
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean copydFile(DocumentFile source, File destination) {
            if (source == null)
                return false;
            InputStream in = null;
            OutputStream out = null;
            try {
                in = getContentResolver().openInputStream(source.getUri());
                out = new FileOutputStream(destination);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean copydFile(File source, DocumentFile destination) {
            if (destination == null)
                return false;
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new FileInputStream(source);
                out = getContentResolver().openOutputStream(destination.getUri());
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean copyFile(File source, File destination) {
            try {
                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(destination);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        private DocumentFile findDocumentFilefromPath(String des_path, String sd_Path, DocumentFile currentDocumentFile){
            DocumentFile mDocumentFile = null;

            if(currentDocumentFile != null){
                String path = des_path;
                path = path.replace(sd_Path, "");
                String[] array = path.split("/");
                DocumentFile tmp = currentDocumentFile;
                for(int i= 1;i<array.length;i++){
                    tmp = tmp.findFile(array[i]);
                }
                mDocumentFile = tmp;
            }
            return mDocumentFile;
        }
    }

    private boolean hasSDPermission(){
        String uid = FileFactory.getSDCardUniqueId();
        String sdKey = LocalPreferences.getSDKey(mContext, uid);
        if(sdKey != ""){
            Uri uriSDKey = Uri.parse(sdKey);
            Constant.mSDCurrentDocumentFile = Constant.mSDRootDocumentFile = DocumentFile.fromTreeUri(this, uriSDKey);
            return true;
        }
        return false;
    }

    private void intentDocumentTreeSD() {
        new SDPermissionGuideDialog(this) {
            @Override
            public void onConfirm(Boolean isClick) {
                if (isClick) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(intent, SD_PERMISSION_REQUEST_CODE);
                }
            }
        };
    }

    private boolean checkSD(Uri uri){
        if (!uri.toString().contains("primary")) {
            if (uri != null) {
                if(uri.getPath().toString().split(":").length > 1){
                    snackBarShow(R.string.snackbar_plz_select_top);
                    intentDocumentTreeSD();
                }else{
                    Constant.mSDCurrentDocumentFile = Constant.mSDRootDocumentFile = DocumentFile.fromTreeUri(this, uri);//sd root path
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    String uid = FileFactory.getSDCardUniqueId();
                    LocalPreferences.setSDKey(this, uid, uri.toString());
                    //ArrayList<DocumentFile> tmpDFiles = new ArrayList<>();
                    //tmpDFiles.add(rootDir);
                    //ActionParameter.dFiles = tmpDFiles;
                    return true;
                }
            }

        }else {
            snackBarShow(R.string.snackbar_plz_select_sd);
            intentDocumentTreeSD();
        }
        return false;
    }

    private void snackBarShow(int resId) {
        Snackbar.make(mRootLayout, resId, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    private void createInfoDialog(Context context, FileInfo fileInfo, int dialog_size) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View mInfoDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_info, null);
        ((TextView) mInfoDialogView.findViewById(R.id.name)).setText(fileInfo.name);
        mInfoDialogView.findViewById(R.id.type).setVisibility(View.GONE);
        mInfoDialogView.findViewById(R.id.type_title).setVisibility(View.GONE);
        if (fileInfo.format_size == null) {
            ((TextView) mInfoDialogView.findViewById(R.id.size)).setText(Formatter.formatFileSize(context, fileInfo.size));
        } else {
            ((TextView) mInfoDialogView.findViewById(R.id.size)).setText(fileInfo.format_size);
        }
        ((TextView) mInfoDialogView.findViewById(R.id.modify_time)).setText(fileInfo.time);
        ((TextView) mInfoDialogView.findViewById(R.id.path)).setText(fileInfo.path);

        builder.setView(mInfoDialogView);
        builder.setTitle(context.getResources().getString(R.string.info_title));
        builder.setIcon(R.mipmap.ic_info_gray);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(dialog_size, dialog_size);
    }

    private void createDialog(Context context, final String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.mipmap.ic_info_gray);

        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void startDestinationActivity(int actionId){
        Intent intent = new Intent();
        Bundle args = new Bundle();
        args.putInt("actionId", actionId);
        intent.putExtras(args);
        intent.setClass(PhotoActivity.this, DestinationActivity.class);
        startActivityForResult(intent, DestinationActivity.REQUEST_CODE);
    }

    private class BottomToolBarClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            final FileInfo fileinfo = mPhotoList.get(mPosition);
            if (view == mInfoView) {
                createInfoDialog(mContext, fileinfo, MainActivity.mScreenW);
            } else if (view == mDeleteView) {
                ArrayList<FileInfo> deleteOneFiles = new ArrayList<FileInfo>();
                deleteOneFiles.add(fileinfo);
                new LocalDeleteDialog(mContext, deleteOneFiles) {
                    @Override
                    public void onConfirm(ArrayList<FileInfo> selectedFiles) {
                        new DeleteTask(fileinfo).execute();
                    }
                };
            } else if (view == mShareView) {
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) &&
                        fileinfo.storagemode == Constant.STORAGEMODE_OTG) {

                } else {
                    Intent share_intent = new Intent(Intent.ACTION_SEND);
                    share_intent.putExtra(Intent.EXTRA_STREAM, fileinfo.uri);
                    share_intent.setType("image/*");
                    share_intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(share_intent, EDIT_REQUEST_CODE);
                }
            }
        }
    }
}
