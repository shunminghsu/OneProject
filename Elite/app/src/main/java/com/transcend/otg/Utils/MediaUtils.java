package com.transcend.otg.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import com.transcend.otg.R;

import java.io.File;

/**
 * Created by wangbojie on 2017/2/21.
 */

public class MediaUtils {

    private static final boolean forResult = true;

    public static void execute(Context context, String filePath, String customizedTitle) {
        File file = new File(filePath);
        if (null == file) return;
        Intent intent = getIntent(file);
        Intent intentChooser = Intent.createChooser(intent, customizedTitle);
        startActivity(context, intentChooser, forResult);
    }

    public static void executeUri(Context context, String uriPath, String customizedTitle) {
        Uri uri = Uri.parse(uriPath);
        Intent intent = getIntentUri(context, uri);
        Intent intentChooser = Intent.createChooser(intent, customizedTitle);
        startActivity(context, intentChooser, forResult);
    }

    private static Intent getIntent(File file) {
        Uri data = Uri.fromFile(file);
        String type = MimeUtil.getMimeType(file.getPath());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(data, type);
        return intent;
    }

    private static Intent getIntentUri(Context mContext, Uri data) {
        mContext.grantUriPermission("com.transcend.otg", data, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        String type = MimeUtil.getMimeType(data.getPath());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        intent.setDataAndType(data, type);
        return intent;
    }

    private static void startActivity(Context context, Intent intent, boolean forResult) {
        try {
            if (forResult)
                ((Activity) context).startActivityForResult(intent, 10);
            else
                context.startActivity(intent);
        }
        catch (Exception e) {
            Toast.makeText(context, "Action Failed", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public static boolean localShare(Activity act, String path) {
        Uri uri = createUri(path);
        String name = parseName(path);
        String type = MimeUtil.getMimeType(path);
        if(type != null && !type.equals("enc")){
            shareIn(act, uri, type, name);
            return true;
        }else{
            return false;
        }
    }

    private static void shareIn(Activity act, Uri uri, String type, String title) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType(type);
        act.startActivity(intent);
    }

    public static String parseName(String path){
        String[] paths = path.split("/");
        String name = paths.length >= 1 ? paths[paths.length-1] : "";
        return name;
    }

    public static Uri createUri(String path){
        Uri uri;
        uri = Uri.fromFile(new File(path));
        return uri;
    }

}
