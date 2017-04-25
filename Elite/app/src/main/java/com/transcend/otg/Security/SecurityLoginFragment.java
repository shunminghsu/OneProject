package com.transcend.otg.Security;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.MainActivity;
import com.transcend.otg.R;

/**
 * Created by wangbojie on 2017/3/29.
 */

public class SecurityLoginFragment extends Fragment{


    private Context mContext;
    private RelativeLayout root;
    private Button btnLogin;
    private EditText editPassword;
    RelativeLayout mLogin;
    //private ProgressBar progressBarLoading;

    private SecurityScsi securityScsi;
    //private View loginFragment;

    public SecurityLoginFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public void onPause() {
        super.onPause();
        clearAllValue();
    }

    private void clearAllValue(){
        editPassword.setText("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = (RelativeLayout) inflater.inflate(R.layout.fragment_security_login, container, false);
        mLogin = (RelativeLayout) root.findViewById(R.id.login_progress_view);
        btnLogin = (Button) root.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogin.setVisibility(View.VISIBLE);
                btnLogin.setEnabled(false);
                String password = editPassword.getText().toString();
                if( password.length() >= 4  && SecurityLogin(password)){
                    MainActivity activity = (MainActivity) getActivity();
                    activity.setDrawerCheckItem(R.id.nav_home);
                    activity.mToolbarTitle.setText(getResources().getString(R.string.drawer_home));
                    activity.showHomeOrFragment(true);
                }
                else{
                    snackBarShow(R.string.error);
                    editPassword.setText("");
                    editPassword.requestFocus();
                }
                mLogin.setVisibility(View.INVISIBLE);
                btnLogin.setEnabled(true);
            }
        });

        init();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return root;
    }

    private void init(){
        editPassword = (EditText)root.findViewById(R.id.editPassword);

        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(mContext);
        UsbMassStorageDevice device = devices[0];
        securityScsi = SecurityScsi.getInstance( device.getUsbDevice() , usbManager);
    }

    private boolean SecurityLogin(String password){
        try {
            securityScsi.SecurityUnlockActivity(password);
            Thread.sleep(1000);
            if(securityScsi.checkSecurityStatus() == Constant.SECURITY_UNLOCK) {
                return true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void snackBarShow(int resId) {
        Snackbar.make(root, resId, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }
}
