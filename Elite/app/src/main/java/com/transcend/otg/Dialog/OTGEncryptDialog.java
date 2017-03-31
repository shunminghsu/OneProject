package com.transcend.otg.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;

import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.R;
import com.transcend.otg.Utils.FileFactory;
import com.transcend.otg.Utils.Md5;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;

/**
 * Created by wangbojie on 2016/7/7.
 */
public abstract class OTGEncryptDialog implements TextWatcher, View.OnClickListener {

    public abstract void onConfirm(String newName, String password, ArrayList<DocumentFile> selectedDFiles);

    private Context mContext;
    private AlertDialog mDialog;
    private Button mDlgBtnPos;
    private TextInputLayout mFieldName, mFieldPassword;

    //private ArrayList<String> mEncryptFileNames;
    private ArrayList<FileInfo> mSelectedFiles;
    private ArrayList<DocumentFile> mSelectedDFiles;

    public OTGEncryptDialog(Context context, ArrayList<FileInfo> selectedFiles) {
        mContext = context;
        //mEncryptFileNames = encryptFileNames;
        mSelectedFiles = selectedFiles;
        init();
        initDialog();
        initFieldName();
    }

    private void init(){
        if(Constant.nowMODE == Constant.MODE.SD){

        }else if(Constant.nowMODE == Constant.MODE.OTG){
            String otgPath = FileFactory.getOTGStoragePath(mContext, Constant.otg_key_path);
            mSelectedDFiles = FileFactory.findDocumentFilefromPathOTG(mSelectedFiles, otgPath, Constant.Activity);
        }
    }

    private void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.encrypt));
        builder.setIcon(R.drawable.ic_menu_camera);
        builder.setView(R.layout.dialog_encrypt);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.confirm, null);
        builder.setCancelable(true);
        mDialog = builder.show();
        mDlgBtnPos = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mDlgBtnPos.setOnClickListener(this);
    }

    private void initFieldName() {
        mFieldName = (TextInputLayout)mDialog.findViewById(R.id.dialog_encrypt_name);
        if (mFieldName.getEditText() == null)
            return;
        mFieldName.getEditText().setText(getUniqueName());
        mFieldName.getEditText().addTextChangedListener(this);
        mFieldPassword = (TextInputLayout)mDialog.findViewById(R.id.dialog_encrypt_password);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        mDlgBtnPos.setEnabled(false);
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
        /*if (isDuplicated(name)) {
            error = mContext.getResources().getString(R.string.duplicate_name);
            enabled = false;
        }*/

        if (!isIlleagal(name)) {
            error = mContext.getResources().getString(R.string.illegal_name);
            enabled = false;
        }
        mFieldName.setError(error);
        mDlgBtnPos.setEnabled(enabled);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        if (v.equals(mDlgBtnPos)) {
            if (mFieldName.getEditText() == null || mFieldName.getEditText().getText().toString().equals("") || mFieldPassword.getEditText().getText().toString().equals("")) return;
            String name = mFieldName.getEditText().getText().toString();
            String password = mFieldPassword.getEditText().getText().toString();
            password = getPassword(password);
            onConfirm(name, password, mSelectedDFiles);
            mDialog.dismiss();
        }
    }

    private String getUniqueName() {
        int index = 1;
        String name = mContext.getResources().getString(R.string.untitled_folder);
        String unique = name;
        //while (mEncryptFileNames.contains(unique)) {
        //    unique = String.format(name + " (%d)", index++);
        //}
        return unique;
    }

    private boolean isInvalid(String name) {
        return (name == null) || (name.isEmpty());
    }

    /*private boolean isDuplicated(String name) {
        if (isInvalid(name)) return false;
        return mEncryptFileNames.contains(name.toLowerCase() + mContext.getResources().getString(R.string.encrypt_subfilename));
    }*/

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
