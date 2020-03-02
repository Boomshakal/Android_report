package com.megmeet.megmeetreport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.megmeet.megmeetreport.ReportActivity;

public class AutoStartBroadcastReceiver extends BroadcastReceiver {

    static final String action_boot = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(action_boot)) {
            Log.e("len", "BOOT:" + action_boot);
            intent = new Intent(context, ReportActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("loginaction", "autostart");
            context.startActivity(intent);
        }
    }
}
