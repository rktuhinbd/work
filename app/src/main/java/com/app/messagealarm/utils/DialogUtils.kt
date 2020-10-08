package com.app.messagealarm.utils

import android.R
import android.R.attr.inputType
import android.R.attr.text
import android.R.id
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.InputType
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import com.awesomedroidapps.inappstoragereader.Utils
import com.google.android.material.textfield.TextInputLayout
import es.dmoral.toasty.Toasty
import java.lang.NumberFormatException


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
                    dialog.dismiss()
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

        fun showRingToneSelectDialog(context: Context, callBack: RepeatCallBack) {
            // setup the alert builder
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Select Ringtone")
            val animals = arrayOf("Default", "Select a song")
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
            val days = arrayOf(
                "Saturday", "Sunday", "Monday", "Wednesday", "Tuesday",
                "Thursday", "Friday"
            )
            val checkedItems = booleanArrayOf(false, false, false, false, false, false, false)
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
            if (dialog != null && !dialog.isShowing) {
                dialog.show()
            }

        }

        fun showInputDialog(
            context: Context,
            currentCount: String,
            message: String,
            callBack: RepeatCallBack
        ) {
            val alert = AlertDialog.Builder(context)
            alert.setCancelable(false)
            alert.setTitle(message)
            alert.setMessage("This number will indicate how much time the alarm music will play, range 1 to 10")
            // Set an EditText view to get user input
            // Set an EditText view to get user input
            val input = EditText(context)
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
            val alert = AlertDialog.Builder(context)
            alert.setCancelable(false)
            alert.setTitle("Select names")
            alert.setMessage(
                "Only messages from this names, will play alarm. Separate with" +
                        " comma for multiple!"
            )
            // Set an EditText view to get user input
            // Set an EditText view to get user input
            val input = EditText(context)
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
            val alert = AlertDialog.Builder(context)
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