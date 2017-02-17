package com.transcend.otg.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import com.transcend.otg.Adapter.SDGuideAdapter;
import com.transcend.otg.R;
import com.transcend.otg.Viewer.ViewerPager;

/**
 * Created by wangbojie on 2017/2/16.
 */

public abstract class SDPermissionGuideDialog implements View.OnClickListener {

    public abstract void onConfirm(Boolean isClick);

    private Context mContext;
    private AlertDialog mDialog;
    private Button mDlgBtnPos, mDlgBtnNeg;
    private ViewerPager viewerPager;
    private SDGuideAdapter mPagerAdapter;

    public SDPermissionGuideDialog(Context context) {
        mContext = context;
        initDialog();
    }

    private void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.sdcard_name));
        builder.setIcon(R.mipmap.ic_sdcard_gray);
        builder.setView(R.layout.dialog_connect_sd);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.confirm, null);
        builder.setCancelable(true);
        mDialog = builder.show();
        mDlgBtnPos = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mDlgBtnPos.setOnClickListener(this);
        mDlgBtnPos.setTextSize(18);
        mDlgBtnNeg = mDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        mDlgBtnNeg.setOnClickListener(this);
        mDlgBtnNeg.setTextSize(18);
        mPagerAdapter = new SDGuideAdapter(mContext);

        viewerPager = (ViewerPager) mDialog.findViewById(R.id.viewer_pager_sd);
        viewerPager.setAdapter(mPagerAdapter);
        viewerPager.setCurrentItem(0);
    }


    @Override
    public void onClick(View v) {
        if (v.equals(mDlgBtnPos)) {
            onConfirm(true);
            mDialog.dismiss();
        } else if (v.equals(mDlgBtnNeg)) {
            onConfirm(false);
            mDialog.dismiss();
        }
    }
}