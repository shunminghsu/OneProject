package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.transcend.otg.Constant.FileInfo;

import java.io.File;
import java.util.ArrayList;

/*
 * Created by wangbojie on 2017/2/8.
 */

public class LocalTypeListLoader extends AsyncTaskLoader<Boolean> {

    private ArrayList<FileInfo>  mFileListImage, mFileListMusic, mFileListVideo, mFileListDoc;
    private Context mContext;

    public LocalTypeListLoader(Context context) {
        super(context);
        mContext = context;
        mFileListImage = new ArrayList<>();
        mFileListMusic = new ArrayList<>();
        mFileListVideo = new ArrayList<>();
        mFileListDoc = new ArrayList<>();
    }

    @Override
    public Boolean loadInBackground() {
        return updateFileList();
    }

    private boolean updateFileList() {
        boolean b_img = getAllImages();
        boolean b_music = getAllMusics();
        boolean b_video = getAllVideos();
        boolean b_doc = getAllDocs();

        return b_img && b_music && b_video && b_doc;
    }

    private boolean getAllImages() {
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
                        mFileListImage.add(fileInfo);
                    }
                }
            }
            imagecursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean getAllMusics() {
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
                        mFileListMusic.add(fileInfo);
                    }
                }
            }
            musiccursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean getAllVideos() {
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
                        mFileListVideo.add(fileInfo);
                    }
                }
            }
            videocursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean getAllDocs() {
        try {
            String[] proj = {MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.TITLE,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.SIZE};

            final String orderBy = MediaStore.Files.FileColumns.DATE_ADDED;
            String selection = MediaStore.Files.FileColumns.MIME_TYPE + "= ? "
                    + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                    + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                    + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                    + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? ";
            String[] selectionArgs = new String[]{"text/plain", "application/msword", "application/pdf", "application/vnd.ms-powerpoint", "application/vnd.ms-excel"};

            Cursor docscursor = mContext.getContentResolver().query(
                    MediaStore.Files.getContentUri("external"), proj, selection, selectionArgs, orderBy + " DESC");
            if (docscursor != null) {
                while (docscursor.moveToNext()) {
                    int pathColumnIndex = docscursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    int nameColumnIndex = docscursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE);
                    int timeColumnIndex = docscursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED);
                    int sizeColumnIndex = docscursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);

                    String docPath = docscursor.getString(pathColumnIndex);
                    String docName = docscursor.getString(nameColumnIndex);
                    String docTime = docscursor.getString(timeColumnIndex);
                    String docSize = docscursor.getString(sizeColumnIndex);
                    File docFile = new File(docPath);
                    if (docFile.exists()) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.path = docPath;
                        fileInfo.name = docName;
                        fileInfo.time = docTime;
                        fileInfo.type = FileInfo.TYPE.FILE;
                        fileInfo.size = Long.valueOf(docSize);
                        mFileListDoc.add(fileInfo);
                    }
                }
            }
            docscursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }



    public ArrayList<FileInfo> getImageList() {
        return mFileListImage;
    }

    public ArrayList<FileInfo> getMusicList() {
        return mFileListMusic;
    }

    public ArrayList<FileInfo> getVideoList() {
        return mFileListVideo;
    }

    public ArrayList<FileInfo> getDocList() {
        return mFileListDoc;
    }
}
