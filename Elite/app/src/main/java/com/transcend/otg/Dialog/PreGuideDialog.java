package com.transcend.otg.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.R;

import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/3/7.
 */

public abstract class PreGuideDialog implements View.OnClickListener{
    public abstract void onConfirm(String type);

    private Context mContext;
    private AlertDialog mDialog;
    private Button mDlgBtnPos;
    private String mType;

    public PreGuideDialog(Context context, String type) {
        mContext = context;
        mType = type;
        initDialog();
    }

    private void initDialog() {
        String message = mContext.getResources().getString(R.string.preguide_message);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.documenttree_guide_title));
        builder.setIcon(R.mipmap.icon_elite_logo);
        builder.setView(R.layout.dialog_delete);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.preguide_knowhow, null);
        builder.setCancelable(true);
        mDialog = builder.show();
        TextView tv = (TextView) mDialog.findViewById(R.id.tv_delete);
        tv.setText(message);
        mDlgBtnPos = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mDlgBtnPos.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mDlgBtnPos)) {
            onConfirm(mType);
            mDialog.dismiss();
        }
    }

}