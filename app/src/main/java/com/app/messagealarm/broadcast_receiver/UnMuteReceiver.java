package com.app.messagealarm.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.app.messagealarm.ui.notifications.FloatingNotification;
import com.app.messagealarm.utils.Constants;
import com.app.messagealarm.utils.MuteUtils;
import com.app.messagealarm.utils.SharedPrefUtils;

public class UnMuteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!SharedPrefUtils.INSTANCE.readBoolean(Constants.PreferenceKeys.IS_MUTED)){
            FloatingNotification.Companion.notifyMute(true);
            SharedPrefUtils.INSTANCE.write(Constants.PreferenceKeys.IS_MUTED, true);
        }else{
            FloatingNotification.Companion.notifyMute(false);
            SharedPrefUtils.INSTANCE.write(Constants.PreferenceKeys.IS_MUTED, false);
        }


    }

}
