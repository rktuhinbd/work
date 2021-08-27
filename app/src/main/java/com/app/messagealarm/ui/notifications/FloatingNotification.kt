package com.app.messagealarm.ui.notifications

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.app.messagealarm.broadcast_receiver.*
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.utils.*
import com.app.messagealarm.work_manager.WorkManagerUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import es.dmoral.toasty.Toasty
import java.util.*


class FloatingNotification {

    companion object {

        /**
         * start of remote notification objects
         */
        /**
         * Custom appearance of the notification, also updated.
         */
        private var firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
        var service: Service? = null
        var notificationView: RemoteViews? = null
        private var notificationBuilder: NotificationCompat.Builder? = null
        val NOTIFICATION_ID = 12
        private var notificationManager: NotificationManagerCompat? = null

        /**
         * end of remote notification
         */
        private const val CHANNEL_ID = "alarm channel"
        private const val CHANNEL_NAME = "alarm app channel"

        private fun startPlaying(
            soundLevel:Int,
            isJustVibrate: Boolean,
            appName: String,
            packageName: String,
            tone: String?,
            isVibrate: Boolean,
            context: Service,
            notificationManager: NotificationManagerCompat,
            numberOfPlay: Int
        ) {
            /**
             * Turn on screen for few seconds
             */
            turnOnScreen(context)

            var thread:Thread? = null
         thread =   Thread(Runnable {
             //here i need run the loop of how much time need to play
             for (x in 0 until numberOfPlay) {
                 if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_STOPPED)) {
                     break
                 }
                 val once = Once()
                 once.run {
                     MediaUtils.playAlarm(
                         thread!!,
                         soundLevel,
                         isJustVibrate,
                         isVibrate,
                         context, tone, (x == (numberOfPlay - 1)),
                         packageName,
                         appName
                     )
                     if (x == numberOfPlay - 1) {
                         //done playing dismiss the activity now
                         //send a notification that you missed the alarm
                         notificationManager.cancel(225)
                         /**
                          * The bottom two lines were making the app mute when the alarm was finished without touch
                          * Now it's ignored by Mujahid By 1 June 2021
                          */
                         //SharedPrefUtils.write(Constants.PreferenceKeys.IS_MUTED, true)
                         //notifyMute(true)
                     }
                 }
             }
         })
            thread.start()
        }

        private fun turnOnScreen(context: Service){
            /**
             * Turn phone screen on
             */
            val powerManager: PowerManager =
                context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE, "appname::WakeLock"
            )
            //acquire will turn on the display
            wakeLock.acquire(1 * 60 * 1000L /*10 minutes*/)
        }


        fun showMissedAlarmNotification(context: Context, packageName: String, appName: String) {
            // sending data to new activity
            val buttonOpenAppBroadcast = Intent(
                context,
                MissedAlarmReceiver::class.java
            ).putExtra(Constants.IntentKeys.PACKAGE_NAME, packageName)
            val buttonOpenApp =
                PendingIntent.getBroadcast(
                    context,
                    0,
                    buttonOpenAppBroadcast,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

            createChannel(context)

            var notificationBuilder: NotificationCompat.Builder? = null
            notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentText("You missed an alarm from $appName")
                .setContentTitle("Swipe to dismiss notification!")
                .setSmallIcon(
                    com.app.messagealarm.R.drawable.ic_notifications_active_black_24dp
                )
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction(
                    com.app.messagealarm.R.drawable.ic_notifications_active_black_24dp,
                    "Open $appName",
                    buttonOpenApp
                )
                .setAutoCancel(true)
            notificationManager = NotificationManagerCompat.from(context)
            notificationManager!!.notify(226, notificationBuilder.build())
        }


        fun showPageDismissNotification(
            title: String,
            context: Context,
            packageName: String,
            appName: String
        ) {

            val buttonOpenAppBroadcast = Intent(
                context,
                PageDismissReceiver::class.java
            ).putExtra(Constants.IntentKeys.PACKAGE_NAME, packageName)
                .setAction("OPEN_APP")
            val buttonOpenApp =
                PendingIntent.getBroadcast(
                    context,
                    0,
                    buttonOpenAppBroadcast,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )


            val btnCancel = Intent(
                context,
                PageDismissReceiver::class.java
            ).putExtra(Constants.IntentKeys.PACKAGE_NAME, packageName)
                .setAction("CANCEL")
            val btnCancelIntent =
                PendingIntent.getBroadcast(context, 0, btnCancel, PendingIntent.FLAG_UPDATE_CURRENT)


            createChannel(context)

            //remote view stating
            val notificationView = RemoteViews(
                context.packageName,
                com.app.messagealarm.R.layout.layout_incoming_notification_collapsed
            )

            val notificationViewFloatingNotification = RemoteViews(
                context.packageName,
                com.app.messagealarm.R.layout.layout_incoming_notification
            )

            notificationViewFloatingNotification.setTextViewText(
                com.app.messagealarm.R.id.txt_notification_title,
                "Message from $appName"
            )

            notificationViewFloatingNotification.setTextViewText(
                com.app.messagealarm.R.id.txt_notification_desc,
                "$title sent you a message"
            )

            notificationView.setTextViewText(
                com.app.messagealarm.R.id.txt_notification_title,
                "Message from $appName"
            )

            notificationViewFloatingNotification.setTextViewText(
                com.app.messagealarm.R.id.btn_notification_action,
                "Open $appName"
            )

            notificationViewFloatingNotification.setOnClickPendingIntent(
                com.app.messagealarm.R.id.btn_notification_action,
                buttonOpenApp
            )
            notificationViewFloatingNotification.setOnClickPendingIntent(
                com.app.messagealarm.R.id.btn_notification_cancel,
                btnCancelIntent
            )
            //remote view ending

            var notificationBuilder: NotificationCompat.Builder? = null
            notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(
                    com.app.messagealarm.R.drawable.ic_notifications_active_black_24dp
                )
                .setCustomBigContentView(notificationViewFloatingNotification)
                .setCustomHeadsUpContentView(notificationViewFloatingNotification)
                .setCustomContentView(notificationView)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true)
                .setOngoing(true)
            notificationManager = NotificationManagerCompat.from(context)
            notificationManager!!.notify(227, notificationBuilder.build())
        }

        fun showFloatingNotification(
            soundLevel: Int,
            title: String,
            isJustVibrate: Boolean,
            appName: String, packageName: String, numberOfPlay: Int,
            isVibrate: Boolean, context: Service, mediaPath: String?
        ) {
            val bundle = Bundle()
            bundle.putString("alarm_by_notification", "true")

            firebaseAnalytics.logEvent("alarm_type", bundle)

            // sending data to new activity
            val buttonOpenAppBroadcast = Intent(
                context,
                OpenAppReceiver::class.java
            ).putExtra(Constants.IntentKeys.PACKAGE_NAME, packageName)
                .setAction("OPEN_APP")

            val buttonOpenApp =
                PendingIntent.getBroadcast(
                    context,
                    0,
                    buttonOpenAppBroadcast,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

            //just cancel the notification

            val btnCancelCast = Intent(
                context,
                OpenAppReceiver::class.java
            ).setAction("CANCEL")

            val btnCancelIntent =
                PendingIntent.getBroadcast(
                    context,
                    0,
                    btnCancelCast,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

            createChannel(context)

            val notificationView = RemoteViews(
                context.packageName,
                com.app.messagealarm.R.layout.layout_incoming_notification_collapsed
            )

            val vivoNotificationView = RemoteViews(context.packageName,
                com.app.messagealarm.R.layout.layout_incoming_notification_vivo)

            val notificationViewFloatingNotification = RemoteViews(
                context.packageName,
                com.app.messagealarm.R.layout.layout_incoming_notification
            )

            notificationViewFloatingNotification.setTextViewText(
                com.app.messagealarm.R.id.txt_notification_title,
                "Message from $appName"
            )

            notificationViewFloatingNotification.setTextViewText(
                com.app.messagealarm.R.id.txt_notification_desc,
                "$title sent you a message"
            )

            notificationView.setTextViewText(
                com.app.messagealarm.R.id.txt_notification_title,
                "Message from $appName"
            )

            notificationViewFloatingNotification.setTextViewText(
                com.app.messagealarm.R.id.btn_notification_action,
                "Open $appName"
            )

            /**
             * vivo section
             */
            vivoNotificationView.setTextViewText(
                com.app.messagealarm.R.id.txt_notification_title,
                "Message from $appName"
            )

            vivoNotificationView.setTextViewText(
                com.app.messagealarm.R.id.btn_notification_action,
                "Open $appName"
            )

            notificationViewFloatingNotification.setOnClickPendingIntent(
                com.app.messagealarm.R.id.btn_notification_action,
                buttonOpenApp
            )

            notificationViewFloatingNotification.setOnClickPendingIntent(
                com.app.messagealarm.R.id.btn_notification_cancel,
                btnCancelIntent
            )

            /**
             * view listener section
             */
            vivoNotificationView.setOnClickPendingIntent(
                com.app.messagealarm.R.id.btn_notification_action,
                buttonOpenApp
            )

            vivoNotificationView.setOnClickPendingIntent(
                com.app.messagealarm.R.id.btn_notification_cancel,
                btnCancelIntent
            )

            var notificationBuilder: NotificationCompat.Builder? = null

            if(!Build.MANUFACTURER.toLowerCase(Locale.getDefault()).contains("vivo")){
                notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setCustomBigContentView(notificationViewFloatingNotification)
                    .setCustomHeadsUpContentView(notificationViewFloatingNotification)
                    .setCustomContentView(notificationView)
                    .setSmallIcon(
                        com.app.messagealarm.R.drawable.ic_notifications_active_black_24dp
                    )
                    .setPriority(Notification.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setOngoing(true)
            }else{
                notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setCustomBigContentView(notificationViewFloatingNotification)
                    .setCustomHeadsUpContentView(notificationViewFloatingNotification)
                    .setCustomContentView(vivoNotificationView)
                    .setSmallIcon(
                        com.app.messagealarm.R.drawable.ic_notifications_active_black_24dp
                    )
                    .setPriority(Notification.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setOngoing(true)
            }

            notificationManager = NotificationManagerCompat.from(context)
            notificationManager!!.notify(225, notificationBuilder.build())
            //start playing
            startPlaying(
                soundLevel,
                isJustVibrate,
                appName,
                packageName,
                mediaPath,
                isVibrate,
                context,
                notificationManager!!,
                numberOfPlay
            )

        }


        fun cancelAlarmNotification() {
            if (notificationManager != null) {
                notificationManager!!.cancel(225)
            }
        }

        fun cancelMissedAlarmNotification() {
            if (notificationManager != null) {
                notificationManager!!.cancel(226)
            }
        }

        fun cancelPageDismissNotification() {
            if (notificationManager != null) {
                notificationManager!!.cancel(227)
            }
        }

        /*
Create noticiation channel if OS version is greater than or eqaul to Oreo
*/
        private fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.description = "Alarm Notifications"
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                Objects.requireNonNull(context.getSystemService(NotificationManager::class.java))
                    .createNotificationChannel(channel)
            }
        }

        /* Used to build and start foreground service. */
        fun startForegroundService(context: Service) {
            SharedPrefUtils.write(Constants.PreferenceKeys.IS_SERVICE_STOPPED, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service", context)
            } else { // Create notification default intent.
                //create save notification for android 7
                //init service
                service = context

                val resultIntent = Intent(context, AlarmApplicationActivity::class.java)
                // Create the TaskStackBuilder and add the intent, which inflates the back stack
                val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(context)

                stackBuilder.addNextIntentWithParentStack(resultIntent)

                val resultPendingIntent: PendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

                notificationView = RemoteViews(
                    context.packageName,
                    com.app.messagealarm.R.layout.layout_foreground_notification
                )
                if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_MUTED)) {
                    notificationView!!.setImageViewResource(
                        com.app.messagealarm.R.id.btn_mute_status,
                        com.app.messagealarm.R.drawable.ic_silence
                    )
                    notificationView!!.setTextViewText(
                        com.app.messagealarm.R.id.txt_desc,
                        DataUtils.getString(com.app.messagealarm.R.string.txt_application_muted)
                    )
                } else {
                    notificationView!!.setImageViewResource(
                        com.app.messagealarm.R.id.btn_mute_status,
                        com.app.messagealarm.R.drawable.ic_snooze
                    )

                    notificationView!!.setTextViewText(
                        com.app.messagealarm.R.id.txt_desc,
                        DataUtils.getString(com.app.messagealarm.R.string.waiting_for_messages)
                    )
                }

                val buttonMuteHandler = Intent(context, UnMuteReceiver::class.java)
                val buttonSkipPendingIntent =
                    PendingIntent.getBroadcast(context, 0, buttonMuteHandler, 0)

                notificationView!!.setOnClickPendingIntent(
                    com.app.messagealarm.R.id.btn_mute_status,
                    buttonSkipPendingIntent
                )

                val buttonPowerHandler = Intent(context, PowerOffReceiver::class.java)
                val buttonPowerOffIntent =
                    PendingIntent.getBroadcast(context, 0, buttonPowerHandler, 0)
                notificationView!!.setOnClickPendingIntent(
                    com.app.messagealarm.R.id.btn_power,
                    buttonPowerOffIntent
                )


                notificationBuilder = NotificationCompat.Builder(context)

                val notification: Notification = notificationBuilder!!
                    .setSmallIcon(com.app.messagealarm.R.mipmap.ic_launcher_round)
                    .setCustomContentView(notificationView)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setContentIntent(resultPendingIntent)
                    .build()

                context.startForeground(NOTIFICATION_ID, notification)

            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createNotificationChannel(
            channelId: String,
            channelName: String,
            context: Service,
        ) {
            //init service
            service = context
            val resultIntent = Intent(context, AlarmApplicationActivity::class.java)
            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addNextIntentWithParentStack(resultIntent)
            val resultPendingIntent: PendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            val chan =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager =
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(chan)
            // Get the layouts to use in the custom notification
            notificationView = RemoteViews(
                context.packageName,
                com.app.messagealarm.R.layout.layout_foreground_notification
            )
            if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_MUTED)) {
                notificationView!!.setImageViewResource(
                    com.app.messagealarm.R.id.btn_mute_status,
                    com.app.messagealarm.R.drawable.ic_silence
                )
                notificationView!!.setTextViewText(
                    com.app.messagealarm.R.id.txt_desc,
                    DataUtils.getString(com.app.messagealarm.R.string.txt_application_muted)
                )
            } else {
                notificationView!!.setImageViewResource(
                    com.app.messagealarm.R.id.btn_mute_status,
                    com.app.messagealarm.R.drawable.ic_snooze
                )
                notificationView!!.setTextViewText(
                    com.app.messagealarm.R.id.txt_desc,
                    DataUtils.getString(com.app.messagealarm.R.string.waiting_for_messages)
                )
            }
            val buttonMuteHandler = Intent(context, UnMuteReceiver::class.java)
            val buttonSkipPendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    0,
                    buttonMuteHandler,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            notificationView!!.setOnClickPendingIntent(
                com.app.messagealarm.R.id.btn_mute_status,
                buttonSkipPendingIntent
            )

            val buttonPowerHandler = Intent(context, PowerOffReceiver::class.java)
            val buttonPowerOffIntent =
                PendingIntent.getBroadcast(
                    context,
                    0,
                    buttonPowerHandler,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            notificationView!!.setOnClickPendingIntent(
                com.app.messagealarm.R.id.btn_power,
                buttonPowerOffIntent
            )

            notificationBuilder = NotificationCompat.Builder(context, channelId)

            val notification: Notification = notificationBuilder!!
                .setSmallIcon(com.app.messagealarm.R.mipmap.ic_launcher_round)
                .setCustomContentView(notificationView)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(resultPendingIntent)
                .build()

            context.startForeground(NOTIFICATION_ID, notification)
        }

        fun notifyMute(isMuted: Boolean) {
            if (notificationView == null || notificationBuilder == null) return
            try{
                val iconID: Int =
                    if (isMuted) com.app.messagealarm.R.drawable.ic_silence else com.app.messagealarm.R.drawable.ic_snooze
                val textString: String = if (!isMuted) {
                    DataUtils.getString(com.app.messagealarm.R.string.waiting_for_messages)
                } else DataUtils.getString(com.app.messagealarm.R.string.txt_application_muted)
                try {
                    notificationView!!.setImageViewResource(
                        com.app.messagealarm.R.id.btn_mute_status,
                        iconID
                    )
                    notificationView!!.setTextViewText(
                        com.app.messagealarm.R.id.txt_desc,
                        textString
                    )
                    notificationBuilder!!.setContent(notificationView)
                    service!!.startForeground(NOTIFICATION_ID, notificationBuilder!!.build())
                } catch (e: ArrayIndexOutOfBoundsException) {

                }
                if (isMuted) {
                    showToastToUser(service!!)
                    //start alarm to dismiss mute
                    WorkManagerUtils.scheduleWorks(service!!)
                }else{
                    showUnMuteToastToUser(service!!)
                    WorkManager.getInstance(service!!).cancelAllWorkByTag("MUTE")
                }
            }catch (e: NullPointerException){
                //skip the crash
            }

        }


        @SuppressLint("CheckResult")
        fun showToastToUser(context: Service) {
            val handler = Handler(context.mainLooper)
            val runnable = Runnable() {
                Toasty.info(
                    context, String.format(
                        "Application muted for %s",
                        SharedPrefUtils.readString(Constants.PreferenceKeys.MUTE_TIME)
                    )
                ).show()
            }
            handler.post(runnable)
        }

        @SuppressLint("CheckResult")
        fun showUnMuteToastToUser(context: Service) {
            val handler = Handler(context.mainLooper)
            val runnable = Runnable() {
                Toasty.info(
                    context, String.format(
                        "Application unmuted!"
                    )
                ).show()
            }
            handler.post(runnable)
        }

    }


}