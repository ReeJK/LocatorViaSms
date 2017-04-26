package com.wnezros.locatorviasms.Broadcast;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

public class StopReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        BroadcastUtils.cancelBroadcasting(context);
        context.sendBroadcast(new Intent(SettingsFragment.UPDATE_ACTION));
    }
}
