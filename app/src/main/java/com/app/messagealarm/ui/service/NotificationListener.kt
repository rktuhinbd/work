package com.app.messagealarm.ui.service

import android.R
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.session.PlaybackState.ACTION_PAUSE
import android.media.session.PlaybackState.ACTION_PLAY
import android.nfc.Tag
import android.os.Build
import android.os.Handler
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.messagealarm.ui.main.MainActivity
import com.app.messagealarm.ui.notifications.FloatingNotification
import com.app.messagealarm.utils.AndroidUtils
import com.app.messagealarm.utils.MediaUtils
import es.dmoral.toasty.Toasty


class NotificationListener : NotificationListenerService() {
    val TAG: String = "LISTENER"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName
        Log.e(TAG, "Package name " + packageName)
        Log.e(TAG, "TITLE " + sbn!!.notification.extras["android.title"])
        Log.e(TAG, "DESC " + sbn!!.notification.extras["android.text"])
        if (packageName == "com.facebook.orca") {
            if (sbn.notification.extras["android.title"]!! != "Chat heads active") {
                Handler().postDelayed(
                    Runnable {
                        FloatingNotification.showFloatingNotification(this)
                    }, 3000
                )
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn!!.packageName == AndroidUtils.getPackageInfo()!!.packageName) {
            MediaUtils.stopAlarm()
            Toasty.info(this, "Snoozed for 20 minutes!").show()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        FloatingNotification.startForegroundService(this)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        //send broadcast to activity to restart this service
        val service = PendingIntent.getService(
            applicationContext,
            1001,
            Intent(applicationContext, NotificationListener::class.java),
            PendingIntent.FLAG_ONE_SHOT
        )
        val alarmManager =
            getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager[AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000] = service
    }

}