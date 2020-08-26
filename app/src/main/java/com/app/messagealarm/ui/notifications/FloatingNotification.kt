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
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.messagealarm.ui.main.MainActivity
import com.app.messagealarm.ui.service.NotificationListener
import com.app.messagealarm.utils.MediaUtils
import com.app.messagealarm.utils.VibratorUtils
import java.util.*


class FloatingNotification {
    companion object {
        private const val CHANNEL_ID = "alarm channel"
        private const val CHANNEL_NAME = "alarm app channel"

        @RequiresApi(Build.VERSION_CODES.O)
        fun showFloatingNotification(context: Service) {
            MediaUtils.playAlarm(context)
            VibratorUtils.startVibrate(context)
            // sending data to new activity
            val receiveCallAction =
                Intent(context, MainActivity::class.java)

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
                .setPriority(Notification.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .addAction(
                    R.drawable.ic_menu_call,
                    "Receive Call",
                    receiveCallPendingIntent
                )
                .setVibrate(LongArray(0))
                .addAction(
                    R.drawable.ic_menu_close_clear_cancel,
                    "Cancel call",
                    cancelCallPendingIntent
                )
                .setAutoCancel(true)
                .setSound(
                    Uri.parse(
                        "android.resource://" + context.packageName
                            .toString() + "/" + com.app.messagealarm.R.raw.crush
                    )
                )
                .setFullScreenIntent(receiveCallPendingIntent, true)
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(225, notificationBuilder.build())
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
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC;
                channel.enableVibration(true)
                channel.setSound(
                    Uri.parse(
                        "android.resource://" + context.packageName
                            .toString() + "/" + com.app.messagealarm.R.raw.crush
                    ),
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_RING)
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).build()
                )

                Objects.requireNonNull(context.getSystemService(NotificationManager::class.java))
                    .createNotificationChannel(channel)
            }
        }

        /* Used to build and start foreground service. */
        fun startForegroundService(context: Service) {
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
            val resultIntent = Intent(context, MainActivity::class.java)
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
            val remoteViews = RemoteViews(
                context.packageName,
                com.app.messagealarm.R.layout.layout_foreground_notification
            )
            val notificationBuilder =
                NotificationCompat.Builder(context, channelId)
            val notification: Notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(android.R.drawable.sym_action_email)
                .setContentTitle("App is running in background")
                .setContentText("testing")
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                // Set Ticker Message
                .setTicker("Noification is created")
                .setContentIntent(resultPendingIntent) //intent
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViews)
                .build()
            notification.flags =
                notification.flags or Notification.FLAG_NO_CLEAR //Do not clear the notification
            context.startForeground(12, notification)
        }


    }

}