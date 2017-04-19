package com.transcend.otg.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.design.widget.TextInputLayout;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;

import com.transcend.otg.Constant.ActionParameter;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.LocalPreferences;
import com.transcend.otg.R;
import com.transcend.otg.Utils.FileFactory;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangbojie on 2016/6/8.
 */
public abstract class OTGNewFolderDialog implements TextWatcher, View.OnClickListener{
    public abstract void onConfirm(String newName, ArrayList<DocumentFile> mDFiles);

    private Context mContext;
    private AlertDialog mDialog;
    private Button mDlgBtnPos;
    private TextInputLayout mFieldName;

    private List<String> mFolderNames;
    private ArrayList<DocumentFile> mDFiles;
    private int mFromWhichActivity;
    private boolean mNewFolderInSD;


    public OTGNewFolderDialog(Context context, List<String> folderNames, int fromWhichActivity, boolean newFolderInSD) {
        mContext = context;
        mFolderNames = folderNames;
        mFromWhichActivity = fromWhichActivity;
        mNewFolderInSD = newFolderInSD;
        initData();
        initDialog();
        initFieldName();
    }

    private void initData(){
        mDFiles = new ArrayList<>();
        if (mNewFolderInSD) {
            String uid = FileFactory.getSDCardUniqueId();
            String sdKey = LocalPreferences.getSDKey(mContext, uid);
            if(sdKey != ""){
                Uri uriSDKey = Uri.parse(sdKey);
                if(mFromWhichActivity == 1){
                    DocumentFile tmpDFile = DocumentFile.fromTreeUri(mContext, uriSDKey);
                    Constant.mCurrentDocumentFileExplore = Constant.mSDRootDocumentFile = Constant.mSDCurrentDocumentFile = tmpDFile;
                    String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
                    ArrayList<FileInfo> mFiles = new ArrayList<>();
                    FileInfo tmpFile = new FileInfo();
                    tmpFile.path = ActionParameter.path;
                    mFiles.add(tmpFile);
                    mDFiles = FileFactory.findDocumentFilefromPathSD(mFiles, sdPath, mFromWhichActivity);
                }else if(mFromWhichActivity == 0){
                    mDFiles.add(DocumentFile.fromTreeUri(mContext, uriSDKey));
                }else if(mFromWhichActivity == 2){
                    DocumentFile tmpDFile = DocumentFile.fromTreeUri(mContext, uriSDKey);
                    Constant.mCurrentDocumentFileDestination = tmpDFile;
                    String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
                    ArrayList<FileInfo> mFiles = new ArrayList<>();
                    FileInfo tmpFile = new FileInfo();
                    tmpFile.path = ActionParameter.path;
                    mFiles.add(tmpFile);
                    mDFiles = FileFactory.findDocumentFilefromPathSD(mFiles, sdPath, mFromWhichActivity);
                }

            }
        } else { //OTG
            if(mFromWhichActivity == 1)
                mDFiles.add(Constant.mCurrentDocumentFileExplore);
            else if(mFromWhichActivity == 0)
                mDFiles.add(Constant.mRootDocumentFile);
            else if(mFromWhichActivity == 2)
                mDFiles.add(Constant.mCurrentDocumentFileDestination);
        }
    }

    private void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.newfolder));
        builder.setIcon(R.mipmap.ic_newfolder_gray);
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
            onConfirm(name, mDFiles);
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
