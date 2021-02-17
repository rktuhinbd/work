package com.app.messagealarm.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.app.messagealarm.ui.notifications.FloatingNotification;
import com.app.messagealarm.utils.Constants;
import com.app.messagealarm.utils.SharedPrefUtils;

public class OpenAppReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() != null){
            if(intent.getAction().equals("CANCEL")){
                if(!SharedPrefUtils.INSTANCE.contains(Constants.PreferenceKeys.IS_FIRST_TIME_ALARM_PLAYED)){
                    SharedPrefUtils.INSTANCE.write(Constants.PreferenceKeys.IS_FIRST_TIME_ALARM_PLAYED, true);
                }
                FloatingNotification.Companion.cancelAlarmNotification();
            }else if(intent.getAction().equals("OPEN_APP")) {
                if(!SharedPrefUtils.INSTANCE.contains(Constants.PreferenceKeys.IS_FIRST_TIME_ALARM_PLAYED)){
                    SharedPrefUtils.INSTANCE.write(Constants.PreferenceKeys.IS_FIRST_TIME_ALARM_PLAYED, true);
                }
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
