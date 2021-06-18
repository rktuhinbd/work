package com.app.messagealarm.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.app.messagealarm.R
import com.app.messagealarm.ui.terms_privacy.TermsPrivacyActivity
import com.app.messagealarm.utils.DialogUtils
import com.app.messagealarm.utils.PermissionUtils
import es.dmoral.toasty.Toasty

class SplashGettingStarted : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_getting_started)
        val button = findViewById<Button>(R.id.btn_get_started)
        button?.setOnClickListener()
        {
          openGiveNotificationDialog()
    }}


    override fun onResume() {
        super.onResume()
        /**
         * if user granted the permission then take the user to terms and privacy page
         */
        if(PermissionUtils.isNotificationAllowed()){
            takeTheUserToTermsAndPrivacy()
        }
    }

    private fun takeTheUserToTermsAndPrivacy(){
        startActivity(Intent(this, TermsPrivacyActivity::class.java))
        finish()
    }

    private fun openGiveNotificationDialog(){
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
                            Toasty.error(this@SplashGettingStarted, getString(R.string.not_supperted))
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