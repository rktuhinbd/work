package com.app.messagealarm.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.R
import com.app.messagealarm.ui.main.add_apps.AddApplicationActivity
import com.app.messagealarm.service.notification_service.NotificationListener
import com.app.messagealarm.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, NotificationListener::class.java))
        }else{
            startService(Intent(this, NotificationListener::class.java))
        }
        setListener()
        askForPermission()
        switch_alarm_status?.isChecked = true
    }

    private fun askForPermission(){
        PermissionUtils.requestPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }


    private fun setListener(){
        fab_button_add_application?.setOnClickListener {
            startActivity(Intent(this, AddApplicationActivity::class.java))
        }

        switch_alarm_status?.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
               ic_alarm_status?.setImageResource(R.drawable.ic_on_button)
            }else{
                ic_alarm_status?.setImageResource(R.drawable.ic_off_button)
            }
        }
    }

}
