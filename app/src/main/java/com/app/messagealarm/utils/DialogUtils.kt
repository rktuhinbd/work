package com.app.messagealarm.utils

import android.R
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface


class DialogUtils {

    companion object {

        fun showDialog(context: Context, title: String, message: String, callback: Callback) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                    R.string.ok
                ) { dialog, which ->
                    callback.onPositive()
                }
                .setIcon(R.drawable.ic_dialog_info)
                .setNegativeButton(
                    R.string.no
                ) { dialog, which ->
                    callback.onNegative()
                }
                .setCancelable(false)
                .show()
        }

        fun showSimpleListDialog(context: Context, callBack: RepeatCallBack) {
            // setup the alert builder
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Alarm Repeat Time")
            val animals = arrayOf("Once", "Daily", "Custom")
            builder.setItems(animals) { dialog, which ->
                when (which) {
                    0 -> { /* Once */
                        callBack.onClick("Once")
                        dialog.dismiss()
                    }
                    1 -> { /* Daily   */
                        callBack.onClick("Daily")
                        dialog.dismiss()
                    }
                    2 -> { /* Custom */
                        callBack.onClick("Custom")
                        dialog.dismiss()
                    }
                }
            }
            // create and show the alert dialog
            val dialog = builder.create()
            if (dialog != null && !dialog.isShowing) {
                dialog.show()
            }
        }

        fun showCheckedItemListDialog(
            context: Context,
            checkedCallBack: CheckedListCallback,
            callback: Callback
        ) {
            // setup the alert builder
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Choose days")
            // user checked an item
            val list: MutableList<String> = ArrayList()
            // add a checkbox list
            val days = arrayOf("Saturday", "Sunday", "Monday", "Wednesday", "Tuesday",
                "Thursday", "Friday")
            val checkedItems = booleanArrayOf(false, false, false, false, false, false, false)
            builder.setMultiChoiceItems(days, checkedItems) { dialog, which, isChecked ->
                // user checked or unchecked a box
                if (isChecked){
                    list.add(days[which])
                }
            }
            // add OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                // user clicked OK
                callback.onPositive()
                checkedCallBack.onChecked(list)
            }

            builder.setNegativeButton("Cancel") { dialog, which ->
                callback.onNegative()
            }

            // create and show the alert dialog
            val dialog = builder.create()
            if (dialog != null && !dialog.isShowing) {
                dialog.show()
            }

        }
    }

    interface Callback {
        fun onPositive()
        fun onNegative()
    }

    interface RepeatCallBack {
        fun onClick(name: String)
    }

    interface CheckedListCallback {
        fun onChecked(list: List<String>)
    }
}