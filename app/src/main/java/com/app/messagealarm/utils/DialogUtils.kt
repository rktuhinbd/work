package com.app.messagealarm.utils

import android.R
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface





class DialogUtils {

    companion object{
        fun showDialog(context:Context, title:String, message:String, callback: Callback){
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok
                ) { dialog, which ->
                    callback.onPositive()
                }
                .setIcon(R.drawable.ic_dialog_info)
                .setNegativeButton(
                    R.string.no
                ){ dialog, which ->
                    callback.onNegative()
                }
                .setCancelable(false)
                .show()
        }
    }

    interface Callback{
        fun onPositive()
        fun onNegative()
    }
}