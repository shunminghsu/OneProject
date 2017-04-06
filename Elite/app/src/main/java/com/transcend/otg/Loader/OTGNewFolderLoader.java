package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import com.transcend.otg.Constant.ActionParameter;
import com.transcend.otg.Constant.Constant;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by wangbojie on 2016/6/8.
 */
public class OTGNewFolderLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = OTGNewFolderLoader.class.getSimpleName();

    private String mName;
    private DocumentFile mDocumentFile;
    private Context mContext;

    public OTGNewFolderLoader(Context context, String name, ArrayList<DocumentFile> documentFiles) {
        super(context);
        mContext = context;
        mName = name;
        mDocumentFile = documentFiles.get(0);
    }

    @Override
    public Boolean loadInBackground() {
        try {
            return createNewFolder();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean createNewFolder() throws InterruptedException {

        if (mDocumentFile.findFile(mName) == null) {
            mDocumentFile.createDirectory(mName);
            if(Constant.nowMODE == Constant.MODE.SD){
                if(mDocumentFile.findFile(mName).exists()){
                    String path = ActionParameter.path;
                    StringBuilder builder = new StringBuilder(path);
                    if (!path.endsWith("/"))
                        builder.append("/");
                    builder.append(mName);
                    MediaScannerConnection.scanFile(mContext, new String[]{builder.toString()}, new String[]{mDocumentFile.findFile(mName).getType()}, null);
//                    File file = new File(builder.toString());
//                    mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                    Thread.sleep(500);
                }else{
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
