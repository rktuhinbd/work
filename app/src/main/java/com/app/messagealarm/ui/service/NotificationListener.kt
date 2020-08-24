package com.app.messagealarm.ui.service

import android.R
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.session.PlaybackState.ACTION_PAUSE
import android.media.session.PlaybackState.ACTION_PLAY
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.messagealarm.ui.main.MainActivity


class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName
        Log.e("PK++", packageName)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    /* Used to build and start foreground service. */
    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("my_service", "My Background Service")
        } else { // Create notification default intent.
            val intent = Intent()
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
            // Create notification builder.
            val builder = NotificationCompat.Builder(this)
            // Make notification show big text.
            val bigTextStyle = NotificationCompat.BigTextStyle()
            bigTextStyle.setBigContentTitle("Music player implemented by foreground service.")
            bigTextStyle.bigText("Android foreground service is a android service which can run in foreground always," +
                    " it can be controlled by user via notification.")
            // Set big text style.
            builder.setStyle(bigTextStyle)
            builder.setWhen(System.currentTimeMillis())
            builder.setSmallIcon(R.mipmap.sym_def_app_icon)
            val largeIconBitmap =
                BitmapFactory.decodeResource(resources, R.drawable.sym_action_chat)
            builder.setLargeIcon(largeIconBitmap)
            // Make the notification max priority.
            builder.priority = Notification.PRIORITY_MAX
            // Make head-up notification.
            builder.setFullScreenIntent(pendingIntent, true)
            // Add Play button intent in notification.
            val playIntent = Intent(this, NotificationListener::class.java)
            playIntent.action = ACTION_PLAY.toString()
            val pendingPlayIntent = PendingIntent.getService(this, 0, playIntent, 0)
            val playAction = NotificationCompat.Action(
                R.drawable.ic_media_play,
                "Play",
                pendingPlayIntent
            )
            builder.addAction(playAction)
            // Add Pause button intent in notification.
            val pauseIntent = Intent(this, NotificationListener::class.java)
            pauseIntent.action = ACTION_PAUSE.toString()
            val pendingPrevIntent = PendingIntent.getService(this, 0, pauseIntent, 0)
            val prevAction = NotificationCompat.Action(
                R.drawable.ic_media_pause,
                "Pause",
                pendingPrevIntent
            )
            builder.addAction(prevAction)
            // Build the notification.
            val notification: Notification = builder.build()
            // Start foreground service.
            startForeground(1, notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        channelId: String,
        channelName: String
    ) {
        val resultIntent = Intent(this, MainActivity::class.java)
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntentWithParentStack(resultIntent)
        val resultPendingIntent: PendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        val chan =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager =
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder =
            NotificationCompat.Builder(this, channelId)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.sym_action_email)
            .setContentTitle("App is running in background")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(resultPendingIntent) //intent
            .build()
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(1, notificationBuilder.build())
        startForeground(1, notification)
    }

    override fun onLowMemory() {
        //send broadcast to activity to restart this service
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        //send broadcast to activity to restart this service
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

}