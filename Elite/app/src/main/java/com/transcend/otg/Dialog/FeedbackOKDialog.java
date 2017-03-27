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

public abstract class FeedbackOKDialog implements View.OnClickListener {

    public abstract void onConfirm(Context mContext);

    private Context mContext;
    private AlertDialog mDialog;
    private Button mDlgBtnPos;

    public FeedbackOKDialog(Context context, boolean success) {
        mContext = context;
        initDialog(success);
    }

    private void initDialog(boolean success) {
        String message = "";
        if(success)
            message = mContext.getResources().getString(R.string.feedback_done);
        else
            message = mContext.getResources().getString(R.string.feedback_fail);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.drawer_feedback));
        builder.setIcon(R.mipmap.ic_userfeedback);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, null);
        builder.setCancelable(false);
        mDialog = builder.show();
        mDlgBtnPos = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mDlgBtnPos.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.equals(mDlgBtnPos)) {
            onConfirm(mContext);
            mDialog.dismiss();
        }
    }
}

