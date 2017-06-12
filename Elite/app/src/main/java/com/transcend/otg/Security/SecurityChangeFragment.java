package com.transcend.otg.Security;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.FirebaseAnalytics.FirebaseAnalyticsFactory;
import com.transcend.otg.R;

/**
 * Created by wangbojie on 2017/3/29.
 */

public class SecurityChangeFragment extends PageView {


    private Context mContext;
    private View root;
    private Button btnChangeOK , btnChangeCancel;
    private EditText editChangeCurrentPassword , editChangeNewPassword , editChangeConfirmPassword;
    private ImageView imageChangeCircle;
    private RelativeLayout mChangeLoading ;

    private SecurityScsi securityScsi;

    public SecurityChangeFragment(Context context){
        super(context);
        mContext = context;
        root = LayoutInflater.from(context).inflate(R.layout.fragment_changepassword,null);
        initChangeCircleImage();
        initChangePasswordEditText();
        initChangePasswordButton();
        initSecurityScsi();
        mChangeLoading = (RelativeLayout)root.findViewById(R.id.change_progress_view);
        addView(root);
    }

    private void initChangeCircleImage(){
        imageChangeCircle = (ImageView)root.findViewById(R.id.imageChangeCircle);
        imageChangeCircle.setVisibility(View.INVISIBLE);
    }

    private void initChangePasswordEditText(){
        editChangeCurrentPassword = (EditText)root.findViewById(R.id.editChangeCurrentPassword);
        editChangeNewPassword = (EditText)root.findViewById(R.id.editChangeNewPassword);
        editChangeNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if( CheckPassword( editChangeNewPassword.getText().toString() , editChangeConfirmPassword.getText().toString() ) && editChangeCurrentPassword.length() > 0 ){
                    imageChangeCircle.setVisibility(View.VISIBLE);
                }
                else{
                    imageChangeCircle.setVisibility(View.INVISIBLE);
                }
            }
        });
        editChangeConfirmPassword = (EditText)root.findViewById(R.id.editChangeConfirmPassword);
        editChangeConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if( CheckPassword( editChangeNewPassword.getText().toString() , editChangeConfirmPassword.getText().toString() ) && editChangeCurrentPassword.length() > 0 ){
                    imageChangeCircle.setVisibility(View.VISIBLE);
                }
                else{
                    imageChangeCircle.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void initChangePasswordButton(){
        btnChangeOK = (Button)root.findViewById(R.id.btnChangeOK);
        btnChangeOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(imageChangeCircle.getVisibility() == View.VISIBLE){
                    mChangeLoading.setVisibility(View.VISIBLE);
                    btnChangeOK.setEnabled(false);
                    btnChangeCancel.setEnabled(false);
                    try {
                        securityScsi.SecurityDisableLockActivity(editChangeCurrentPassword.getText().toString());
                        Thread.sleep(1000);
                        if(securityScsi.checkSecurityStatus() != Constant.SECURITY_DISABLE){
                            snackBarShow(R.string.error);
                            cleanChangeEdit();
                            btnChangeOK.setEnabled(true);
                            btnChangeCancel.setEnabled(true);
                            mChangeLoading.setVisibility(View.INVISIBLE);
                            return;
                        }

                        Thread.sleep(1000);
                        securityScsi.SecurityLockActivity(editChangeNewPassword.getText().toString());
                        Thread.sleep(1000);
                        if(securityScsi.checkSecurityStatus() == Constant.SECURITY_UNLOCK){
                            FirebaseAnalyticsFactory.getInstance(mContext).sendEvent(FirebaseAnalyticsFactory.FRAGMENT.SECURITY, FirebaseAnalyticsFactory.EVENT.SECURITY_CHANGE);
                            snackBarShow(R.string.done);
                            Back2Home();
                        }
                        else{
                            snackBarShow(R.string.error);
                            cleanChangeEdit();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        btnChangeOK.setEnabled(true);
                        btnChangeCancel.setEnabled(true);
                        mChangeLoading.setVisibility(View.INVISIBLE);
                    }
                }
                else{
                    snackBarShow(R.string.msg_password_incorrect);
                    cleanChangeEdit();
                }
                btnChangeOK.setEnabled(true);
                btnChangeCancel.setEnabled(true);
                mChangeLoading.setVisibility(View.INVISIBLE);
            }
        });

        btnChangeCancel = (Button)root.findViewById(R.id.btnChangeCancel);
        btnChangeCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanChangeEdit();
            }
        });
    }

    private boolean CheckPassword( String settingPassword , String confirmPassword ){
        boolean isFollowPasswordRule = false;
        if(settingPassword.equals(confirmPassword) && !settingPassword.contains(" ")){
            if(settingPassword.length() >= 4 && settingPassword.length() <= 16 ){
                isFollowPasswordRule = true;
            }
        }
        return isFollowPasswordRule;
    }

    private void cleanChangeEdit(){
        editChangeCurrentPassword.setText("");
        editChangeNewPassword.setText("");
        editChangeConfirmPassword.setText("");
        editChangeCurrentPassword.requestFocus();
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
        if(devices.length > 0) {
            UsbMassStorageDevice device = devices[0];
            securityScsi = SecurityScsi.getInstance(device.getUsbDevice(), usbManager, false);
        }
    }

}
