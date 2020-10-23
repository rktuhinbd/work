package com.app.messagealarm.ui.main.add_options

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_add_app_options.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class AddApplicationOption : BottomSheetDialogFragment(), AddApplicationOptionView {

    var once:Once? = null
    public var alarmTonePath:String? = null
    var ringtoneName:String? = null
    private var addApplicationEntity = ApplicationEntity()
    private var holderEntity = ApplicationEntity()
    private var addApplicationOptionPresenter: AddApplicationOptionPresenter? = null
    val REQUEST_CODE_PICK_AUDIO = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addApplicationOptionPresenter = AddApplicationOptionPresenter(this)
        once = Once()
    }


    private fun darkMode(){
        if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)){
            btn_close?.setImageResource(R.drawable.ic_close_white)
            btn_save?.setImageResource(R.drawable.ic_tick_white)
            img_repeat?.setImageResource(R.drawable.ic_repeat_white)
            img_ringtone?.setImageResource(R.drawable.ic_music_white)
            img_vibrate?.setImageResource(R.drawable.ic_vibrate_white)
            img_just_vibrate?.setImageResource(R.drawable.ic_vibrate_white)
            img_custom_time?.setImageResource(R.drawable.ic_time_white)
            img_number_of_play?.setImageResource(R.drawable.ic_loop_white)
            img_sender_name?.setImageResource(R.drawable.ic_name_white)
            img_message_body?.setImageResource(R.drawable.ic_message_white)
            img_start_time?.setImageResource(R.drawable.ic_watch_white)
            img_end_time?.setImageResource(R.drawable.ic_watch_white)
        }else{
            btn_close?.setImageResource(R.drawable.ic_close)
            btn_save?.setImageResource(R.drawable.ic_tick)
            img_repeat?.setImageResource(R.drawable.ic_repeat)
            img_ringtone?.setImageResource(R.drawable.ic_music)
            img_vibrate?.setImageResource(R.drawable.ic_vibration)
            img_just_vibrate?.setImageResource(R.drawable.ic_vibration)
            img_custom_time?.setImageResource(R.drawable.ic_clock)
            img_number_of_play?.setImageResource(R.drawable.ic_refresh)
            img_sender_name?.setImageResource(R.drawable.ic_name)
            img_message_body?.setImageResource(R.drawable.ic_message)
            img_start_time?.setImageResource(R.drawable.ic_stopwatch)
            img_end_time?.setImageResource(R.drawable.ic_stopwatch)
        }
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
        handleEditAndViewMode()
        darkMode()
    }

    private fun handleEditAndViewMode(){
            defaultValuesToDataModel()
            if(!arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!){
                if(arguments?.getSerializable(Constants.BundleKeys.APP) != null){
                    addApplicationOptionPresenter?.getAppByPackageName(
                        (arguments?.getSerializable(Constants.BundleKeys.APP) as InstalledApps).packageName
                    )
                }
            }else{
                //edit mode from home
                if(arguments?.getString(Constants.BundleKeys.PACKAGE_NAME) != null){
                    addApplicationOptionPresenter?.getAppByPackageName(
                        arguments?.getString(
                            Constants.BundleKeys.PACKAGE_NAME
                        )!!
                    )
                }
            }
    }

    override fun onResume() {
        super.onResume()
        //back button handler
        dialog!!.setOnKeyListener(object : DialogInterface.OnKeyListener {
            override fun onKey(
                dialog: DialogInterface?, keyCode: Int,
                event: KeyEvent
            ): Boolean {
                return if (keyCode == KeyEvent.KEYCODE_BACK) {
                    //This is the filter
                    once?.run(Runnable {
                        btn_close?.performClick()
                    })
                    return true
                } else false // pass on to be processed as normal
            }
        })
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
        requireActivity().startActivityForResult(intent, REQUEST_CODE_PICK_AUDIO)
    }

    private fun setListener() {
        btn_close?.setOnClickListener {
            if(checkForDefault()){
                dismiss()
            }else{
                showDiscardDialog()
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
                requireActivity(),
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
                requireActivity(),
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
            DialogUtils.showRingToneSelectDialog(
                requireActivity(),
                object : DialogUtils.RepeatCallBack {
                    override fun onClick(name: String) {
                        if (name.contains("Select a song")) {
                            pickAudioFromStorage()
                            /**
                             * set custom alarm tone type to data model
                             */
                            addApplicationEntity.ringTone = "Default"
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
            val c: Calendar = startTimeCalender()
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
                        if (addApplicationEntity.isCustomTime) {
                            addApplicationEntity.startTime = txt_start_time_value?.text.toString()
                        }
                    }, hour, minute, false
                )
            timePickerDialog.show()
        }


        view_end_time?.setOnClickListener {
            val c: Calendar = endTimeCalender()
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
                        if (addApplicationEntity.isCustomTime) {
                            addApplicationEntity.endTime = txt_end_time_value?.text.toString()
                        }
                    }, hour, minute, false

                )
            timePickerDialog.show()
        }


        view_number_of_play?.setOnClickListener {
            DialogUtils.showInputDialog(requireActivity(),
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
            DialogUtils.showSimpleListDialog(
                requireActivity(),
                object : DialogUtils.RepeatCallBack {
                    override fun onClick(name: String) {
                        txt_repeat_value?.text = name
                        /**
                         * set alarm repeat value to data model
                         */
                        addApplicationEntity.alarmRepeat = name
                        if (name.contains("Custom")) {
                            DialogUtils.showCheckedItemListDialog(
                                addApplicationEntity.repeatDays,
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

   fun setToneName(name: String){
        addApplicationEntity.ringTone = name
    }

    private fun showDiscardDialog() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_discard_layout)
        val txtCancel = dialog.findViewById<TextView>(R.id.btn_cancel)
        val txtDiscard = dialog.findViewById<TextView>(R.id.btn_discard)
        Objects.requireNonNull(dialog.window!!).setBackgroundDrawableResource(android.R.color.transparent)
        txtCancel.setOnClickListener {
            if (dialog.isShowing) {
                once = Once()
                dialog.cancel()
            }
        }
       txtDiscard.setOnClickListener {
           dismiss()
           if (dialog.isShowing) {
               dialog.cancel()
           }
       }
        val window = dialog.window
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val wlp = window.attributes;
         wlp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window.attributes = wlp
        if(!dialog.isShowing){
            dialog.show()
        }
    }


    @SuppressLint("SimpleDateFormat")
    fun startTimeCalender():Calendar{
        /**
         * User will be doing in 12 hours
         * Background works will be in 24 hours
         */
        return try {
            val dfDate  = SimpleDateFormat("hh:mm aa")
            val cal = Calendar.getInstance()
            cal.time = dfDate.parse(txt_start_time_value?.text.toString())!!
             cal
        }catch (ex: ParseException){
            return Calendar.getInstance()
        }

    }

    @SuppressLint("SimpleDateFormat")
    fun endTimeCalender():Calendar{
        return try {
            val dfDate  = SimpleDateFormat("hh:mm aa")
            val cal = Calendar.getInstance()
            cal.time = dfDate.parse(txt_end_time_value?.text.toString())!!
             cal
        }catch (ex: ParseException){
            return Calendar.getInstance()
        }

    }


    private fun defaultValuesToDataModel() : ApplicationEntity {
        addApplicationEntity.alarmRepeat = "Once"
        addApplicationEntity.ringTone = "Default"
        addApplicationEntity.isVibrateOnAlarm = false
        addApplicationEntity.isCustomTime = false
        addApplicationEntity.numberOfPlay = 2
        addApplicationEntity.startTime = "6:00 am"
        addApplicationEntity.endTime = "12:00 pm"
        addApplicationEntity.senderNames = "None"
        addApplicationEntity.messageBody = "None"
        addApplicationEntity.isRunningStatus = true

        //set this to holder object for checking default
        holderEntity.alarmRepeat = "Once"
        holderEntity.ringTone = "Default"
        holderEntity.isVibrateOnAlarm = false
        holderEntity.isCustomTime = false
        holderEntity.numberOfPlay = 2
        holderEntity.startTime = "6:00 am"
        holderEntity.endTime = "12:00 pm"
        holderEntity.senderNames = "None"
        holderEntity.messageBody = "None"
        holderEntity.isRunningStatus = true

        return addApplicationEntity
    }

    private fun saveApplication(){
        /**
         * Populate Application entity from UI controller data
         * with start of other values
         */
        //start progress bar
        showProgressBar()
        if(!arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!){
            val app = arguments?.getSerializable(Constants.BundleKeys.APP) as InstalledApps
            addApplicationEntity.appName = app.appName
            addApplicationEntity.packageName = app.packageName
            addApplicationEntity.tone_path = alarmTonePath
            Thread(Runnable {
                try {
                    val bitmap = app.drawableIcon
                    addApplicationOptionPresenter?.saveBitmapToFile(
                        requireActivity(),
                        bitmap.toBitmap()
                    )
                } catch (e: Exception) {
                    hideProgressBar()
                    Toasty.error(requireActivity(), e.message!!).show()
                }
            }).start()
        }else{
            saveWithTimeConstrain()
        }
    }

    private fun showProgressBar(){
        progress_bar_option?.visibility = View.VISIBLE
    }

    private fun hideProgressBar(){
        progress_bar_option?.visibility = View.INVISIBLE
    }


    @SuppressLint("SimpleDateFormat")
    private fun isTimeConstrained(startTime: String, endTime: String):Boolean{
       return try{
               val dfDate  = SimpleDateFormat("hh:mm aa")
               dfDate.parse(startTime)!!.before(dfDate.parse(endTime))
        }catch (ex: ParseException){
           return false
        }
    }

    private fun checkForDefault():Boolean{
        var isDefault = false
            if(txt_repeat_value?.text.toString().trim() == holderEntity.alarmRepeat){
                if(txt_ringtone_value?.text.toString().trim() == holderEntity.ringTone){
                    if(switch_vibrate?.isChecked == holderEntity.isVibrateOnAlarm){
                        if(switch_custom_time?.isChecked == holderEntity.isCustomTime){
                            if(txt_number_of_play_value?.text.toString().trim()[0].toString() ==
                                holderEntity.numberOfPlay.toString()){
                                if(txt_sender_name_value?.text.toString() == holderEntity.senderNames){
                                    if(txt_message_body_value?.text.toString() == holderEntity.messageBody){
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
       requireActivity().runOnUiThread {
               Toasty.success(requireActivity(), getString(R.string.application_save_success)).show()
               dismiss()
               requireActivity().finish()
       }
    }

    override fun onApplicationSaveError(message: String) {
     requireActivity().runOnUiThread {
         Toasty.error(requireActivity(), message).show()
     }
    }

    override fun onApplicationUpdateSuccess() {
       requireActivity().runOnUiThread {
           Toasty.success(requireActivity(), getString(R.string.update_successful)).show()
           if(!arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!){
               dismiss()
               requireActivity().finish()
           }else{
               dismiss()
           }
       }
    }

    override fun onApplicationUpdateError(message: String) {
      requireActivity().runOnUiThread {
          Toasty.error(requireActivity(), message).show()
      }
    }

    override fun onBitmapSaveSuccess(path: String) {
        addApplicationEntity.bitmapPath = path
        /**
         * End of other values
         */
        saveWithTimeConstrain()
        requireActivity().runOnUiThread {
            hideProgressBar()
        }
    }

    private fun saveWithTimeConstrain(){
        addApplicationEntity.tone_path = alarmTonePath
        //if start time and end time constrained
        if(switch_custom_time?.isChecked!!){
            if(isTimeConstrained(
                    txt_start_time_value?.text.toString(),
                    txt_end_time_value?.text.toString()
                )){
                addApplicationOptionPresenter?.saveApplication(addApplicationEntity)
            }else{
                hideProgressBar()
                requireActivity().runOnUiThread {
                    Toasty.info(requireActivity(), getString(R.string.time_constrain_error)).show()
                }
            }
        }else{
            addApplicationOptionPresenter?.saveApplication(addApplicationEntity)
        }
    }

    override fun onBitmapSaveError() {
        requireActivity().runOnUiThread {
            Toasty.error(requireActivity(), DataUtils.getString(R.string.something_wrong)).show()
            hideProgressBar()
        }
    }

    private fun setPresetValueToUi(app: ApplicationEntity){
        if(app.alarmRepeat == "Custom"){
            txt_repeat_value?.text = app.repeatDays
        }else{
            txt_repeat_value?.text = app.alarmRepeat
        }
        txt_ringtone_value?.text = app.ringTone
        switch_vibrate?.isChecked = app.isVibrateOnAlarm
        switch_custom_time?.isChecked = app.isCustomTime
        txt_start_time_value?.text = app.startTime
        txt_end_time_value?.text = app.endTime
        txt_number_of_play_value?.text = String.format("%d times", app.numberOfPlay)
        txt_sender_name_value?.text = app.senderNames
        txt_message_body_value?.text = app.messageBody
    }

    override fun onApplicationGetSuccess(app: ApplicationEntity) {
        //show edited value to
        addApplicationEntity = app
        convertToHolderEntity(addApplicationEntity)
        requireActivity().runOnUiThread {
            setPresetValueToUi(app)
        }
    }

    private fun convertToHolderEntity(app: ApplicationEntity){
        holderEntity.endTime = app.endTime
        holderEntity.startTime = app.startTime
        holderEntity.ringTone = app.ringTone
        holderEntity.numberOfPlay = app.numberOfPlay
        holderEntity.bitmapPath = app.bitmapPath
        holderEntity.messageBody = app.messageBody
        holderEntity.senderNames = app.senderNames
        holderEntity.isCustomTime = app.isCustomTime
        holderEntity.isVibrateOnAlarm = app.isVibrateOnAlarm
        holderEntity.tone_path = app.tone_path
        holderEntity.alarmRepeat = app.alarmRepeat
    }

    override fun onApplicationGetError(message: String) {
        defaultValuesToDataModel()
    }

    override fun onIllegalState() {
        defaultValuesToDataModel()
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setPresetValueToUi(defaultValuesToDataModel())
    }

}