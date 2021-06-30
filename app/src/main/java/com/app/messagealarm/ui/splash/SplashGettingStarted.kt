package com.app.messagealarm.ui.splash

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.app.messagealarm.R
import com.app.messagealarm.ui.terms_privacy.TermsAndConditionActivity
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.DialogUtils
import com.app.messagealarm.utils.PermissionUtils
import com.app.messagealarm.utils.SharedPrefUtils
import com.google.android.material.button.MaterialButton
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_splash_getting_started.*

class SplashGettingStarted : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_getting_started)
        val button = findViewById<Button>(R.id.btn_get_started)
        text_terms_policy?.movementMethod = LinkMovementMethod.getInstance()
        text_terms_policy?.setLinkTextColor(ContextCompat.getColor(this, R.color.color_white))
        button?.setOnClickListener()
        {
          openGiveNotificationDialogNew()
    }
        SharedPrefUtils.write(Constants.PreferenceKeys.IS_GETTING_STARTED_PAGE_VISITED, true)
    }


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
        startActivity(Intent(this, TermsAndConditionActivity::class.java))
        finish()
    }


    private fun openGiveNotificationDialogNew(){
        if(!isFinishing){
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_privacy_policy_layout)
            val cancelButton = dialog.findViewById<TextView>(R.id.text_cancel)
            val continueButton = dialog.findViewById<TextView>(R.id.text_continue)
            cancelButton.setOnClickListener {
                if(dialog.isShowing){
                    dialog.dismiss()
                }
            }
            continueButton.setOnClickListener {
                if(dialog.isShowing){
                    dialog.dismiss()
                }
                try {
                    val intent =
                        Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    startActivity(intent)
                } catch (e: NoClassDefFoundError) {
                    Toasty.error(this@SplashGettingStarted, getString(R.string.not_supperted))
                        .show()
                }
            }
            val window: Window = dialog.window!!
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            //
            if(!dialog.isShowing){
                dialog.show()
            }
        }
    }
}