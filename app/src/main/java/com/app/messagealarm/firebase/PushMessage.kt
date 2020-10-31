package com.app.messagealarm.firebase


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.app.messagealarm.R
import com.app.messagealarm.networking.RetrofitClient
import com.app.messagealarm.utils.DataUtils
import com.app.messagealarm.work_manager.WorkManagerUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class PushMessage : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage?) {
        WorkManagerUtils.scheduleSyncWork(this, 0, 0,0)
       createNotification(p0)
    }

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
        val tokenCall = RetrofitClient.getApiService().registerToken(p0)
        tokenCall.execute()
    }


    private fun createNotification(
      remoteMessage:RemoteMessage?
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
        builder
            .setSmallIcon(R.mipmap.ic_launcher)
            .setSound(defaultSoundUri)
            .setAutoCancel(true)
        // Set the image for the notification
            val bitmap: Bitmap = getBitmapfromUrl(remoteMessage!!.data?.get("image"))!!
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(null)
            ).setLargeIcon(bitmap)
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

}