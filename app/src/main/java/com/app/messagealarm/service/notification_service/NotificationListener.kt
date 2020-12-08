package com.app.messagealarm.service.notification_service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.service.AlarmService
import com.app.messagealarm.ui.notifications.FloatingNotification
import com.app.messagealarm.utils.*
import es.dmoral.toasty.Toasty


class NotificationListener : NotificationListenerService(),
    NotificationListenerView {

    var isSecondThreadStarted = false
    var isThreadStarted = false
    var isPlayAble = true
    var isThreadExecuted = false
    val sbnList = ArrayList<StatusBarNotification>()
    val tempList = ArrayList<StatusBarNotification>()


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
        * */
        Log.e("LISTENER", "PACKAGE = " + sbn!!.packageName.toString())
        Log.e("LISTENER", "TITLE = " + sbn.notification.extras["android.title"].toString())
        Log.e("LISTENER", "DESC = " + sbn.notification.extras["android.text"].toString())
        if (sbn.packageName.toString() != "com.app.messagealarm") {
            if (sbn.notification.extras["android.title"].toString() != "WhatsApp") {
                //sbnList.add(sbn)
                if(sbnList.size > 0){
                    if (sbnList[0].packageName.toString() == sbn.packageName.toString()) {
                        sbnList.add(sbn)
                        Log.e("SAME_N", "true")
                    }else{
                        Log.e("SAME_D", "true")
                        tempList.add(sbn)
                        if(!isSecondThreadStarted){
                            Log.e("THREAD_2", "true")
                            tempList.forEach {
                                Log.e("TEMP_LIST", it.packageName + " = AND = " + it.notification.extras["android.title"])
                            }
                            Thread(Runnable {
                                checkForMessage(tempList, true)
                            }).start()
                        }
                    }
                }else{
                    sbnList.add(sbn)
                    Log.e("SAME_F", "true")
                }
                if (!isThreadStarted) {
                    Thread(Runnable {
                        Log.e("THREAD_1", "true")
                        checkForMessage(sbnList, false)
                    }).start()
                }
            }

        }

    }

    private fun checkForMessage(listItems: ArrayList<StatusBarNotification>, secondThread: Boolean) {
        if(secondThread){
            isSecondThreadStarted = true
        }else{
            isThreadStarted = true
        }
        var count = 0
        while (count != 5) {
            count++
            Log.e("REAL_COUNT", count.toString())
            Thread.sleep(1000)
            if (count == 5) {
                listItems.forEach {
                    Log.e("HAVE_LIST", it.packageName + " = AND = " + it.notification.extras["android.title"])
                }
                Handler(mainLooper).post(Runnable {
                    //got an message stop all getting message process
                    if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_SERVICE_STOPPED)) {
                        Log.e("SBN_SIZE", listItems.size.toString())
                        if (listItems.size > 0) {
                            if (listItems[0].packageName.toString() == WHATSAPP_PKG) {
                                NotificationListenerPresenter(this).filterByAppConstrains(
                                    listItems[listItems.size - 1].packageName.toString(),
                                    AndroidUtils.getCurrentLangCode(this),
                                    listItems[listItems.size - 1].notification.extras["android.title"].toString()
                                        .trim(),
                                    listItems[listItems.size - 1].notification.extras["android.text"].toString()
                                        .trim(),
                                    listItems[listItems.size - 1]
                                )
                                if(secondThread){
                                    isSecondThreadStarted = false
                                    tempList.clear()
                                }else{
                                    isThreadStarted = false
                                    this.sbnList.clear()
                                }
                                listItems.clear()
                            } else {
                                NotificationListenerPresenter(this).filterByAppConstrains(
                                    listItems[0].packageName.toString(),
                                    AndroidUtils.getCurrentLangCode(this),
                                    listItems[0].notification.extras["android.title"].toString()
                                        .trim(),
                                    listItems[0].notification.extras["android.text"].toString()
                                        .trim(),
                                    listItems[0]
                                )
                                if(secondThread){
                                    isSecondThreadStarted = false
                                    tempList.clear()
                                }else{
                                    isThreadStarted = false
                                    this.sbnList.clear()
                                }
                                listItems.clear()
                            }

                        }
                    }
                })
            }
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
            if (sbn.notification.extras["android.text"].toString() == "Swipe to dismiss the alarm!") {
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
                else -> {
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

    private fun resetValues(){
        isThreadStarted = false
        this.sbnList.clear()
        isSecondThreadStarted = false
        tempList.clear()
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