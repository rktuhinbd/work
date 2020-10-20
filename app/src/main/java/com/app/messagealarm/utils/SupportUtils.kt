package com.app.messagealarm.utils

import android.R.id.message
import android.app.Activity
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity


class SupportUtils {
    companion object{
        fun sendEmail(activity:Activity){
            val email = Intent(Intent.ACTION_SEND)
            email.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>("support@messagealarm.com"))
            email.putExtra(Intent.EXTRA_SUBJECT, "Hey, I need help")
            email.putExtra(Intent.EXTRA_TEXT, message)
//need this to prompts email client only
            //need this to prompts email client only
            email.type = "message/rfc822"
            activity.startActivity(Intent.createChooser(email, "Choose an Email client :"))
        }
    }
}