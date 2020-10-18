package com.app.messagealarm.ui.splash

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.service.notification.NotificationListenerService.requestRebind
import android.view.animation.AnimationUtils
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.R
import com.app.messagealarm.service.notification_service.NotificationListener
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.utils.DialogUtils
import com.app.messagealarm.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_splash.*



class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    private fun handlePermission(){
        if(PermissionUtils.isNotificationAllowed()){
            val animation = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top)
            txt_title?.startAnimation(animation)
            progress_bar_splash?.startAnimation(animation)
            runProgressWithSteps()
            tryReconnectService()
        }else{
            DialogUtils.showDialog(
                    this,
            getString(R.string.txt_notification_permission),
            getString(R.string.txt_notification_permission_message),
            object :DialogUtils.Callback{
                override fun onPositive() {
                    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    startActivity(intent)
                }
                override fun onNegative() {
                    finish()
                }
            }
            )
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


    override fun onResume() {
        super.onResume()
        handlePermission()
    }

    private fun takeUserToHome(){
        val intent = Intent(this, AlarmApplicationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun runProgressWithSteps(){
        var progress = 0
        val total = 2000
        object : CountDownTimer(total.toLong(),15) {
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
