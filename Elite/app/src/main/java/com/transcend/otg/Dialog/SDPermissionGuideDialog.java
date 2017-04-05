package com.transcend.otg.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.view.ViewPager;
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
        builder.setTitle(mContext.getResources().getString(R.string.documenttree_guide_title));
        builder.setIcon(R.mipmap.ic_sdcard_gray);
        builder.setView(R.layout.dialog_connect_sd);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.confirm, null);
        builder.setCancelable(true);
        mDialog = builder.show();
        mDlgBtnPos = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mDlgBtnPos.setOnClickListener(this);
        mDlgBtnPos.setTextSize(18);
        mDlgBtnPos.setText(mContext.getResources().getString(R.string.permission_guide_next));
        mDlgBtnNeg = mDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        mDlgBtnNeg.setOnClickListener(this);
        mDlgBtnNeg.setTextSize(18);
        mPagerAdapter = new SDGuideAdapter(mContext);

        viewerPager = (ViewerPager) mDialog.findViewById(R.id.viewer_pager_sd);
        viewerPager.setAdapter(mPagerAdapter);
        viewerPager.setCurrentItem(0);
        viewerPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position == 2)
                    mDlgBtnPos.setText(mContext.getResources().getString(R.string.confirm));
                else
                    mDlgBtnPos.setText(mContext.getResources().getString(R.string.permission_guide_next));
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    @Override
    public void onClick(View v) {
        if (v.equals(mDlgBtnPos)) {
            int current = viewerPager.getCurrentItem();
            if(current == 0){
                viewerPager.setCurrentItem(1, true);
            }else if(current == 1){
                viewerPager.setCurrentItem(2, true);
            }else {
                onConfirm(true);
                mDialog.dismiss();
            }
        }else if (v.equals(mDlgBtnNeg)){
            onConfirm(false);
            mDialog.dismiss();
        }
    }
}