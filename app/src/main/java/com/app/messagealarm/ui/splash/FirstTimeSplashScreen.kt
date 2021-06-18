package com.app.messagealarm.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.app.messagealarm.R
import com.app.messagealarm.utils.PermissionUtils

class FirstTimeSplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_time_splash_screen)
        startAction()
    }

    private fun startAction(){
        if(PermissionUtils.isNotificationAllowed()){
            openSplashActivity()
        }else{
            //open getting started page with 3 second delay
            Handler(Looper.myLooper()!!).postDelayed({
             openGettingStartedPage()
            },3000)
        }
    }


    private fun openGettingStartedPage(){
        startActivity(Intent(this, SplashGettingStarted::class.java))
        finish()
    }


    private fun openSplashActivity(){
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }
}