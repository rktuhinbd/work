package com.app.messagealarm.service.notification_service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
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

        NotificationListenerPresenter(this).filterByAppConstrains(
                sbn.packageName.toString(), AndroidUtils.getCurrentLangCode(this),
                sbn.notification.extras["android.title"].toString().trim(),
            sbn.notification.extras["android.text"].toString().trim(), sbn)
    }

    @Synchronized
    fun doMagic(sbn: StatusBarNotification?) {
        NotificationListenerPresenter(this)
            .getApplicationList(sbn)
    }

    private fun filterApps(sbn: StatusBarNotification?) {
        when (sbn!!.packageName) {
            MESSENGER_PKG -> {
                messengerFilter(
                    sbn.notification.extras["android.title"].toString(),
                    sbn.notification.extras["android.text"].toString()
                )
            }
            WHATSAPP_PKG -> {
                whatsAppFilter(
                    sbn.notification.extras["android.title"].toString(),
                    sbn.notification.extras["android.text"].toString()
                )
            }
            else -> isPlayAble = true
        }
    }


    private fun whatsAppFilter(title: String, desc: String) {

        isPlayAble = when {
            title == "WhatsApp" -> {
                false
            }

            desc == "Ringing…" -> {
                false
            }

            desc == "Calling…" -> {
                false
            }

            desc == "Ongoing voice call" -> {
                false
            }

            desc == "Checking for new messages" -> {
                false
            }

            desc == "Ongoing video call" -> {
                false
            }

            desc == "Incoming voice call" -> {
                false
            }

            desc == "Missed voice call" -> {
                false
            }

            desc.contains("new messages", false) -> {
                false
            }

            title == "Check message" -> {
                false
            }

            title == "null" -> {
                false
            }

            desc == "null" -> {
                false
            }

            title.isEmpty() -> {
                false
            }

            desc.isEmpty() -> {
                false
            }

            else -> true
        }
    }


    private fun messengerFilter(title: String, desc: String) {
        isPlayAble = when {
            title == "Chat heads active" -> {
                false
            }
            desc == "Start a conversation" -> {
                false
            }
            desc == "Calling from Messenger" -> {
                false
            }

            desc.contains("You missed a call from", false) -> {
                false
            }

            desc.contains("Tap to return to call", false) -> {
                false
            }

            title == "null" -> {
                false
            }

            desc == "null" -> {
                false
            }

            title.isEmpty() -> {
                false
            }

            desc.isEmpty() -> {
                false
            }

            else -> true
        }
    }

    private fun viberFilter() {

    }

    private fun upworkFilter() {

    }

    private fun fiverrFilter() {

    }

    private fun freelancerFilter() {

    }

    private fun imoFilter() {

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
        stopForeground(true)
        stopSelf()
        onDestroy()
        SharedPrefUtils.write(Constants.PreferenceKeys.IS_SERVICE_STOPPED, true)
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
        if (SharedPrefUtils.contains(Constants.PreferenceKeys.IS_SERVICE_STOPPED)) {
            if (!isServiceStopped) {
                scheduleService()
            }
        } else {
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

}