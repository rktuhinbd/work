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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.BuildConfig
import com.app.messagealarm.R
import com.app.messagealarm.networking.RetrofitClient
import com.app.messagealarm.service.notification_service.NotificationListener
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.utils.*
import com.app.messagealarm.work_manager.WorkManagerUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class PushMessage : FirebaseMessagingService(), PushMessageView {

    override fun onMessageReceived(p0: RemoteMessage) {
        val data: Map<String, String> = p0.data
        try{
            when {
                data["action"] == Constants.ACTION.SYNC -> {
                    val pushMessagePresenter = PushMessagePresenter(this)
                    pushMessagePresenter.cleanDb()
                }
                data["action"] == Constants.ACTION.BUY -> {
                    //Open and Show the Buy Screen of the app
                    createNotification(p0, data["action"]!!)
                }
                data["action"] == Constants.ACTION.UPDATE -> {
                    //Open the play store with our app link so user can update the app
                    //First Open a dialog in MainActivity then take the user the play store from the dialog
                }
                data["action"] == Constants.ACTION.OPEN_SERVICE ->{
                    /**
                     * If service is killed and user didn't turned of the service, open the service
                     */
                 if(!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_SERVICE_STOPPED))   {
                     if(!AndroidUtils.isServiceRunning(this, NotificationListener::class.java)){
                         startMagicService(this)
                     }
                 }
                }
                else ->{
                    createNotification(p0, data["action"]!!)
                }
            }
        }catch (e: NullPointerException){
            //skipping the crash
        }
    }

    private fun startMagicService(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(context, NotificationListener::class.java)
            context.startForegroundService(intent)
        } else {
            val intent = Intent(context, NotificationListener::class.java)
            context.startService(intent)
        }
    }


    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        //save token to shared preference
        SharedPrefUtils.write(Constants.PreferenceKeys.FIREBASE_TOKEN, p0)
        val tokenCall = RetrofitClient.getApiService().registerToken(p0)
        val herokuTokenCall = RetrofitClient.getApiServiceHeroku().registerTokenForHeroku(p0)
        //if app is build in debug mode don't call this function
        if(!BuildConfig.DEBUG) {
            tokenCall.execute()
            herokuTokenCall.execute()
            /**
             * tested both platform getting the token
             */
        }else{
            Log.e("TOKEN", p0)
        }
    }


    private fun createNotificationVibration(){
        var count = 0
        //create vibration for 4 seconds
        Thread(Runnable {
            //start vibration
            VibratorUtils.startVibrate(BaseApplication.getBaseApplicationContext(), 3000)
            while (count < 3) {
                Thread.sleep(1000)
                count++
                if (count == 2) {
                    //stop vibration
                    VibratorUtils.stopVibrate()
                    count = 0
                    break
                }
            }
        }).start()
    }

    private fun createNotification(
        remoteMessage: RemoteMessage?,
        action:String
    ) {
        if(action != Constants.ACTION.SYNC || action != Constants.ACTION.UPDATE ||
                action != Constants.ACTION.OPEN_SERVICE
                ){
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
            //start vibrations
            createNotificationVibration()
        }

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