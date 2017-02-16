package com.transcend.otg.Loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.transcend.otg.Browser.BrowserFragment;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Utils.FileFactory;
import com.transcend.otg.Utils.FileInfoSort;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by henry_hsu on 2017/2/10.
 */

public class TabInfoLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {
    private ArrayList<FileInfo> mFileList;
    private Context mContext;
    private int mType;
    private String mOuterStoragePath;

    public TabInfoLoader(Context context, int type, String outer_path) {
        super(context);
        mType = type;
        mFileList = new ArrayList<>();
        mContext = context;
        mOuterStoragePath = outer_path;
    }

    @Override
    public ArrayList<FileInfo> loadInBackground() {
        Log.d("henry" ,"loadInBackground" );
        mFileList.clear();
        switch(mType) {
            case BrowserFragment.LIST_TYPE_IMAGE:
                return getAllImages();
            case BrowserFragment.LIST_TYPE_VIDEO:
                return getAllVideos();
            case BrowserFragment.LIST_TYPE_MUSIC:
                return getAllMusics();
            case BrowserFragment.LIST_TYPE_DOCUMENT:
                return getAllDocs();
            case BrowserFragment.LIST_TYPE_FOLDER:
                return updateFileList();
            default:
                return getAllImages();
        }
    }

    @Override
    protected void onStartLoading() {
        Log.d("henry" ,"onStartLoading" );
        if (takeContentChanged() || mFileList.size() == 0)
            forceLoad();
    }

    @Override
    protected void onStopLoading() {
        Log.d("henry" ,"onStopLoading" );
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        cancelLoad();
    }

