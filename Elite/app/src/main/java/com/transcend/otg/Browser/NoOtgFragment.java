package com.transcend.otg.Browser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Dialog.OTGPermissionGuideDialog;
import com.transcend.otg.LocalPreferences;
import com.transcend.otg.R;
import com.transcend.otg.Utils.FileFactory;

import java.util.ArrayList;

/**
 * Created by henry_hsu on 2017/2/14.
 */

public class NoOtgFragment extends Fragment {

    private OnRefreshClickedListener mListener;
    public interface OnRefreshClickedListener {
        void onOtgRefreshClick();
    }

    public void setOtgRefreshClickedListener(OnRefreshClickedListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.no_outer_storage_layout, container, false);
        ((TextView)view.findViewById(R.id.no_storage_title)).setText(R.string.no_otg);
        view.findViewById(R.id.no_outer_storage).setBackgroundResource(R.drawable.img_notfoundpic_otg);
        Button refreshBtn = (Button)view.findViewById(R.id.check_btn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onOtgRefreshClick();
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        mListener = null;
        super.onDestroy();
    }
}