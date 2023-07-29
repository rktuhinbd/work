package com.app.messagealarm.ui.main.configure_options.add_options_alarm

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.Hint
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.model.entity.ApplicationEntityNew
import com.app.messagealarm.model.repository.ApplicationRepository
import com.app.messagealarm.service.AlarmServicePresenter
import com.app.messagealarm.ui.buy_pro.BuyProActivity
import com.app.messagealarm.ui.main.add_apps.AddApplicationActivity
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.ui.main.configure_options.adapter.SenderNameAdapter
import com.app.messagealarm.ui.main.configure_options.presenter.OptionPresenter
import com.app.messagealarm.ui.main.configure_options.view.OptionView
import com.app.messagealarm.ui.main.configure_options.viewmodel.OptionViewModel
import com.app.messagealarm.ui.main.configure_options.viewmodel.OptionViewModelFactory
import com.app.messagealarm.utils.*
import com.app.messagealarm.utils.TimeUtils.Companion.isTimeConstrained
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_alarm_options.*
import kotlinx.android.synthetic.main.dialog_alarm_options.btn_close
import kotlinx.android.synthetic.main.dialog_alarm_options.btn_exclude_sender_name_clear
import kotlinx.android.synthetic.main.dialog_alarm_options.btn_message_body_clear
import kotlinx.android.synthetic.main.dialog_alarm_options.btn_save
import kotlinx.android.synthetic.main.dialog_alarm_options.btn_sender_name_clear
import kotlinx.android.synthetic.main.dialog_alarm_options.img_custom_time
import kotlinx.android.synthetic.main.dialog_alarm_options.img_exclude_sender_name
import kotlinx.android.synthetic.main.dialog_alarm_options.img_message_body
import kotlinx.android.synthetic.main.dialog_alarm_options.img_number_of_play
import kotlinx.android.synthetic.main.dialog_alarm_options.img_sender_name
import kotlinx.android.synthetic.main.dialog_alarm_options.img_sound_level
import kotlinx.android.synthetic.main.dialog_alarm_options.img_vibrate
import kotlinx.android.synthetic.main.dialog_alarm_options.progress_bar_option
import kotlinx.android.synthetic.main.dialog_alarm_options.progress_sound_level
import kotlinx.android.synthetic.main.dialog_alarm_options.switch_custom_time
import kotlinx.android.synthetic.main.dialog_alarm_options.switch_vibrate
import kotlinx.android.synthetic.main.dialog_alarm_options.txt_exclude_sender_name_value
import kotlinx.android.synthetic.main.dialog_alarm_options.txt_message_body_value
import kotlinx.android.synthetic.main.dialog_alarm_options.txt_number_of_play_value
import kotlinx.android.synthetic.main.dialog_alarm_options.txt_percent_sound_level
import kotlinx.android.synthetic.main.dialog_alarm_options.txt_pro_vibrate
import kotlinx.android.synthetic.main.dialog_alarm_options.txt_sender_name_value
import kotlinx.android.synthetic.main.dialog_alarm_options.view_custom_time
import kotlinx.android.synthetic.main.dialog_alarm_options.view_exclude_sender_name
import kotlinx.android.synthetic.main.dialog_alarm_options.view_message_body
import kotlinx.android.synthetic.main.dialog_alarm_options.view_number_of_play
import kotlinx.android.synthetic.main.dialog_alarm_options.view_sender_name
import kotlinx.android.synthetic.main.dialog_alarm_options.view_sound_level
import kotlinx.android.synthetic.main.dialog_alarm_options.view_vibrate
import kotlinx.android.synthetic.main.dialog_speak_options.*
import xyz.aprildown.ultimateringtonepicker.RingtonePickerActivity
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker
import java.text.SimpleDateFormat
import java.util.*


class AlarmOptionDialog : BottomSheetDialogFragment(), OptionView {

    var shouldOnStatus = false
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    var once: Once? = null
    public var alarmTonePath: String? = null
    var appName: String? = ""
    var ringtoneName: String? = null
    private var addApplicationEntity = ApplicationEntity()
    private var addApplicationEntityNew = ApplicationEntityNew()
    private var holderEntity = ApplicationEntity()
    private var holderEntityNew = ApplicationEntity()

    private lateinit var applicationRepository: ApplicationRepository
    private lateinit var viewModel: OptionViewModel

    private var optionPresenter: OptionPresenter? = null
    val REQUEST_CODE_PICK_AUDIO = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
        optionPresenter = OptionPresenter(this)
        once = Once()
        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics
    }


    /**
     * Flash light option added on version 2.0.4
     */
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
            img_exclude_sender_name?.setImageResource(R.drawable.ic_ignore_white)
            img_sound_level?.setImageResource(R.drawable.volume_white)
            img_flash?.setImageResource(R.drawable.flashlight_white)
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
            img_exclude_sender_name?.setImageResource(R.drawable.ic_ignore)
            img_flash?.setImageResource(R.drawable.flashlight)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_alarm_options, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setListener()
        handleEditAndViewMode()
        enableProMode()
