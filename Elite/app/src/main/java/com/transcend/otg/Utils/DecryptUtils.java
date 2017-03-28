package com.transcend.otg.Utils;

import android.support.v4.provider.DocumentFile;

import com.transcend.otg.Constant.FileInfo;

import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/3/24.
 */

public class DecryptUtils {
    public static ArrayList<DocumentFile> selectedDocumentFile = new ArrayList<DocumentFile>();
    public static DocumentFile afterDecryptDFile = null;
    public static String decryptFileName = "";
    public static String password = "";
    public static String afterDecryptPath = "";
    public static String beforeDecryptPath = "";

    public static String getCopyToSDPath() {
        return copyToSDPath;
    }

    public static void setCopyToSDPath(String copyToSDPath) {
        DecryptUtils.copyToSDPath = copyToSDPath;
    }

    public static String copyToSDPath = "";

    public static ArrayList<DocumentFile> getSelectedDocumentFile() {
        return selectedDocumentFile;
    }

    public static void setSelectedDocumentFile(ArrayList<DocumentFile> selectedDocumentFile) {
        DecryptUtils.selectedDocumentFile = selectedDocumentFile;
    }

    public static DocumentFile getAfterDecryptDFile() {
        return afterDecryptDFile;
    }

    public static void setAfterDecryptDFile(DocumentFile afterDecryptDFile) {
        DecryptUtils.afterDecryptDFile = afterDecryptDFile;
    }

    public static String getDecryptFileName() {
        return decryptFileName;
    }

    public static void setDecryptFileName(String decryptFileName) {
        DecryptUtils.decryptFileName = decryptFileName;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        DecryptUtils.password = password;
    }

    public static String getAfterDecryptPath() {
        return afterDecryptPath;
    }

    public static void setAfterDecryptPath(String afterDecryptPath) {
        DecryptUtils.afterDecryptPath = afterDecryptPath;
    }

    public static String getBeforeDecryptPath() {
        return beforeDecryptPath;
    }

    public static void setBeforeDecryptPath(String beforeDecryptPath) {
        DecryptUtils.beforeDecryptPath = beforeDecryptPath;
    }

}
