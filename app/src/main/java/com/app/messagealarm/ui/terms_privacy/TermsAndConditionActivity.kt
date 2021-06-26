package com.app.messagealarm.ui.terms_privacy

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService.requestRebind
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.app.messagealarm.R
import com.app.messagealarm.service.notification_service.NotificationListener
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import com.google.android.material.button.MaterialButton

class TermsAndConditionActivity : AppCompatActivity() {


    var btnAgree: MaterialButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_privacy)
        btnAgree = findViewById(R.id.btn_agree)
        btnAgree?.setOnClickListener {
            tryReconnectService()
            defaultPreferences()
            startActivity(Intent(this, AlarmApplicationActivity::class.java))
            finish()
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
}