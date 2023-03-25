package com.app.messagealarm.ui.main.configure_options.add_options_speak

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.app.messagealarm.R
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.ui.main.configure_options.view.OptionView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.RangeSlider
import kotlinx.android.synthetic.main.dialog_speak_options.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class SpeakOptionDialog : BottomSheetDialogFragment(), OptionView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_speak_options, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setListener()
        init()
    }

    private fun setListener(){
        btn_close?.setOnClickListener {
            dismiss()
        }
        range_slider?.setLabelFormatter {
            (it.toInt() + 1).toString()
        }
    }

    fun init(){
        var updateTimer: Timer? = null
        val timeZone = TimeZone.getDefault()
        // Create a SimpleDateFormat with the desired output format
        range_slider?.stepSize = 1F
        val dateFormat = SimpleDateFormat("h a", Locale.getDefault())
        range_slider.addOnChangeListener { slider, value, fromUser ->
            // Cancel any previously scheduled updates
            updateTimer?.cancel()
            // Schedule an update in 250 milliseconds
            updateTimer = Timer()
            updateTimer?.schedule(object : TimerTask() {
                override fun run() {
                    // Get the slider values
                    val values = slider.values
                    // Get the user's timezone
                    dateFormat.timeZone = timeZone
                    // Convert the slider values to user time and format as AM/PM time
                    val startTime = Calendar.getInstance()
                    startTime.set(Calendar.HOUR_OF_DAY, values[0].toInt())
                    startTime.set(Calendar.MINUTE, 0)
                    val formattedStartTime = dateFormat.format(startTime.time)
                    val endTime = Calendar.getInstance()
                    endTime.set(Calendar.HOUR_OF_DAY, values[1].toInt())
                    endTime.set(Calendar.MINUTE, 0)
                    val formattedEndTime = dateFormat.format(endTime.time)
                    activity?.runOnUiThread {
                        // Update the TextViews
                        txt_hour?.let {
                            it.text = formattedStartTime
                        }
                        txt_end_hour?.text = formattedEndTime
                    }
                }
            }, 200)
        }
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
        try {
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
            val behavior: BottomSheetBehavior<*> =
                BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!)
            behavior.isDraggable = false
            val layoutParams = bottomSheet.layoutParams
            val windowHeight = getWindowHeight()
            if (layoutParams != null) {
                layoutParams.height = windowHeight
            }
            bottomSheet.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        } catch (e: NullPointerException) {

        }
    }

    private fun getWindowHeight(): Int { // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager.defaultDisplay
            .getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }


    override fun onApplicationSaveSuccess() {
        TODO("Not yet implemented")
    }

    override fun onApplicationSaveError(message: String) {
        TODO("Not yet implemented")
    }

    override fun onApplicationUpdateSuccess() {
        TODO("Not yet implemented")
    }

    override fun onApplicationUpdateError(message: String) {
        TODO("Not yet implemented")
    }

    override fun onBitmapSaveSuccess(path: String) {
        TODO("Not yet implemented")
    }

    override fun onBitmapSaveError() {
        TODO("Not yet implemented")
    }

    override fun onApplicationGetSuccess(app: ApplicationEntity) {
        TODO("Not yet implemented")
    }

    override fun onApplicationGetError(message: String) {
        TODO("Not yet implemented")
    }

    override fun onIllegalState() {
        TODO("Not yet implemented")
    }

}