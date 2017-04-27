package com.transcend.otg.Security;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Dialog.SecurityDisableDialog;
import com.transcend.otg.MainActivity;
import com.transcend.otg.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangbojie on 2017/3/29.
 */

public class SecurityPasswordFragment extends Fragment {


    private Context mContext;
    private RelativeLayout root;
    private ViewPager mViewPager;
    private ArrayList<View> viewList;
    private ArrayList<String> titleList;
    private PagerTabStrip pagerTabStrip;

    private EditText editRemovePassword , editChangeCurrentPassword , editChangeNewPassword , editChangeConfirmPassword;
    private Button btnRemoveOK , btnRemoveCancel , btnChangeOK , btnChangeCancel;
    private ImageView imageChangeCircle;
    private RelativeLayout mRemoveLoading , mChangeLoading ;
    private SecurityScsi securityScsi;
    private View removeFragment, changeFragment;

    public SecurityPasswordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = (RelativeLayout) inflater.inflate(R.layout.fragment_security_password, container, false);
        mViewPager = (ViewPager) root.findViewById(R.id.viewPager);
        pagerTabStrip = (PagerTabStrip)root.findViewById(R.id.pager_header);
        pagerTabStrip.setDrawFullUnderline(false);
        pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.colorPrimary));
        pagerTabStrip.setTextColor(getResources().getColor(R.color.colorBlack));
        pagerTabStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP , 18);
        final LayoutInflater mInflater = getActivity().getLayoutInflater().from(mContext);

        removeFragment = mInflater.inflate(R.layout.fragment_removepassword, null);
        changeFragment = mInflater.inflate(R.layout.fragment_changepassword, null);

        viewList = new ArrayList<View>();
        viewList.add(removeFragment);
        viewList.add(changeFragment);
        titleList = new ArrayList<String>();// 每个页面的Title数据
        titleList.add(getResources().getString(R.string.LRemovePW));
        initRemovePasswordEditText();
        initRemovePasswordButton();
        titleList.add(getResources().getString(R.string.LChangePW));
        initChangePasswordEditText();
        initChangePasswordButton();
        initChangeCircleImage();


        mViewPager.setAdapter(new SecurityPasswordAdapter(viewList, titleList));
        mViewPager.setCurrentItem(0);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initSecurityScsi();
        mRemoveLoading = (RelativeLayout)removeFragment.findViewById(R.id.remove_progress_view);
        mChangeLoading = (RelativeLayout)changeFragment.findViewById(R.id.change_progress_view);

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        clearAllValue();
    }

    private void clearAllValue(){
        editRemovePassword.setText("");
        editChangeCurrentPassword.setText("");
        editChangeNewPassword.setText("");
        editChangeConfirmPassword.setText("");
    }

    private void initChangePasswordEditText(){
        editChangeCurrentPassword = (EditText)changeFragment.findViewById(R.id.editChangeCurrentPassword);
        editChangeNewPassword = (EditText)changeFragment.findViewById(R.id.editChangeNewPassword);
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
        editChangeConfirmPassword = (EditText)changeFragment.findViewById(R.id.editChangeConfirmPassword);
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
        btnChangeOK = (Button)changeFragment.findViewById(R.id.btnChangeOK);
        btnChangeOK.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SecurityWarningDialog securityWarningDialog = new SecurityWarningDialog(SecurityPasswordFragment.this.getActivity() , "security" , "warning") {
                //    @Override
                //    public void onConfirm(String type) {
                //
                //    }
                //};

                if(imageChangeCircle.getVisibility() == View.VISIBLE){
                    mChangeLoading.setVisibility(View.VISIBLE);
                    btnChangeOK.setEnabled(false);
                    btnChangeCancel.setEnabled(false);
                    try {
                        securityScsi.SecurityDisableLockActivity(editChangeCurrentPassword.getText().toString());
                        if(securityScsi.checkSecurityStatus() != Constant.SECURITY_DISABLE){
                            snackBarShow(R.string.error);
                            cleanChangeEdit();
                        }

                        Thread.sleep(1000);
                        securityScsi.SecurityLockActivity(editChangeNewPassword.getText().toString());
                        Thread.sleep(1000);
                        if(securityScsi.checkSecurityStatus() == Constant.SECURITY_UNLOCK){
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

        btnChangeCancel = (Button)changeFragment.findViewById(R.id.btnChangeCancel);
        btnChangeCancel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBarShow(R.string.msg_password_incorrect);
                cleanChangeEdit();
            }
        });
    }

    private void initChangeCircleImage(){
        imageChangeCircle = (ImageView)changeFragment.findViewById(R.id.imageChangeCircle);
        imageChangeCircle.setVisibility(View.INVISIBLE);
    }

    private void initRemovePasswordEditText(){
        editRemovePassword = (EditText)removeFragment.findViewById(R.id.editRemovePassword);
    }

    private void initRemovePasswordButton(){
        btnRemoveOK = (Button)removeFragment.findViewById(R.id.btnRemoveOK);
        btnRemoveOK.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SecurityDisableDialog(removeFragment.getContext()){

                    @Override
                    public void onConfirm(boolean bExit) {
                        if(bExit){
                            try {
                                mRemoveLoading.setVisibility(View.VISIBLE);
                                btnRemoveOK.setEnabled(false);
                                btnRemoveCancel.setEnabled(false);
                                securityScsi.SecurityDisableLockActivity(editRemovePassword.getText().toString());
                                Thread.sleep(1000);
                                if(securityScsi.checkSecurityStatus() == Constant.SECURITY_DISABLE){
                                    snackBarShow(R.string.done);
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
                                mRemoveLoading.setVisibility(View.INVISIBLE);
                            }
                            btnRemoveOK.setEnabled(true);
                            btnRemoveCancel.setEnabled(true);
                            mRemoveLoading.setVisibility(View.INVISIBLE);
                        }
                    }
                };
            }
        });

        btnRemoveCancel = (Button)removeFragment.findViewById(R.id.btnRemoveCancel);
        btnRemoveCancel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                editRemovePassword.setText("");
                editRemovePassword.requestFocus();
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

    private void initSecurityScsi(){
        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(mContext);
        UsbMassStorageDevice device = devices[0];
        securityScsi = SecurityScsi.getInstance(device.getUsbDevice(), usbManager, false);
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


    public class SecurityPasswordAdapter extends PagerAdapter {
        private List<View> mListViews;
        private List<String> titleList;

        public SecurityPasswordAdapter(List<View> mListViews, List<String> mTitles) {
            this.mListViews = mListViews;
            this.titleList = mTitles;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)   {
            container.removeView((View) object);
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mListViews.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return  mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0==arg1;
        }
    }

}
