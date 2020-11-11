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
import android.widget.*
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.utils.*
import com.app.messagealarm.utils.TimeUtils.Companion.isTimeConstrained
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_add_app_options.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


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

            if(switch_vibrate.isChecked){
                switch_just_vibrate.isChecked = false
            }

        }


        switch_just_vibrate?.setOnCheckedChangeListener{ buttonView, isChecked ->

            addApplicationEntity.isJustVibrate = isChecked

            if(isChecked){
                switch_vibrate.isChecked = false
            }
        }

        view_custom_time?.setOnClickListener {
            switch_custom_time?.performClick()
        }

        view_vibrate?.setOnClickListener {
            switch_vibrate?.performClick()
        }

        view_just_vibrate?.setOnClickListener {
            switch_just_vibrate?.performClick()
        }

        view_sender_name?.setOnClickListener {
            if(txt_sender_name_value?.text != "None"){
                val nameList = txt_sender_name_value?.text.toString().split(", ")
                senderNameDialog(nameList.toMutableList() as ArrayList<String>)
            }else{
                val list = ArrayList<String>()
                senderNameDialog(list)
            }
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
                            if(PermissionUtils.isAllowed(android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                                pickAudioFromStorage()
                                /**
                                 * set custom alarm tone type to data model
                                 */
                                addApplicationEntity.ringTone = "Default"
                            }else{
                                askForPermission()
                            }
                        } else {
                            txt_ringtone_value?.text = name
                            /**
                             * set default alarm tone type to data model
                             */
                            addApplicationEntity.ringTone = name
                            alarmTonePath = null
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


        btn_message_body_clear?.setOnClickListener {
            DialogUtils.showDialog(requireActivity(), getString(R.string.txt_clear_message_body),
                getString(R.string.txt_desc_clear_message), object : DialogUtils.Callback {
                    override fun onPositive() {
                        addApplicationEntity.messageBody = "None"
                        txt_message_body_value?.text = "None"
                        btn_message_body_clear?.visibility = View.GONE
                    }

                    override fun onNegative() {

                    }

                })
        }


        btn_sender_name_clear?.setOnClickListener {
            DialogUtils.showDialog(requireActivity(), getString(R.string.txt_clear_sender_name),
                getString(R.string.txt_desc_clear_sender_namne), object : DialogUtils.Callback {
                    override fun onPositive() {
                        addApplicationEntity.senderNames = "None"
                        txt_sender_name_value?.text = "None"
                        btn_sender_name_clear?.visibility = View.GONE
                    }

                    override fun onNegative() {

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

    private fun askForPermission() {
        PermissionUtils.requestPermission(
            this, android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        PermissionUtils.requestPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

   fun setToneName(name: String){
       if(name.length > 29){
           addApplicationEntity.ringTone = name.substring(0, 29)
       }else{
           addApplicationEntity.ringTone = name
       }

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


    private fun senderNameDialog(list: ArrayList<String>){
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_sender_name)
        //init views
        val cancelButton = dialog.findViewById<MaterialButton>(R.id.btn_cancel)
        val saveButton = dialog.findViewById<MaterialButton>(R.id.btn_save)
        val placeHolder = dialog.findViewById<ImageView>(R.id.img_placeholder)
        val etName = dialog.findViewById<EditText>(R.id.et_sender_name)
        val imageButton = dialog.findViewById<ImageButton>(R.id.btn_add)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recycler_view_sender_name)
        val layoutManager = FlexboxLayoutManager(requireActivity())
        val adapter = SenderNameAdapter(list, object : SenderNameAdapter.ItemClickListener {
            override fun onAllItemRemoved() {
                saveButton.isEnabled = false
                placeHolder.visibility = View.VISIBLE
                recyclerView.visibility = View.INVISIBLE
            }

        })


        //list not empty
        if(list.size != 0){
            recyclerView.visibility = View.VISIBLE
            placeHolder.visibility = View.INVISIBLE
            saveButton.isEnabled = true
        }

        layoutManager.flexDirection = FlexDirection.COLUMN
        layoutManager.justifyContent = JustifyContent.FLEX_START
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter



        imageButton.setOnClickListener {
            if(etName.text.toString().isNotEmpty()){
                adapter.addName(etName.text.toString())
                etName.setText("")
                saveButton.isEnabled = true
                placeHolder.visibility = View.INVISIBLE
                recyclerView.visibility = View.VISIBLE
                recyclerView.post { recyclerView.smoothScrollToPosition(adapter.itemCount - 1) }
            }else{
                Toasty.info(requireActivity(), "Name can't be empty!").show()
            }
        }

        saveButton.setOnClickListener {
           val name =  adapter.convertList()
            if (name.isNotEmpty()) {
                txt_sender_name_value?.text = name
                btn_sender_name_clear?.visibility = View.VISIBLE
                addApplicationEntity.senderNames = name
                dialog.dismiss()
            } else {
                btn_sender_name_clear?.visibility = View.GONE
                txt_sender_name_value?.text = "None"
                addApplicationEntity.senderNames = "None"
                dialog.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        val window: Window = dialog.window!!
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //
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
            val dfDate  = SimpleDateFormat("hh:mm a")
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
            val dfDate  = SimpleDateFormat("hh:mm a")
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
        addApplicationEntity.isJustVibrate = false
        addApplicationEntity.isCustomTime = false
        addApplicationEntity.numberOfPlay = 2
        addApplicationEntity.startTime = "6:00 AM"
        addApplicationEntity.endTime = "12:00 PM"
        addApplicationEntity.senderNames = "None"
        addApplicationEntity.messageBody = "None"
        addApplicationEntity.isRunningStatus = true

        //set this to holder object for checking default
        holderEntity.alarmRepeat = "Once"
        holderEntity.ringTone = "Default"
        holderEntity.isVibrateOnAlarm = false
        holderEntity.isJustVibrate = false
        holderEntity.isCustomTime = false
        holderEntity.numberOfPlay = 2
        holderEntity.startTime = "6:00 AM"
        holderEntity.endTime = "12:00 PM"
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
                        app.packageName,
                        bitmap.toBitmap()
                    )
                } catch (e: Exception) {
                    requireActivity().runOnUiThread {
                        hideProgressBar()
                        Toasty.error(requireActivity(), e.message!!).show()
                    }
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


    private fun checkForDefault():Boolean{
        var isDefault = false
        var repeat = ""
        repeat = if(holderEntity.alarmRepeat == "Custom"){
            holderEntity.repeatDays
        }else{
            holderEntity.alarmRepeat
        }
            if(txt_repeat_value?.text.toString().trim() == repeat){
                if(txt_ringtone_value?.text.toString().trim() == holderEntity.ringTone){
                    if(switch_vibrate?.isChecked == holderEntity.isVibrateOnAlarm){
                        if(switch_just_vibrate?.isChecked == holderEntity.isJustVibrate) {
                            if (switch_custom_time?.isChecked == holderEntity.isCustomTime) {
                                if (txt_number_of_play_value?.text.toString()
                                        .trim()[0].toString() ==
                                    holderEntity.numberOfPlay.toString()
                                ) {
                                    if (txt_sender_name_value?.text.toString() == holderEntity.senderNames) {
                                        if (txt_message_body_value?.text.toString() == holderEntity.messageBody) {
                                            isDefault = true
                                        }
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
                )
            ){
                addApplicationOptionPresenter?.saveApplication(addApplicationEntity)
            }else{
                requireActivity().runOnUiThread {
                    hideProgressBar()
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
        switch_just_vibrate?.isChecked = app.isJustVibrate
        txt_start_time_value?.text = app.startTime
        txt_end_time_value?.text = app.endTime
        txt_number_of_play_value?.text = String.format("%d times", app.numberOfPlay)
        txt_sender_name_value?.text = app.senderNames
        txt_message_body_value?.text = app.messageBody
    }

    override fun onApplicationGetSuccess(app: ApplicationEntity) {
        //show edited value to
        if(app.senderNames != "None"){
            btn_sender_name_clear?.visibility = View.VISIBLE
        }
        if(app.messageBody != "None"){
            btn_message_body_clear?.visibility = View.VISIBLE
        }
        addApplicationEntity = app
        alarmTonePath = app.tone_path
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
        holderEntity.isJustVibrate = app.isJustVibrate
        holderEntity.isVibrateOnAlarm = app.isVibrateOnAlarm
        holderEntity.tone_path = app.tone_path
        holderEntity.alarmRepeat = app.alarmRepeat
        holderEntity.repeatDays = app.repeatDays
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