package com.transcend.otg.Loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.support.v4.provider.DocumentFile;

import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Constant.LoaderID;

import java.util.ArrayList;
import java.util.List;



/**
 * Created by wangbojie on 2017/3/3.
 */

public class OTGActionService extends LocalActionService {

    public OTGActionService() {
        OTGLIST = LoaderID.OTG_FILE_LIST;
        RENAME_OTG = LoaderID.OTG_RENAME;

    }



    @Override
    public void onLoadFinished(Context context, Loader<Boolean> loader, Boolean success) {

    }
}
