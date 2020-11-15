package com.app.messagealarm.ui.notifications

import android.R
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.messagealarm.broadcast_receiver.*
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.utils.*
import com.app.messagealarm.work_manager.WorkManagerUtils
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
        var service: Service? = null
        var notificationView: RemoteViews? = null
        var notificationBuilder: NotificationCompat.Builder? = null
        val NOTIFICATION_ID = 12
        var notificationManager: NotificationManagerCompat? = null

        /**
         * end of remote notification
         */


        private const val CHANNEL_ID = "alarm channel"
        private const val CHANNEL_NAME = "alarm app channel"

        private fun startPlaying(
            isJustVibrate: Boolean,
            appName: String,
            packageName: String,
            tone: String?,
            isVibrate: Boolean,
            context: Service,
            notificationManager: NotificationManagerCompat,
            numberOfPlay: Int
        ) {
            Thread(Runnable {
                //here i need run the loop of how much time need to play
                for (x in 0 until numberOfPlay) {
                    if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_STOPPED)) {
                        break
                    }
                    val once = Once()
                    once.run(
                        Runnable {
                            MediaUtils.playAlarm(
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
                                SharedPrefUtils.write(Constants.PreferenceKeys.IS_MUTED, true)
                                notifyMute(true)
                            }
                        }
                    )

                }
            }).start()
        }


        fun showMissedAlarmNotification(context: Context, packageName: String, appName: String) {
            // sending data to new activity
            val buttonOpenAppBroadcast = Intent(
                context,
                MissedAlarmReceiver::class.java
            ).putExtra(Constants.IntentKeys.PACKAGE_NAME, packageName)
            val buttonOpenApp =
                PendingIntent.getBroadcast(context, 0, buttonOpenAppBroadcast, 0)

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


        fun showPageDismissNotification(context: Context, packageName: String, appName: String) {

            val buttonOpenAppBroadcast = Intent(
                context,
                PageDismissReceiver::class.java
            ).putExtra(Constants.IntentKeys.PACKAGE_NAME, packageName)
            val buttonOpenApp =
                PendingIntent.getBroadcast(context, 0, buttonOpenAppBroadcast, 0)

            createChannel(context)

            var notificationBuilder: NotificationCompat.Builder? = null
            notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentText("You have an alarm from $appName")
                .setContentTitle("Swipe to dismiss alarm!")
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
            notificationManager!!.notify(227, notificationBuilder.build())
        }

        fun showFloatingNotification(
            isJustVibrate: Boolean,
            appName: String, packageName: String, numberOfPlay: Int,
            isVibrate: Boolean, context: Service, mediaPath: String?
        ) {
            // sending data to new activity
            val buttonOpenAppBroadcast = Intent(
                context,
                OpenAppReceiver::class.java
            ).putExtra(Constants.IntentKeys.PACKAGE_NAME, packageName)
            val buttonOpenApp =
                PendingIntent.getBroadcast(context, 0, buttonOpenAppBroadcast, 0)

            createChannel(context)

            var notificationBuilder: NotificationCompat.Builder? = null
            notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentText("You got a message from $appName")
                .setContentTitle("Swipe to dismiss alarm!")
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
            notificationManager!!.notify(225, notificationBuilder.build())
            //start playing
            startPlaying(
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
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                channel.description = "Alarm Notifications"
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                Objects.requireNonNull(context.getSystemService(NotificationManager::class.java))
                    .createNotificationChannel(channel)
            }
        }

        /* Used to build and start foreground service. */
        fun startForegroundService(context: Service, isMuted: Boolean) {
            SharedPrefUtils.write(Constants.PreferenceKeys.IS_SERVICE_STOPPED, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service", context, isMuted)
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
            isMuted: Boolean
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
                notificationView!!.setTextViewText(com.app.messagealarm.R.id.txt_desc, textString)
                notificationBuilder!!.setContent(notificationView)
                service!!.startForeground(NOTIFICATION_ID, notificationBuilder!!.build())
            } catch (e: ArrayIndexOutOfBoundsException) {

            }
            if (isMuted) {
                showToastToUser(service!!)
                //start alarm to dismiss mute
                WorkManagerUtils.scheduleWorks(service!!)
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

    }


}