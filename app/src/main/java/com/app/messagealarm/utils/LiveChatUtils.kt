package com.app.messagealarm.utils

import android.R
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.Gravity
import androidx.core.content.ContextCompat.startActivity
import es.dmoral.toasty.Toasty
import java.net.URLEncoder


class LiveChatUtils {
    companion object{
         fun openWhatsApp(activity:Activity) {
            try {
                val packageManager: PackageManager = activity.packageManager
                val i = Intent(Intent.ACTION_VIEW)
                val url = "https://wa.me/message/BLNUTEVIUC5KP1"
                i.setPackage("com.whatsapp")
                i.data = Uri.parse(url)
                if (i.resolveActivity(packageManager) != null) {
                    activity.startActivity(i)
                } else {
                    Toasty.error(activity, "WhatsApp is required, " +
                            "But not found!").show()
                }
            } catch (e: Exception) {
                Toasty.error(activity, "WhatsApp is required, " +
                        "But not found!").show()
            }
        }
    }
}