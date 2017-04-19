package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.transcend.otg.Constant.ActionParameter;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.R;

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

        DocumentFile dfile = mSelectFiles.get(0);
        if (dfile.exists()) {
            if (dfile.getParentFile().findFile(mNewName) != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setIcon(R.mipmap.icon_elite_logo);
                builder.setTitle(context.getResources().getString(R.string.app_name));
                String exist = context.getResources().getString(R.string.file_exist);
                builder.setMessage(exist);
                builder.setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
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
                if(ActionParameter.files.get(0).storagemode == Constant.STORAGEMODE_SD){
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
