package com.app.messagealarm.ui.main.configure_options.add_options_speak

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.app.messagealarm.R
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.ui.main.configure_options.view.OptionView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_speak_options.*

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
    }

    private fun setListener(){
        btn_close?.setOnClickListener {
            dismiss()
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