package com.transcend.otg.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import com.transcend.otg.R;


/**
 * Created by wangbojie on 2016/12/8.
 */

public class EmptyNotificationDialog implements View.OnClickListener {

    private Context mContext;
    private AlertDialog mDialog;
    private Button mDlgBtnPos;

    public EmptyNotificationDialog(Context context) {
        mContext = context;
        initDialog();
    }

    private void initDialog() {
        String  message = mContext.getResources().getString(R.string.feedback_empty);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.drawer_feedback));
        builder.setIcon(R.mipmap.ic_userfeedback);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.yes, null);
        builder.setCancelable(true);
        mDialog = builder.show();
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

