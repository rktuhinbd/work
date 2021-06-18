package com.app.messagealarm.ui.splash

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.service.notification.NotificationListenerService.requestRebind
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.JobIntentService.enqueueWork
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.R
import com.app.messagealarm.service.app_reader_intent_service.AppsReaderIntentService
import com.app.messagealarm.service.notification_service.NotificationListener
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.DialogUtils
import com.app.messagealarm.utils.PermissionUtils
import com.app.messagealarm.utils.SharedPrefUtils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        changeTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val mIntent = Intent(this, AppsReaderIntentService::class.java)
        AppsReaderIntentService.enqueueWork(this, mIntent)
    }

    private fun handlePermission() {
        if (PermissionUtils.isNotificationAllowed()) {
            val animation = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top)
            txt_title?.startAnimation(animation)
            progress_bar_splash?.startAnimation(animation)
            defaultPreferences()
            runProgressWithSteps()
            tryReconnectService()
        } else {
            /**
             * take user to getting started page
             */
            if (!isFinishing) {
                DialogUtils.showDialog(
                    this,
                    getString(R.string.txt_notification_permission),
                    getString(R.string.txt_notification_permission_message),
                    object : DialogUtils.Callback {
                        override fun onPositive() {
                            try {
                                val intent =
                                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                startActivity(intent)
                            } catch (e: NoClassDefFoundError) {
                                Toasty.error(this@SplashActivity, getString(R.string.not_supperted))
                                    .show()
                            }
                        }
                        override fun onNegative() {
                            finish()
                        }
                    }
                )
            }

        }
    }

    private fun changeTheme() {
        if (!SharedPrefUtils.contains(Constants.PreferenceKeys.IS_DARK_MODE)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
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
        handlePermission()
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
}
