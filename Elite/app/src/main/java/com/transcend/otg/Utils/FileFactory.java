package com.transcend.otg.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.R;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangbojie on 2017/2/6.
 */

public class FileFactory {
    private static final String TAG = FileFactory.class.getSimpleName();
    private static FileFactory mFileFactory;
    private static final Object mMute = new Object();
    private List<String> mNotificationList;
    private Map<String, String> mRealPathMap;
    private int RealPathMapLifeCycle = 10;

    //try to return sdcard path, if sdcard not found, return null
    public static String getOuterStoragePath(Context mContext, String key_word) {
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = null;
            Method getPath = null;
            Method isRemovable = null;
            Method getState = null;
            try {
                getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
                getPath = storageVolumeClazz.getMethod("getPath");
                isRemovable = storageVolumeClazz.getMethod("isRemovable");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                Boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (removable && path != null) {
                    if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                        return path;
                    } else if (path.toLowerCase().contains(key_word)) {
                        return path;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getOTGStoragePath(Context mContext, String key_word){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            return File.separator + mContext.getResources().getString(R.string.nav_otg);
        }else{
            StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
            Class<?> storageVolumeClazz = null;
            try {
                storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
                Method getVolumeList = null;
                Method getPath = null;
                Method isRemovable = null;
                Method getState = null;
                try {
                    getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
                    getPath = storageVolumeClazz.getMethod("getPath");
                    isRemovable = storageVolumeClazz.getMethod("isRemovable");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

                Object result = getVolumeList.invoke(mStorageManager);
                final int length = Array.getLength(result);
                for (int i = 0; i < length; i++) {
                    Object storageVolumeElement = Array.get(result, i);
                    String path = (String) getPath.invoke(storageVolumeElement);
                    Boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                    if (removable && path != null) {
                        if (path.toLowerCase().contains(key_word)) {
                            return path;
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return File.separator + mContext.getResources().getString(R.string.nav_otg);
        }
    }

    //try to return the state of path, if path not found, return null
    public static Boolean getMountedState(Context mContext, String _path) {
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = null;
            Method getPath = null;
            Method isRemovable = null;
            Method getState = null;
            try {
                getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
                getPath = storageVolumeClazz.getMethod("getPath");
                isRemovable = storageVolumeClazz.getMethod("isRemovable");
                getState = storageVolumeClazz.getMethod("getState");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                String state = (String) getState.invoke(storageVolumeElement);
                Boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (removable) {
                    if (path != null && path.equals(_path)) {
                        return state.equals("mounted");
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<File> getStoragePath(Context mContext) {
        List<File> stgList = new ArrayList<File>();
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = null;
            Method getPath = null;
            Method isRemovable = null;
            try {
                getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
                getPath = storageVolumeClazz.getMethod("getPath");
                isRemovable = storageVolumeClazz.getMethod("isRemovable");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            Method getSubSystem = null;
            try {
                getSubSystem = storageVolumeClazz.getMethod("getSubSystem");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                String subSystem = "";
                if (getSubSystem != null) {
                    subSystem = (String) getSubSystem.invoke(storageVolumeElement);
                }
                if (!subSystem.contains("usb")) {
                    if (!path.toLowerCase().contains("private")) {
                        stgList.add(new File(path));
                    }

                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return stgList;
    }

    public static List<File> getStorages1(Context ctx) {
        StorageManager stgMgr = (StorageManager) ctx.getSystemService(Context.STORAGE_SERVICE);
        List<File> stgList = new ArrayList<File>();

        try {
            String[] paths = (String[]) stgMgr.getClass().getMethod("getVolumePaths").invoke(stgMgr);
            for (int i = 0; i < paths.length; i++) {
                String status = (String) stgMgr.getClass().getMethod("getVolumeState", String.class).invoke(stgMgr, paths[i]);
                if (status.equals(Environment.MEDIA_MOUNTED))
                    stgList.add(new File(paths[i]));
            }

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return stgList;
    }


    public FileFactory() {
        mNotificationList = new ArrayList<String>();
        mRealPathMap = new HashMap<String, String>();
    }

    public static FileFactory getInstance() {
        synchronized (mMute) {
            if (mFileFactory == null)
                mFileFactory = new FileFactory();
        }
        return mFileFactory;
    }

    public void addFileTypeSortRule(ArrayList<FileInfo> fileList) {
        ArrayList<FileInfo> tmp = new ArrayList<FileInfo>();
        for (FileInfo file : fileList) {
            tmp.add(file);
        }
        fileList.clear();
        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                for (FileInfo file : tmp) {
                    if (file.type == Constant.TYPE_DIR) {
                        fileList.add(file);
                    }
                }
            }

            if (i == 1) {
                for (FileInfo file : tmp) {
                    if (file.type != Constant.TYPE_DIR) {
                        fileList.add(file);
                    }
                }
            }
        }

    }

    public void addFolderFilterRule(String path, ArrayList<FileInfo> fileList) {
        for (FileInfo file : fileList) {
            if (file.name.equals("homes")) {
                fileList.remove(file);
                break;
            }
        }

        for (FileInfo file : fileList) {
            if (file.name.equals("Public")) {
                fileList.remove(file);
                fileList.add(0, file);
                break;
            }
        }

        for (FileInfo file : fileList) {
            if (file.time.startsWith("1970/01/01")) {
                file.time = "";
            }
        }
    }

    public String getPhotoPath(boolean thumbnail, String path) {
        String url;
        url = "file://" + path;
        return url;
    }

    public boolean doFileNameCompare(DocumentFile[] tmpDFile, ArrayList<String> tmpFile) {
        int fileCount = 0;
        for (int fi = 0; fi < tmpFile.size(); fi++) {
            String name = tmpFile.get(fi);
            for (int df = 0; df < tmpDFile.length; df++) {
                if (name.equals(tmpDFile[df].getName())) {
                    fileCount++;
                    break;
                }
            }
        }
        if (fileCount == tmpFile.size()) {
            return true;
        } else {
            return false;
        }
    }

    public int getNotificationID() {
        int id = 1;
        if (mNotificationList.size() > 0) {
            String value = mNotificationList.get(mNotificationList.size() - 1);
            id = Integer.parseInt(value) + 1;
            mNotificationList.add(Integer.toString(id));
        } else {
            mNotificationList.add(Integer.toString(id));
        }
        return id;
    }

    public void releaseNotificationID(int id) {
        String value = "" + id;
        mNotificationList.remove(value);
    }

    public static ArrayList<DocumentFile> findDocumentFilefromPathSD(ArrayList<FileInfo> fileInfos, String sdPath, int fromWhichActivity){
        ArrayList<DocumentFile> mDocumentFiles = new ArrayList<>();
        DocumentFile currentDocumentFile = null;
        if(fromWhichActivity == 1){
            currentDocumentFile = Constant.mCurrentDocumentFileExplore;
        }else if(fromWhichActivity == 0){
            currentDocumentFile = Constant.mSDCurrentDocumentFile;
        }else if(fromWhichActivity == 2){
            currentDocumentFile = Constant.mCurrentDocumentFileDestination;
        }

        if(currentDocumentFile != null){
            for(FileInfo file : fileInfos){
                String path = file.path;
                path = path.replace(sdPath, "");
                String[] array = path.split("/");
                DocumentFile tmp = currentDocumentFile;
                for(int i= 1;i<array.length;i++){
                    tmp = tmp.findFile(array[i]);
                }
                mDocumentFiles.add(tmp);
            }
        }
        return mDocumentFiles;
    }

    public static ArrayList<DocumentFile> findDocumentFilefromPathOTG(ArrayList<FileInfo> fileInfos, String otgPath, int fromWhichActivity){
        ArrayList<DocumentFile> mDocumentFiles = new ArrayList<>();
        DocumentFile currentDocumentFile = null;

        if(fromWhichActivity == 1){
            currentDocumentFile = Constant.mRootDocumentFile;
        }else if(fromWhichActivity == 0){
            currentDocumentFile = Constant.mRootDocumentFile;
        }else if(fromWhichActivity == 2){

        }
        if(currentDocumentFile != null){
            for(FileInfo file : fileInfos){
                String path = file.path;
                path = path.replace(otgPath, "");
                String[] array = path.split("/");
                DocumentFile tmp = currentDocumentFile;
                for(int i= 1;i<array.length;i++){
                    tmp = tmp.findFile(array[i]);
                }
                mDocumentFiles.add(tmp);
            }
        }
        return mDocumentFiles;
    }

    public static ArrayList<DocumentFile> findDocumentFilefromName(ArrayList<FileInfo> fileInfos, int fromWhichActivity){
        ArrayList<DocumentFile> mDocumentFiles = new ArrayList<>();
        DocumentFile currentDocumentFile = null;
        if(fromWhichActivity == 1){
            currentDocumentFile = Constant.mCurrentDocumentFileExplore;
        }else if(fromWhichActivity == 0){
            if(Constant.nowMODE == Constant.MODE.SD)
                currentDocumentFile = Constant.mSDCurrentDocumentFile;
            else if(Constant.nowMODE == Constant.MODE.OTG)
                currentDocumentFile = Constant.mCurrentDocumentFile;
        }else if(fromWhichActivity == 2){

        }

        if(currentDocumentFile != null){
            for(FileInfo file : fileInfos){
                DocumentFile tmp = currentDocumentFile.findFile(file.name);
                mDocumentFiles.add(tmp);
            }
        }
        return mDocumentFiles;
    }

    //local file can't use this function
    //rootPath mean: /storage/0000-0000
    //rootName mean: 0000-0000
    public static DocumentFile findDocumentFilefromName(Context context, FileInfo fileInfo){
        String rootPath;
        String rootName = null;
        DocumentFile tmp;
        if (fileInfo.storagemode == Constant.STORAGEMODE_SD) {
            tmp = Constant.mSDRootDocumentFile;
            rootPath = getOuterStoragePath(context, Constant.sd_key_path);
        } else {//(fileInfo.storagemode == Constant.STORAGEMODE_OTG)
            tmp = Constant.mRootDocumentFile;
            rootPath = getOTGStoragePath(context, Constant.otg_key_path);
        }

        String[] rootPath_array = rootPath.split("/");
        for (int i = rootPath_array.length-1;i >= 0;i--) {
            if (rootPath_array[i].length() != 0) {
                rootName = rootPath_array[i];
                break;
            }
        }

        if (rootName == null || rootName.length() == 0) {
            Log.d(TAG, "findDocumentFile fail, rootPath: "+rootPath);
            return null;
        }

        String[] array = fileInfo.path.split("/");
        boolean findRootName = false;
        for(int i = 0;i < array.length;i++){
            if (rootName.equals(array[i]) || findRootName) {
                if (findRootName)
                    tmp = tmp.findFile(array[i]);
                findRootName = true;
            } else {
                continue;
            }
        }
        return tmp;
    }

    public static long getStorageFreeSizeLong(String filePath) {
        StatFs stat = new StatFs(filePath);
        long bytesLeftAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        return bytesLeftAvailable;
    }

    public static long getUsedStorageSizeLong(String filePath) {
        StatFs stat = new StatFs(filePath);
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
        long bytesLeftAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        return bytesAvailable - bytesLeftAvailable;

    }

    public static String getStorageFreeSize(String filePath) {
        StatFs stat = new StatFs(filePath);
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
        String size = MathUtils.getStorageSize(bytesAvailable);
        return size;
    }

    public static String getUsedStorageSize(String filePath) {
        StatFs stat = new StatFs(filePath);
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
        long bytesLeftAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        String size = MathUtils.getStorageSize(bytesAvailable - bytesLeftAvailable);
        return size;

    }
}
