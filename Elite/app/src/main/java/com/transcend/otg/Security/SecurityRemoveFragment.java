package com.transcend.otg.Security;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Dialog.SecurityDisableDialog;
import com.transcend.otg.FirebaseAnalytics.FirebaseAnalyticsFactory;
import com.transcend.otg.R;

/**
 * Created by wangbojie on 2017/3/29.
 */

public class SecurityRemoveFragment extends PageView {

    private Context mContext;
    private View root;
    private Button btnRemoveOK , btnRemoveCancel;
    private EditText editRemovePassword;

    private SecurityScsi securityScsi;

    public SecurityRemoveFragment(Context context){
        super(context);
        mContext = context;
        root = LayoutInflater.from(context).inflate(R.layout.fragment_removepassword,null);
        initRemovePasswordEditText();
        initRemovePasswordButton();
        initSecurityScsi();
        addView(root);
    }

    private void initRemovePasswordEditText(){
        editRemovePassword = (EditText)root.findViewById(R.id.editRemovePassword);
    }

    private void initRemovePasswordButton(){
        btnRemoveOK = (Button)root.findViewById(R.id.btnRemoveOK);
        btnRemoveOK.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SecurityDisableDialog(root.getContext(),mContext.getResources().getString(R.string.MSG_UserRemovePWLostSecurity)){

                    @Override
                    public void onConfirm(boolean bExit) {
                        if(bExit){
                            try {
                                root.setVisibility(View.VISIBLE);
                                btnRemoveOK.setEnabled(false);
                                btnRemoveCancel.setEnabled(false);
                                securityScsi.SecurityDisableLockActivity(editRemovePassword.getText().toString());
                                Thread.sleep(1000);
                                if(securityScsi.checkSecurityStatus() == Constant.SECURITY_DISABLE){
                                    FirebaseAnalyticsFactory.getInstance(mContext).sendEvent(FirebaseAnalyticsFactory.FRAGMENT.SECURITY, FirebaseAnalyticsFactory.EVENT.SECURITY_REMOVE);
                                    Snackbar.make(getRootView(), R.string.done, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                    Back2Home();
                                }
                                else{
                                    snackBarShow(R.string.msg_password_incorrect);
                                    editRemovePassword.setText("");
                                    editRemovePassword.requestFocus();
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                btnRemoveOK.setEnabled(true);
                                btnRemoveCancel.setEnabled(true);
                                root.setVisibility(View.INVISIBLE);
                            }
                            btnRemoveOK.setEnabled(true);
                            btnRemoveCancel.setEnabled(true);
                        }
                    }
                };
            }
        });

        btnRemoveCancel = (Button)root.findViewById(R.id.btnRemoveCancel);
        btnRemoveCancel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                editRemovePassword.setText("");
                editRemovePassword.requestFocus();
            }
        });
    }

    private void snackBarShow(int resId) {
        Snackbar.make(getRootView(), resId, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    private void Back2Home(){
        SecurityListener.getInstance().notifySecurityListener(SecurityListener.SecurityStatus.Detached);
    }

    private void initSecurityScsi(){
        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(mContext);
        if( devices.length > 0) {
            UsbMassStorageDevice device = devices[0];
            securityScsi = SecurityScsi.getInstance(device.getUsbDevice(), usbManager, false);
        }
    }
}
