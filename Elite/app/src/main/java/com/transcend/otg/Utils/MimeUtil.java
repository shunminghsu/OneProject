package com.transcend.otg.Utils;

import org.apache.commons.io.FilenameUtils;

/**
 * Created by wangbojie on 2017/2/6.
 */

public class MimeUtil {
    private static final String IMAGE = "image";
    private static final String VIDEO = "video";
    private static final String AUDIO = "audio";
    private static final String ENCRYPT = "enc";


    public static boolean isPhoto(String path) {
        String mime = getMimeType(path);
        if (mime == null)
            return false;
        return mime.contains(IMAGE);
    }

    public static boolean isVideo(String path) {
        String mime = getMimeType(path);
        if (mime == null)
            return false;
        return mime.contains(VIDEO);
    }

    public static boolean isMusic(String path) {
        String mime = getMimeType(path);
        if (mime == null)
            return false;
        return mime.contains(AUDIO);
    }

    public static boolean isEncrypt(String path) {
        String mime = getMimeType(path);
        if (mime == null)
            return false;
        return mime.contains(ENCRYPT);
    }

    public static boolean isDocument(String path) {
        String ext = FilenameUtils.getExtension(path);
        if ("doc".equals(ext))
            return true;
        if ("docx".equals(ext))
            return true;
        if ("xls".equals(ext))
            return true;
        if ("ppt".equals(ext))
            return true;
        if ("pdf".equals(ext))
            return true;
        if ("txt".equals(ext))
            return true;
        return false;
    }

    public static String getMimeType(String path) {
        String ext = FilenameUtils.getExtension(path);
        if (ext != null) {
            return MimeTypeMapExt.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
        }
        return null;
    }
}
