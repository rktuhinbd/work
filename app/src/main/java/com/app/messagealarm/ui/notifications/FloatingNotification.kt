package com.app.messagealarm.ui.notifications

import android.R
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.service.notification_service.NotificationListener
import com.app.messagealarm.utils.*
import java.util.*


class FloatingNotification {

    companion object {
        private const val CHANNEL_ID = "alarm channel"
        private const val CHANNEL_NAME = "alarm app channel"

        private fun startPlaying(tone:String?, isVibrate: Boolean,context: Service,
                                 notificationManager: NotificationManagerCompat,  numberOfPlay: Int){
            Thread(Runnable {
                val once = Once()
                //here i need run the loop of how much time need to play
                for (x in 0 until numberOfPlay){
                    once.run(
                        Runnable {
                            ExoPlayerUtils.playAudio(
                                isVibrate,
                                context, tone)
                        }
                    )
                    if(x == numberOfPlay - 1){
                        //done playing dismiss the activity now
                        //send a notification that you missed the alarm
                        notificationManager.cancel(225)
                    }
                }
            }).start()
        }

        fun showFloatingNotification(numberOfPlay:Int, isVibrate:Boolean, context: Service, mediaPath:String?) {
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
            startPlaying(mediaPath, isVibrate, context, notificationManager, numberOfPlay)
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
        fun startForegroundService(context: Service) {
            SharedPrefUtils.write(Constants.PreferenceKeys.IS_SERVICE_STOPPED, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service", context)
            } else { // Create notification default intent.
                val intent = Intent()
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
                // Create notification builder.
                val builder = NotificationCompat.Builder(context)
                // Make notification show big text.
                val bigTextStyle = NotificationCompat.BigTextStyle()
                bigTextStyle.setBigContentTitle("Music player implemented by foreground service.")
                bigTextStyle.bigText(
                    "Android foreground service is a android service which can run in foreground always," +
                            " it can be controlled by user via notification."
                )
                // Set big text style.
                builder.setStyle(bigTextStyle)
                builder.setWhen(System.currentTimeMillis())
                builder.setSmallIcon(android.R.mipmap.sym_def_app_icon)
                val largeIconBitmap =
                    BitmapFactory.decodeResource(
                        context.resources,
                        android.R.drawable.sym_action_chat
                    )
                builder.setLargeIcon(largeIconBitmap)
                // Make the notification max priority.
                builder.priority = Notification.PRIORITY_MAX
                // Make head-up notification.
                builder.setFullScreenIntent(pendingIntent, true)
                // Add Play button intent in notification.
                val playIntent = Intent(context, NotificationListener::class.java)
                playIntent.action = PlaybackState.ACTION_PLAY.toString()
                val pendingPlayIntent = PendingIntent.getService(context, 0, playIntent, 0)
                val playAction = NotificationCompat.Action(
                    android.R.drawable.ic_media_play,
                    "Play",
                    pendingPlayIntent
                )
                builder.addAction(playAction)
                // Add Pause button intent in notification.
                val pauseIntent = Intent(context, NotificationListener::class.java)
                pauseIntent.action = PlaybackState.ACTION_PAUSE.toString()
                val pendingPrevIntent = PendingIntent.getService(context, 0, pauseIntent, 0)
                val prevAction = NotificationCompat.Action(
                    android.R.drawable.ic_media_pause,
                    "Pause",
                    pendingPrevIntent
                )
                builder.addAction(prevAction)
                // Build the notification.
                val notification: Notification = builder.build()
                // Start foreground service.
                context.startForeground(1, notification)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createNotificationChannel(
            channelId: String,
            channelName: String,
            context: Service
        ) {
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
            val notificationLayout = RemoteViews(context.packageName, com.app.messagealarm.R.layout.layout_foreground_notification)
            val notificationLayoutExpanded = RemoteViews(context.packageName, com.app.messagealarm.R.layout.layout_foreground_notification)
            val notificationBuilder =
                NotificationCompat.Builder(context, channelId)
            val notification: Notification = notificationBuilder
                .setSmallIcon(R.drawable.sym_call_missed)
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(resultPendingIntent)
                .build()
            context.startForeground(12, notification)
        }


    }

}