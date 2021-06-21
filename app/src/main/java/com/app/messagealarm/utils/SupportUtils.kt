package com.app.messagealarm.utils

import android.R.id.message
import android.app.Activity
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import com.app.messagealarm.BuildConfig


class SupportUtils {
    companion object {
        fun sendEmail(activity: Activity) {
            val email = Intent(Intent.ACTION_SEND)
            email.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>("info@messagealarm.app"))
            email.putExtra(Intent.EXTRA_SUBJECT, "Hey, I need help!")
            email.putExtra(Intent.EXTRA_TEXT, message)
            //need this to prompts email client only
            //need this to prompts email client only
            email.type = "message/rfc822"
            activity.startActivity(Intent.createChooser(email, "Choose an Email client :"))
        }

        fun sendEmailSell(activity: Activity){
            val email = Intent(Intent.ACTION_SEND)
            email.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>("sales@mk7lab.com"))
            email.putExtra(Intent.EXTRA_SUBJECT, "Hi, I have a project for Mk7Lab!")
            email.putExtra(Intent.EXTRA_TEXT, message)
//need this to prompts email client only
            //need this to prompts email client only
            email.type = "message/rfc822"
            activity.startActivity(Intent.createChooser(email, "Choose an Email client :"))
        }

        fun sendEmailLanguage(activity: Activity) {
            val email = Intent(Intent.ACTION_SEND)
            email.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>("info@messagealarm.app"))
            email.putExtra(Intent.EXTRA_SUBJECT, "Hey, I wants you to add my language in this app!")
            email.putExtra(Intent.EXTRA_TEXT, "Please mention your desired language name: ")
//need this to prompts email client only
            //need this to prompts email client only
            email.type = "message/rfc822"
            activity.startActivity(Intent.createChooser(email, "Choose an Email client :"))
        }

        fun shareApp(activity: Activity) {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Message Alarm - Never miss an important message"
                )
                var shareMessage =
                    "\nHi, I am using Message Alarm, it's helping me not missing any important message's from any clients or friends!\n\n"
                shareMessage =
                    shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n"
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                activity.startActivity(Intent.createChooser(shareIntent, "Choose App"))
            } catch (e: Exception) { //e.toString();

            }
        }
    }
}