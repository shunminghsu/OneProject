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
import com.transcend.otg.R;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangbojie on 2016/6/8.
 */
public abstract class OTGNewFolderDialog implements TextWatcher, View.OnClickListener{
    public abstract void onConfirm(String newName, ArrayList<DocumentFile> mRootDFile);

    private Context mContext;
    private AlertDialog mDialog;
    private Button mDlgBtnPos;
    private TextInputLayout mFieldName;

    private List<String> mFolderNames;
    private ArrayList<DocumentFile> mRootDFile;

    public OTGNewFolderDialog(Context context, List<String> folderNames) {
        mContext = context;
        mFolderNames = folderNames;
        initData();
        initDialog();
        initFieldName();
    }

    private void initData(){
        mRootDFile = new ArrayList<>();
        mRootDFile.add(Constant.mCurrentDocumentFile);
    }

    private void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.menu_new_folder));
        builder.setIcon(R.drawable.ic_menu_camera);
        builder.setView(R.layout.dialog_new_folder);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.confirm, null);
        builder.setCancelable(true);
        mDialog = builder.show();
        mDlgBtnPos = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mDlgBtnPos.setOnClickListener(this);
    }

    private void initFieldName() {
        mFieldName = (TextInputLayout)mDialog.findViewById(R.id.dialog_folder_create_name);
        if (mFieldName.getEditText() == null)
            return;
        mFieldName.getEditText().setText(getUniqueName());
        mFieldName.getEditText().addTextChangedListener(this);
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

    @Override
    public void onClick(View v) {
        if (v.equals(mDlgBtnPos)) {
            if (mFieldName.getEditText() == null || mFieldName.getEditText().getText().toString().equals("")) return;
            String name = mFieldName.getEditText().getText().toString();
            onConfirm(name, mRootDFile);
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
        mFieldName.setError(error);
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
}
