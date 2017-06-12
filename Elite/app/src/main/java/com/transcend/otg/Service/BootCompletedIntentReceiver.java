package com.transcend.otg.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.transcend.otg.Service.OTGInsertService;

/**
 * Created by wangbojie on 2017/4/26.
 */

public class BootCompletedIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, OTGInsertService.class);
            context.startService(pushIntent);
        }
    }
}
