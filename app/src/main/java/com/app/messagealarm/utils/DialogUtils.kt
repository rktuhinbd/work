package com.app.messagealarm.utils

import android.R
import android.R.attr.inputType
import android.R.attr.text
import android.R.id
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.text.InputType
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import java.lang.NumberFormatException


open class DialogUtils {

    companion object {

        fun showSimpleDialog(context: Context, title: String, message: String) {
            MaterialAlertDialogBuilder(context, com.app.messagealarm.R.style.MyAlertDialogTheme)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                    R.string.ok
                ) { dialog, which ->
                    dialog.dismiss()
                }
                .setIcon(com.app.messagealarm.R.drawable.ic_info)
                .setCancelable(false)
                .show()
        }

        fun showOnlyPositiveDialog(context: Context, title: String, message: String, callback: Callback){
            MaterialAlertDialogBuilder(context, com.app.messagealarm.R.style.MyAlertDialogTheme)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                    R.string.ok
                ) { dialog, which ->
                    callback.onPositive()
                    dialog.dismiss()
                }
                .setIcon(com.app.messagealarm.R.drawable.ic_info)
                .setCancelable(false)
                .show()
        }

        fun showDialog(context: Context, title: String, message: String, callback: Callback) {
          val dialog =  MaterialAlertDialogBuilder(context, com.app.messagealarm.R.style.MyAlertDialogTheme)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                    R.string.ok
                ) { dialog, which ->
                    dialog.dismiss()
                    callback.onPositive()
                }
                .setIcon(com.app.messagealarm.R.drawable.ic_info)
                .setNegativeButton(
                    R.string.cancel
                ) { dialog, which ->
                    dialog.cancel()
                    callback.onNegative()
                }
                .setCancelable(false)
            dialog.show()
        }

        fun showDialogDrawOverApp(context: Context, title: String, message: String, callback: Callback) {
            val dialog =  MaterialAlertDialogBuilder(context, com.app.messagealarm.R.style.MyAlertDialogTheme)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                    R.string.ok
                ) { dialog, which ->
                    dialog.dismiss()
                    callback.onPositive()
                }
                .setIcon(com.app.messagealarm.R.drawable.ic_info)
                .setNegativeButton(
                    "Later"
                ) { dialog, which ->
                    dialog.cancel()
                    callback.onNegative()
                }
                .setCancelable(false)
            dialog.show()
        }

        fun showUpdateDialog(context: Context, title: String, message: String, callback: Callback) {
            val dialog =  MaterialAlertDialogBuilder(context, com.app.messagealarm.R.style.MyAlertDialogTheme)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                    "Update"
                ) { dialog, which ->
                    dialog.dismiss()
                    callback.onPositive()
                }
                .setIcon(com.app.messagealarm.R.drawable.ic_info)
                .setNegativeButton(
                   "Later"
                ) { dialog, which ->
                    dialog.cancel()
                    callback.onNegative()
                }
                .setCancelable(false)
                .create()
            if(!dialog.isShowing){
                dialog.show()
            }
        }




        fun showSimpleListDialog(context: Context, callBack: RepeatCallBack) {
            // setup the alert builder
            val builder = MaterialAlertDialogBuilder(context,com.app.messagealarm.R.style.MyAlertDialogTheme)
            builder.setTitle("Alarm Repeat Time")
            val animals = arrayOf( "Always", "Once", "Custom")
            builder.setItems(animals) { dialog, which ->
                when (which) {
                    0 -> { /* Once */
                        callBack.onClick("Always")
                        dialog.dismiss()

                    }
                    1 -> { /* Daily   */
                        callBack.onClick("Once")
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
            if (!dialog.isShowing) {
                dialog.show()
            }
        }

        fun showRingToneSelectDialog(context: Context, callBack: RepeatCallBack) {
            // setup the alert builder
            val builder = MaterialAlertDialogBuilder(context, com.app.messagealarm.R.style.MyAlertDialogTheme)
            builder.setTitle("Select Alarm tone")
            val animals = arrayOf("Default", "Select a song","Speak the message")
            builder.setItems(animals) { dialog, which ->
                when (which) {
                    0 -> { /* Default */
                        callBack.onClick("Default")
                        dialog.dismiss()
                    }
                    1 -> { /* Select   */
                        callBack.onClick("Select a song")
                        dialog.dismiss()
                    }
                    2 -> { /* Select   */
                        callBack.onClick("Speak the message")
                        dialog.dismiss()
                    }
                }
            }
            // create and show the alert dialog
            val dialog = builder.create()
            if (!dialog.isShowing) {
                dialog.show()
            }
        }

        fun showCheckedItemListDialog(
            repeatDays:String?,
            context: Context,
            checkedCallBack: CheckedListCallback,
            callback: Callback
        ) {
            // setup the alert builder
            val builder = MaterialAlertDialogBuilder(context, com.app.messagealarm.R.style.MyAlertDialogTheme)
            builder.setTitle("Choose days")
            // user checked an item
            val list: MutableList<String> = ArrayList()
            // add a checkbox list
            val days = arrayOf(
                "Saturday", "Sunday", "Monday","Tuesday", "Wednesday",
                "Thursday", "Friday"
            )
            //check which days already added and create checked items
            //this code is not working yet, have to fix.
            val daysList = repeatDays?.split(", ")
            val checkedItems = booleanArrayOf(false, false, false, false, false, false, false)
            if(daysList!= null && daysList.isNotEmpty()){
                for (x in days.indices){
                    for(element in daysList){
                        if(days[x].contains(element)){
                            list.add(days[x])
                            checkedItems[x] = true
                        }
                    }
                }
            }
            //end of that
            builder.setMultiChoiceItems(days, checkedItems) { dialog, which, isChecked ->
                // user checked or unchecked a box
                if (isChecked) {
                    list.add(days[which])
                }else{
                    list.remove(days[which])
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
            if (!dialog.isShowing) {
                dialog.show()
            }

        }

        fun showInputDialog(
            context: Context,
            currentCount: String,
            message: String,
            callBack: RepeatCallBack
        ) {
            val alert = MaterialAlertDialogBuilder(context, com.app.messagealarm.R.style.MyAlertDialogTheme)
            alert.setCancelable(false)
            alert.setTitle(message)
            alert.setMessage("This number will indicate how much time the alarm music will play, range 1 to 10")
            // Set an EditText view to get user input
            // Set an EditText view to get user input
            val input = EditText(context)
            if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)){
                input.setTextColor(Color.WHITE)
                input.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            }
            input.setText(currentCount)
            input.isSingleLine = true
            input.inputType = InputType.TYPE_CLASS_NUMBER
            val container = FrameLayout(context)
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val left_margin: Int = ViewUtils.dpToPx(20).toInt()
            val top_margin: Int = ViewUtils.dpToPx(10).toInt()
            val right_margin: Int = ViewUtils.dpToPx(20).toInt()
            val bottom_margin: Int = ViewUtils.dpToPx(10).toInt()
            params.setMargins(left_margin, top_margin, right_margin, bottom_margin)
            input.layoutParams = params
            container.addView(input)
            alert.setView(container)

            alert.setPositiveButton("Ok",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    val value = input.text.toString()
                    try {
                        if (value.toInt() in 1..10) {
                            callBack.onClick(value)
                        } else {
                            callBack.onClick("10")
                        }
                    } catch (ex: NumberFormatException) {
                        callBack.onClick("10")
                    }
                    return@OnClickListener
                })

            alert.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, which ->
                    // TODO Auto-generated method stub
                    return@OnClickListener
                })
            alert.show()
        }

        fun showSenderNameDialog(context: Context, currentName: String, callBack: RepeatCallBack) {
            val alert = AlertDialog.Builder(context, com.app.messagealarm.R.style.MyAlertDialogTheme)
            alert.setCancelable(false)
            alert.setTitle("Select names")
            alert.setMessage(
                "Only messages from this names, will play alarm. Separate with" +
                        " comma for multiple!"
            )
            // Set an EditText view to get user input
            // Set an EditText view to get user input
            val input = EditText(context)
            if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)){
                input.setTextColor(Color.WHITE)
                input.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            }
            if (currentName != "None") {
                input.setText(currentName)
            }
            input.maxLines = 3
            input.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            val container = FrameLayout(context)
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val left_margin: Int = ViewUtils.dpToPx(20).toInt()
            val top_margin: Int = ViewUtils.dpToPx(10).toInt()
            val right_margin: Int = ViewUtils.dpToPx(20).toInt()
            val bottom_margin: Int = ViewUtils.dpToPx(10).toInt()
            params.setMargins(left_margin, top_margin, right_margin, bottom_margin)
            input.layoutParams = params
            container.addView(input)
            alert.setView(container)
            alert.setPositiveButton("Ok",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    val value = input.text.toString()
                    callBack.onClick(value)
                    return@OnClickListener
                })

            alert.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, which ->
                    // TODO Auto-generated method stub
                    return@OnClickListener
                })
            alert.show()
        }

        fun showMessageBodyDialog(context: Context, currentName: String, callBack: RepeatCallBack) {
            val alert = MaterialAlertDialogBuilder(context, com.app.messagealarm.R.style.MyAlertDialogTheme)
            alert.setCancelable(false)
            alert.setTitle("Select message body")
            alert.setMessage(
                "Only messages with this text will play alarm!")
            // Set an EditText view to get user input
            // Set an EditText view to get user input
            val input = EditText(context)
            if (currentName != "None") {
                input.setText(currentName)
            }
            if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)){
                input.setTextColor(Color.WHITE)
                input.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            }
            input.maxLines = 3
            input.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            val container = FrameLayout(context)
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val left_margin: Int = ViewUtils.dpToPx(20).toInt()
            val top_margin: Int = ViewUtils.dpToPx(10).toInt()
            val right_margin: Int = ViewUtils.dpToPx(20).toInt()
            val bottom_margin: Int = ViewUtils.dpToPx(10).toInt()
            params.setMargins(left_margin, top_margin, right_margin, bottom_margin)
            input.layoutParams = params
            container.addView(input)
            alert.setView(container)
            alert.setPositiveButton("Ok",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    val value = input.text.toString()
                    callBack.onClick(value)
                    return@OnClickListener
                })

            alert.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, which ->
                    // TODO Auto-generated method stub
                    return@OnClickListener
                })
            alert.show()
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