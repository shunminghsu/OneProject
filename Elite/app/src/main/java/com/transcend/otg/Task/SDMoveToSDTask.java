package com.transcend.otg.Task;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.support.v4.provider.DocumentFile;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Utils.FileFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by henry_hsu on 2017/4/18.
 */

public class SDMoveToSDTask extends AsyncTask<DocumentFile, String, Boolean> {
    private FileInfo mSource;
    private DocumentFile desDfile;//for sendBroadcast Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
    private String mDesDirPath;
    private boolean mIsCopy;
    private Context mContext;

    public SDMoveToSDTask(Context context, FileInfo source, String des_dir_path, boolean isCopy) {
        mSource = source;
        mDesDirPath = des_dir_path;
        mContext = context;
        mIsCopy = isCopy;
    }

    @Override
    protected Boolean doInBackground(DocumentFile... params) {
        String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);

        DocumentFile sourceDfile = FileFactory.findDocumentFilefromName(mContext, mSource);
        DocumentFile destDParent = findDocumentFilefromPath(mDesDirPath, sdPath, Constant.mSDRootDocumentFile);

        if (sourceDfile.isDirectory()) {
            return copydDir(sourceDfile, destDParent);
        } else {
            desDfile = destDParent.createFile(sourceDfile.getType(), mSource.name);
            return copydFile(sourceDfile, desDfile);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            if (mSource.type == Constant.TYPE_DIR) {
                File parent = new File(mDesDirPath);
                File newFile = new File(parent, mSource.name);
                scanDir(newFile, mContext);
            } else {
                // des file name could not be same as source name when file exist ex: name(1), name(2),...
                // so we get the name after the copied file created.
                File parent = new File(mDesDirPath);
                File newFile = new File(parent, desDfile.getName());
                MediaScannerConnection.scanFile(mContext, new String[]{newFile.getPath()}, new String[]{desDfile.getType()}, null);
            }

            if (!mIsCopy && Constant.mSDRootDocumentFile != null) {
                DocumentFile df = FileFactory.findDocumentFilefromName(mContext, mSource);
                df.delete();
            }
        }
    }

    private boolean copydFile(DocumentFile source, DocumentFile destination) {
        if (source == null)
            return false;
        InputStream in = null;
        OutputStream out = null;
        try {
            in = mContext.getContentResolver().openInputStream(source.getUri());
            out = mContext.getContentResolver().openOutputStream(destination.getUri());
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean copydDir(DocumentFile source, DocumentFile dest_parent) {
        DocumentFile dest_dir = dest_parent.createDirectory(source.getName());
        if (dest_dir.exists()) {
            for (DocumentFile f : source.listFiles()) {
                if (f.isFile()) {
                    copydFile(f, dest_dir.createFile(f.getType(), f.getName()));
                } else if (f.isDirectory()) {
                    copydDir(f, dest_dir);
                }
            }
            return true;
        }
        return false;
    }

    private void scanDir(File dir, Context context) {
        MediaScannerConnection.scanFile(context, new String[]{dir.getPath()}, new String[]{null}, null);
        for (File f : dir.listFiles()) {
            if (f.isDirectory())
                scanDir(f, context);
            else {
                String sdPath = FileFactory.getOuterStoragePath(context, Constant.sd_key_path);
                DocumentFile df = findDocumentFilefromPath(f.getPath(), sdPath, Constant.mSDRootDocumentFile);
                MediaScannerConnection.scanFile(context, new String[]{f.getPath()}, new String[]{df.getType()}, null);
            }
        }
    }

    private DocumentFile findDocumentFilefromPath(String des_path, String sd_Path, DocumentFile currentDocumentFile){
        DocumentFile mDocumentFile = null;

        if(currentDocumentFile != null){
            String path = des_path;
            path = path.replace(sd_Path, "");
            String[] array = path.split("/");
            DocumentFile tmp = currentDocumentFile;
            for(int i= 1;i<array.length;i++){
                tmp = tmp.findFile(array[i]);
            }
            mDocumentFile = tmp;
        }
        return mDocumentFile;
    }
}