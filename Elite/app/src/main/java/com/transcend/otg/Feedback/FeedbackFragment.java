package com.transcend.otg.Feedback;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;


import com.jaredrummler.android.device.DeviceName;
import com.transcend.otg.Dialog.EmptyNotificationDialog;
import com.transcend.otg.Dialog.FeedbackOKDialog;
import com.transcend.otg.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import jcifs.util.Base64;


/**
 * Created by wangbojie on 2017/3/25.
 */

public class FeedbackFragment extends Fragment {
    private Context mContext;
    private TextInputLayout mInputName, mInputEmail, mInputDevice;
    private EditText mEditTextName, mEditTextEmail, mEditTextDevice, mEditTextMessage;
    private View mProgressBar;
    private Button mBtnSend;
    private Spinner mSpinnerRegion;
    private static final String TAG = FeedbackFragment.class.getSimpleName();

    private static final String CATEGORY_LIST_URL = "http://www.transcend-info.com/Service/SMSService.svc/web/GetSrvCategoryList";
//    private static final String CATEGORY_LIST_URL = "http://10.13.5.10:85/Service/SMSService.svc/web/GetSrvCategoryList";
    private static final String FEEDBACK_URL = "http://www.transcend-info.com/Service/SMSService.svc/web/ServiceMailCaseAdd";
//    private static final String FEEDBACK_URL = "http://10.13.5.10:85/Service/RDAppWcf.svc/web/ServiceMailCaseAdd";
    private static final int ID_ERROR_HANDLING = -1;
    private static final int ID_GET_CATEGORY_LIST = 0;
    private static final int ID_SEND_FEEDBACK = 1;
    private static final String KEY_SERVICE_TYPE = "service_type";
    private static final String KEY_SERVICE_CATEGORY = "service_category";
    private static final String PRODUCT_NAME = "Car Video Recorders";
    private static String REGION = "Taiwan";
    private static String REGION_ISO = "tw";

    public FeedbackFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_feedback, container, false);
        mContext = getContext();
        mInputName = (TextInputLayout) root.findViewById(R.id.input_layout_name);
        mInputEmail = (TextInputLayout) root.findViewById(R.id.input_layout_email);
        mInputDevice = (TextInputLayout) root.findViewById(R.id.input_layout_device);
        mSpinnerRegion = (Spinner) root.findViewById(R.id.spinner_region);

        mEditTextName = (EditText) root.findViewById(R.id.input_name);
        mEditTextEmail = (EditText) root.findViewById(R.id.input_email);
        mEditTextDevice = (EditText) root.findViewById(R.id.input_device);
        mEditTextMessage = (EditText) root.findViewById(R.id.input_message);

        mEditTextName.addTextChangedListener(new MyTextWatcher(mEditTextName));
        mEditTextEmail.addTextChangedListener(new MyTextWatcher(mEditTextEmail));
        mEditTextMessage.addTextChangedListener(new MyTextWatcher(mEditTextMessage));
        mProgressBar = root.findViewById(R.id.settings_progress_view);
        mBtnSend = (Button) root.findViewById(R.id.btn_send);
        mBtnSend.setTransformationMethod(null);
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isInputValid()){
                    REGION = mSpinnerRegion.getSelectedItem().toString();
                    REGION_ISO = REGION;
                    mProgressBar.setVisibility(View.VISIBLE);
                    sendRequest(configurePostRequest(CATEGORY_LIST_URL), null, ID_GET_CATEGORY_LIST);
                }else{
                    new EmptyNotificationDialog(mContext);
                }
            }
        });
        clearAllValue();
        getDeviceName();
        getRegionName();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return root;
    }

    private void clearAllValue(){
        mEditTextName.setText("");
        mEditTextEmail.setText("");
        mEditTextMessage.setText("");
    }

    private void sendRequest(final HttpURLConnection connection, final String jsonData, final int messageId)
    {
        if (connection == null) {
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                Message msg = new Message();
                try {
                    OutputStream out = connection.getOutputStream();
                    if (jsonData != null) {
                        out.write(jsonData.getBytes());
                    }
                    out.flush();
                    out.close();

                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        String result = getResponseResult(connection);
                        Log.d(TAG, "response result: " + result);

                        if (messageId == ID_GET_CATEGORY_LIST) {
                            setMessageData(msg, result);
                        }

                        msg.what = messageId;
                        mHandler.sendMessage(msg);
                    }

                } catch (IOException e) {
                    Log.d(TAG, "IOException========================================================================");
                    e.printStackTrace();
                    doErrorHandling();
                    Log.d(TAG, "IOException========================================================================");
                }
            }

        }).start();

    }

    private HttpURLConnection configurePostRequest(String requestUrl) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("content-type", "application/json");
            conn.setDoOutput(true);
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);

        } catch (Exception e) {
            Log.d(TAG, "HttpURLConnection========================================================================");
            e.printStackTrace();
            doErrorHandling();
            Log.d(TAG, "HttpURLConnection========================================================================");
        }
        return conn;
    }

    private void doErrorHandling() {
        Message msg = new Message();
        msg.what = ID_ERROR_HANDLING;
        mHandler.sendMessage(msg);
    }

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case ID_GET_CATEGORY_LIST:
                    String feedbackData = getFeedbackData(msg);
                    if (feedbackData != null) {
                        sendRequest(configurePostRequest(FEEDBACK_URL), feedbackData, ID_SEND_FEEDBACK);
                    }
                    break;
                case ID_SEND_FEEDBACK:
                    mProgressBar.setVisibility(View.INVISIBLE);
                    new FeedbackOKDialog(mContext, true) {
                        @Override
                        public void onConfirm(Context mContext) {

                        }
                    };
                    break;
                case ID_ERROR_HANDLING:
                    mProgressBar.setVisibility(View.INVISIBLE);
                    new FeedbackOKDialog(mContext, false) {
                        @Override
                        public void onConfirm(Context mContext) {
                            //do nothing
                        }
                    };
                    break;
            }
        }

        private String getFeedbackData(Message msg) {
            String jsonData = null;
            if (msg.getData() != null) {
                String srvType = msg.getData().getString(KEY_SERVICE_TYPE);
                String srvCategory = msg.getData().getString(KEY_SERVICE_CATEGORY);
                String platformInfo = "Device OS version & device name : " + mEditTextDevice.getText().toString();

                jsonData = "{\"DataModel\":{\"CustName\":\"" + mEditTextName.getText().toString() + "\"" +
                        ",\"CustEmail\":\"" + mEditTextEmail.getText().toString() + "\"" +
                        ",\"Region\":\"" + REGION + "\"" +
                        ",\"ISOCode\":\"" + REGION_ISO + "\"" +
                        ",\"Request\":\"" + platformInfo + "\"" +
                        ",\"SrvType\":\"" + srvType + "\"" +
                        ",\"SrvCategory\":\"" + srvCategory + "\"" +
                        ",\"ProductName \":\"" + PRODUCT_NAME + "\"" +
                        ",\"LocalProb\":\"" + Base64.encode(mEditTextMessage.getText().toString().getBytes()) + "\"}}";
            }
            return jsonData;
        }
    };

    private void getDeviceName() {
        String deviceName = DeviceName.getDeviceName();
        String deviceOS = android.os.Build.VERSION.RELEASE;;
        if(deviceName!=""){
            mEditTextDevice.setText("OS : " + deviceOS + ", " + deviceName );
        }
    }

    private void getRegionName(){
        Locale current = getResources().getConfiguration().locale;

        Locale[] locale = Locale.getAvailableLocales();
        ArrayList<String> countries = new ArrayList<String>();
        String country;
        for( Locale loc : locale ){
            country = loc.getDisplayCountry();
            if( country.length() > 0 && !countries.contains(country) ){
                countries.add( country );
            }
        }
        Collections.sort(countries, String.CASE_INSENSITIVE_ORDER);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, R.layout.support_simple_spinner_dropdown_item, countries);
        mSpinnerRegion.setAdapter(adapter);
        mSpinnerRegion.setSelection(adapter.getPosition(current.getDisplayCountry()));

