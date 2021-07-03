package com.app.messagealarm.ui.main.add_options

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.model.Hint
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.service.AlarmServicePresenter
import com.app.messagealarm.ui.buy_pro.BuyProActivity
import com.app.messagealarm.ui.main.add_apps.AddApplicationActivity
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.utils.*
import com.app.messagealarm.utils.TimeUtils.Companion.isTimeConstrained
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_add_app_options.*
import xyz.aprildown.ultimateringtonepicker.RingtonePickerActivity
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddApplicationOption : BottomSheetDialogFragment(), AddApplicationOptionView {

    var shouldOnStatus = false
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    var once: Once? = null
    public var alarmTonePath: String? = null
    var ringtoneName: String? = null
    private var addApplicationEntity = ApplicationEntity()
    private var holderEntity = ApplicationEntity()
    private var addApplicationOptionPresenter: AddApplicationOptionPresenter? = null
    val REQUEST_CODE_PICK_AUDIO = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addApplicationOptionPresenter = AddApplicationOptionPresenter(this)
        once = Once()
        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics
    }


    private fun darkMode() {
        if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)) {
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
            img_start_time?.setImageResource(R.drawable.ic_start_time_white)
            img_end_time?.setImageResource(R.drawable.ic_end_time_white)
            img_exclude_sender_name?.setImageResource(R.drawable.ic_ignore_white)
            img_sound_level?.setImageResource(R.drawable.volume_white)
        } else {
            img_sound_level?.setImageResource(R.drawable.volume)
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
            img_start_time?.setImageResource(R.drawable.ic_start_time)
            img_end_time?.setImageResource(R.drawable.ic_end_time)
            img_exclude_sender_name?.setImageResource(R.drawable.ic_ignore)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_app_options, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setListener()
        handleEditAndViewMode()
        enableProMode()
        setPresetValueToUi(defaultValuesToDataModel())
        darkMode()
        //should show after at least 2 seconds and need to fix the not attached to activity crash
        /*Handler(Looper.getMainLooper()).postDelayed(Runnable {
            if(isAdded){
                HintUtils.showHintsToUser(requireActivity(), getOptionTutorialHintText(), R.layout.layout_target,"OPTIONS",
                    img_repeat, img_ringtone, img_vibrate, img_just_vibrate, img_custom_time,
                    img_number_of_play, img_sender_name, img_exclude_sender_name, img_message_body)
            }
        },500)*/
    }

    private fun getOptionTutorialHintText(): List<Hint> {
        val map = ArrayList<Hint>()
        map.add(Hint("Alarm Repeat", "Select how often the Alarm should repeat"))
        map.add(Hint("Alarm Tone", "Select the music for your Alarm"))
        map.add(Hint("Vibrate With Sound", "Vibrate phone with music on Alarm"))
        map.add(Hint("Just Vibrate, No Sound", "Vibrate phone without music on Alarm"))
        map.add(
            Hint(
                "Custom Time",
                "Select start time and end time for Alarm, Messages within the time range will play Alarm"
            )
        )
        map.add(
            Hint(
                "Number Of Play",
                "The number of time the music will play on Alarm, each session is 30 seconds long"
            )
        )
        map.add(
            Hint(
                "Add Sender Name",
                "Add sender names, only messages from those persons will play Alarm"
            )
        )
        map.add(
            Hint(
                "Exclude Sender Name",
                "Exclude sender names, Messages from this persons will be ignored for the Alarm"
            )
        )
        map.add(
            Hint(
                "Message Body",
                "If the message contains this texts only then the Alarm will be played"
            )
        )
        return map
    }

    private fun isProModeEnabled(): Boolean {
        return SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)
    }

    private fun enableProMode() {
        if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)) {
            switch_just_vibrate?.isEnabled = true
            switch_vibrate?.isEnabled = true
            progress_sound_level?.isEnabled = true
            txt_pro_just_vibrate?.visibility = View.GONE
            txt_pro_vibrate?.visibility = View.GONE
            txt_pro_sound_level?.visibility = View.GONE
        } else {
            switch_vibrate?.isChecked = false
            switch_vibrate?.isEnabled = false
            progress_sound_level?.isEnabled = false
            switch_just_vibrate?.isChecked = false
            switch_just_vibrate?.isEnabled = false
            txt_pro_sound_level?.visibility = View.VISIBLE
            txt_pro_just_vibrate?.visibility = View.VISIBLE
            txt_pro_vibrate?.visibility = View.VISIBLE
        }
    }

    private fun handleEditAndViewMode() {
        try {
            defaultValuesToDataModel()
            if (!arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
                if (arguments?.getSerializable(Constants.BundleKeys.APP) != null) {
                    addApplicationOptionPresenter?.getAppByPackageName(
                        (arguments?.getSerializable(Constants.BundleKeys.APP) as InstalledApps).packageName
                    )
                }
            } else {
                //edit mode from home
                if (arguments?.getString(Constants.BundleKeys.PACKAGE_NAME) != null) {
                    addApplicationOptionPresenter?.getAppByPackageName(
                        arguments?.getString(
                            Constants.BundleKeys.PACKAGE_NAME
                        )!!
                    )
                }
            }
        } catch (e: NullPointerException) {
            //skip the crash
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

    private fun visitProScreen() {
        if (activity is AlarmApplicationActivity) {
            dismissAllowingStateLoss()
            val intent = Intent(activity, BuyProActivity::class.java)
            requireActivity().startActivityForResult(
                intent,
                Constants.ACTION.ACTION_PURCHASE_FROM_MAIN
            )
        } else if (activity is AddApplicationActivity) {
            dismissAllowingStateLoss()
            requireActivity().startActivityForResult(
                Intent(
                    requireActivity(),
                    BuyProActivity::class.java
                ),
                Constants.ACTION.ACTION_PURCHASE_FROM_ADD
            )
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

    public fun pickAudioFromStorage() {
        try {
            if (isAdded) {
                val settings = UltimateRingtonePicker.Settings(
                    systemRingtonePicker = UltimateRingtonePicker.SystemRingtonePicker(
                        customSection = UltimateRingtonePicker.SystemRingtonePicker.CustomSection(),
                        defaultSection = UltimateRingtonePicker.SystemRingtonePicker.DefaultSection(
                            showSilent = false,
                            defaultTitle = "Default Ringtone"
                        ),
                        ringtoneTypes = listOf(
                        )
                    ),
                    deviceRingtonePicker = UltimateRingtonePicker.DeviceRingtonePicker(
                        deviceRingtoneTypes = listOf(
                            UltimateRingtonePicker.RingtoneCategoryType.All
                        ),
                        false
                    )
                )
                /*   val intent =
                       Intent(
                           Intent.ACTION_PICK,
                           MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                       )
                       requireActivity().startActivityForResult(intent, REQUEST_CODE_PICK_AUDIO)*/
                requireActivity().startActivityForResult(
                    RingtonePickerActivity.getIntent(
                        context = requireActivity(),
                        settings = settings,
                        windowTitle = "Alarm Tone"
                    ),
                    REQUEST_CODE_PICK_AUDIO
                )
            }
        } catch (e: Exception) {

        }
    }

    private fun setListener() {
        btn_close?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
                if (checkForDefault()) {
                    dismissAllowingStateLoss()
                } else {
                    showDiscardDialog()
                }
            }
        }


        btn_save?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
                try {
                    if (checkForDefault()) {
                        shouldOnStatus = false
                    } else {
                        //save application and turn switch on
                        addApplicationEntity.isRunningStatus = true
                        shouldOnStatus = true
                    }
                    var packageName = ""

                    /**
                     * this is an special case where sender name is used but ignore names are not
                     */
                    var senderName = ""
                    if (!arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
                        val app =
                            arguments?.getSerializable(Constants.BundleKeys.APP) as InstalledApps
                        packageName = app.packageName
                        senderName = addApplicationEntity.senderNames
                    } else {
                        if (holderEntity.packageName != null) {
                            packageName = holderEntity.packageName
                        }
                        senderName = holderEntity.senderNames
                    }
                    /**
                     * When in edit mode, and cleaning the name. then need to clean the holder entity. the bug is in edit mode
                     */
                    if (packageName == Constants.APP.IMO_PACKAGE) {
                        if (senderName == "None") {
                            Toasty.info(
                                requireActivity(),
                                "IMO can send notification without real message," +
                                        " please add at least one sender name!"
                            ).show()
                        } else {
                            saveApplication()
                        }
                    } else {
                        saveApplication()
                    }
                } catch (e: NullPointerException) {
                    //skip the crash
                }

            }

        }

        switch_custom_time?.setOnCheckedChangeListener { buttonView, isChecked ->
            /**
             * set is custom time to data model
             */
            if (!BaseApplication.isHintShowing) {
                addApplicationEntity.isCustomTime = isChecked
                if (isChecked) {
                    visibleCustomTimeLayout()
                } else {
                    hideCustomTimeLayout()
                }
            }
        }

        switch_vibrate?.setOnCheckedChangeListener { buttonView, isChecked ->
            /**
             * set vibrate option to data model
             */
            if (!BaseApplication.isHintShowing) {
                addApplicationEntity.isVibrateOnAlarm = isChecked
                if (switch_vibrate.isChecked) {
                    switch_just_vibrate.isChecked = false
                    progress_sound_level.isEnabled = true
                    progress_sound_level.progress = 100
                }
            }
        }


        switch_just_vibrate?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!BaseApplication.isHintShowing) {
                addApplicationEntity.isJustVibrate = isChecked
                if (isChecked) {
                    progress_sound_level.isEnabled = false
                    progress_sound_level.progress = 0
                    switch_vibrate.isChecked = false
                }else{
                    progress_sound_level.isEnabled = true
                    progress_sound_level.progress = 100
                }
            }
        }

        view_custom_time?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
                switch_custom_time?.performClick()
            }
        }

        view_vibrate?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
                if (isProModeEnabled()) {
                    switch_vibrate?.performClick()
                } else {
                    //trigger pro screen
                    visitProScreen()
                }
            }
        }

        /**
         * Sound level seekbar
         */
        progress_sound_level?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                addApplicationEntity.soundLevel = progress
                txt_percent_sound_level?.text = "${progress}%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        view_sound_level?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
                if (!isProModeEnabled()) {
                    //trigger pro screen
                    visitProScreen()
                }
            }
        }

        view_just_vibrate?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
                if (isProModeEnabled()) {
                    switch_just_vibrate?.performClick()
                } else {
                    //trigger pro screen
                    visitProScreen()
                }

            }
        }

        view_sender_name?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
                if (txt_exclude_sender_name_value?.text == "None") {
                    if (txt_sender_name_value?.text != "None") {
                        val nameList = txt_sender_name_value?.text.toString().split(", ")
                        senderNameDialog(nameList.toMutableList() as ArrayList<String>)
                    } else {
                        val list = ArrayList<String>()
                        senderNameDialog(list)
                    }
                } else {
                    Toasty.info(
                        requireActivity(),
                        "First clear the Ignored Sender Name, both can't be used!"
                    ).show()
                }
            }
        }

        /**
         * exclude sender name function
         */

        view_exclude_sender_name?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
                if (txt_sender_name_value?.text == "None") {
                    if (txt_exclude_sender_name_value?.text != "None") {
                        val nameList = txt_exclude_sender_name_value?.text.toString().split(", ")
                        excludeSenderNameDialog(nameList.toMutableList() as ArrayList<String>)
                    } else {
                        val list = ArrayList<String>()
                        excludeSenderNameDialog(list)
                    }
                } else {
                    Toasty.info(
                        requireActivity(),
                        "First clear the Sender Name, both can't be used!"
                    ).show()
                }

            }
        }


        view_message_body?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
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
        }

        view_ringtone?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
                DialogUtils.showRingToneSelectDialog(
                    requireActivity(),
                    object : DialogUtils.RepeatCallBack {
                        override fun onClick(name: String) {
                            if (name.contains("Select a song")) {
                                if (PermissionUtils.isAllowed(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                    pickAudioFromStorage()
                                    /**
                                     * set custom alarm tone type to data model
                                     */
                                    addApplicationEntity.ringTone = "Default"
                                } else {
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

        }

        view_start_time?.setOnClickListener {
            val c: Calendar = startTimeCalender()
            val hour: Int = c.get(Calendar.HOUR_OF_DAY)
            val minute: Int = c.get(Calendar.MINUTE)
            val timePickerDialog =
                TimePickerDialog(
                    context,
                    { view, hourOfDay, min ->
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
                    { view, hourOfDay, _min ->
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
            if (!BaseApplication.isHintShowing) {
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
                        if (arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
                            holderEntity.senderNames = "None"

                        }
                        addApplicationEntity.senderNames = "None"
                        txt_sender_name_value?.text = "None"
                        btn_sender_name_clear?.visibility = View.GONE
                    }

                    override fun onNegative() {

                    }

                })
        }

        btn_exclude_sender_name_clear?.setOnClickListener {
            DialogUtils.showDialog(requireActivity(), getString(R.string.txt_clear_ignore_name),
                getString(R.string.txt_desc_clear_ignored_namne), object : DialogUtils.Callback {
                    override fun onPositive() {
                        if (arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
                            holderEntity.ignored_names = "None"

                        }
                        addApplicationEntity.ignored_names = "None"
                        txt_exclude_sender_name_value?.text = "None"
                        btn_exclude_sender_name_clear?.visibility = View.GONE
                    }

                    override fun onNegative() {

                    }

                })
        }


        view_repeat_bg?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
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
                                            if (list.isEmpty()) {
                                                Toasty.info(
                                                    requireActivity(),
                                                    "No day selected, Always set as default!"
                                                ).show()
                                                txt_repeat_value?.text = "Always"
                                                addApplicationEntity.alarmRepeat = "Always"
                                            } else {
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
                                                addApplicationEntity.repeatDays =
                                                    selectedDays.substring(
                                                        0,
                                                        selectedDays.length - 2
                                                    )
                                            }
                                        }
                                    },
                                    object : DialogUtils.Callback {
                                        override fun onPositive() {

                                        }

                                        override fun onNegative() {
                                            if (addApplicationEntity.repeatDays == null) {
                                                txt_repeat_value?.text = "Always"
                                                addApplicationEntity.alarmRepeat = "Always"
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    })
            }
        }
    }

    fun askForPermission() {
        PermissionUtils.requestPermission(
            requireActivity(),
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    fun setToneName(name: String) {
        if (name.length > 29) {
            addApplicationEntity.ringTone = name.substring(0, 29)
        } else {
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
        Objects.requireNonNull(dialog.window!!)
            .setBackgroundDrawableResource(android.R.color.transparent)
        txtCancel.setOnClickListener {
            if (dialog.isShowing) {
                once = Once()
                dialog.cancel()
            }
        }
        txtDiscard.setOnClickListener {
            dismissAllowingStateLoss()
            if (dialog.isShowing) {
                dialog.cancel()
            }
        }
        val window = dialog.window
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val wlp = window.attributes;
        wlp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window.attributes = wlp
        if (!dialog.isShowing) {
            dialog.show()
        }
    }


    /**
     * @param List of String
     * This function shows the sender name dialog
     */
    private fun senderNameDialog(list: ArrayList<String>) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_sender_name)
        //init views
        val cancelButton = dialog.findViewById<MaterialButton>(R.id.btn_cancel)
        val saveButton = dialog.findViewById<MaterialButton>(R.id.btn_save)
        val placeHolder = dialog.findViewById<ImageView>(R.id.img_placeholder)
        val etName = dialog.findViewById<EditText>(R.id.et_sender_name)
        val txtHint = dialog.findViewById<TextView>(R.id.txt_hint_sender_name)
        /**
         * show app name at end of hint and make app name green color
         */
        try {
            if (arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
               val text =
                    String.format("Hint: Please add username name from %s", holderEntity.appName)
                val spannable: Spannable = SpannableString(text)
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(requireActivity(), R.color.success_color)),
                    35,
                    text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                txtHint.setText(spannable, TextView.BufferType.SPANNABLE)
            } else {
                val app = arguments?.getSerializable(Constants.BundleKeys.APP) as InstalledApps
                val text =
                    String.format("Hint: Please add username name from %s", app.appName)
                val spannable: Spannable = SpannableString(text)
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(requireActivity(), R.color.success_color)),
                    35,
                    text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                txtHint.setText(spannable, TextView.BufferType.SPANNABLE)
            }
        } catch (e: java.lang.NullPointerException) {

        }
        val imageButton = dialog.findViewById<TextView>(R.id.btn_add)
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
        if (list.size != 0) {
            recyclerView.visibility = View.VISIBLE
            placeHolder.visibility = View.INVISIBLE
            saveButton.isEnabled = true
        }
        layoutManager.flexDirection = FlexDirection.COLUMN
        layoutManager.justifyContent = JustifyContent.FLEX_START
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        imageButton.setOnClickListener {
            if (etName.text.toString().isNotEmpty()) {
                adapter.addName(
                    etName.text.toString().trim(),
                    txt_exclude_sender_name_value?.text.toString()
                )
                etName.setText("")
                saveButton.isEnabled = true
                placeHolder.visibility = View.INVISIBLE
                recyclerView.visibility = View.VISIBLE
                if (adapter.itemCount > 0) {
                    recyclerView.post { recyclerView.smoothScrollToPosition(adapter.itemCount - 1) }
                }
            } else {
                Toasty.info(requireActivity(), "Name can't be empty!").show()
            }
        }

        saveButton.setOnClickListener {
            val name = adapter.convertList()
            if (name.isNotEmpty()) {
                txt_sender_name_value?.text = name
                btn_sender_name_clear?.visibility = View.VISIBLE
                addApplicationEntity.senderNames = name
                if (arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
                    holderEntity.senderNames = name
                }
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
        if (!dialog.isShowing) {
            dialog.show()
        }
    }


    /**
     * @param List of String
     * This function shows the exclude sender name dialog
     */
    private fun excludeSenderNameDialog(list: ArrayList<String>) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_exclude_sender_name)
        //init views
        val cancelButton = dialog.findViewById<MaterialButton>(R.id.btn_cancel)
        val saveButton = dialog.findViewById<MaterialButton>(R.id.btn_save)
        val placeHolder = dialog.findViewById<ImageView>(R.id.img_placeholder)
        val etName = dialog.findViewById<EditText>(R.id.et_sender_name)
        val imageButton = dialog.findViewById<TextView>(R.id.btn_add)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recycler_view_sender_name)
        val layoutManager = FlexboxLayoutManager(requireActivity())
        val txtHint = dialog.findViewById<TextView>(R.id.txt_hint_sender_name)
        /**
         * show app name at end of hint and make app name green color
         */
        try {
            if (arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
                val text =
                    String.format("Hint: Please add username name from %s", holderEntity.appName)
                val spannable: Spannable = SpannableString(text)
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(requireActivity(), R.color.success_color)),
                    35,
                    text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                txtHint.setText(spannable, TextView.BufferType.SPANNABLE)
            } else {
                val app = arguments?.getSerializable(Constants.BundleKeys.APP) as InstalledApps
                val text =
                    String.format("Hint: Please add username name from %s", app.appName)
                val spannable: Spannable = SpannableString(text)
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(requireActivity(), R.color.success_color)),
                    35,
                    text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                txtHint.setText(spannable, TextView.BufferType.SPANNABLE)
            }
        } catch (e: java.lang.NullPointerException) {

        }
        val adapter = SenderNameAdapter(list, object : SenderNameAdapter.ItemClickListener {
            override fun onAllItemRemoved() {
                saveButton.isEnabled = false
                placeHolder.visibility = View.VISIBLE
                recyclerView.visibility = View.INVISIBLE
            }
        })
        //list not empty
        if (list.size != 0) {
            recyclerView.visibility = View.VISIBLE
            placeHolder.visibility = View.INVISIBLE
            saveButton.isEnabled = true
        }
        layoutManager.flexDirection = FlexDirection.COLUMN
        layoutManager.justifyContent = JustifyContent.FLEX_START
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        imageButton.setOnClickListener {
            if (etName.text.toString().isNotEmpty()) {
                adapter.addName(etName.text.toString(), txt_sender_name_value?.text.toString())
                etName.setText("")
                saveButton.isEnabled = true
                placeHolder.visibility = View.INVISIBLE
                recyclerView.visibility = View.VISIBLE
                if (adapter.itemCount > 0) {
                    recyclerView.post { recyclerView.smoothScrollToPosition(adapter.itemCount - 1) }
                }
            } else {
                Toasty.info(requireActivity(), "Name can't be empty!").show()
            }
        }

        saveButton.setOnClickListener {
            val name = adapter.convertList()
            if (name.isNotEmpty()) {
                txt_exclude_sender_name_value?.text = name
                btn_exclude_sender_name_clear?.visibility = View.VISIBLE
                addApplicationEntity.ignored_names = name
                if (arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
                    holderEntity.ignored_names = name
                }
                dialog.dismiss()
            } else {
                btn_exclude_sender_name_clear?.visibility = View.GONE
                txt_exclude_sender_name_value?.text = "None"
                addApplicationEntity.ignored_names = "None"
                dialog.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        val window: Window = dialog.window!!
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun startTimeCalender(): Calendar {
        /**
         * User will be doing in 12 hours
         * Background works will be in 24 hours
         */
        return try {
            val dfDate = SimpleDateFormat("hh:mm a")
            val cal = Calendar.getInstance()
            cal.time = dfDate.parse(txt_start_time_value?.text.toString())!!
            cal
        } catch (ex: ParseException) {
            return Calendar.getInstance()
        }

    }

    @SuppressLint("SimpleDateFormat")
    fun endTimeCalender(): Calendar {
        return try {
            val dfDate = SimpleDateFormat("hh:mm a")
            val cal = Calendar.getInstance()
            cal.time = dfDate.parse(txt_end_time_value?.text.toString())!!
            cal
        } catch (ex: ParseException) {
            return Calendar.getInstance()
        }
    }


    private fun defaultValuesToDataModel(): ApplicationEntity {
        addApplicationEntity.alarmRepeat = "Always"
        addApplicationEntity.ringTone = "Default"
        addApplicationEntity.isVibrateOnAlarm = false
        addApplicationEntity.isJustVibrate = false
        addApplicationEntity.isCustomTime = false
        addApplicationEntity.numberOfPlay = 2
        addApplicationEntity.startTime = "6:00 AM"
        addApplicationEntity.endTime = "12:00 PM"
        addApplicationEntity.senderNames = "None"
        addApplicationEntity.ignored_names = "None"
        addApplicationEntity.messageBody = "None"
        addApplicationEntity.isRunningStatus = true
        /**
         * if user from BD then sound level 100 for free
         * else 70 for free
         */
        if(SharedPrefUtils.contains(Constants.PreferenceKeys.COUNTRY_CODE)){
            if(SharedPrefUtils.readString(Constants.PreferenceKeys.COUNTRY_CODE) == "BD"){
                addApplicationEntity.soundLevel = 100
                holderEntity.soundLevel = 100

            }else{
                holderEntity.soundLevel = 70
                addApplicationEntity.soundLevel = 70
            }
        }else{
            holderEntity.soundLevel = 70
            addApplicationEntity.soundLevel = 70
        }
        //set this to holder object for checking default
        holderEntity.alarmRepeat = "Always"
        holderEntity.ringTone = "Default"
        holderEntity.isVibrateOnAlarm = false
        holderEntity.isJustVibrate = false
        holderEntity.isCustomTime = false
        holderEntity.numberOfPlay = 2
        holderEntity.startTime = "6:00 AM"
        holderEntity.endTime = "12:00 PM"
        holderEntity.senderNames = "None"
        holderEntity.ignored_names = "None"
        holderEntity.messageBody = "None"
        holderEntity.isRunningStatus = true

        return addApplicationEntity
    }

    private fun saveApplication() {
        /**
         * Populate Application entity from UI controller data
         * with start of other values
         */
        //start progress bar
        showProgressBar()
        try {
            if (!arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
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
                        /**
                         * Check for latest version
                         */
                        addApplicationOptionPresenter?.checkForLatestUpdate()

                        /**
                         * Check for unknown app
                         */
                        addApplicationOptionPresenter?.checkForUnknownApp(
                            requireContext(),
                            addApplicationEntity.appName,
                            addApplicationEntity.packageName
                        )
                    } catch (e: Exception) {
                       e.printStackTrace()
                    }
                }).start()
            } else {
                saveWithTimeConstrain()
            }
        } catch (e: NullPointerException) {
            //skip the crash
        }

    }

    private fun showProgressBar() {
        progress_bar_option?.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progress_bar_option?.visibility = View.INVISIBLE
    }


    private fun checkForDefault(): Boolean {
        var isDefault = false
        var repeat = ""
        repeat = if (holderEntity.alarmRepeat == "Custom") {
            holderEntity.repeatDays
        } else {
            holderEntity.alarmRepeat
        }

        if (txt_repeat_value?.text.toString().trim() == repeat) {
            if (txt_ringtone_value?.text.toString().trim() == holderEntity.ringTone) {
                if (switch_vibrate?.isChecked == holderEntity.isVibrateOnAlarm) {
                    if (switch_just_vibrate?.isChecked == holderEntity.isJustVibrate) {
                        if (switch_custom_time?.isChecked == holderEntity.isCustomTime) {
                            if (txt_number_of_play_value?.text.toString().split(" ")
                                        [0].trim() ==
                                holderEntity.numberOfPlay.toString()
                            ) {
                                if (txt_sender_name_value?.text.toString() == holderEntity.senderNames) {
                                    if (txt_exclude_sender_name_value?.text.toString() == holderEntity.ignored_names) {
                                        if (txt_message_body_value?.text.toString() == holderEntity.messageBody) {
                                            if(progress_sound_level?.progress == holderEntity.soundLevel){
                                                isDefault = true
                                            }
                                        }
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
        if (isAdded) {
            requireActivity().runOnUiThread {
                Toasty.success(requireActivity(), getString(R.string.application_save_success))
                    .show()
                dismissAllowingStateLoss()
                requireActivity().finish()
            }
        }
    }

    override fun onApplicationSaveError(message: String) {
        if (isAdded) {
            requireActivity().runOnUiThread {
                Toasty.error(requireActivity(), message).show()
            }
        }
    }

    override fun onApplicationUpdateSuccess() {
        if (isAdded) {
            requireActivity().runOnUiThread {
                Toasty.success(requireActivity(), getString(R.string.update_successful)).show()
                if (!arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
                    if (shouldOnStatus) {
                        AlarmServicePresenter.updateAppStatus(true, holderEntity.id)
                    }
                    dismissAllowingStateLoss()
                    requireActivity().finish()
                } else {
                    if (shouldOnStatus) {
                        AlarmServicePresenter.updateAppStatus(true, holderEntity.id)
                        //notify adapter
                        (activity as AlarmApplicationActivity).notifyCurrentAdapter()
                    }
                    dismissAllowingStateLoss()
                }
            }
        }
    }

    override fun onApplicationUpdateError(message: String) {
        if (isAdded) {
            requireActivity().runOnUiThread {
                Toasty.error(requireActivity(), message).show()
            }
        }
    }

    override fun onBitmapSaveSuccess(path: String) {
        addApplicationEntity.bitmapPath = path
        /**
         * End of other values
         */
        saveWithTimeConstrain()
        if (isAdded) {
            requireActivity().runOnUiThread {
                hideProgressBar()
            }
        }
    }

    private fun saveWithTimeConstrain() {
        addApplicationEntity.tone_path = alarmTonePath
        //if start time and end time constrained
        if (switch_custom_time?.isChecked!!) {
            if (isTimeConstrained(
                    txt_start_time_value?.text.toString(),
                    txt_end_time_value?.text.toString()
                )
            ) {
                addApplicationOptionPresenter?.saveApplication(
                    addApplicationEntity,
                    firebaseAnalytics
                )
            } else {
                requireActivity().runOnUiThread {
                    hideProgressBar()
                    Toasty.info(requireActivity(), getString(R.string.time_constrain_error)).show()
                }
            }
        } else {
            addApplicationOptionPresenter?.saveApplication(addApplicationEntity, firebaseAnalytics)
        }
    }

    override fun onBitmapSaveError() {
        if (isAdded) {
            requireActivity().runOnUiThread {
                Toasty.error(requireActivity(), DataUtils.getString(R.string.something_wrong))
                    .show()
                hideProgressBar()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setPresetValueToUi(app: ApplicationEntity) {
        if (app.alarmRepeat == "Custom") {
            txt_repeat_value?.text = app.repeatDays
        } else {
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
        txt_exclude_sender_name_value?.text = app.ignored_names
        txt_message_body_value?.text = app.messageBody
        //new added
        progress_sound_level?.progress = app.soundLevel
        txt_percent_sound_level?.text = "${app.soundLevel}%"
    }

    override fun onApplicationGetSuccess(app: ApplicationEntity) {
        //show edited value to
        if (app.senderNames != "None") {
            btn_sender_name_clear?.visibility = View.VISIBLE
        }
        if (app.messageBody != "None") {
            btn_message_body_clear?.visibility = View.VISIBLE
        }
        if (app.ignored_names != "None") {
            btn_exclude_sender_name_clear?.visibility = View.VISIBLE
        }
        addApplicationEntity = app
        alarmTonePath = app.tone_path
        convertToHolderEntity(addApplicationEntity)
        if (isAdded) {
            requireActivity().runOnUiThread {
                setPresetValueToUi(app)
            }
        }
    }

    private fun convertToHolderEntity(app: ApplicationEntity) {
        holderEntity.appName = app.appName
        holderEntity.packageName = app.packageName
        holderEntity.endTime = app.endTime
        holderEntity.startTime = app.startTime
        holderEntity.ringTone = app.ringTone
        holderEntity.numberOfPlay = app.numberOfPlay
        holderEntity.bitmapPath = app.bitmapPath
        holderEntity.messageBody = app.messageBody
        holderEntity.senderNames = app.senderNames
        holderEntity.ignored_names = app.ignored_names
        holderEntity.isCustomTime = app.isCustomTime
        holderEntity.isJustVibrate = app.isJustVibrate
        holderEntity.isVibrateOnAlarm = app.isVibrateOnAlarm
        holderEntity.tone_path = app.tone_path
        holderEntity.alarmRepeat = app.alarmRepeat
        holderEntity.repeatDays = app.repeatDays
        holderEntity.soundLevel = app.soundLevel
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