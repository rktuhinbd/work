package com.app.messagealarm.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AnimationUtils
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.R
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
