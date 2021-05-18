package com.app.messagealarm.broadcast_receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.app.messagealarm.BaseApplication;
import com.app.messagealarm.service.notification_service.NotificationListener;
import com.app.messagealarm.utils.AndroidUtils;
import com.app.messagealarm.utils.MediaUtils;

public class PowerOffReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        stopService(context);
        stopPowerSwitchInActivity(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaUtils.Companion.stopAlarm();
            NotificationManager nMgr = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            nMgr.cancelAll();
        }
    }

    private void stopService(Context context) {
        if (AndroidUtils.Companion.isServiceRunning(context, NotificationListener.class)) {
            Intent intent = new Intent(context, NotificationListener.class);
            intent.setAction(NotificationListener.ACTION_STOP_FOREGROUND_SERVICE);
            context.startService(intent);
        }
    }

    private void stopPowerSwitchInActivity(Context context) {
        Intent intent = new Intent("turn_off_switch");
        //context.sendBroadcast(intent);
        /**
         * Using this for RemoteServiceException in some devices
         */
        LocalBroadcastManager.getInstance(BaseApplication.Companion.getBaseApplicationContext()).sendBroadcast(intent);
    }
}
