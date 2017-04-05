package com.transcend.otg.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.R;

import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/3/7.
 */

public abstract class LocalDeleteDialog implements View.OnClickListener{
    public abstract void onConfirm(ArrayList<FileInfo> paths);

    private Context mContext;
    private AlertDialog mDialog;
    private Button mDlgBtnPos;

    private ArrayList<FileInfo> mFiles;

    public LocalDeleteDialog(Context context, ArrayList<FileInfo> paths) {
        mContext = context;
        mFiles = paths;
        initDialog();
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
            onConfirm(mFiles);
            mDialog.dismiss();
        }
    }

}