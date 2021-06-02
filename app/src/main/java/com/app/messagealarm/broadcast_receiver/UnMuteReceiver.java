package com.app.messagealarm.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.app.messagealarm.ui.notifications.FloatingNotification;
import com.app.messagealarm.utils.Constants;
import com.app.messagealarm.utils.MuteUtils;
import com.app.messagealarm.utils.SharedPrefUtils;

import es.dmoral.toasty.Toasty;

public class UnMuteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!SharedPrefUtils.INSTANCE.readBoolean(Constants.PreferenceKeys.IS_MUTED)){
            SharedPrefUtils.INSTANCE.write(Constants.PreferenceKeys.IS_MUTED, true);
            FloatingNotification.Companion.notifyMute(true);
        }else{
            SharedPrefUtils.INSTANCE.write(Constants.PreferenceKeys.IS_MUTED, false);
            FloatingNotification.Companion.notifyMute(false);
        }
    }

}
