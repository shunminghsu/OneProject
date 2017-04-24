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

public abstract class SecurityDisableDialog implements View.OnClickListener {
    public abstract void onConfirm(boolean bExit);

    private Context mContext;
    private AlertDialog mDialog;
    private Button mDlgBtnPos;

    public SecurityDisableDialog(Context context) {
        mContext = context;
        initDialog();
    }

    private void initDialog() {
        String  message = mContext.getResources().getString(R.string.MSG_UserRemovePWLostSecurity);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.Lsecurity));
        builder.setIcon(R.mipmap.icon_elite_logo);
        builder.setView(R.layout.dialog_security_disable);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.yes, null);
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
            onConfirm(true);
            mDialog.dismiss();
        }
    }
}

