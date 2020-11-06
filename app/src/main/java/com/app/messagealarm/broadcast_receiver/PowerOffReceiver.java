package com.app.messagealarm.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.app.messagealarm.service.notification_service.NotificationListener;
import com.app.messagealarm.utils.AndroidUtils;

public class PowerOffReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
            stopService(context);
            stopPowerSwitchInActivity(context);

    }

    private void stopService(Context context){
        if(AndroidUtils.Companion.isServiceRunning(context, NotificationListener.class)){
            Intent intent = new Intent(context, NotificationListener.class);
            intent.setAction(NotificationListener.ACTION_STOP_FOREGROUND_SERVICE);
            context.startService(intent);
        }
    }

    private void stopPowerSwitchInActivity(Context context){
        Intent intent = new Intent("turn_off_switch");
        context.sendBroadcast(intent);
    }
}
