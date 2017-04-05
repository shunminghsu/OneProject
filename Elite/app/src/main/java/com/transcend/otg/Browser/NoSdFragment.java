package com.transcend.otg.Browser;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.transcend.otg.R;

/**
 * Created by henry_hsu on 2017/2/13.
 */

public class NoSdFragment extends Fragment{

    private OnRefreshClickedListener mListener;
    public interface OnRefreshClickedListener {
        void onSdRefreshClick();
    }

    public void setSdRefreshClickedListener(OnRefreshClickedListener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.no_outer_storage_layout, container, false);
        ((ImageView)view.findViewById(R.id.no_outer_storage)).setBackgroundResource(R.drawable.img_notfoundpic_sd);
        ((TextView)view.findViewById(R.id.no_storage_title)).setText(R.string.no_sd);
        (view.findViewById(R.id.check_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSdRefreshClick();
            }
        });
        return view;
    }
}
