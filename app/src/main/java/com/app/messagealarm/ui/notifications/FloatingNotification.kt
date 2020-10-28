package com.app.messagealarm.ui.notifications

import android.R
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.messagealarm.broadcast_receiver.UnMuteReceiver
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.utils.*
import java.util.*


class FloatingNotification {

    companion object {

        /**
         * start of remote notification objects
         */
        /**
         * Custom appearance of the notification, also updated.
         */
        var service:Service? = null
        var notificationView: RemoteViews? = null
        var notificationBuilder: NotificationCompat.Builder? = null
        val NOTIFICATION_ID = 12
        /**
         * end of remote notification
         */


        private const val CHANNEL_ID = "alarm channel"
        private const val CHANNEL_NAME = "alarm app channel"

        private fun startPlaying(
            appName: String,
            packageName: String,
            tone: String?,
            isVibrate: Boolean,
            context: Service,
            notificationManager: NotificationManagerCompat,
            numberOfPlay: Int
        ){
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
                                isVibrate,
                                context, tone, (x == (numberOfPlay - 1))
                            )
                            if (x == numberOfPlay - 1) {
                                //done playing dismiss the activity now
                                //send a notification that you missed the alarm
                                notificationManager.cancel(225)
                            }
                        }
                    )

                }
            }).start()
        }

        fun showFloatingNotification(
            appName: String, packageName: String, numberOfPlay: Int,
            isVibrate: Boolean, context: Service, mediaPath: String?
        ) {
            // sending data to new activity
            val receiveCallAction =
                Intent(context, AlarmApplicationActivity::class.java)

            val receiveCallPendingIntent = PendingIntent.getBroadcast(
                context,
                1200,
                receiveCallAction,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val cancelCallPendingIntent = PendingIntent.getBroadcast(
                context,
                1201,
                receiveCallAction,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            createChannel(context)
            var notificationBuilder: NotificationCompat.Builder? = null
            notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentText("Message from upwork")
                .setContentTitle("Check message")
                .setSmallIcon(
                    R.drawable.ic_btn_speak_now
                )
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .addAction(
                    R.drawable.ic_menu_call,
                    "Receive Call",
                    receiveCallPendingIntent
                )
                .addAction(
                    R.drawable.ic_menu_close_clear_cancel,
                    "Cancel call",
                    cancelCallPendingIntent
                )
                .setAutoCancel(true)
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(225, notificationBuilder.build())
            //start playing
            startPlaying(
                appName,
                packageName,
                mediaPath,
                isVibrate,
                context,
                notificationManager,
                numberOfPlay
            )
        }

        /*
Create noticiation channel if OS version is greater than or eqaul to Oreo
*/
        private fun createChannel(context: Service) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                channel.description = "Call Notifications"
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

              notificationView =   RemoteViews(
                  context.packageName,
                  com.app.messagealarm.R.layout.layout_foreground_notification
              )

            notificationView!!.setImageViewResource(
                com.app.messagealarm.R.id.btn_mute_status,
                com.app.messagealarm.R.drawable.ic_snooze
            )

            notificationView!!.setTextViewText(
                com.app.messagealarm.R.id.txt_desc,
                DataUtils.getString(com.app.messagealarm.R.string.waiting_for_messages)
            )

            // And now, building and attaching the Skip button.
            // And now, building and attaching the Skip button.
            val buttonMuteHandler = Intent(context, UnMuteReceiver::class.java)
            val buttonSkipPendingIntent =
                PendingIntent.getBroadcast(context, 0, buttonMuteHandler, 0)
            notificationView!!.setOnClickPendingIntent(com.app.messagealarm.R.id.btn_mute_status, buttonSkipPendingIntent)

             notificationBuilder = NotificationCompat.Builder(context, channelId)

            val notification: Notification = notificationBuilder!!
                .setSmallIcon(R.drawable.sym_call_missed)
                .setCustomContentView(notificationView)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(resultPendingIntent)
                .build()

            context.startForeground(NOTIFICATION_ID, notification)
        }



        fun notifyMute(isMuted: Boolean) {
            if (notificationView == null || notificationBuilder == null) return
            val iconID: Int = if (isMuted) com.app.messagealarm.R.drawable.ic_silence else com.app.messagealarm.R.drawable.ic_snooze
            val textString : String =  if(isMuted) DataUtils.getString(com.app.messagealarm.R.string.waiting_for_messages) else "Application muted"
            notificationView!!.setImageViewResource(com.app.messagealarm.R.id.btn_mute_status, iconID)
            notificationView!!.setTextViewText(com.app.messagealarm.R.id.txt_desc, textString)
            notificationBuilder!!.setContent(notificationView)
//		notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            // Sets the notification to run on the foreground.
            // (why not the former commented line?)
            service!!.startForeground(NOTIFICATION_ID, notificationBuilder!!.build())
        }


    }








}