package com.app.messagealarm.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.app.messagealarm.BaseApplication;
import com.app.messagealarm.service.notification_service.NotificationListener;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                    startMagicService(context);
            }
        }
    }


    private void startMagicService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(context, NotificationListener.class);
            context.startForegroundService(intent);
        } else {
            Intent intent = new Intent(context, NotificationListener.class);
            context.startService(intent);
        }
    }


}
