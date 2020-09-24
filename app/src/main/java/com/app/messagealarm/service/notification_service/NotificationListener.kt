package com.app.messagealarm.service.notification_service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.service.AlarmService
import com.app.messagealarm.ui.notifications.FloatingNotification
import com.app.messagealarm.utils.AndroidUtils
import com.app.messagealarm.utils.MediaUtils
import com.app.messagealarm.utils.VibratorUtils
import es.dmoral.toasty.Toasty


class NotificationListener : NotificationListenerService(),
    NotificationListenerView {

    companion object{
        val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
    }
    val TAG: String = "LISTENER"

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName
        Log.e(TAG, "Package name " + packageName)
        Log.e(TAG, "TITLE " + sbn!!.notification.extras["android.title"])
        Log.e(TAG, "DESC " + sbn!!.notification.extras["android.text"])
        NotificationListenerPresenter(this).getApplicationList(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        //NOTE: Music not getting off on notification removed
        if (MediaUtils.isPlaying() && sbn!!.packageName == AndroidUtils.getPackageInfo()!!.packageName) {
            MediaUtils.stopAlarm()
            VibratorUtils.stopVibrate()
            Toasty.info(this, "Snoozed for 20 minutes!").show()
        }else{
            Toasty.info(this, "Alarm Service Stopped!").show()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null){
            when(intent.action){
                ACTION_STOP_FOREGROUND_SERVICE -> stopForeGroundService()
                else -> FloatingNotification.startForegroundService(this)
            }
        }

        return Service.START_STICKY
    }

    private fun stopForeGroundService(){
        stopForeground(true)
        stopSelf()
        onDestroy()
    }

    override fun onCreate() {
        super.onCreate()
        FloatingNotification.startForegroundService(this)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        //send broadcast to activity to restart this service
       /* val service = PendingIntent.getService(
            applicationContext,
            1001,
            Intent(applicationContext, NotificationListener::class.java),
            PendingIntent.FLAG_ONE_SHOT
        )
        val alarmManager =
            getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager[AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000] = service*/
    }

    override fun onApplicationListGetSuccess(
        list: List<ApplicationEntity>,
        sbn: StatusBarNotification?
    ) {
        AlarmService.playAlarmOnNotification(sbn, list, this)
    }

    override fun onApplicationListGetError() {

    }


}