package com.app.messagealarm.service.notification_service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Process
import android.os.Process.killProcess
import android.os.Process.myPid
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.service.AlarmService
import com.app.messagealarm.ui.notifications.FloatingNotification
import com.app.messagealarm.utils.*
import es.dmoral.toasty.Toasty


class NotificationListener : NotificationListenerService(),
    NotificationListenerView {


    var isPlayAble = true
    var isThreadExecuted = false
    val sbnList = ArrayList<StatusBarNotification>()

    companion object {
        private val MESSENGER_PKG = "com.facebook.orca"
        private val WHATSAPP_PKG = "com.whatsapp"
        private val VIBER_PKG = ""
        private val IMO_PKG = ""
        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"

    }

    private val TAG: String = "LISTENER"

    @SuppressLint("LogNotTimber")
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
       // filterApps(sbn)
        //check for logs
        /*
        * There is issue, that within 5 seconds if the activity alarm is dismissed, it's start the thread alarm again.
        * Need to solve it ASAP
        *
        * */
        Log.e("LISTENER", "PACKAGE = " +  sbn!!.packageName.toString())
        Log.e("LISTENER", "TITLE = " +  sbn.notification.extras["android.title"].toString())
        Log.e("LISTENER", "DESC = " +  sbn.notification.extras["android.text"].toString())

        if(!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_SERVICE_STOPPED)){
            NotificationListenerPresenter(this).filterByAppConstrains(
                sbn.packageName.toString(), AndroidUtils.getCurrentLangCode(this),
                sbn.notification.extras["android.title"].toString().trim(),
                sbn.notification.extras["android.text"].toString().trim(), sbn)
        }
    }

    @Synchronized
    fun doMagic(sbn: StatusBarNotification?) {
        NotificationListenerPresenter(this)
            .getApplicationList(sbn)
    }


    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        //NOTE: Music not getting off on notification removed
        if (sbn!!.packageName == AndroidUtils.getPackageInfo()!!.packageName) {
            if(sbn.notification.extras["android.text"].toString() == "Swipe to dismiss the alarm!"){
                SharedPrefUtils.write(Constants.PreferenceKeys.IS_NOTIFICATION_SWIPED, true)
            }
                MediaUtils.stopAlarm()
        } else if (sbn.packageName == AndroidUtils.getPackageInfo()!!.packageName) {
            Toasty.info(this, "Alarm Service Stopped!").show()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                ACTION_STOP_FOREGROUND_SERVICE -> stopForeGroundService()
                else ->{
                    FloatingNotification.startForegroundService(this, false)
                }
            }
        }
        return Service.START_STICKY
    }


    private fun stopForeGroundService() {
        SharedPrefUtils.write(Constants.PreferenceKeys.IS_SERVICE_STOPPED, true)
        onDestroy()
    }


    override fun onCreate() {
        super.onCreate()
        //FloatingNotification.startForegroundService(this, false)
       // muteListener()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        //send broadcast to activity to restart this service
        restartService()
    }

    private fun restartService() {
        val isServiceStopped =
            SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_SERVICE_STOPPED)
            if (!isServiceStopped) {
                scheduleService()
            }
    }

    private fun scheduleService() {
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

    override fun onApplicationListGetSuccess(
        list: List<ApplicationEntity>,
        sbn: StatusBarNotification?
    ) {
        AlarmService.playAlarmOnNotification(sbn, list, this)
    }

    override fun onApplicationListGetError() {

    }

    override fun isPlayAbleSuccess(sbn: StatusBarNotification?) {
        SharedPrefUtils.write(Constants.PreferenceKeys.IS_STOPPED, false)
        doMagic(sbn)
    }

    override fun onDestroy() {
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

}