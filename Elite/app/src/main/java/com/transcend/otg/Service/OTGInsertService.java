package com.transcend.otg.Service;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.view.WindowManager;

import com.transcend.otg.R;

import java.util.List;

/**
 * Created by wangbojie on 2017/4/26.
 */
public class OTGInsertService extends Service {

    private IntentFilter intentFilter;
    private AlertDialog dialog;
    final CharSequence[] items = {"Use by default for OTG device"};
    final boolean[] itemsBoolean = new boolean[]{false};

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        doRegisterIntent();
        return Service.START_STICKY;
    }


    @Override
    public void onDestroy() {
        dialog.dismiss();
        super.onDestroy();
    }

    public void doRegisterIntent() {
        intentFilter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, intentFilter);
    }


    public final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                synchronized (this) {
                    if (!isAppOnForeground())
                        if(dialog != null){
                            if(!dialog.isShowing())
                                doDialog(context);
                        }else{
                            doDialog(context);
                        }

                }
            }
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    if (dialog != null)
                        dialog.cancel();
                }
            }
        }
    };

    private void doDialog(Context context) {
        dialog = new AlertDialog.Builder(context, R.style.AppDialog)
                .setTitle(R.string.app_name)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(getString(R.string.open_app_when_insert_otg))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("com.transcend.otg");
                        startActivity(LaunchIntent);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    private boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(
                Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null)
            return false;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
