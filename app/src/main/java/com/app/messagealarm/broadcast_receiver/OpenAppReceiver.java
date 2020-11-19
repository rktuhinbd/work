package com.app.messagealarm.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.app.messagealarm.ui.notifications.FloatingNotification;
import com.app.messagealarm.utils.Constants;

public class OpenAppReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() != null){
            if(intent.getAction().equals("CANCEL")){
                FloatingNotification.Companion.cancelAlarmNotification();
            }else if(intent.getAction().equals("OPEN_APP")) {
                openApp(context, intent.getStringExtra(Constants.IntentKeys.PACKAGE_NAME));
                FloatingNotification.Companion.cancelAlarmNotification();
            }
        }
    }

    private void openApp(Context context, String packageName){
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        context.startActivity(intent);
    }
}