    private ArrayList<FileInfo> getAllImages() {
        try {
            String[] proj = {MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_ADDED};

            final String orderBy = MediaStore.Images.Media.DATE_ADDED;
            Cursor imagecursor = mContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj,
                    null, null, orderBy + " DESC");
            if (imagecursor != null) {
                while (imagecursor.moveToNext()) {
                    int pathColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    int nameColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                    int timeColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
                    int sizeColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.SIZE);

                    String picPath = imagecursor.getString(pathColumnIndex);
                    String picName = imagecursor.getString(nameColumnIndex);
                    String picTime = imagecursor.getString(timeColumnIndex);
                    String picSize = imagecursor.getString(sizeColumnIndex);
                    File picFile = new File(picPath);
                    if (picFile.exists()) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = picPath;
                        fileInfo.name = picName;
                        fileInfo.time = picTime;
                        fileInfo.type = FileInfo.TYPE.PHOTO;
                        fileInfo.size = Long.valueOf(picSize);
                        if (mOuterStoragePath == null) {
                            if (picPath.contains(Constant.ROOT_LOCAL))
                                mFileList.add(fileInfo);
                        } else {
                            if (picPath.contains(mOuterStoragePath))
                                mFileList.add(fileInfo);
                        }
                    }
                }
            }
            imagecursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return mFileList;
        }
        return mFileList;
    }

    private ArrayList<FileInfo> getAllMusics() {
        try {
            String[] proj = {MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.DATE_ADDED};

            final String orderBy = MediaStore.Audio.Media.DATE_ADDED;
            Cursor musiccursor = mContext.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj,
                    null, null, orderBy + " DESC");
            if (musiccursor != null) {
                while (musiccursor.moveToNext()) {
                    int pathColumnIndex = musiccursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                    int nameColumnIndex = musiccursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                    int timeColumnIndex = musiccursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
                    int sizeColumnIndex = musiccursor.getColumnIndex(MediaStore.Audio.Media.SIZE);

                    String musicPath = musiccursor.getString(pathColumnIndex);
                    String musicName = musiccursor.getString(nameColumnIndex);
                    String musicTime = musiccursor.getString(timeColumnIndex);
                    String musicSize = musiccursor.getString(sizeColumnIndex);
                    File musicFile = new File(musicPath);
                    if (musicFile.exists()) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = musicPath;
                        fileInfo.name = musicName;
                        fileInfo.time = musicTime;
                        fileInfo.type = FileInfo.TYPE.MUSIC;
                        fileInfo.size = Long.valueOf(musicSize);
                        if (mOuterStoragePath == null) {
                            if (musicPath.contains(Constant.ROOT_LOCAL))
                                mFileList.add(fileInfo);
                        } else {
                            if (musicPath.contains(mOuterStoragePath))
                                mFileList.add(fileInfo);
                        }
                    }
                }
            }
            musiccursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return mFileList;
        }
        return mFileList;
    }

    private ArrayList<FileInfo> getAllVideos() {
        try {
            String[] videoTypes = {MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DATE_ADDED};
            final String orderBy = MediaStore.Video.Media.DATE_ADDED;
            Cursor videocursor = mContext. getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    videoTypes, null, null, orderBy + " DESC");

            if (videocursor != null) {
                while (videocursor.moveToNext()) {
                    int pathColumnIndex = videocursor.getColumnIndex(MediaStore.Video.Media.DATA);
                    int nameColumnIndex = videocursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
                    int timeColumnIndex = videocursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED);
                    int sizeColumnIndex = videocursor.getColumnIndex(MediaStore.Video.Media.SIZE);

                    String videoPath = videocursor.getString(pathColumnIndex);
                    String videoName = videocursor.getString(nameColumnIndex);
                    String videoTime = videocursor.getString(timeColumnIndex);
                    String videoSize = videocursor.getString(sizeColumnIndex);
                    File videoFile = new File(videoPath);
                    if (videoFile.exists()) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = videoPath;
                        fileInfo.name = videoName;
                        fileInfo.time = videoTime;
                        fileInfo.type = FileInfo.TYPE.VIDEO;
                        fileInfo.size = Long.valueOf(videoSize);
                        if (mOuterStoragePath == null) {
                            if (videoPath.contains(Constant.ROOT_LOCAL))
                                mFileList.add(fileInfo);
                        } else {
                            if (videoPath.contains(mOuterStoragePath))
                                mFileList.add(fileInfo);
                        }
                    }
                }
            }
            videocursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return mFileList;
        }
        return mFileList;
    }

    private ArrayList<FileInfo> getAllDocs() {
        try {
            String[] proj = {MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.SIZE};

            final String orderBy = MediaStore.Files.FileColumns.DATE_ADDED;

            String select = "(" + MediaStore.Files.FileColumns.DATA + " LIKE '%.doc'" + " or "
                                + MediaStore.Files.FileColumns.DATA + " LIKE '%.docx'" + " or "
                                + MediaStore.Files.FileColumns.DATA + " LIKE '%.xls'" + " or "
                                + MediaStore.Files.FileColumns.DATA + " LIKE '%.ppt'" + " or "
                                + MediaStore.Files.FileColumns.DATA + " LIKE '%.pdf'" + " or "
                                + MediaStore.Files.FileColumns.DATA + " LIKE '%.txt'" + ")";

            Cursor docscursor = mContext.getContentResolver().query(
                    MediaStore.Files.getContentUri("external"), proj, select, null, orderBy + " DESC");
            if (docscursor != null) {
                while (docscursor.moveToNext()) {
                    int pathColumnIndex = docscursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    int timeColumnIndex = docscursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED);
                    int sizeColumnIndex = docscursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);

                    String docPath = docscursor.getString(pathColumnIndex);
                    String docTime = docscursor.getString(timeColumnIndex);
                    String docSize = docscursor.getString(sizeColumnIndex);
                    File docFile = new File(docPath);
                    if (docFile.exists()) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = docPath;
                        fileInfo.name = docFile.getName();
                        fileInfo.time = docTime;
                        fileInfo.type = FileInfo.TYPE.FILE;
                        fileInfo.size = Long.valueOf(docSize);
                        if (mOuterStoragePath == null) {
                            if (docPath.contains(Constant.ROOT_LOCAL))
                                mFileList.add(fileInfo);
                        } else {
                            if (docPath.contains(mOuterStoragePath))
                                mFileList.add(fileInfo);
                        }
                    }
                }
            }
            docscursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return mFileList;
        }
        return mFileList;
    }

    private ArrayList<FileInfo> updateFileList() {
        String path;
        if (mOuterStoragePath == null)
            path = Constant.ROOT_LOCAL;
        else
            path = FileFactory.getSdPath(mContext);
        File dir = new File(path);
        if (!dir.exists())
            return mFileList;
        File files[] = dir.listFiles();
        for (File file : files) {
            if (file.isHidden())
                continue;
            FileInfo fileInfo = new FileInfo();
            fileInfo.path = file.getPath();
            fileInfo.name = file.getName();
            fileInfo.time = FileInfo.getTime(file.lastModified());
            fileInfo.type = file.isFile() ? FileInfo.getType(file.getPath()) : FileInfo.TYPE.DIR;
            fileInfo.size = file.length();
            mFileList.add(fileInfo);
        }
        Collections.sort(mFileList, FileInfoSort.comparator(mContext));
        FileFactory.getInstance().addFolderFilterRule(path, mFileList);
        FileFactory.getInstance().addFileTypeSortRule(mFileList);

        return mFileList;
    }
}