//        TelephonyManager tm = (TelephonyManager)mContext.getSystemService(mContext.TELEPHONY_SERVICE);
//        String countryCodeValue = tm.getSimCountryIso();
//        if(countryCodeValue==""){
//            countryCodeValue = getResources().getConfiguration().locale.getCountry();
//        }

//        REGION = countryCodeValue;
//        REGION_ISO = getResources().getConfiguration().locale.getISO3Country();
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private void setMessageData(Message msg, String responseData) {
        Bundle data = new Bundle();
        data.putString(KEY_SERVICE_TYPE, String.valueOf(15));
        data.putString(KEY_SERVICE_CATEGORY, String.valueOf(228));
        msg.setData(data);
    }

    private String getResponseResult(HttpURLConnection connection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = in.readLine();

        Log.d(TAG, "get category list result: " + line);
        in.close();
        return line;
    }

    private boolean isInputValid()
    {
        return isValidName() && isValidEmail() && isValidMessage() && isValidDevice() ;
    }

    private boolean isValidName() {
        if (mEditTextName.getText().toString().trim().isEmpty()) {
            mInputName.setHint(getString(R.string.feedback_edittext_require));
            return false;
        } else {
            mInputName.setHintEnabled(false);
            return true;
        }
    }

    private boolean isValidEmail() {
        String email = mEditTextEmail.getText().toString().trim();
        boolean isValid = !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();

        if (!isValid) {
            mInputEmail.setHint(getString(R.string.feedback_edittext_require));
            return false;
        } else {
            mInputEmail.setHintEnabled(false);
            return true;
        }
    }

    private boolean isValidDevice() {
        if (mEditTextDevice.getText().toString().trim().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidMessage() {
        String message = mEditTextMessage.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            return false;
        } else {

            return true;
        }
    }


    private class MyTextWatcher implements TextWatcher {
        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_name:
                    isValidName();
                    break;
                case R.id.input_email:
                    isValidEmail();
                    break;
                case R.id.input_message:
//                    isValidMessage();
                    break;
            }
        }
    }
}
