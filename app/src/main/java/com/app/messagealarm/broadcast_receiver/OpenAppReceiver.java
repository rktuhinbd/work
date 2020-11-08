package com.app.messagealarm.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.app.messagealarm.ui.notifications.FloatingNotification;
import com.app.messagealarm.utils.Constants;

public class OpenAppReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        openApp(context, intent.getStringExtra(Constants.IntentKeys.PACKAGE_NAME));
        if(intent.getIntExtra(Constants.IntentKeys.TYPE_ALARM, 0) == Constants.Default.TYPE_ALARM){
            FloatingNotification.Companion.cancelAlarmNotification();
        }else {
            FloatingNotification.Companion.cancelMissedAlarmNotification();
        }

    }

    private void openApp(Context context, String packageName){
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        context.startActivity(intent);
    }
}
