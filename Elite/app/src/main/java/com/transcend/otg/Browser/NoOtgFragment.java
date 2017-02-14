package com.transcend.otg.Browser;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Home.HomeFragment;
import com.transcend.otg.MainActivity;
import com.transcend.otg.R;

import java.io.IOException;

/**
 * Created by henry_hsu on 2017/2/14.
 */

public class NoOtgFragment extends Fragment {

    protected Context mContext;
    private String TAG = NoOtgFragment.class.getSimpleName();
    private static final String ACTION_USB_PERMISSION = "com.transcend.otg.Browser.USB_PERMISSION";
    private UsbMassStorageDevice device;
    private OTGFragment otgFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getActivity();
        otgFragment = new OTGFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.no_outer_storage_layout, container, false);
        ((TextView)view.findViewById(R.id.no_outer_storage)).setText("Otg not found");
        (view.findViewById(R.id.check_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverDevice();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initBroadcast();
    }

    @Override
    public void onPause() {
        super.onPause();
        mContext.unregisterReceiver(usbReceiver);
    }

    private void initBroadcast(){
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(usbReceiver, filter);
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {

                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device != null) {
                        setupDevice();
                    }
                }

            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
//                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//
//                Log.d(TAG, "USB device attached");
//
//                if (device != null) {
//                    discoverDevice();
//                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
//                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//
//                Log.d(TAG, "USB device detached");
//
//                // determine if connected device is a mass storage devuce
//                if (device != null) {
//                    if (MainActivity.this.device != null) {
//                        MainActivity.this.device.close();
//                    }
//                    // check if there are other devices or set action bar title
//                    // to no device if not
//                    discoverDevice();
//                }
            }

        }
    };

    private void setupDevice() {
        try {
            device.init();
            Constant.nowMODE = Constant.MODE.OTG;
            Constant.nowDevice = device;
            replaceFragment(otgFragment);
        } catch (IOException e) {
            Log.e(TAG, "error setting up device", e);
        }

    }

    private void discoverDevice() {
        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(mContext);

        if (devices.length == 0) {
            Log.w(TAG, "no device found!");
            return;
        }
        device = devices[0];

        UsbDevice usbDevice = (UsbDevice) getActivity().getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);

        if (!(usbDevice != null && usbManager.hasPermission(usbDevice))) {
            PendingIntent permissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(
                    ACTION_USB_PERMISSION), 0);
            usbManager.requestPermission(device.getUsbDevice(), permissionIntent);
        }
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}