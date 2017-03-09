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
    public static String getOuterStoragePath(Context context, String key_path) {

        Method getService = null;
        Method asInterface = null;
        try {
            getService = Class.forName("android.os.ServiceManager")
                    .getDeclaredMethod("getService", String.class);
            asInterface = Class.forName("android.os.storage.IMountService$Stub")
                    .getDeclaredMethod("asInterface", IBinder.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (!getService.isAccessible()) getService.setAccessible(true);
        if (!asInterface.isAccessible()) asInterface.setAccessible(true);

        IBinder service = null;
        Object mountService = null;
        try {
            service = (IBinder) getService.invoke(null, "mount");
            mountService = asInterface.invoke(null, service);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        Object[] storageVolumes = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = context.getPackageName();

            try {
                int uid = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.uid;
                Method getVolumeList = mountService.getClass().getDeclaredMethod(
                        "getVolumeList", int.class, String.class, int.class);
                if (!getVolumeList.isAccessible()) getVolumeList.setAccessible(true);
                storageVolumes = (Object[]) getVolumeList.invoke(mountService, uid, packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Method getVolumeList = mountService.getClass().getDeclaredMethod("getVolumeList");
                if (!getVolumeList.isAccessible()) getVolumeList.setAccessible(true);
                storageVolumes = (Object[]) getVolumeList.invoke(mountService, (Object[]) null);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        if (storageVolumes == null) return null;

        for (Object storageVolume : storageVolumes) {
            Class<?> cls = storageVolume.getClass();
            Method isRemovable = null;
            try {
                isRemovable = cls.getDeclaredMethod("isRemovable");
                if (!isRemovable.isAccessible()) isRemovable.setAccessible(true);
                if ((boolean) isRemovable.invoke(storageVolume, (Object[]) null)) {
                    Method getState = cls.getDeclaredMethod("getState");
                    if (!getState.isAccessible()) getState.setAccessible(true);
                    String state = (String) getState.invoke(storageVolume, (Object[]) null);
                    if (state.equals("mounted")) {
                        Method getPath = cls.getDeclaredMethod("getPath");
                        if (!getPath.isAccessible()) getPath.setAccessible(true);
                        String path = (String) getPath.invoke(storageVolume, (Object[]) null);
                        if (path.toLowerCase().contains(key_path.toLowerCase()))
                            return path;
                    }
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
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

    public static ArrayList<DocumentFile> findDocumentFilefromPath(ArrayList<FileInfo> fileInfos){
        ArrayList<DocumentFile> mDocumentFiles = new ArrayList<>();
        DocumentFile currentDocumentFile = Constant.mCurrentDocumentFile;
        for(FileInfo file : fileInfos){
            String path = file.path;
            String[] array = path.split("/");
            DocumentFile tmp = currentDocumentFile;
            for(int i= 1;i<array.length;i++){
                tmp = tmp.findFile(array[i]);
            }
            mDocumentFiles.add(tmp);
        }
        return mDocumentFiles;
    }

    public static ArrayList<DocumentFile> findDocumentFilefromName(ArrayList<FileInfo> fileInfos){
        ArrayList<DocumentFile> mDocumentFiles = new ArrayList<>();
        DocumentFile currentDocumentFile = Constant.mCurrentDocumentFile;
        for(FileInfo file : fileInfos){
            DocumentFile tmp = currentDocumentFile.findFile(file.name);
            mDocumentFiles.add(tmp);
        }
        return mDocumentFiles;
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
