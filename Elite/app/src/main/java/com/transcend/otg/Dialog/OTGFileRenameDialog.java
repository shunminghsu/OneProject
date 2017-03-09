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

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangbojie on 2017/3/8.
 */
public abstract class OTGFileRenameDialog implements TextWatcher, View.OnClickListener {

    public abstract void onConfirm(String newName, String oldName, ArrayList<DocumentFile> dFiles);

    private Context mContext;
    private AlertDialog mDialog;
    private Button mDlgBtnPos;
    private TextInputLayout mFieldName;

    private String mName, mType;
    private ArrayList<DocumentFile> mDFiles;
    private boolean mIgnoreType, mFromName;
    private List<String> mNames;
    private ArrayList<FileInfo> mFiles;

    public OTGFileRenameDialog(Context context, ArrayList<FileInfo> files, boolean fromName) {
        mContext = context;
        mFiles = files;
        mNames = new ArrayList<>();
        mIgnoreType = false;
        mFromName = fromName;

        initNames();
        initDialog();
        initFieldName();
    }

    private void initNames(){
        if(mFromName)
            mDFiles = FileFactory.findDocumentFilefromName(mFiles);
        else
            mDFiles = FileFactory.findDocumentFilefromPath(mFiles);
        String name = mFiles.get(0).name;
        mIgnoreType = (mFiles.get(0).type == Constant.TYPE_DIR);
        DocumentFile[] parentFiles = mDFiles.get(0).getParentFile().listFiles();
        for (DocumentFile file : parentFiles) {
            mNames.add(file.getName().toLowerCase());
        }
        if (!mIgnoreType) {
            mName = FilenameUtils.getBaseName(name);
            mType = FilenameUtils.getExtension(name);
        }else{
            mName = name;
        }

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
    public void onClick(View v) {
        if (v.equals(mDlgBtnPos)) {
            if (mFieldName.getEditText() == null || mFieldName.getEditText().getText().toString().equals("")) return;
            String name = addExtension(mFieldName.getEditText().getText().toString());
            onConfirm(name, mName, mDFiles);
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
        if(!mIgnoreType)
            name = name + "." + mType;
        return mNames.contains(name);
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