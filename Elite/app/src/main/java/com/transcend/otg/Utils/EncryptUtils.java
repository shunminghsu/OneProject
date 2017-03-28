package com.transcend.otg.Utils;

import android.support.v4.provider.DocumentFile;

import com.transcend.otg.Constant.FileInfo;

import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/3/22.
 */
public class EncryptUtils {
    public static ArrayList<DocumentFile> selectedDocumentFile = new ArrayList<DocumentFile>();
    public static DocumentFile afterEncryptDFile = null;
    public static String encryptFileName = "";
    public static String password = "";
    public static String afterEncryptPath = "";
    public static String beforeEncryptPath = "";
    public static ArrayList<FileInfo> selectLocalFile = new ArrayList<FileInfo>();
    public static String copyToSDPath = "";

    public static String getCopyToSDPath() {
        return copyToSDPath;
    }

    public static void setCopyToSDPath(String copyToSDPath) {
        EncryptUtils.copyToSDPath = copyToSDPath;
    }

    public static ArrayList<DocumentFile> getSelectedDocumentFile() {
        return selectedDocumentFile;
    }

    public static void setSelectedDocumentFile(ArrayList<DocumentFile> selectedDocumentFile) {
        EncryptUtils.selectedDocumentFile = selectedDocumentFile;
    }

    public static DocumentFile getAfterEncryptDFile() {
        return afterEncryptDFile;
    }

    public static void setAfterEncryptDFile(DocumentFile afterEncryptDFile) {
        EncryptUtils.afterEncryptDFile = afterEncryptDFile;
    }

    public static String getEncryptFileName() {
        return encryptFileName;
    }

    public static void setEncryptFileName(String encryptFileName) {
        EncryptUtils.encryptFileName = encryptFileName;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        EncryptUtils.password = password;
    }

    public static String getAfterEncryptPath() {
        return afterEncryptPath;
    }

    public static void setAfterEncryptPath(String afterEncryptPath) {
        EncryptUtils.afterEncryptPath = afterEncryptPath;
    }

    public static String getBeforeEncryptPath() {
        return beforeEncryptPath;
    }

    public static void setBeforeEncryptPath(String beforeEncryptPath) {
        EncryptUtils.beforeEncryptPath = beforeEncryptPath;
    }

    public static ArrayList<FileInfo> getSelectLocalFile() {
        return selectLocalFile;
    }

    public static void setSelectLocalFile(ArrayList<FileInfo> selectLocalFile) {
        EncryptUtils.selectLocalFile = selectLocalFile;
    }
}
