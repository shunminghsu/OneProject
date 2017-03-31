package com.transcend.otg.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.transcend.otg.R;

import org.apache.commons.io.FilenameUtils;

import java.util.List;

/**
 * Created by wangbojie on 2017/3/6.
 */
public abstract class LocalRenameDialog implements TextWatcher, View.OnClickListener {

    public abstract void onConfirm(String newName);

    private Context mContext;
    private AlertDialog mDialog;
    private Button mDlgBtnPos;
    private TextInputLayout mFieldName;

    private String mName, mType;
    private boolean mIgnoreType;

    public LocalRenameDialog(Context context, boolean ignoreType, String name) {
        mContext = context;
        mIgnoreType = ignoreType;
        if(!ignoreType){
            mName = FilenameUtils.getBaseName(name);
            mType = FilenameUtils.getExtension(name);
        } else {
            mName = name;
        }
        initDialog();
        initFieldName();
    }

    private void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.rename));
        builder.setIcon(R.drawable.ic_menu_camera);
        builder.setView(R.layout.dialog_rename);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.confirm, null);
        builder.setCancelable(true);
        mDialog = builder.show();
        mDlgBtnPos = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mDlgBtnPos.setOnClickListener(this);
    }

    private void initFieldName() {
        mFieldName = (TextInputLayout)mDialog.findViewById(R.id.dialog_rename_name);
        if (mFieldName.getEditText() == null)
            return;
        mFieldName.getEditText().setText(mName);
        mFieldName.getEditText().addTextChangedListener(this);
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

        if (!isIlleagal(name.toLowerCase())) {
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
            if (mFieldName.getEditText() == null) return;
            String name = addExtension(mFieldName.getEditText().getText().toString());
            onConfirm(name);
            mDialog.dismiss();
        }
    }

    private boolean isInvalid(String name) {
        return (name == null) || (name.isEmpty());
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

    private String addExtension(String name){
        if(!mIgnoreType)
            name = name + "." + mType;
        return name;
    }
}
