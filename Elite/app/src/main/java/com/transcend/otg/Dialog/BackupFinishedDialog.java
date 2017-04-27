package com.transcend.otg.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.transcend.otg.R;


/**
 * Created by wangbojie on 2016/12/8.
 */

public class BackupFinishedDialog implements View.OnClickListener {

    private Context mContext;
    private AlertDialog mDialog;
    private Button mDlgBtnPos, mDlgBtnNeg;

    public BackupFinishedDialog(Context context) {
        mContext = context;
        initDialog();
    }

    private void initDialog() {
        String message = mContext.getResources().getString(R.string.backup_finish);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.drawer_backup));
        builder.setIcon(R.mipmap.ic_drawer_backup);
        builder.setView(R.layout.dialog_ask_exit);
        builder.setPositiveButton(R.string.ok, null);
        builder.setCancelable(true);
        mDialog = builder.show();
        TextView tv = (TextView) mDialog.findViewById(R.id.message);
        tv.setText(message);
        mDlgBtnPos = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mDlgBtnPos.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.equals(mDlgBtnPos)) {
            mDialog.dismiss();
        }
    }
}

