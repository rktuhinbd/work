package com.app.messagealarm.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import com.app.messagealarm.R
import timber.log.Timber

/**
 * This is a class that contains utils for progress dialogs
 * @author Mohd. Asfaq-E-Azam Rifat
 * */
class ProgressDialogUtils private constructor() {
    private var mAlertDialog: AlertDialog? = null

    /**
     * This method shows a progress dialog
     * @param context current UI context
     * @return created [AlertDialog]
     * */
    @SuppressLint("InflateParams")
    fun showProgressDialog(context: Context): AlertDialog? {
        Timber.d((context as Activity).javaClass.simpleName)
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater
            .from(context)
            .inflate(
                R.layout.progresss_dialog_layout,
                null,
                false
            )

        view.findViewById<AppCompatTextView>(R.id.text_view_message).setTypeface(null, Typeface.NORMAL)
        builder.setCancelable(true)
        builder.setView(view)

        mAlertDialog = builder.create()
        mAlertDialog?.show()

        return mAlertDialog
    }

    /**
     * This method hides the progress dialog (if any visible alert dialog is found)
     * */
    fun hideProgressDialog() {
        if (mAlertDialog != null) {
            mAlertDialog!!.dismiss()
            mAlertDialog = null
        }
    }

    companion object {
        private var sInstance: ProgressDialogUtils? = null

        fun on(): ProgressDialogUtils {
            if (sInstance == null) {
                sInstance = ProgressDialogUtils()
            }

            return sInstance as ProgressDialogUtils
        }
    }
}
