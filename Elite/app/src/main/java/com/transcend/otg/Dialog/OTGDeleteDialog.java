package com.transcend.otg.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.LocalPreferences;
import com.transcend.otg.R;
import com.transcend.otg.Utils.FileFactory;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/3/8.
 */
public abstract class OTGDeleteDialog implements OnClickListener {

    public abstract void onConfirm(ArrayList<DocumentFile> files);

    private Context mContext;
    private AlertDialog mDialog;
    private Button mDlgBtnPos;

    private ArrayList<FileInfo> mFiles;
    private ArrayList<DocumentFile> mDFiles;
    private boolean bFromName, bFromExploreActivity;

    public OTGDeleteDialog(Context context, ArrayList<FileInfo> files, boolean fromName, boolean fromExploreActivity) {
        mContext = context;
        mFiles = files;
        bFromName = fromName;
        bFromExploreActivity = fromExploreActivity;
        initData();
        initDialog();
    }

    private void initData(){
        if(Constant.nowMODE == Constant.MODE.SD){
            String sdKey = LocalPreferences.getSDKey(mContext);
            if(sdKey != ""){
                Uri uriSDKey = Uri.parse(sdKey);
                DocumentFile tmpDFile = DocumentFile.fromTreeUri(mContext, uriSDKey);
                Constant.mCurrentDocumentFileExplore = Constant.mSDRootDocumentFile = Constant.mSDCurrentDocumentFile = tmpDFile;
                String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
                if(bFromName)
                    mDFiles = FileFactory.findDocumentFilefromName(mFiles, Constant.Activity);
                else
                    mDFiles = FileFactory.findDocumentFilefromPathSD(mFiles, sdPath, Constant.Activity);
            }
        }else if(Constant.nowMODE == Constant.MODE.OTG){
            if(bFromName)
                mDFiles = FileFactory.findDocumentFilefromName(mFiles, Constant.Activity);
            else{
                String otgPath = FileFactory.getOTGStoragePath(mContext, Constant.otg_key_path);
                mDFiles = FileFactory.findDocumentFilefromPathOTG(mFiles, otgPath, Constant.Activity);
            }
        }
    }

    private void initDialog() {
        String format = mContext.getResources().getString(R.string.conj_deleted);
        String message = String.format(format, mFiles.size());
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.delete));
        builder.setIcon(R.mipmap.ic_delete_gray);
        builder.setMessage(message);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.confirm, null);
        builder.setCancelable(true);
        mDialog = builder.show();
        mDlgBtnPos = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mDlgBtnPos.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.equals(mDlgBtnPos)) {
            onConfirm(mDFiles);
            mDialog.dismiss();
        }
    }
}
