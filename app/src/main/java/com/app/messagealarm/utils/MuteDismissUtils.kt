package com.app.messagealarm.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.app.messagealarm.broadcast_receiver.AlarmReceiver
import java.util.*


class MuteDismissUtils {
    companion object{
        fun setAlarmForDismissingMute(context: Context){
            // get a Calendar object with current time
            val cal: Calendar = Calendar.getInstance()
            // add 30 seconds to the calendar object
            cal.add(Calendar.SECOND, 30)
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            val sender = PendingIntent.getBroadcast(
                context,
                192837,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            // Get the AlarmManager service
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            //am[AlarmManager.RTC_WAKEUP, cal.timeInMillis] = sender
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                am.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, sender)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, sender)
            }
        }
    }
}