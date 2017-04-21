package com.transcend.otg.Security;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.mjdev.libaums.UsbMassStorageDevice;
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

    private SecurityScsi securityScsi;
    //private View loginFragment;

    public SecurityLoginFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

  @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = (RelativeLayout) inflater.inflate(R.layout.fragment_security_login, container, false);
        btnLogin = (Button) root.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!SecurityLogin(editPassword.getText().toString())){
                    MainActivity activity = (MainActivity) getActivity();
                    activity.setDrawerCheckItem(R.id.nav_home);
                    activity.mToolbarTitle.setText(getResources().getString(R.string.drawer_home));
                    activity.showHomeOrFragment(true);
                }
                else{
                    Toast.makeText(SecurityLoginFragment.this.getActivity(), getString(R.string.msg_password_incorrect), Toast.LENGTH_LONG).show();
                    editPassword.setText("");
                    editPassword.requestFocus();
                }
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
        securityScsi = new SecurityScsi( device.getUsbDevice() , usbManager);
    }

    private boolean SecurityLogin(String password){
        try {
            securityScsi.SecurityUnlockActivity(password);
            Thread.sleep(2000);
            securityScsi.SecurityIDActivity();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return securityScsi.getSecurityStatus();
    }

}
