package com.app.messagealarm.ui.widget

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.VideoView
import com.app.messagealarm.R
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_dialog_onboarding.*

class TutorialBottomSheetDialog(val activity: Activity) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.AppBottomSheetDialogTheme)
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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.layout_bottom_sheet_tutorial, container, false)
        val videoView = v.findViewById<VideoView>(R.id.video_view_tutorial)
        val skipButton = v.findViewById<TextView>(R.id.btn_skip)
        skipButton.setOnClickListener {
            (activity as AlarmApplicationActivity).changeStateOfSpeedDial()
            SharedPrefUtils.write(Constants.PreferenceKeys.IS_VIDEO_SHOWED, true)
            dismiss()
        }
        val path = "android.resource://" + requireActivity().packageName.toString() + "/" + R.raw.video_tutorial
        videoView.setVideoURI(Uri.parse(path))
        videoView.setOnPreparedListener {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            it.setScreenOnWhilePlaying(true)
        }
        videoView.setOnCompletionListener {
            skipButton.text = "CLOSE"
            videoView.stopPlayback()
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        videoView.start()
        return v
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dia ->
            val dialog = dia as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet!!).state =
                BottomSheetBehavior.STATE_EXPANDED
            BottomSheetBehavior.from(bottomSheet).skipCollapsed = true
            BottomSheetBehavior.from(bottomSheet).isHideable = true
            setupFullHeight(dia)
        }

        return bottomSheetDialog
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_VIDEO_SHOWED)){
            (activity as AlarmApplicationActivity).changeStateOfSpeedDial()
        }
    }


}