package com.app.messagealarm.utils

import com.app.messagealarm.service.notification_service.NotificationListener

class MuteUtils {
    companion object{

        fun muteApp(){
            SharedPrefUtils.write(Constants.PreferenceKeys.IS_MUTED, true)
        }


        fun unMuteApp(){
            SharedPrefUtils.write(Constants.PreferenceKeys.IS_MUTED, false)
        }
    }
}