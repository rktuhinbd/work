package com.app.messagealarm.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.app.messagealarm.ui.notifications.FloatingNotification;
import com.app.messagealarm.utils.Constants;
import com.app.messagealarm.utils.SharedPrefUtils;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("ALARM", "TRUE");
        SharedPrefUtils.INSTANCE.write(Constants.PreferenceKeys.IS_MUTED, false);
        FloatingNotification.Companion.notifyMute(false);
    }
}
