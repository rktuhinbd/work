package com.app.messagealarm.utils

class SnoozeUtils{
    companion object{
         fun activateSnoozeMode(status: Boolean){
            SharedPrefUtils.write(
                Constants.PreferenceKeys.IS_SNOOZED_MODE_ACTIVE, status
            )
        }

         fun isSnoozedModeActivate() : Boolean{
            return false //SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_SNOOZED_MODE_ACTIVE)
        }
    }
}