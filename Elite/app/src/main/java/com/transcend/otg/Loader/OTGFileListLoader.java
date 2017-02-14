package com.transcend.otg.Loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.transcend.otg.Browser.BrowserFragment;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wangbojie on 2017/2/13.
 */

public class OTGFileListLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {
    private String TAG = OTGFileListLoader.class.getSimpleName();
    private ArrayList<FileInfo> mFileList;
    private ArrayList<FileInfo> mAllFileList;
    private Context mContext;
    private int mType;
    private UsbMassStorageDevice device;
    private FileSystem currentFs;
    private UsbFile root;
    private List<UsbFile> files;

    public OTGFileListLoader(Context context, int type) {
        super(context);
        mFileList = new ArrayList<>();
        mAllFileList = new ArrayList<>();
        mContext = context;
        setupDevice();
    }

    private void setupDevice() {
        try {
            device = Constant.nowDevice;
            currentFs = device.getPartitions().get(0).getFileSystem();
            Log.d(TAG, "Capacity: " + currentFs.getCapacity());
            Log.d(TAG, "Occupied Space: " + currentFs.getOccupiedSpace());
            Log.d(TAG, "Free Space: " + currentFs.getFreeSpace());
            Log.d(TAG, "Chunk size: " + currentFs.getChunkSize());
            root = currentFs.getRootDirectory();

        } catch (Exception e) {
            Log.e(TAG, "error setting up device", e);
        }

    }

    @Override
    public ArrayList<FileInfo> loadInBackground() {
        mFileList.clear();
        switch (mType) {
            case BrowserFragment.LIST_TYPE_IMAGE:
                return getFileList();
//            case BrowserFragment.LIST_TYPE_VIDEO:
//                return getAllVideos();
//            case BrowserFragment.LIST_TYPE_MUSIC:
//                return getAllMusics();
//            case BrowserFragment.LIST_TYPE_DOCUMENT:
//                return getAllDocs();
            default:
                return getFileList();
        }
    }

    @Override
    protected void onStartLoading() {
        if (takeContentChanged() || mFileList.size() == 0)
            forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }


    private ArrayList<FileInfo> getFileList() {
        files = new ArrayList<>();
        if (root != null) {
            try {
                files = Arrays.asList(root.listFiles());
                for (UsbFile file : files) {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.path = file.getName();
                    fileInfo.name = file.getName();
                    fileInfo.time = FileInfo.getTime(file.lastModified());
                    fileInfo.type = !file.isDirectory() ? FileInfo.getType(file.getName()) : FileInfo.TYPE.DIR;
                    if (file.isDirectory())
                        fileInfo.size = 0;
                    else
                        fileInfo.size = file.getLength();
                    mFileList.add(fileInfo);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return mFileList;
    }

    private ArrayList<FileInfo> getAllImages() {
        for (UsbFile file : files){
            if (file.isDirectory()){
                exploreDirectory(file);
            }else {

            }
        }
        return mFileList;
    }


    private void exploreDirectory(UsbFile list){
        try {
            UsbFile[] inList = list.listFiles();
            if(list.isDirectory()){

            }else{

            }
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

}
