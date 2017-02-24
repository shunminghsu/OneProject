package com.transcend.otg.Constant;

import android.net.Uri;
import com.transcend.otg.Utils.MimeUtil;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by wangbojie on 2017/2/6.
 */

public class FileInfo implements Serializable {

    public enum TYPE {
        DIR,
        PHOTO,
        VIDEO,
        MUSIC,
        FILE,
        ENCRYPT
    }

    public String path;

    public String name;
    public String time;
    public TYPE type;
    public Uri uri;
    public long size;
    public String format_size;
    public long album_id;
    public boolean checked;

    public FileInfo() {

    }

    public static TYPE getType(String path) {
        if (MimeUtil.isPhoto(path))
            return TYPE.PHOTO;
        if (MimeUtil.isVideo(path))
            return TYPE.VIDEO;
        if (MimeUtil.isMusic(path))
            return TYPE.MUSIC;
        if (MimeUtil.isEncrypt(path))
            return TYPE.ENCRYPT;
        return TYPE.FILE;
    }

    public static String getTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date(time));
    }

    public static Date getDate(String time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static ArrayList<String> getSDCardFileName(String mPath) {
        ArrayList<String> sdName = new ArrayList<String>();
        File dir = new File(mPath);

        File files[] = dir.listFiles();
        for (File file : files) {
            if (file.isHidden())
                continue;
            String name = file.getName();
            sdName.add(name);
        }
        return sdName;
    }
}
