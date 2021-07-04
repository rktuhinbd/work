package com.app.messagealarm.ui.splash

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.service.notification.NotificationListenerService.requestRebind
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatDelegate
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.BuildConfig
import com.app.messagealarm.R
import com.app.messagealarm.common.CommonPresenter
import com.app.messagealarm.common.CommonView
import com.app.messagealarm.model.response.UserInfoGlobal
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SplashActivity : BaseActivity(), CommonView {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
       // changeTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val commonPresenter = CommonPresenter(this)
        commonPresenter.knowUserFromWhichCountry()
        handleUpdate()
        val mIntent = Intent(this, AppsReaderIntentService::class.java)
        AppsReaderIntentService.enqueueWork(this, mIntent)
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
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                    } catch (e: ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
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

    override fun onSuccess() {

    }

    override fun onSuccess(token: String) {

    }

    override fun onError() {

    }
}
