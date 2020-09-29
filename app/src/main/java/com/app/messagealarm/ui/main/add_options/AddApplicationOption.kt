package com.app.messagealarm.ui.main.add_options

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.graphics.drawable.toBitmap
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.DialogUtils
import com.app.messagealarm.utils.FileUtils
import com.app.messagealarm.utils.TimeUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_add_app_options.*

import java.util.*


class AddApplicationOption : BottomSheetDialogFragment(), AddApplicationOptionView {

    public var alarmTonePath:String? = null
    private val addApplicationEntity = ApplicationEntity()
    private var addApplicationOptionPresenter: AddApplicationOptionPresenter? = null
    val REQUEST_CODE_PICK_AUDIO = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addApplicationOptionPresenter = AddApplicationOptionPresenter(this)
        defaultValuesToDataModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_app_options, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setListener()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            setupFullHeight(bottomSheetDialog)
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!)
        behavior.isDraggable = false
        val layoutParams = bottomSheet.layoutParams
        val windowHeight = getWindowHeight()
        if (layoutParams != null) {
            layoutParams.height = windowHeight
        }
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun pickAudioFromStorage() {
        val intent =
            Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        activity!!.startActivityForResult(intent, REQUEST_CODE_PICK_AUDIO)
    }

    private fun setListener() {
        btn_close?.setOnClickListener {
            Log.e("CHECK", checkForDefault().toString())
            if(checkForDefault()){
                dismiss()
            }else{
                Toasty.info(activity!!, "Show dialog").show()
            }
        }

        btn_save?.setOnClickListener {
            saveApplication()
        }

        switch_custom_time?.setOnCheckedChangeListener { buttonView, isChecked ->
            /**
             * set is custom time to data model
             */
            addApplicationEntity.isCustomTime = isChecked
            if (isChecked) {
                visibleCustomTimeLayout()
            } else {
                hideCustomTimeLayout()
            }
        }

        switch_vibrate?.setOnCheckedChangeListener { buttonView, isChecked ->
            /**
             * set vibrate option to data model
             */
            addApplicationEntity.isVibrateOnAlarm = isChecked
        }

        view_custom_time?.setOnClickListener {
            switch_custom_time?.performClick()
        }

        view_vibrate?.setOnClickListener {
            switch_vibrate?.performClick()
        }

        view_sender_name?.setOnClickListener {
            DialogUtils.showSenderNameDialog(
                activity!!,
                txt_sender_name_value?.text.toString(),
                object : DialogUtils.RepeatCallBack {
                    override fun onClick(name: String) {
                        if (name.isNotEmpty()) {
                            txt_sender_name_value?.text = name
                            btn_sender_name_clear?.visibility = View.VISIBLE
                            /**
                             * set sender name to data model
                             */
                            addApplicationEntity.senderNames = name
                        } else {
                            btn_sender_name_clear?.visibility = View.GONE
                            txt_sender_name_value?.text = "None"
                            /**
                             * set None sender name to data model
                             */
                            addApplicationEntity.senderNames = "None"
                        }
                    }

                })
        }


        view_message_body?.setOnClickListener {
            DialogUtils.showMessageBodyDialog(
                activity!!,
                txt_message_body_value?.text.toString(),
                object : DialogUtils.RepeatCallBack {
                    override fun onClick(name: String) {
                        if (name.isNotEmpty()) {
                            txt_message_body_value?.text = name
                            btn_message_body_clear?.visibility = View.VISIBLE
                            /**
                             * set message body to data model
                             */
                            addApplicationEntity.messageBody = name
                        } else {
                            btn_message_body_clear?.visibility = View.GONE
                            txt_message_body_value?.text = "None"
                            /**
                             * set none to message body data model
                             */
                            addApplicationEntity.messageBody = "None"
                        }
                    }

                })
        }

        view_ringtone?.setOnClickListener {
            DialogUtils.showRingToneSelectDialog(activity!!, object : DialogUtils.RepeatCallBack {
                override fun onClick(name: String) {
                    if (name.contains("Select a song")) {
                        pickAudioFromStorage()
                        /**
                         * set custom alarm tone type to data model
                         */
                        addApplicationEntity.ringTone = "Custom"
                    } else {
                        txt_ringtone_value?.text = name
                        /**
                         * set default alarm tone type to data model
                         */
                        addApplicationEntity.ringTone = name
                    }
                }
            })
        }

        view_start_time?.setOnClickListener {
            val c: Calendar = Calendar.getInstance()
            val hour: Int = c.get(Calendar.HOUR_OF_DAY)
            val minute: Int = c.get(Calendar.MINUTE)
            val timePickerDialog =
                TimePickerDialog(
                    context,
                    OnTimeSetListener { view, hourOfDay, min ->
                        txt_start_time_value?.text = TimeUtils.getTimeWithAMOrPM(hourOfDay, min)
                        /**
                         * set start time to data model
                         */
                        if(addApplicationEntity.isCustomTime){
                            addApplicationEntity.startTime = txt_start_time_value?.text.toString()
                        }
                    }, hour, minute, true
                )
            timePickerDialog.show()
        }


        view_end_time?.setOnClickListener {
            val c: Calendar = Calendar.getInstance()
            val hour: Int = c.get(Calendar.HOUR_OF_DAY)
            val minute: Int = c.get(Calendar.MINUTE)
            val timePickerDialog =
                TimePickerDialog(
                    context,
                    OnTimeSetListener { view, hourOfDay, _min ->
                        txt_end_time_value?.text = TimeUtils.getTimeWithAMOrPM(hourOfDay, _min)
                        /**
                         * set end time to data model
                         */
                        if(addApplicationEntity.isCustomTime){
                            addApplicationEntity.endTime = txt_end_time_value?.text.toString()
                        }
                    }, hour, minute, true

                )
            timePickerDialog.show()
        }


        view_number_of_play?.setOnClickListener {
            DialogUtils.showInputDialog(activity!!,
                txt_number_of_play_value.text.toString().replace(" times", ""),
                "Select number of play",
                object : DialogUtils.RepeatCallBack {
                    @SuppressLint("SetTextI18n")
                    override fun onClick(name: String) {
                        /**
                         * set number of play to data model
                         */
                        addApplicationEntity.numberOfPlay = name.trim().toInt()
                        txt_number_of_play_value?.text = """$name times"""
                    }
                })
        }


        view_repeat_bg?.setOnClickListener {
            DialogUtils.showSimpleListDialog(activity!!, object : DialogUtils.RepeatCallBack {
                override fun onClick(name: String) {
                    txt_repeat_value?.text = name
                    /**
                     * set alarm repeat value to data model
                     */
                    addApplicationEntity.alarmRepeat = name
                    if (name.contains("Custom")) {
                        DialogUtils.showCheckedItemListDialog(
                            activity!!,
                            object : DialogUtils.CheckedListCallback {
                                @SuppressLint("SetTextI18n")
                                override fun onChecked(list: List<String>) {
                                    var selectedDays: String = ""
                                    list.forEach {
                                        selectedDays += "${it.substring(0, 3)}, "
                                    }
                                    txt_repeat_value?.text = selectedDays.substring(
                                        0,
                                        selectedDays.length - 2
                                    )
                                    /**
                                     * set alarm repeat days to data model
                                     */
                                    addApplicationEntity.repeatDays = selectedDays.substring(
                                        0,
                                        selectedDays.length - 2
                                    )
                                }
                            },
                            object : DialogUtils.Callback {
                                override fun onPositive() {

                                }

                                override fun onNegative() {

                                }
                            }
                        )
                    }
                }
            })
        }
    }

    private fun defaultValuesToDataModel(){
        addApplicationEntity.alarmRepeat = "Once"
        addApplicationEntity.ringTone = "Default"
        addApplicationEntity.isVibrateOnAlarm = false
        addApplicationEntity.isCustomTime = false
        addApplicationEntity.numberOfPlay = 2
        addApplicationEntity.senderNames = "None"
        addApplicationEntity.messageBody = "None"
        addApplicationEntity.isRunningStatus = true
    }

    private fun saveApplication(){
        /**
         * Populate Application entity from UI controller data
         * with start of other values
         */
        val app = arguments?.getSerializable(Constants.BundleKeys.APP) as InstalledApps
        addApplicationEntity.appName = app.appName
        addApplicationEntity.packageName = app.packageName
        addApplicationEntity.tone_path = alarmTonePath
        Thread(Runnable {
            val bitmap = app.drawableIcon
            addApplicationOptionPresenter?.saveBitmapToFile(bitmap.toBitmap())
        }).start()
    }

    private fun checkForDefault():Boolean{
        var isDefault = false
            if(txt_repeat_value?.text.toString().trim() == addApplicationEntity.alarmRepeat){
                if(txt_ringtone_value?.text.toString().trim() == addApplicationEntity.ringTone){
                    if(switch_vibrate?.isChecked == addApplicationEntity.isVibrateOnAlarm){
                        if(switch_custom_time?.isChecked == addApplicationEntity.isCustomTime){
                            if(txt_number_of_play_value?.text.toString().trim()[0].toString() ==
                                addApplicationEntity.numberOfPlay.toString()){
                                if(txt_sender_name_value?.text.toString() == addApplicationEntity.senderNames){
                                    if(txt_message_body_value?.text.toString() == addApplicationEntity.messageBody){
                                        isDefault = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        return isDefault
    }

    private fun visibleCustomTimeLayout() {
        layout_start_time?.visibility = View.VISIBLE
        layout_end_time?.visibility = View.VISIBLE
    }


    private fun hideCustomTimeLayout() {
        layout_start_time?.visibility = View.GONE
        layout_end_time?.visibility = View.GONE
    }

    private fun getWindowHeight(): Int { // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager.defaultDisplay
            .getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    override fun onApplicationSaveSuccess() {
       activity!!.runOnUiThread {
               Toasty.success(activity!!, getString(R.string.application_save_success)).show()
               dismiss()
               activity!!.finish()
       }
    }

    override fun onApplicationSaveError(message: String) {
     activity!!.runOnUiThread {
         Toasty.error(activity!!, message).show()
     }
    }

    override fun onBitmapSaveSuccess(path: String) {
        addApplicationEntity.bitmapPath = path
        /**
         * End of other values
         */
        addApplicationOptionPresenter?.saveApplication(addApplicationEntity)
    }


}