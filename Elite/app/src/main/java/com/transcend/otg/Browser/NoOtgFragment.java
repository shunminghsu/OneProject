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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Dialog.OTGPermissionGuideDialog;
import com.transcend.otg.LocalPreferences;
import com.transcend.otg.R;

/**
 * Created by henry_hsu on 2017/2/14.
 */

public class NoOtgFragment extends Fragment {

    protected Context mContext;
    private String TAG = NoOtgFragment.class.getSimpleName();
    private static final String ACTION_USB_PERMISSION = "com.transcend.otg.Browser.USB_PERMISSION";
    private UsbMassStorageDevice device;
    private OTGFragment otgFragment;
    private int mOTGDocumentTreeID = 1000;
    private Toast mToast;
    private DocumentFile rootDir, otgDir;
    private UsbMassStorageDevice[] devices;
    private View view = null;


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
        view = inflater.inflate(R.layout.no_outer_storage_layout, container, false);
        ((TextView)view.findViewById(R.id.no_outer_storage)).setText(getResources().getText(R.string.no_otg));
        (view.findViewById(R.id.check_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverDevice();
                if(devices.length == 0)
                    snackBarShow(R.string.no_otg);
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
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

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
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                Log.d(TAG, "USB device detached");

                if (device != null && Constant.nowMODE == Constant.MODE.OTG) {
                    discoverDevice();
                }
            }

        }
    };

    private void discoverDevice() {
        devices = UsbMassStorageDevice.getMassStorageDevices(mContext);

        if (devices.length == 0) {
            Log.w(TAG, "no device found!");
            Constant.pickedDir = Constant.rootDir = null;
            Constant.rootUri = null;
            return;
        }
        device = devices[0];
        String otgKey = LocalPreferences.getOTGKey(mContext, device.getUsbDevice().getSerialNumber());
        if(otgKey != "" || otgKey == null){
            Uri uriTree = Uri.parse(otgKey);
            if(checkStorage(uriTree)){
                replaceFragment(otgFragment);
            }
        }else{
            intentDocumentTree();
        }
    }

    private void intentDocumentTree() {
        new OTGPermissionGuideDialog(mContext) {
            @Override
            public void onConfirm(Boolean isClick) {
                if (isClick) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(intent, mOTGDocumentTreeID);
                }
            }
        };
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }


    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if(reqCode == mOTGDocumentTreeID && resCode == getActivity().RESULT_OK){
            Uri uriTree = data.getData();
            if(checkStorage(uriTree)){
                replaceFragment(otgFragment);
            }
        }
    }

    private boolean checkStorage(Uri uri){
        if (!uri.toString().contains("primary")) {
            if (uri != null) {
                if(uri.getPath().toString().split(":").length > 1){
                    snackBarShow(R.string.snackbar_plz_select_top);
                    intentDocumentTree();
                }else{
                    rootDir = DocumentFile.fromTreeUri(mContext, uri);//OTG root path
                    mContext.getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    LocalPreferences.setOTGKey(mContext, device.getUsbDevice().getSerialNumber(), uri.toString());
                    Constant.pickedDir = Constant.rootDir = otgDir = rootDir;
                    Constant.rootUri = uri;
                    return true;
                }

            }

        }else {
            snackBarShow(R.string.snackbar_plz_select_otg);
            intentDocumentTree();
        }
        return false;
    }

    private void snackBarShow(int resId) {
        Snackbar.make(view, resId, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }
}