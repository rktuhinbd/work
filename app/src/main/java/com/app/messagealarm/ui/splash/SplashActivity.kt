package com.app.messagealarm.ui.splash

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.service.notification.NotificationListenerService.requestRebind
import android.view.animation.AnimationUtils
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.BuildConfig
import com.app.messagealarm.R
import com.app.messagealarm.common.CommonPresenter
import com.app.messagealarm.common.CommonView
import com.app.messagealarm.networking.RetrofitClient
import com.app.messagealarm.service.app_reader_intent_service.AppsReaderIntentService
import com.app.messagealarm.service.notification_service.NotificationListener
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.DialogUtils
import com.app.messagealarm.utils.SharedPrefUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_splash.*
import java.util.*


class SplashActivity : BaseActivity(), CommonView {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
       // changeTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val commonPresenter = CommonPresenter(this)
        if(!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_USER_INFO_SAVED)){
            commonPresenter.knowUserFromWhichCountry()
        }else{
            updateTokenApiCall(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED))
        }
        handleUpdate()
        if (BaseApplication.installedApps.isEmpty()) {
            val mIntent = Intent(this, AppsReaderIntentService::class.java)
            AppsReaderIntentService.enqueueWork(this, mIntent)
        }
        firebaseAnalytics = Firebase.analytics
    }


    private fun isUpdateAvailable(): Boolean {
        var isUpdateAvailable = false
        if(SharedPrefUtils.contains(Constants.PreferenceKeys.UPDATED_VERSION)){
            if(BuildConfig.VERSION_NAME.trim() != SharedPrefUtils.readString(Constants.PreferenceKeys.UPDATED_VERSION.trim())){
                isUpdateAvailable = true
            }
        }
       return isUpdateAvailable
    }


    private fun startWorks() {
            val animation = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top)
            txt_title?.startAnimation(animation)
            progress_bar_splash?.startAnimation(animation)
            defaultPreferences()
            runProgressWithSteps()
            tryReconnectService()
    }

    private fun tryReconnectService() {
        toggleNotificationListenerService()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val componentName = ComponentName(
                applicationContext,
                NotificationListener::class.java
            )
            //It say to Notification Manager RE-BIND your service to listen notifications again inmediatelly!
            requestRebind(componentName)
        }
    }

    /**
     * Try deactivate/activate your component service
     */
    private fun toggleNotificationListenerService() {
        val pm = packageManager
        pm.setComponentEnabledSetting(
            ComponentName(this, NotificationListener::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            ComponentName(this, NotificationListener::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }


    private fun defaultPreferences() {
        if (!SharedPrefUtils.contains(Constants.PreferenceKeys.THEME)) {
            SharedPrefUtils.write(Constants.PreferenceKeys.THEME, "Light")
        }
        if (!SharedPrefUtils.contains(Constants.PreferenceKeys.MUTE_TIME)) {
            SharedPrefUtils.write(Constants.PreferenceKeys.MUTE_TIME, "10 min")
        }
        if (!SharedPrefUtils.contains(Constants.PreferenceKeys.IS_MUTED)) {
            SharedPrefUtils.write(Constants.PreferenceKeys.IS_MUTED, false)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun handleUpdate(){
        if(!isUpdateAvailable()){
            startWorks()
        }else{
            DialogUtils.showUpdateDialog(this, "New Update", "Get excited new features in ${
                SharedPrefUtils.readString(
                    Constants.PreferenceKeys.UPDATED_VERSION, "new"
                )
            } version", object : DialogUtils.Callback {
                override fun onPositive() {
                    val bundle = Bundle()
                    bundle.putString("clicked_update", "yes")
                    firebaseAnalytics.logEvent("update_status", bundle)
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        finish()
                    } catch (e: ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        finish()
                    }
                }
                override fun onNegative() {
                    val bundle = Bundle()
                    bundle.putString("clicked_later", "yes")
                    firebaseAnalytics.logEvent("update_status", bundle)
                    startWorks()
                }
            })
        }
    }

    private fun takeUserToHome() {
        val intent = Intent(this, AlarmApplicationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun runProgressWithSteps() {
        var progress = 0
        val total = 2000
        object : CountDownTimer(total.toLong(), 15) {
            override fun onFinish() {
                //take user to app
                takeUserToHome()
            }
            override fun onTick(millisUntilFinished: Long) {
                progress += 1
                progress_bar_splash?.progress = progress
            }
        }.start()
    }

    /**
     * 2.0.1 user to 2.0.2 sending token and status to server so we can know user status
     */
    private fun updateUserToken(isPaid:Boolean){
        Thread{
            if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_FIREBASE_TOKEN_SYNCED_2_0_2)){
              updateTokenApiCall(isPaid)
            }
        }.start()
    }

    /**
     * 2.0.2 users to 2.0.3
     */
    private fun updateTokenApiCall(isPaid: Boolean){
        RetrofitClient.getApiService().updateCurrentToken(
            SharedPrefUtils.readString(Constants.PreferenceKeys.FIREBASE_TOKEN),
            SharedPrefUtils.readString(Constants.PreferenceKeys.COUNTRY),
            if (isPaid) "1" else "0",
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ANDROID_ID
            ),
            TimeZone.getDefault().id,
            SharedPrefUtils.readInt(
                Constants.PreferenceKeys.ALARM_COUNT
            )
        ).execute()
    }

    override fun onSuccess() {
        //call update token api here
        updateUserToken(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED))
    }

    override fun onSuccess(token: String) {

    }

    override fun onError() {

    }
}
