package com.app.messagealarm.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.app.messagealarm.ui.notifications.FloatingNotification;
import com.app.messagealarm.utils.MuteUtils;

public class UnMuteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        FloatingNotification.Companion.notifyMute(true);
    }

}
