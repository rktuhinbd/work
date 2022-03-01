package com.app.messagealarm.firebase


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.BuildConfig
import com.app.messagealarm.R
import com.app.messagealarm.common.CommonPresenter
import com.app.messagealarm.common.CommonView
import com.app.messagealarm.model.response.TokenResponse
import com.app.messagealarm.networking.RetrofitClient
import com.app.messagealarm.service.notification_service.NotificationListener
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.utils.*
import com.app.messagealarm.work_manager.WorkManagerUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.jetbrains.anko.internals.AnkoInternals.getContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class PushMessage : FirebaseMessagingService(), PushMessageView, CommonView {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    var bmp:Bitmap? = null

    override fun onMessageReceived(p0: RemoteMessage) {
        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics

        val data: Map<String, String> = p0.data
        try {
            when {
                data["action"] == Constants.ACTION.SYNC -> {
                    val pushMessagePresenter = PushMessagePresenter(this)
                    pushMessagePresenter.cleanDb()
                }
                data["action"] == Constants.ACTION.BUY -> {
                    //Open and Show the Buy Screen of the app
                    createNotification(p0, data["action"]!!)
                }
                data["action"]!!.split("/")[0] == Constants.ACTION.UPDATE -> {
                    //Open the play store with our app link so user can update the app
                    //First Open a dialog in MainActivity then take the user the play store from the dialog
                    createNotification(p0, data["action"]!!.split("/")[0])
                }
                data["action"] == Constants.ACTION.OPEN_SERVICE -> {
                    // Obtain the FirebaseAnalytics instance.
                    //log about screen open log
                    val bundle = Bundle()
                    bundle.putString("push_service_triggered", "yes")
                    firebaseAnalytics.logEvent("push_service", bundle)
                    /**
                     * If service is killed and user didn't turned of the service, open the service
                     */
                    if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_SERVICE_STOPPED)) {
                        if (!AndroidUtils.isServiceRunning(
                                this,
                                NotificationListener::class.java
                            )
                        ) {
                            if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_TERMS_ACCEPTED)){
                                startMagicService(this)
                            }
                        }else{
                            val bundleService = Bundle()
                            bundleService.putString("service_running", "yes")
                            firebaseAnalytics.logEvent("service_running", bundleService)
                        }
                    }
                }
                else -> {
                    createNotification(p0, data["action"]!!)
                }
            }
        } catch (e: NullPointerException) {
            //skipping the crash
        }
    }

    private fun startMagicService(context: Context) {
        val bundle = Bundle()
        bundle.putString("push_service_triggered_and_opened", "yes")
        firebaseAnalytics.logEvent("push_service", bundle)
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
        /**
         * Know user from which country for new user
         * When first time app open
         */
         val commonPresenter = CommonPresenter(this)
        commonPresenter.knowUserFromWhichCountry(p0)
    }

    private fun sendPushToken(p0: String) {
        Thread {
            val tokenCall = RetrofitClient.getApiService().registerToken(
                p0,
                if (SharedPrefUtils.readString(Constants.PreferenceKeys.COUNTRY).isNotEmpty())
                    SharedPrefUtils.readString(Constants.PreferenceKeys.COUNTRY) else "Unknown",
                Settings.Secure.getString(
                    contentResolver,
                    Settings.Secure.ANDROID_ID
                ),
                TimeZone.getDefault().id
            ).also {
                it.enqueue(object : Callback<TokenResponse> {
                    override fun onResponse(
                        call: Call<TokenResponse>,
                        response: Response<TokenResponse>
                    ) {
                        if (response.isSuccessful) {
                            SharedPrefUtils.write(
                                Constants.PreferenceKeys.IS_FIREBASE_TOKEN_SYNCED_2_0_2,
                                true
                            )
                        }
                    }

                    override fun onFailure(call: Call<TokenResponse>, t: Throwable) {

                    }
                })
            }
        }.start()
    }


    private fun createNotificationVibration() {
        var count = 0
        //create vibration for 4 seconds
        Thread(Runnable {
            //start vibration
            VibratorUtils.startVibrate(BaseApplication.getBaseApplicationContext(), 2500)
            while (count < 3) {
                Thread.sleep(500)
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
        action: String
    ) {
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
        /**
         * if PRO type Notification open BUY PRO PAGE ELSE MAIN PAGE
         */
        var contentIntent: PendingIntent? = null
        when (action) {
            Constants.ACTION.UPDATE -> {
                val intent: Intent = try {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                } catch (e: ActivityNotFoundException) {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                contentIntent = PendingIntent.getActivity(
                    this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            else -> {
                contentIntent = PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, AlarmApplicationActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                try {
                    if(remoteMessage!!.data["image"] != null){
                            //Write file
                            bmp = getBitmapfromUrl(/*remoteMessage.data["image"]*/"https://picsum.photos/seed/picsum/500/300")
                            val bitmap = bmp
                            //Convert to byte array
                            val stream = ByteArrayOutputStream()
                            bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
                            val byteArray: ByteArray = stream.toByteArray()
                            contentIntent = PendingIntent.getActivity(
                                this, 0,
                                Intent(this, AlarmApplicationActivity::class.java)
                                    .putExtra(Constants.IntentKeys.PUSH_IMAGE, byteArray)
                                    .putExtra(
                                        Constants.IntentKeys.IS_BUY_PRO,
                                        action == Constants.ACTION.BUY
                                    )
                                    .putExtra(Constants.IntentKeys.IS_PUSH_URL, true)
                                    .putExtra(Constants.IntentKeys.PUSH_URL, "https://www.facebook.com/")
                                    .putExtra(
                                        Constants.IntentKeys.PUSH_TITLE,
                                        remoteMessage.data["title"]
                                    )
                                    .putExtra(
                                        Constants.IntentKeys.PUSH_DESC,
                                        remoteMessage.data["body"]
                                    ), PendingIntent.FLAG_UPDATE_CURRENT
                            )
                    }else{
                        contentIntent = PendingIntent.getActivity(
                            this, 0,
                            Intent(this, AlarmApplicationActivity::class.java)
                                .putExtra(
                                    Constants.IntentKeys.IS_BUY_PRO,
                                    action == Constants.ACTION.BUY
                                )
                                .putExtra(
                                    Constants.IntentKeys.PUSH_TITLE,
                                    remoteMessage.data["title"]
                                )
                                .putExtra(Constants.IntentKeys.PUSH_URL, "https://www.facebook.com/")
                                .putExtra(Constants.IntentKeys.IS_PUSH_URL, true)
                                .putExtra(
                                    Constants.IntentKeys.PUSH_DESC,
                                    remoteMessage.data["body"]
                                ), PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    }

                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
        builder
            .setColor(ContextCompat.getColor(this, R.color.success_color))
            .setSmallIcon(R.drawable.ic_notification)
            .setSound(defaultSoundUri)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            builder.setContentTitle("messageAlarm")
        if (remoteMessage!!.data["image"] != null && bmp != null) {
            // Set the image for the notification
            val bitmap = bmp
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(null)
            ).setLargeIcon(bitmap)
            //content text as title and we will pass description in the app dialog for description
            if (remoteMessage.data["title"] != null && remoteMessage.data["title"]!!.isNotEmpty()) {
                builder.setContentText(remoteMessage.data["title"])
            }
        } else {
            if (remoteMessage.data["title"] != null && remoteMessage.data["title"]!!.isNotEmpty()) {
                builder.setStyle(
                    NotificationCompat.BigTextStyle().bigText(remoteMessage.data["title"])
                )
            }
        }
        notificationManager.notify(1, builder.build())
        //start vibrations
        createNotificationVibration()
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

    override fun onSuccess() {

    }

    override fun onSuccess(token: String) {
        //if app is build in debug mode don't call this function
            sendPushToken(token)
            //send push token for non debug mode
            if(!BuildConfig.DEBUG){
                RetrofitClient.getApiServiceHeroku().registerTokenForHeroku(token).enqueue(
                    object : Callback<TokenResponse> {
                        override fun onResponse(
                            call: Call<TokenResponse>,
                            response: Response<TokenResponse>
                        ) {
                            if (response.isSuccessful) {
                                //heroku token sync success
                                SharedPrefUtils.write(
                                    Constants.PreferenceKeys.IS_HEROKU_TOKEN_SYNCED,
                                    true
                                )
                            }
                        }

                        override fun onFailure(call: Call<TokenResponse>, t: Throwable) {

                        }
                    })
            }

    }

    override fun onError() {

    }

}