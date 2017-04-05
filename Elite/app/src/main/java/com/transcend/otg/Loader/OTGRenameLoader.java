package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.transcend.otg.Constant.ActionParameter;
import com.transcend.otg.Constant.Constant;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by wangbojie on 2016/6/8.
 */
public class OTGRenameLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = OTGRenameLoader.class.getSimpleName();
    private Context mContext;
    private ArrayList<DocumentFile> mSelectFiles;
    private String mNewName;

    public OTGRenameLoader(Context context, String name, ArrayList<DocumentFile> dFile) {
        super(context);
        mContext = context;
        mNewName = name;
        mSelectFiles = dFile;
    }

    @Override
    public Boolean loadInBackground() {
        try {
            return rename();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean rename() throws InterruptedException {
        DocumentFile dfile = mSelectFiles.get(0);
        if (dfile.exists()) {
            if (dfile.getParentFile().findFile(mNewName) != null) {
                //Log.d("henry", "new file exist");
                return false;
            }
            String oldName = dfile.getName();
            if(dfile.renameTo(mNewName)){
                if(Constant.nowMODE == Constant.MODE.SD){
                    String newName = mNewName;
                    String path = ActionParameter.files.get(0).path;
                    path = path.replace(oldName, newName);
                    //File rename = new File(path);
                    //mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(rename)));
                    MediaScannerConnection.scanFile(mContext, new String[]{path}, new String[]{dfile.getType()}, null);
                    Thread.sleep(500);
                }
                return true;
            }else{
                return false;
            }

        }
        return false;
    }

    public String getNewName(){
        return mNewName;
    }
}
