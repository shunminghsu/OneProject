package com.transcend.otg.Browser;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.transcend.otg.R;

/**
 * Created by henry_hsu on 2017/2/13.
 */

public class NoSdFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.no_outer_storage_layout, container, false);
        ((TextView)view.findViewById(R.id.no_outer_storage)).setText("sd card not found");
        (view.findViewById(R.id.check_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return view;
    }
}
