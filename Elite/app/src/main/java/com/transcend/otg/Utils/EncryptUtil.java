package com.transcend.otg.Utils;

import android.support.v4.provider.DocumentFile;

import com.transcend.otg.Constant.FileInfo;

import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/3/22.
 */
public class EncryptUtil {
    public static ArrayList<DocumentFile> selectDocumentFile = new ArrayList<DocumentFile>();
    public static String encryptFileName = "";
    public static String password = "";
    public static String afterEncryptPath = "";
    public static String beforeEncryptPath = "";

    public static ArrayList<FileInfo> selectLocalFile = new ArrayList<FileInfo>();

    public static ArrayList<DocumentFile> getSelectDocumentFile() {
        return selectDocumentFile;
    }

    public static void setSelectDocumentFile(ArrayList<DocumentFile> selectDocumentFile) {
        EncryptUtil.selectDocumentFile = selectDocumentFile;
    }

    public static String getEncryptFileName() {
        return encryptFileName;
    }

    public static void setEncryptFileName(String encryptFileName) {
        EncryptUtil.encryptFileName = encryptFileName;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        EncryptUtil.password = password;
    }

    public static String getAfterEncryptPath() {
        return afterEncryptPath;
    }

    public static void setAfterEncryptPath(String afterEncryptPath) {
        EncryptUtil.afterEncryptPath = afterEncryptPath;
    }

    public static String getBeforeEncryptPath() {
        return beforeEncryptPath;
    }

    public static void setBeforeEncryptPath(String beforeEncryptPath) {
        EncryptUtil.beforeEncryptPath = beforeEncryptPath;
    }

    public static ArrayList<FileInfo> getSelectLocalFile() {
        return selectLocalFile;
    }

    public static void setSelectLocalFile(ArrayList<FileInfo> selectLocalFile) {
        EncryptUtil.selectLocalFile = selectLocalFile;
    }
}
