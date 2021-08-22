package com.app.messagealarm.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.app.messagealarm.R
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationPresenter
import com.app.messagealarm.ui.terms_privacy.TermsAndConditionActivity
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.PermissionUtils
import com.app.messagealarm.utils.SharedPrefUtils
import com.app.messagealarm.work_manager.WorkManagerUtils

class FirstTimeSplashScreen : AppCompatActivity(), FirstSplashView {

    private val alarmAppPresenter = FirstSplashPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_time_splash_screen)
        startAction()
        alarmAppPresenter.getRequiredTableSize()
    }

    private fun startAction(){
        if(PermissionUtils.isNotificationAllowed()){
            if(!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_TERMS_ACCEPTED)){
                    takeTheUserToTermsAndPrivacy()
            }else{
                openSplashActivity()
            }
        }else{
            //open getting started page with 3 second delay
            Handler(Looper.myLooper()!!).postDelayed({
             openGettingStartedPage()
            },3000)
        }
    }


    private fun takeTheUserToTermsAndPrivacy(){
        startActivity(Intent(this, TermsAndConditionActivity::class.java))
        finish()
    }


    private fun openGettingStartedPage(){
        startActivity(Intent(this, SplashGettingStarted::class.java))
        finish()
    }


    private fun openSplashActivity(){
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }

    override fun onTablesSizeRequestSuccess(appSize: Int, langSize: Int, appConstrainSize: Int) {
        WorkManagerUtils.scheduleSyncWork(this, appSize, langSize, appConstrainSize)
    }
}