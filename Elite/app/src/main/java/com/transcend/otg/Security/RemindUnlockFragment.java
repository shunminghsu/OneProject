package com.transcend.otg.Security;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transcend.otg.R;

/**
 * Created by henry_hsu on 2017/2/14.
 */

public class RemindUnlockFragment extends Fragment {

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
        View view = inflater.inflate(R.layout.remind_unlock_layout, container, false);
//        ((TextView)view.findViewById(R.id.no_storage_title)).setText(R.string.no_otg);
        view.findViewById(R.id.remind_unlock_storage).setBackgroundResource(R.drawable.img_reminding_unlock);

        return view;
    }

    @Override
    public void onDestroy() {
        mListener = null;
        super.onDestroy();
    }
}