//        setPresetValueToUi(defaultValuesToDataModel())
        setPresetValueToUiNew(defaultValuesToDataModelNew())
        darkMode()
        //should show after at least 2 seconds and need to fix the not attached to activity crash
        /*  Handler(Looper.getMainLooper()).postDelayed(Runnable {
              if(isAdded){
                  HintUtils.showHintsToUser(requireActivity(), getOptionTutorialHintText(), R.layout.layout_target,"OPTIONS",
                       img_sender_name, img_exclude_sender_name, img_message_body)
              }
          },500)*/

        applicationRepository =
            ApplicationRepository(AppDatabase.getInstance(requireActivity()).applicationDaoNew())
        viewModel = ViewModelProvider(this, OptionViewModelFactory(applicationRepository)).get(
            OptionViewModel::class.java
        )

        initObserver()

    }

    private fun initObserver() {

        viewModel.application.observe(this) { applications ->
            // Update the cached copy of the words in the adapter.
            applications?.let {
                // Update your UI here

                onApplicationGetSuccessNew(it)

                Log.d(
                    "AlarmDialog",
                    "setObserver: ${GsonBuilder().setPrettyPrinting().create().toJson(it)}"
                )
            }
        }
    }

    /**
     * probably in next update insa-Allah
     */
    private fun getOptionTutorialHintText(): List<Hint> {
        val map = ArrayList<Hint>()
        /*map.add(Hint("Alarm Repeat", "Select how often the Alarm should repeat"))
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
        )*/
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

    /**
     * Note: On 22 aug MK changed the just vibrate code,
     *as Mk planned to make just vibrate a free feature
     */
    /**
     * Flashlight added on 2.0.4
     */
    private fun enableProMode() {
        if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)) {
            // switch_just_vibrate?.isEnabled = true
            switch_vibrate?.isEnabled = true
            switch_flash?.isEnabled = true
            progress_sound_level?.isEnabled = true
            //txt_pro_just_vibrate?.visibility = View.GONE
            txt_pro_vibrate?.visibility = View.GONE
            txt_pro_flash?.visibility = View.GONE
            txt_pro_sound_level?.visibility = View.GONE
        } else {
            switch_vibrate?.isChecked = false
            switch_flash?.isEnabled = false
            switch_vibrate?.isEnabled = false
            progress_sound_level?.isEnabled = false
            // switch_just_vibrate?.isChecked = false
            //switch_just_vibrate?.isEnabled = false
            txt_pro_sound_level?.visibility = View.VISIBLE
            txt_pro_flash?.visibility = View.VISIBLE
            // txt_pro_just_vibrate?.visibility = View.VISIBLE
            txt_pro_vibrate?.visibility = View.VISIBLE
        }
    }

    private fun handleEditAndViewMode() {
        try {
            defaultValuesToDataModel()
            defaultValuesToDataModelNew()
            if (!arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
                if (arguments?.getSerializable(Constants.BundleKeys.APP) != null) {
                    val app =
                        (arguments?.getSerializable(Constants.BundleKeys.APP) as InstalledApps)
                    optionPresenter?.getAppByPackageNameAndType(
                        app.packageName,
                        Constants.NotifyOptions.ALARM
                    )
                    this.appName = app.appName
                }
            } else {
                //edit mode from home
                if (arguments?.getString(Constants.BundleKeys.PACKAGE_NAME) != null) {
                    optionPresenter?.getAppByPackageNameAndType(
                        arguments?.getString(
                            Constants.BundleKeys.PACKAGE_NAME
                        )!!, Constants.NotifyOptions.ALARM
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
                            RingtoneManager.TYPE_RINGTONE
                        )
                    ),
                    deviceRingtonePicker = UltimateRingtonePicker.DeviceRingtonePicker(
                        deviceRingtoneTypes = listOf(
                            UltimateRingtonePicker.RingtoneCategoryType.All
                        ),
                        false
                    )
                )
                requireActivity().startActivityForResult(
                    RingtonePickerActivity.getIntent(
                        SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE),
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
                    addApplicationEntityNew.isAlarmEnabled = true
                    addApplicationEntity.alertType = Constants.NotifyOptions.ALARM
                    if (checkForDefault()) {
                        shouldOnStatus = false
                    } else {
                        //save application and turn switch on
                        addApplicationEntityNew.isRunningStatus = true
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
                        senderName = addApplicationEntityNew.senderNames ?: ""
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
            if (!BaseApplication.isHintShowing) {
                addApplicationEntityNew.isCustomTime = isChecked
                addApplicationEntity.isCustomTime = isChecked
                rangeSlider?.isEnabled = isChecked
            }
        }

        rangeSlider?.isEnabled = false
        rangeSlider?.stepSize = 1F

        view_custom_time?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
                switch_custom_time?.performClick()
            }
        }

        rangeSlider?.setLabelFormatter {
            (it.toInt() + 1).toString()
        }

        var updateTimer: Timer? = null
        val timeZone = TimeZone.getDefault()
        val dateFormat = SimpleDateFormat("h a", Locale.getDefault())
        rangeSlider.addOnChangeListener { slider, value, fromUser ->
            // Cancel any previously scheduled updates
            updateTimer?.cancel()
            // Schedule an update in 250 milliseconds
            updateTimer = Timer()
            updateTimer?.schedule(object : TimerTask() {
                override fun run() {
                    val values = slider.values
                    dateFormat.timeZone = timeZone
                    val startTime = Calendar.getInstance()
                    startTime.set(Calendar.HOUR_OF_DAY, values[0].toInt())
                    startTime.set(Calendar.MINUTE, 0)
                    val formattedStartTime = dateFormat.format(startTime.time)
                    val endTime = Calendar.getInstance()
                    endTime.set(Calendar.HOUR_OF_DAY, values[1].toInt())
                    endTime.set(Calendar.MINUTE, 0)
                    val formattedEndTime = dateFormat.format(endTime.time)
                    activity?.runOnUiThread {
                        txtHour?.let {
                            it.text = formattedStartTime
                        }
                        txtEndHour?.text = formattedEndTime

                        if (addApplicationEntity.isCustomTime) {
                            addApplicationEntity.startTime = formattedStartTime
                            addApplicationEntity.endTime = formattedEndTime
                        }

                        if (addApplicationEntityNew.isCustomTime) {
                            addApplicationEntityNew.startTime = formattedStartTime
                            addApplicationEntityNew.endTime = formattedEndTime
                        }
                    }
                }
            }, 200)
        }

        switch_flash?.setOnCheckedChangeListener { buttonView, isChecked ->
            /**
             * Set flash option to the data model
             */
            addApplicationEntity.isIs_flash_on = isChecked
            addApplicationEntityNew.isFlashOn = isChecked
        }

        switch_vibrate?.setOnCheckedChangeListener { buttonView, isChecked ->
            /**
             * set vibrate option to data model
             */
            if (!BaseApplication.isHintShowing) {
                addApplicationEntity.isVibrateOnAlarm = isChecked
                addApplicationEntityNew.isVibrateOnAlarm = isChecked
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
                addApplicationEntityNew.isJustVibrate = isChecked
                if (isChecked) {
                    progress_sound_level.progress = 0
                    switch_vibrate.isChecked = false
                } else {
                    if (isProModeEnabled()) {
                        progress_sound_level.progress = 100
                    } else {
                        progress_sound_level.progress = AndroidUtils.getSoundLevel()
                    }
                }
            }
        }

        view_custom_time?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
                switch_custom_time?.performClick()
            }
        }

        view_flash?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
                switch_flash?.performClick()
            }
        }

        view_vibrate?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
                if (isProModeEnabled()) {
                    switch_vibrate?.performClick()
                } else {
                    showVibrateDialog()
                }
            }
        }

        /**
         * Sound level seekbar
         */
        progress_sound_level?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                addApplicationEntity.sound_level = progress
                addApplicationEntityNew.soundLevel = progress
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
                    showSoundControlDialog()
                }
            }
        }

        view_just_vibrate?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
                switch_just_vibrate?.performClick()
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
                        "Please clear the Ignored sender name first!"
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
                        "Please clear the Sender name first!"
                    ).show()
                }

            }
        }


        view_message_body?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
                /*  DialogUtils.showMessageBodyDialog(
                      requireActivity(),
                      txt_message_body_value?.text.toString(),
                      object : DialogUtils.RepeatCallBack {
                          override fun onClick(name: String) {
                              if (name.isNotEmpty()) {
                                  txt_message_body_value?.text = name
                                  btn_message_body_clear?.visibility = View.VISIBLE
                                  */
                /**
                 * set message body to data model
                 *//*
                                addApplicationEntity.messageBody = name
                            } else {
                                btn_message_body_clear?.visibility = View.GONE
                                txt_message_body_value?.text = "None"
                                */
                /**
                 * set none to message body data model
                 *//*
                                addApplicationEntity.messageBody = "None"
                            }
                        }

                    })*/
                if (txt_message_body_value?.text != "None") {
                    val nameList = txt_message_body_value?.text.toString().split(", ")
                    showMessageKeywordsDialog(nameList.toMutableList() as ArrayList<String>)
                } else {
                    val list = ArrayList<String>()
                    showMessageKeywordsDialog(list)
                }
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
                                    addApplicationEntityNew.ringTone = "Default"
                                } else {
                                    askForPermission()
                                }
                            } else if (name.contains("Speak the message")) {
                                txt_ringtone_value?.text = name
                                /**
                                 * Set SPEAK as flag in the local database
                                 */
                                addApplicationEntity.ringTone = "SPEAK"
                                addApplicationEntityNew.ringTone = "SPEAK"
                                alarmTonePath = "SPEAK"
                            } else {
                                txt_ringtone_value?.text = name
                                /**
                                 * set default alarm tone type to data model
                                 */
                                addApplicationEntity.ringTone = name
                                addApplicationEntityNew.ringTone = name
                                alarmTonePath = null
                            }
                        }
                    })
            }

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
                            addApplicationEntityNew.numberOfPlay = name.trim().toInt()
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
                        addApplicationEntityNew.messageBody = "None"
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
                        addApplicationEntityNew.senderNames = "None"
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
                        addApplicationEntityNew.ignoredNames = "None"
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
                            addApplicationEntityNew.alarmRepeat = name
                            if (name.contains("Custom")) {
                                DialogUtils.showCheckedItemListDialog(
                                    addApplicationEntityNew.repeatDays,
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
                                                addApplicationEntityNew.alarmRepeat = "Always"
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

                                                addApplicationEntityNew.repeatDays =
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

                                            if (addApplicationEntityNew.repeatDays == null) {
                                                txt_repeat_value?.text = "Always"
                                                addApplicationEntityNew.alarmRepeat = "Always"
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
            addApplicationEntityNew.ringTone = name.substring(0, 29)
        } else {
            addApplicationEntity.ringTone = name
            addApplicationEntityNew.ringTone = name
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
     * show just vibrate dialog
     */

    private fun showJustVibrateDialog() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_just_vibrate_dialog)
        val btnClose = dialog.findViewById<FloatingActionButton>(R.id.fab_close_vibrate)
        val btnBuyProJustVibrate = dialog.findViewById<MaterialButton>(R.id.button_just_vibrate)
        btnBuyProJustVibrate.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            visitProScreen()
        }
        btnClose?.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        val window: Window = dialog.window!!
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    /**
     * show sound & vibration dialog
     */
    private fun showVibrateDialog() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_vibrate_dialog)
        val btnClose = dialog.findViewById<FloatingActionButton>(R.id.fab_close_vibrate)
        val btnBuyProVibrate = dialog.findViewById<MaterialButton>(R.id.button_vibrate)
        btnBuyProVibrate.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            visitProScreen()
        }
        btnClose?.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        val window: Window = dialog.window!!
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun showMessageKeywordsDialog(list: ArrayList<String>) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_message_keywords)
        //init views
        val placeHolder = dialog.findViewById<ImageView>(R.id.img_placeholder)
        val etName = dialog.findViewById<EditText>(R.id.et_message_keywords)
        val imageButton = dialog.findViewById<ImageView>(R.id.btn_add)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recycler_view_sender_name)
        val layoutManager = FlexboxLayoutManager(requireActivity())
        val txtHint = dialog.findViewById<TextView>(R.id.txt_hint_sender_name)
        val cancelFloatingButton = dialog.findViewById<FloatingActionButton>(R.id.fabClose)
        val fabSave = dialog.findViewById<FloatingActionButton>(R.id.fabSave)
        val btnPro = dialog.findViewById<MaterialButton>(R.id.btn_pro)
        val txtInfoHint = dialog.findViewById<TextView>(R.id.txt_hint)

        /**
         * show app name at end of hint and make app name green color
         */
        try {
            val text =
                String.format(
                    "If this keywords are in message, alarm will play. Add keywords for %s",
                    appName
                )
            val spannable: Spannable = SpannableString(text)
            spannable.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.success_color
                    )
                ),
                66,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            txtHint.setText(spannable, TextView.BufferType.SPANNABLE)
            txtHint.movementMethod = LinkMovementMethod.getInstance()
        } catch (e: java.lang.NullPointerException) {

        }
        val adapter = SenderNameAdapter(list, object : SenderNameAdapter.ItemClickListener {
            override fun onAllItemRemoved() {
                fabSave.visibility = View.GONE
                placeHolder.visibility = View.VISIBLE
                recyclerView.visibility = View.INVISIBLE
                btnPro.visibility = View.GONE
                txtInfoHint.visibility = View.GONE
                etName.isEnabled = true
                imageButton.isEnabled = true
                imageButton.setBackgroundResource(R.drawable.add_button_background)
            }

            override fun onSingleItemRemove(list: ArrayList<String>) {

            }
        })

        /**
         * buy pro status
         */
        if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)) {
            //free user
            if (list.size > 0) {
                btnPro.visibility = View.VISIBLE
                txtInfoHint.visibility = View.VISIBLE
                etName.isEnabled = false
                imageButton.setBackgroundResource(R.drawable.disabled_add_button)
                imageButton.isEnabled = false
            } else {
                btnPro.visibility = View.GONE
                txtInfoHint.visibility = View.GONE
                etName.isEnabled = true
                imageButton.isEnabled = true
                imageButton.setBackgroundResource(R.drawable.add_button_background)
            }
        } else {
            btnPro.visibility = View.GONE
            txtInfoHint.visibility = View.GONE
            etName.isEnabled = true
            imageButton.isEnabled = true
            imageButton.setBackgroundResource(R.drawable.add_button_background)
        }

        //list not empty
        if (list.size != 0) {
            recyclerView.visibility = View.VISIBLE
            placeHolder.visibility = View.INVISIBLE
            fabSave.visibility = View.VISIBLE

        }
        layoutManager.flexDirection = FlexDirection.COLUMN
        layoutManager.justifyContent = JustifyContent.FLEX_START
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        imageButton.setOnClickListener {
            if (etName.text.toString().isNotEmpty()) {
                adapter.addName(etName.text.toString().trim())
                etName.setText("")
                fabSave.visibility = View.VISIBLE
                placeHolder.visibility = View.INVISIBLE
                recyclerView.visibility = View.VISIBLE
                if (adapter.itemCount > 0) {
                    recyclerView.post { recyclerView.smoothScrollToPosition(adapter.itemCount - 1) }
                }
                /**
                 * buy pro status
                 */
                if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)) {
                    //free user
                    if ((recyclerView.adapter as SenderNameAdapter).itemCount > 0) {
                        btnPro.visibility = View.VISIBLE
                        txtInfoHint.visibility = View.VISIBLE
                        etName.isEnabled = false
                        imageButton.setBackgroundResource(R.drawable.disabled_add_button)
                        imageButton.isEnabled = false
                    } else {
                        btnPro.visibility = View.GONE
                        txtInfoHint.visibility = View.GONE
                        etName.isEnabled = true
                        imageButton.isEnabled = true
                        imageButton.setBackgroundResource(R.drawable.add_button_background)
                    }
                } else {
                    btnPro.visibility = View.GONE
                    txtInfoHint.visibility = View.GONE
                    etName.isEnabled = true
                    imageButton.isEnabled = true
                    imageButton.setBackgroundResource(R.drawable.add_button_background)
                }

            } else {
                Toasty.info(requireActivity(), "Name can't be empty!").show()
            }
        }

        fabSave.setOnClickListener {
            /**
             * save to list
             */
            val name = adapter.convertList()
            if (name.isNotEmpty()) {
                txt_message_body_value?.text = name
                btn_message_body_clear?.visibility = View.VISIBLE
                addApplicationEntity.messageBody = name
                addApplicationEntityNew.messageBody = name
                if (arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
                    holderEntity.messageBody = name
                }
                dialog.dismiss()
            } else {
                btn_message_body_clear?.visibility = View.GONE
                txt_message_body_value?.text = "None"
                addApplicationEntity.messageBody = "None"
                addApplicationEntityNew.messageBody = "None"
                dialog.dismiss()
            }
            /**
             * end of save to list
             */
        }

        btnPro.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            visitProScreen()
        }

        cancelFloatingButton.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }

        val window: Window = dialog.window!!
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    /**
     * show sound control dialog
     */
    private fun showSoundControlDialog() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_sound_dialog)
        val btnClose = dialog.findViewById<FloatingActionButton>(R.id.fab_close_vibrate)
        val btnBuyProSoundControl = dialog.findViewById<MaterialButton>(R.id.button_sound_control)
        btnBuyProSoundControl.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            visitProScreen()
        }
        btnClose?.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
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
     * This function shows the sender name dialog
     */
    private fun senderNameDialog(list: ArrayList<String>) {
        /* val dialog = Dialog(requireActivity())
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
         */
        /**
         * show app name at end of hint and make app name green color
         *//*
        try {
            if (arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
                val text =
                    String.format(
                        "Hint: Only messages from this users will play alarm, add username from %s",
                        holderEntity.appName
                    )
                val spannable: Spannable = SpannableString(text)
                spannable.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.success_color
                        )
                    ),
                    70,
                    text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                txtHint.setText(spannable, TextView.BufferType.SPANNABLE)
            } else {
                val app = arguments?.getSerializable(Constants.BundleKeys.APP) as InstalledApps
                val text =
                    String.format(
                        "Hint: Only messages from this users will play alarm, add username from %s",
                        app.appName
                    )
                val spannable: Spannable = SpannableString(text)
                spannable.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.success_color
                        )
                    ),
                    70,
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

            override fun onSingleItemRemove(list: ArrayList<String>) {

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
                    etName.text.toString().trim()
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
        }*/

        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_sender_name_new)
        //init views
        val placeHolder = dialog.findViewById<ImageView>(R.id.img_placeholder)
        val etName = dialog.findViewById<EditText>(R.id.et_sender_name)
        val imageButton = dialog.findViewById<ImageView>(R.id.btn_add)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recycler_view_sender_name)
        val layoutManager = FlexboxLayoutManager(requireActivity())
        val txtHint = dialog.findViewById<TextView>(R.id.txt_hint_sender_name)
        val cancelFloatingButton = dialog.findViewById<FloatingActionButton>(R.id.fabClose)
        val fabSave = dialog.findViewById<FloatingActionButton>(R.id.fabSave)
        val btnPro = dialog.findViewById<MaterialButton>(R.id.btn_pro)
        val txtInfoHint = dialog.findViewById<TextView>(R.id.txt_hint)

        /**
         * show app name at end of hint and make app name green color
         */
        try {
            val text =
                String.format(
                    "Only messages from this users will play alarm, add username from %s",
                    appName
                )
            val spannable: Spannable = SpannableString(text)
            spannable.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.success_color
                    )
                ),
                64,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            txtHint.setText(spannable, TextView.BufferType.SPANNABLE)
            txtHint.movementMethod = LinkMovementMethod.getInstance()
        } catch (e: java.lang.NullPointerException) {

        }
        val adapter = SenderNameAdapter(list, object : SenderNameAdapter.ItemClickListener {
            override fun onAllItemRemoved() {
                fabSave.visibility = View.GONE
                placeHolder.visibility = View.VISIBLE
                recyclerView.visibility = View.INVISIBLE
                btnPro.visibility = View.GONE
                txtInfoHint.visibility = View.GONE
                etName.isEnabled = true
                imageButton.isEnabled = true
                imageButton.setBackgroundResource(R.drawable.add_button_background)
            }

            override fun onSingleItemRemove(list: ArrayList<String>) {

            }
        })

        /**
         * buy pro status
         */
        if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)) {
            //free user
            if (list.size > 0) {
                btnPro.visibility = View.VISIBLE
                txtInfoHint.visibility = View.VISIBLE
                etName.isEnabled = false
                imageButton.setBackgroundResource(R.drawable.disabled_add_button)
                imageButton.isEnabled = false
            } else {
                btnPro.visibility = View.GONE
                txtInfoHint.visibility = View.GONE
                etName.isEnabled = true
                imageButton.isEnabled = true
                imageButton.setBackgroundResource(R.drawable.add_button_background)
            }
        } else {
            btnPro.visibility = View.GONE
            txtInfoHint.visibility = View.GONE
            etName.isEnabled = true
            imageButton.isEnabled = true
            imageButton.setBackgroundResource(R.drawable.add_button_background)
        }

        //list not empty
        if (list.size != 0) {
            recyclerView.visibility = View.VISIBLE
            placeHolder.visibility = View.INVISIBLE
            fabSave.visibility = View.VISIBLE

        }
        layoutManager.flexDirection = FlexDirection.COLUMN
        layoutManager.justifyContent = JustifyContent.FLEX_START
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        imageButton.setOnClickListener {
            if (etName.text.toString().isNotEmpty()) {
                adapter.addName(etName.text.toString().trim())
                etName.setText("")
                fabSave.visibility = View.VISIBLE
                placeHolder.visibility = View.INVISIBLE
                recyclerView.visibility = View.VISIBLE
                if (adapter.itemCount > 0) {
                    recyclerView.post { recyclerView.smoothScrollToPosition(adapter.itemCount - 1) }
                }
                /**
                 * buy pro status
                 */
                if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)) {
                    //free user
                    if ((recyclerView.adapter as SenderNameAdapter).itemCount > 0) {
                        btnPro.visibility = View.VISIBLE
                        txtInfoHint.visibility = View.VISIBLE
                        etName.isEnabled = false
                        imageButton.setBackgroundResource(R.drawable.disabled_add_button)
                        imageButton.isEnabled = false
                    } else {
                        btnPro.visibility = View.GONE
                        txtInfoHint.visibility = View.GONE
                        etName.isEnabled = true
                        imageButton.isEnabled = true
                        imageButton.setBackgroundResource(R.drawable.add_button_background)
                    }
                } else {
                    btnPro.visibility = View.GONE
                    txtInfoHint.visibility = View.GONE
                    etName.isEnabled = true
                    imageButton.isEnabled = true
                    imageButton.setBackgroundResource(R.drawable.add_button_background)
                }

            } else {
                Toasty.info(requireActivity(), "Name can't be empty!").show()
            }
        }

        fabSave.setOnClickListener {
            /**
             * save to list
             */
            val name = adapter.convertList()
            if (name.isNotEmpty()) {
                txt_sender_name_value?.text = name
                btn_sender_name_clear?.visibility = View.VISIBLE
                addApplicationEntity.senderNames = name
                addApplicationEntityNew.senderNames = name
                if (arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
                    holderEntity.senderNames = name
                }
                dialog.dismiss()
            } else {
                btn_sender_name_clear?.visibility = View.GONE
                txt_sender_name_value?.text = "None"
                addApplicationEntity.senderNames = "None"
                addApplicationEntityNew.senderNames = "None"
                dialog.dismiss()
            }
            /**
             * end of save to list
             */
        }

        btnPro.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            visitProScreen()
        }

        cancelFloatingButton.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
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
        val placeHolder = dialog.findViewById<ImageView>(R.id.img_placeholder)
        val etName = dialog.findViewById<EditText>(R.id.et_sender_name)
        val imageButton = dialog.findViewById<ImageView>(R.id.btn_add)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recycler_view_sender_name)
        val layoutManager = FlexboxLayoutManager(requireActivity())
        val txtHint = dialog.findViewById<TextView>(R.id.txt_hint_sender_name)
        val cancelFloatingButton = dialog.findViewById<FloatingActionButton>(R.id.fabClose)
        val fabSave = dialog.findViewById<FloatingActionButton>(R.id.fabSave)
        val btnPro = dialog.findViewById<MaterialButton>(R.id.btn_pro)
        val txtInfoHint = dialog.findViewById<TextView>(R.id.txt_hint)

        /**
         * show app name at end of hint and make app name green color
         */
        try {
            val text =
                String.format(
                    "Messages from this users will not play alarm, add username from %s",
                    appName
                )
            val spannable: Spannable = SpannableString(text)
            spannable.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.success_color
                    )
                ),
                64,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            txtHint.setText(spannable, TextView.BufferType.SPANNABLE)
            txtHint.movementMethod = LinkMovementMethod.getInstance()
        } catch (e: java.lang.NullPointerException) {

        }
        val adapter = SenderNameAdapter(list, object : SenderNameAdapter.ItemClickListener {
            override fun onAllItemRemoved() {
                fabSave.visibility = View.GONE
                placeHolder.visibility = View.VISIBLE
                recyclerView.visibility = View.INVISIBLE
                btnPro.visibility = View.GONE
                txtInfoHint.visibility = View.GONE
                etName.isEnabled = true
                imageButton.isEnabled = true
                imageButton.setBackgroundResource(R.drawable.add_button_background)
            }

            override fun onSingleItemRemove(list: ArrayList<String>) {

            }
        })

        /**
         * buy pro status
         */
        if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)) {
            //free user
            if (list.size > 0) {
                btnPro.visibility = View.VISIBLE
                txtInfoHint.visibility = View.VISIBLE
                etName.isEnabled = false
                imageButton.setBackgroundResource(R.drawable.disabled_add_button)
                imageButton.isEnabled = false
            } else {
                btnPro.visibility = View.GONE
                txtInfoHint.visibility = View.GONE
                etName.isEnabled = true
                imageButton.isEnabled = true
                imageButton.setBackgroundResource(R.drawable.add_button_background)
            }
        } else {
            btnPro.visibility = View.GONE
            txtInfoHint.visibility = View.GONE
            etName.isEnabled = true
            imageButton.isEnabled = true
            imageButton.setBackgroundResource(R.drawable.add_button_background)
        }

        //list not empty
        if (list.size != 0) {
            recyclerView.visibility = View.VISIBLE
            placeHolder.visibility = View.INVISIBLE
            fabSave.visibility = View.VISIBLE

        }
        layoutManager.flexDirection = FlexDirection.COLUMN
        layoutManager.justifyContent = JustifyContent.FLEX_START
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        imageButton.setOnClickListener {
            if (etName.text.toString().isNotEmpty()) {
                adapter.addName(etName.text.toString().trim())
                etName.setText("")
                fabSave.visibility = View.VISIBLE
                placeHolder.visibility = View.INVISIBLE
                recyclerView.visibility = View.VISIBLE
                if (adapter.itemCount > 0) {
                    recyclerView.post { recyclerView.smoothScrollToPosition(adapter.itemCount - 1) }
                }
                /**
                 * buy pro status
                 */
                if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)) {
                    //free user
                    if ((recyclerView.adapter as SenderNameAdapter).itemCount > 0) {
                        btnPro.visibility = View.VISIBLE
                        txtInfoHint.visibility = View.VISIBLE
                        etName.isEnabled = false
                        imageButton.setBackgroundResource(R.drawable.disabled_add_button)
                        imageButton.isEnabled = false
                    } else {
                        btnPro.visibility = View.GONE
                        txtInfoHint.visibility = View.GONE
                        etName.isEnabled = true
                        imageButton.isEnabled = true
                        imageButton.setBackgroundResource(R.drawable.add_button_background)
                    }
                } else {
                    btnPro.visibility = View.GONE
                    txtInfoHint.visibility = View.GONE
                    etName.isEnabled = true
                    imageButton.isEnabled = true
                    imageButton.setBackgroundResource(R.drawable.add_button_background)
                }

            } else {
                Toasty.info(requireActivity(), "Name can't be empty!").show()
            }
        }

        fabSave.setOnClickListener {
            /**
             * save to list
             */
            val name = adapter.convertList()
            if (name.isNotEmpty()) {
                txt_exclude_sender_name_value?.text = name
                btn_exclude_sender_name_clear?.visibility = View.VISIBLE
                addApplicationEntity.ignored_names = name
                addApplicationEntityNew.ignoredNames = name
                if (arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
                    holderEntity.ignored_names = name
                }
                dialog.dismiss()
            } else {
                btn_exclude_sender_name_clear?.visibility = View.GONE
                txt_exclude_sender_name_value?.text = "None"
                addApplicationEntity.ignored_names = "None"
                addApplicationEntityNew.ignoredNames = "None"
                dialog.dismiss()
            }
            /**
             * end of save to list
             */
        }

        btnPro.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            visitProScreen()
        }

        cancelFloatingButton.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }

        val window: Window = dialog.window!!
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //
        if (!dialog.isShowing) {
            dialog.show()
        }
    }


    private fun defaultValuesToDataModel(): ApplicationEntity {
        addApplicationEntity.alarmRepeat = "Always"
        addApplicationEntity.ringTone = "Default"
        addApplicationEntity.isVibrateOnAlarm = false
        addApplicationEntity.isJustVibrate = false
        addApplicationEntity.isCustomTime = false
        addApplicationEntity.numberOfPlay = 2
        addApplicationEntity.startTime = "12 AM"
        addApplicationEntity.endTime = "11 PM"
        addApplicationEntity.senderNames = "None"
        addApplicationEntity.ignored_names = "None"
        addApplicationEntity.messageBody = "None"
        addApplicationEntity.isRunningStatus = true
        //previous code
        /* if(SharedPrefUtils.contains(Constants.PreferenceKeys.COUNTRY_CODE)){
             if(SharedPrefUtils.readString(Constants.PreferenceKeys.COUNTRY_CODE) == "BD"){
                 addApplicationEntity.sound_level = 100
                 holderEntity.sound_level = 100
             }else{
                 if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)){
                     holderEntity.sound_level = 100
                     addApplicationEntity.sound_level = 100
                 }else{
                     holderEntity.sound_level = 80
                     addApplicationEntity.sound_level = 80
                 }
             }
         }else{
             if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)){
                 holderEntity.sound_level = 100
                 addApplicationEntity.sound_level = 100
             }else{
                 holderEntity.sound_level = 80
                 addApplicationEntity.sound_level = 80
             }
         }*/
        if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)) {
            holderEntity.sound_level = 100
            addApplicationEntity.sound_level = 100
        } else {
            holderEntity.sound_level = AndroidUtils.getSoundLevel()
            addApplicationEntity.sound_level = AndroidUtils.getSoundLevel()
        }
        //set this to holder object for checking default
        holderEntity.alarmRepeat = "Always"
        holderEntity.ringTone = "Default"
        holderEntity.isVibrateOnAlarm = false
        holderEntity.isJustVibrate = false
        holderEntity.isCustomTime = false
        holderEntity.numberOfPlay = 2
        holderEntity.startTime = "12 AM"
        holderEntity.endTime = "11 PM"
        holderEntity.senderNames = "None"
        holderEntity.ignored_names = "None"
        holderEntity.messageBody = "None"
        holderEntity.isRunningStatus = true

        return addApplicationEntity
    }

    private fun defaultValuesToDataModelNew(): ApplicationEntityNew {
        addApplicationEntityNew.alarmRepeat = "Always"
        addApplicationEntityNew.ringTone = "Default"
        addApplicationEntityNew.isVibrateOnAlarm = false
        addApplicationEntityNew.isJustVibrate = false
        addApplicationEntityNew.isCustomTime = false
        addApplicationEntityNew.numberOfPlay = 2
        addApplicationEntityNew.startTime = "12 AM"
        addApplicationEntityNew.endTime = "11 PM"
        addApplicationEntityNew.senderNames = "None"
        addApplicationEntityNew.ignoredNames = "None"
        addApplicationEntityNew.messageBody = "None"
        addApplicationEntityNew.isRunningStatus = true

        if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)) {
            holderEntityNew.sound_level = 100
            addApplicationEntity.sound_level = 100
        } else {
            holderEntityNew.sound_level = AndroidUtils.getSoundLevel()
            addApplicationEntity.sound_level = AndroidUtils.getSoundLevel()
        }
        //set this to holder object for checking default
        holderEntityNew.alarmRepeat = "Always"
        holderEntityNew.ringTone = "Default"
        holderEntityNew.isVibrateOnAlarm = false
        holderEntityNew.isJustVibrate = false
        holderEntityNew.isCustomTime = false
        holderEntityNew.numberOfPlay = 2
        holderEntityNew.startTime = "12 AM"
        holderEntityNew.endTime = "11 PM"
        holderEntityNew.senderNames = "None"
        holderEntityNew.ignored_names = "None"
        holderEntityNew.messageBody = "None"
        holderEntityNew.isRunningStatus = true

        return addApplicationEntityNew
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

                        viewModel.saveBitmapToFile(
                            requireActivity(),
                            app.packageName,
                            bitmap.toBitmap()
                        )

                        optionPresenter?.saveBitmapToFile(
                            requireActivity(),
                            app.packageName,
                            bitmap.toBitmap()
                        )
                        /**
                         * Check for latest version
                         * @date 27th Feb 22
                         * This call going on every single alarm to sync the data
                         */
                        optionPresenter?.checkForLatestUpdate()

                        /**
                         * Check for unknown app
                         */
                        optionPresenter?.checkForUnknownApp(
                            requireActivity(),
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
                    if (switch_flash?.isChecked == holderEntity.isIs_flash_on) {
                        if (switch_just_vibrate?.isChecked == holderEntity.isJustVibrate) {
                            if (switch_custom_time?.isChecked == holderEntity.isCustomTime) {
                                if (txt_number_of_play_value?.text.toString().split(" ")
                                        [0].trim() ==
                                    holderEntity.numberOfPlay.toString()
                                ) {
                                    if (txt_sender_name_value?.text.toString() == holderEntity.senderNames) {
                                        if (txt_exclude_sender_name_value?.text.toString() == holderEntity.ignored_names) {
                                            if (txt_message_body_value?.text.toString() == holderEntity.messageBody) {
                                                if (progress_sound_level?.progress == holderEntity.sound_level) {
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
        }
        return isDefault
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
                requireActivity().setResult(Activity.RESULT_OK)
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
                    txtHour?.text.toString(),
                    txtEndHour?.text.toString()
                )
            ) {

                viewModel.saveApplication(
                    addApplicationEntityNew,
                    firebaseAnalytics
                )

                optionPresenter?.saveApplication(
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
            optionPresenter?.saveApplication(addApplicationEntity, firebaseAnalytics)
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
        switch_flash?.isChecked = app.isIs_flash_on
        switch_custom_time?.isChecked = app.isCustomTime
        switch_just_vibrate?.isChecked = app.isJustVibrate
        txtHour?.text = app.startTime
        txtEndHour?.text = app.endTime
        rangeSlider?.values = listOf(
            TimeUtils.convert12HrTo24Hr(addApplicationEntity.startTime),
            TimeUtils.convert12HrTo24Hr(addApplicationEntity.endTime)
        )
        txt_number_of_play_value?.text = String.format("%d times", app.numberOfPlay)
        txt_sender_name_value?.text = app.senderNames
        txt_exclude_sender_name_value?.text = app.ignored_names
        txt_message_body_value?.text = app.messageBody
        //new added
        progress_sound_level?.progress = app.sound_level
        txt_percent_sound_level?.text = "${app.sound_level}%"
    }

    @SuppressLint("SetTextI18n")
    private fun setPresetValueToUiNew(app: ApplicationEntityNew) {
        if (app.alarmRepeat == "Custom") {
            txt_repeat_value?.text = app.repeatDays
        } else {
            txt_repeat_value?.text = app.alarmRepeat
        }
        txt_ringtone_value?.text = app.ringTone
        switch_vibrate?.isChecked = app.isVibrateOnAlarm
        switch_flash?.isChecked = app.isFlashOn
        switch_custom_time?.isChecked = app.isCustomTime
        switch_just_vibrate?.isChecked = app.isJustVibrate
        txtHour?.text = app.startTime
        txtEndHour?.text = app.endTime
        rangeSlider?.values = listOf(
            TimeUtils.convert12HrTo24Hr(addApplicationEntity.startTime),
            TimeUtils.convert12HrTo24Hr(addApplicationEntity.endTime)
        )
        txt_number_of_play_value?.text = String.format("%d times", app.numberOfPlay)
        txt_sender_name_value?.text = app.senderNames
        txt_exclude_sender_name_value?.text = app.ignoredNames
        txt_message_body_value?.text = app.messageBody
        //new added
        progress_sound_level?.progress = app.soundLevel
        txt_percent_sound_level?.text = "${app.soundLevel}%"
    }

    override fun onApplicationGetSuccess(app: ApplicationEntity) {

        return

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
        this.appName = app.appName
        convertToHolderEntity(addApplicationEntityNew)
        if (isAdded) {
            requireActivity().runOnUiThread {
                setPresetValueToUi(app)
            }
        }
    }

    fun onApplicationGetSuccessNew(app: ApplicationEntityNew) {
        //show edited value to
        if (app.senderNames != "None") {
            btn_sender_name_clear?.visibility = View.VISIBLE
        }
        if (app.messageBody != "None") {
            btn_message_body_clear?.visibility = View.VISIBLE
        }
        if (app.ignoredNames != "None") {
            btn_exclude_sender_name_clear?.visibility = View.VISIBLE
        }
        addApplicationEntityNew = app
        alarmTonePath = app.tonePath
        this.appName = app.appName
        convertToHolderEntity(addApplicationEntityNew)
        if (isAdded) {
            requireActivity().runOnUiThread {
                setPresetValueToUiNew(app)
            }
        }
    }

    private fun convertToHolderEntity(app: ApplicationEntityNew) {
        holderEntity.appName = app.appName
        holderEntity.packageName = app.packageName
        holderEntity.endTime = app.endTime
        holderEntity.startTime = app.startTime
        holderEntity.ringTone = app.ringTone
        holderEntity.numberOfPlay = app.numberOfPlay
        holderEntity.bitmapPath = app.bitmapPath
        holderEntity.messageBody = app.messageBody
        holderEntity.senderNames = app.senderNames
        holderEntity.ignored_names = app.ignoredNames
        holderEntity.isCustomTime = app.isCustomTime
        holderEntity.isJustVibrate = app.isJustVibrate
        holderEntity.isVibrateOnAlarm = app.isVibrateOnAlarm
        holderEntity.tone_path = app.tonePath
        holderEntity.alarmRepeat = app.alarmRepeat
        holderEntity.repeatDays = app.repeatDays
        holderEntity.sound_level = app.soundLevel
        holderEntity.isIs_flash_on = app.isFlashOn
    }

    override fun onApplicationGetError(message: String) {
//        defaultValuesToDataModel()
        defaultValuesToDataModelNew()
    }

    override fun onIllegalState() {
//        defaultValuesToDataModel()
        defaultValuesToDataModelNew()
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setPresetValueToUiNew(defaultValuesToDataModelNew())
    }

}