package com.app.messagealarm.ui.main.add_options

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.app.messagealarm.R
import com.app.messagealarm.utils.DialogUtils
import com.app.messagealarm.utils.TimeUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_add_app_options.*

import java.util.*
import kotlin.math.min


class AddApplicationOption : BottomSheetDialogFragment() {

    val REQUEST_CODE_PICK_AUDIO = 1
    var isDefault = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun pickAudioFromStorage(){
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        activity!!.startActivityForResult(intent, REQUEST_CODE_PICK_AUDIO)
    }

    private fun setListener() {
        switch_custom_time?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                visibleCustomTimeLayout()
            } else {
                hideCustomTimeLayout()
            }
        }
        view_custom_time?.setOnClickListener {
            switch_custom_time?.performClick()
        }

        view_vibrate?.setOnClickListener {
            switch_vibrate?.performClick()
        }

        view_ringtone?.setOnClickListener {
            DialogUtils.showRingToneSelectDialog(activity!!, object : DialogUtils.RepeatCallBack {
                override fun onClick(name: String) {
                    if(name.contains("Select a song")){
                        pickAudioFromStorage()
                    }else{
                        txt_ringtone_value?.text = name
                    }
                }
            })
        }

        view_start_time?.setOnClickListener {
            val c: Calendar = Calendar.getInstance()
            val hour: Int = c.get(Calendar.HOUR_OF_DAY)
            val minute: Int = c.get(Calendar.MINUTE)
            val timePickerDialog =
                TimePickerDialog(context,
                    OnTimeSetListener { view, hourOfDay, min -> txt_start_time_value?.text = TimeUtils.getTimeWithAMOrPM(hourOfDay, min)
                    }, hour, minute, false
                )
            timePickerDialog.show()
        }


        view_end_time?.setOnClickListener {
            val c: Calendar = Calendar.getInstance()
            val hour: Int = c.get(Calendar.HOUR_OF_DAY)
            val minute: Int = c.get(Calendar.MINUTE)
            val timePickerDialog =
                TimePickerDialog(context,
                    OnTimeSetListener { view, hourOfDay, _min ->
                        txt_end_time_value?.text = TimeUtils.getTimeWithAMOrPM(hourOfDay, _min)}, hour, minute, false

                )
            timePickerDialog.show()
        }


        view_number_of_play?.setOnClickListener {
            DialogUtils.showInputDialog(activity!!, "Select number of play",
                object : DialogUtils.RepeatCallBack {
                    @SuppressLint("SetTextI18n")
                    override fun onClick(name: String) {
                        txt_number_of_play_value?.text = """$name times"""
                    }
                })
        }


        view_repeat_bg?.setOnClickListener {
            DialogUtils.showSimpleListDialog(activity!!, object : DialogUtils.RepeatCallBack {
                override fun onClick(name: String) {
                    txt_repeat_value?.text = name
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
}