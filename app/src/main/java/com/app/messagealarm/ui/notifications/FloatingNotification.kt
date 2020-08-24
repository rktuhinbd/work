package com.app.messagealarm.ui.notifications

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.session.PlaybackState
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.messagealarm.R
import com.app.messagealarm.ui.main.MainActivity
import com.app.messagealarm.ui.service.NotificationListener

class FloatingNotification{
    companion object{

        @RequiresApi(Build.VERSION_CODES.O)
        fun showFloatingNotification(context: Service){
            val resultIntent = Intent(context, MainActivity::class.java)
            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addNextIntentWithParentStack(resultIntent)
            val resultPendingIntent: PendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            val chan =
                NotificationChannel("my floating", "main notificaation", NotificationManager.IMPORTANCE_HIGH)
            val manager =
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(chan)
            val notificationBuilder =
                NotificationCompat.Builder(context, "my floating")
            val notification: Notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(android.R.drawable.sym_action_email)
                .setContentTitle("Incomming message from upwork")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(resultPendingIntent, true)
                .build()
            context.startForeground(334, notification)
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
                bigTextStyle.bigText("Android foreground service is a android service which can run in foreground always," +
                        " it can be controlled by user via notification.")
                // Set big text style.
                builder.setStyle(bigTextStyle)
                builder.setWhen(System.currentTimeMillis())
                builder.setSmallIcon(android.R.mipmap.sym_def_app_icon)
                val largeIconBitmap =
                    BitmapFactory.decodeResource(context.resources, android.R.drawable.sym_action_chat)
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
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager =
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(chan)
            val notificationBuilder =
                NotificationCompat.Builder(context, channelId)
            val notification: Notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(android.R.drawable.sym_action_email)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(resultPendingIntent) //intent
                .build()
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1, notificationBuilder.build())
            context.startForeground(1, notification)
        }


    }

}