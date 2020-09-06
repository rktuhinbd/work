package com.app.messagealarm.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.R
import com.app.messagealarm.ui.main.add_apps.AddApplicationActivity
import com.app.messagealarm.ui.service.NotificationListener
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
    }


    private fun setListener(){
        fab_button_add_application?.setOnClickListener {
            startActivity(Intent(this, AddApplicationActivity::class.java))
        }
    }

}
