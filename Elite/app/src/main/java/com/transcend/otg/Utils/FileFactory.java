package com.transcend.otg.Utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import com.transcend.otg.Constant.FileInfo;

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
                    if (path != null && path.contains(key_word)) {
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
                    if (file.type == FileInfo.TYPE.DIR) {
                        fileList.add(file);
                    }
                }
            }

            if (i == 1) {
                for (FileInfo file : tmp) {
                    if (file.type != FileInfo.TYPE.DIR) {
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

//    public static String getStorageSize(String filePath) {
//        StatFs stat = new StatFs(filePath);
//        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
//        String size = MathUtil.getStorageSize(bytesAvailable);
//        return size;
//    }
//
//    public static String getUsedStorageSize(String filePath) {
//        StatFs stat = new StatFs(filePath);
//        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
//        long bytesLeftAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
//        String size = MathUtil.getStorageSize(bytesAvailable - bytesLeftAvailable);
//        return size;
//
//    }
}
