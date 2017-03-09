package com.transcend.otg.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.R;
import com.transcend.otg.Utils.FileFactory;

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

    public OTGDeleteDialog(Context context, ArrayList<FileInfo> files) {
        mContext = context;
        mFiles = files;
        mDFiles = FileFactory.findDocumentFilefromPath(mFiles);
        initDialog();
    }

    private void initDialog() {
        String format = mContext.getResources().getString(R.string.conj_deleted);
        String message = String.format(format, mFiles.size());
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.delete));
        builder.setIcon(R.drawable.ic_menu_camera);
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
