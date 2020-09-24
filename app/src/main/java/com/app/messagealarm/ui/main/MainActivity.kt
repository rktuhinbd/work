package com.app.messagealarm.ui.main

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.R
import com.app.messagealarm.ui.main.add_apps.AddApplicationActivity
import com.app.messagealarm.service.notification_service.NotificationListener
import com.app.messagealarm.utils.AndroidUtils
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.PermissionUtils
import com.app.messagealarm.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setListener()
        askForPermission()
        handleService()
    }

    private fun handleService(){
        val isServiceStopped = SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_SERVICE_STOPPED)
        if(SharedPrefUtils.contains(Constants.PreferenceKeys.IS_SERVICE_STOPPED)){
            if(isServiceStopped){
                switch_alarm_status?.isChecked = false
            }else{
                switch_alarm_status?.isChecked = true
                startMagicService()
            }
        }else{
            startMagicService()
        }
    }

    private fun askForPermission(){
        PermissionUtils.requestPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }


    private fun stopService(){
        if(AndroidUtils.isServiceRunning(this, NotificationListener::class.java)){
            val intent = Intent(this, NotificationListener::class.java)
            intent.action = NotificationListener.ACTION_STOP_FOREGROUND_SERVICE
            startService(intent)
        }
    }

    private fun startMagicService(){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(this, NotificationListener::class.java)
                startForegroundService(intent)
            }else{
                val intent = Intent(this, NotificationListener::class.java)
                startService(intent)
            }
    }


    private fun setListener(){

        fab_button_add_application?.setOnClickListener {
            startActivity(Intent(this, AddApplicationActivity::class.java))
        }

        switch_alarm_status?.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
               ic_alarm_status?.setImageResource(R.drawable.ic_on_button)
               startMagicService()
            }else{
                ic_alarm_status?.setImageResource(R.drawable.ic_off_button)
                stopService()
            }
        }
    }

}
