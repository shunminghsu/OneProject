package com.transcend.otg.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;

import com.transcend.otg.R;
import com.transcend.otg.Utils.Md5;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by wangbojie on 2017/3/23.
 */
public abstract class LocalDecryptDialog implements View.OnClickListener, TextWatcher {

    public abstract void onConfirm(String newFolderPath, String password, String mFilePath);

    private Context mContext;
    private AlertDialog mDialog;
    private Button mDlgBtnPos;
    private TextInputLayout mFieldFolderName, mFieldPassword;
    private String mFilePath;

    private ArrayList<String> mFolderNames;

    public LocalDecryptDialog(Context context, ArrayList<String> folderNames, String filePath) {
        mContext = context;
        mFolderNames = folderNames;
        mFilePath = filePath;
        initDialog();
        initFieldName();
    }

    private void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.decrypt));
        builder.setIcon(R.mipmap.ic_decrypt_gray);
        builder.setView(R.layout.dialog_decrypt);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.confirm, null);
        builder.setCancelable(true);
        mDialog = builder.show();
        mDlgBtnPos = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mDlgBtnPos.setOnClickListener(this);
    }

    private void initFieldName() {
        mFieldFolderName = (TextInputLayout) mDialog.findViewById(R.id.dialog_decrypt_name);
        if (mFieldFolderName.getEditText() == null)
            return;
        mFieldFolderName.getEditText().setText(getUniqueName());
        mFieldFolderName.getEditText().addTextChangedListener(this);
        mFieldPassword = (TextInputLayout) mDialog.findViewById(R.id.dialog_decrypt_password);
    }


    @Override
    public void onClick(View v) {
        if (v.equals(mDlgBtnPos)) {
            if (mFieldFolderName.getEditText().getText().toString().equals("") || mFieldPassword.getEditText().getText().toString().equals("") || mFieldPassword.getEditText().getText().toString().equals("")) {
                return;
            }
            String name = mFieldFolderName.getEditText().getText().toString();
            String password = mFieldPassword.getEditText().getText().toString();
            password = getPassword(password);
            File tmpFile = new File(mFilePath);
            String newFolderPath = tmpFile.getParent() + File.separator + name;

            onConfirm(newFolderPath, password, mFilePath);
            mDialog.dismiss();
        }else{
            mDialog.dismiss();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String name = s.toString();
        String error = null;
        boolean enabled = true;
        if (isInvalid(name)) {
            error = mContext.getResources().getString(R.string.invalid_name);
            enabled = false;
        }
        if (isDuplicated(name)) {
            error = mContext.getResources().getString(R.string.duplicate_name);
            enabled = false;
        }
        if (!isIlleagal(name)) {
            error = mContext.getResources().getString(R.string.illegal_name);
            enabled = false;
        }
        mFieldFolderName.setError(error);
        mDlgBtnPos.setEnabled(enabled);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    private boolean isInvalid(String name) {
        return (name == null) || (name.isEmpty());
    }

    private boolean isDuplicated(String name) {
        if (isInvalid(name)) return false;
        return mFolderNames.contains(name.toLowerCase());
    }
    private boolean isIlleagal(String name){
        if (isInvalid(name)) return true;
        String prefix = FilenameUtils.getBaseName(name);
        if(name.contains("\\"))return false;
        if(name.contains("/"))return false;
        if(prefix.contains(":"))return false;
        if(prefix.contains("*"))return false;
        if(prefix.contains("?"))return false;
        if(prefix.contains("<"))return false;
        if(prefix.contains(">"))return false;
        if(prefix.contains("|"))return false;
        if(prefix.contains("\""))return false;
        if(prefix.startsWith("."))return false;
        if(name.equals("."))return false;

        return true;
    }

    private String getUniqueName() {
        int index = 1;
        String name = mContext.getResources().getString(R.string.untitled_folder);
        String unique = name;
        while (mFolderNames.contains(unique)) {
            unique = String.format(name + " (%d)", index++);
        }
        return unique;
    }

    private static String getPassword(String str) {

        String result = str;
        String enc = result;
        String md5 = null;
        for (int i=0; i<3; i++) {
            md5 = Md5.encode(enc);
            enc = md5.toUpperCase();
        }

        return  enc.toString();
    }
}
