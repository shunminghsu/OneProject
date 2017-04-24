package com.transcend.otg.Security;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.MainActivity;
import com.transcend.otg.R;

/**
 * Created by wangbojie on 2017/3/29.
 */

public class SecuritySettingFragment extends Fragment{


    private Context mContext;
    private RelativeLayout root;
    private Button btnLogin;
    private EditText editSettingPassword , editSettingConfirmPassword;
    private Button btnSettingOK , btnSettingCancel;
    private ImageView imageSettingCircle;
    private RelativeLayout mSettingLoading;
    private SecurityScsi securityScsi;
    //private View loginFragment;

    public SecuritySettingFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

  @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = (RelativeLayout) inflater.inflate(R.layout.fragment_settingpassword, container, false);
        mSettingLoading = (RelativeLayout) root.findViewById(R.id.setting_progress_view);


        initSettingPasswordEditText();
        initSettingPasswordButton();
        initSettingCircleImage();
        initSecurityScsi();

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return root;
    }

    private void initSettingPasswordEditText(){
        editSettingPassword = (EditText)root.findViewById(R.id.editSettingPassword);
        editSettingPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if( CheckPassword( editSettingPassword.getText().toString() , editSettingConfirmPassword.getText().toString() ) ){
                    imageSettingCircle.setVisibility(View.VISIBLE);
                }
                else{
                    imageSettingCircle.setVisibility(View.INVISIBLE);
                }
            }
        });

        editSettingConfirmPassword = (EditText)root.findViewById(R.id.editSettingConfirmPassword);
        editSettingConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if( CheckPassword( editSettingPassword.getText().toString() , editSettingConfirmPassword.getText().toString() ) ){
                    imageSettingCircle.setVisibility(View.VISIBLE);
                }
                else{
                    imageSettingCircle.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void initSettingPasswordButton(){
        btnSettingOK = (Button)root.findViewById(R.id.btnSettingOK);
        btnSettingOK.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageSettingCircle.getVisibility() == View.VISIBLE){
                    mSettingLoading.setVisibility(View.VISIBLE);
                    btnSettingOK.setEnabled(false);
                    btnSettingCancel.setEnabled(false);
                    try{
                        securityScsi.SecurityLockActivity(editSettingPassword.getText().toString());
                        Thread.sleep(1000);
                        if(securityScsi.getSecurityStatus() == Constant.SECURITY_UNLOCK){
                            snackBarShow(R.string.done);
                            Back2Home();
                        }
                        else{
                            snackBarShow(R.string.error);
                            cleanSettingEdit();
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        btnSettingOK.setEnabled(true);
                        btnSettingCancel.setEnabled(true);
                        mSettingLoading.setVisibility(View.INVISIBLE);
                    }
                }
                else{
                    cleanSettingEdit();
                }
                btnSettingOK.setEnabled(true);
                btnSettingCancel.setEnabled(true);
                mSettingLoading.setVisibility(View.INVISIBLE);
            }
        });

        btnSettingCancel = (Button)root.findViewById(R.id.btnSettingCancel);
        btnSettingCancel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanSettingEdit();
            }
        });
    }

    private void initSettingCircleImage(){
        imageSettingCircle = (ImageView) root.findViewById(R.id.imageSettingCircle);
        imageSettingCircle.setVisibility(View.INVISIBLE);
    }

    private void initSecurityScsi(){
        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(mContext);
        UsbMassStorageDevice device = devices[0];
        securityScsi = new SecurityScsi(device.getUsbDevice() , usbManager);
    }

    private void cleanSettingEdit(){
        editSettingPassword.setText("");
        editSettingConfirmPassword.setText("");
        editSettingPassword.requestFocus();
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

    private void Back2Home(){
        MainActivity activity = (MainActivity) getActivity();
        activity.setDrawerCheckItem(R.id.nav_home);
        activity.mToolbarTitle.setText(getResources().getString(R.string.drawer_home));
        activity.showHomeOrFragment(true);
    }

    private void snackBarShow(int resId) {
        Snackbar.make(root, resId, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }
}
