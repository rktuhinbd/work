package com.app.messagealarm.firebase


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.app.messagealarm.BuildConfig
import com.app.messagealarm.R
import com.app.messagealarm.networking.RetrofitClient
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.DataUtils
import com.app.messagealarm.utils.SharedPrefUtils
import com.app.messagealarm.work_manager.WorkManagerUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class PushMessage : FirebaseMessagingService(), PushMessageView {

    override fun onMessageReceived(p0: RemoteMessage) {
        val data: Map<String, String> = p0.data
        createNotification(p0)
        if(data["action"] == Constants.ACTION.SYNC){
            val pushMessagePresenter = PushMessagePresenter(this)
            pushMessagePresenter.cleanDb()
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        //save token to shared preference
        SharedPrefUtils.write(Constants.PreferenceKeys.FIREBASE_TOKEN, p0)
        val tokenCall = RetrofitClient.getApiService().registerToken(p0)
        //if app is build in debug mode don't call this function
        if(!BuildConfig.DEBUG){
            tokenCall.execute()
        }
    }


    private fun createNotification(
        remoteMessage: RemoteMessage?
    ) { // Let's create a
        // notification builder object
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, DataUtils.getString(R.string.notification_channel))
        // Create a notificationManager object
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // If android version is greater than 8.0 then create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Create a notification channel
            val notificationChannel = NotificationChannel(
                DataUtils.getString(R.string.notification_channel),
                DataUtils.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            // Set properties to notification channel
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(100, 200, 300)
            // Pass the notificationChannel object to notificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        // Set the notification parameters to the notification builder object
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, AlarmApplicationActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setSmallIcon(R.drawable.ic_notification)
            .setSound(defaultSoundUri)
            .setContentTitle(remoteMessage!!.data["title"])
            .setContentText(remoteMessage.data["body"])
            .setAutoCancel(true)
            .setContentIntent(contentIntent)


        if(remoteMessage.data["image"] != null){
            // Set the image for the notification
            val bitmap: Bitmap = getBitmapfromUrl(remoteMessage.data["image"])!!
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(null)
            ).setLargeIcon(bitmap)
        }
        notificationManager.notify(1, builder.build())
    }

    private fun getBitmapfromUrl(imageUrl: String?): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            null
        }
    }

    override fun onDbCleanSuccess() {
        WorkManagerUtils.scheduleSyncWork(this, 0, 0, 0)
    }

}