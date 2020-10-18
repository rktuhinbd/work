package com.app.messagealarm.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.app.messagealarm.BaseApplication;
import com.app.messagealarm.service.notification_service.NotificationListener;

import static android.service.notification.NotificationListenerService.requestRebind;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                    startMagicService(context);
                    tryReconnectService(context);
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



    public void tryReconnectService(Context context) {
        toggleNotificationListenerService(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ComponentName componentName =
                    new ComponentName(context, NotificationListener.class);

            //It say to Notification Manager RE-BIND your service to listen notifications again inmediatelly!
            requestRebind(componentName);
        }
    }

    /**
     * Try deactivate/activate your component service
     */
    private void toggleNotificationListenerService(Context context) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context, NotificationListener.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(context, NotificationListener.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }


}
