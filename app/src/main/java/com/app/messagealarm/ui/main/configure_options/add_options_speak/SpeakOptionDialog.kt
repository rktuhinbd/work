package com.app.messagealarm.ui.main.configure_options.add_options_speak

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.ui.buy_pro.BuyProActivity
import com.app.messagealarm.ui.main.ApplicationRepository
import com.app.messagealarm.ui.main.ApplicationViewModel
import com.app.messagealarm.ui.main.ApplicationViewModelFactory
import com.app.messagealarm.ui.main.add_apps.AddApplicationActivity
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.ui.main.configure_options.adapter.SenderNameAdapter
import com.app.messagealarm.ui.main.configure_options.presenter.OptionPresenter
import com.app.messagealarm.ui.main.configure_options.view.OptionView
import com.app.messagealarm.utils.*
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_alarm_options.*
import kotlinx.android.synthetic.main.dialog_speak_options.*
import kotlinx.android.synthetic.main.dialog_speak_options.btn_close
import kotlinx.android.synthetic.main.dialog_speak_options.btn_exclude_sender_name_clear
import kotlinx.android.synthetic.main.dialog_speak_options.btn_message_body_clear
import kotlinx.android.synthetic.main.dialog_speak_options.btn_save
import kotlinx.android.synthetic.main.dialog_speak_options.btn_sender_name_clear
import kotlinx.android.synthetic.main.dialog_speak_options.progress_bar_option
import kotlinx.android.synthetic.main.dialog_speak_options.progress_sound_level
import kotlinx.android.synthetic.main.dialog_speak_options.switch_custom_time
import kotlinx.android.synthetic.main.dialog_speak_options.switch_vibrate
import kotlinx.android.synthetic.main.dialog_speak_options.txt_exclude_sender_name_value
import kotlinx.android.synthetic.main.dialog_speak_options.txt_message_body_value
import kotlinx.android.synthetic.main.dialog_speak_options.txt_number_of_play_value
import kotlinx.android.synthetic.main.dialog_speak_options.txt_percent_sound_level
import kotlinx.android.synthetic.main.dialog_speak_options.txt_pro_vibrate
import kotlinx.android.synthetic.main.dialog_speak_options.txt_sender_name_value
import kotlinx.android.synthetic.main.dialog_speak_options.view_custom_time
import kotlinx.android.synthetic.main.dialog_speak_options.view_exclude_sender_name
import kotlinx.android.synthetic.main.dialog_speak_options.view_message_body
import kotlinx.android.synthetic.main.dialog_speak_options.view_number_of_play
import kotlinx.android.synthetic.main.dialog_speak_options.view_sender_name
import kotlinx.android.synthetic.main.dialog_speak_options.view_vibrate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SpeakOptionDialog : BottomSheetDialogFragment(), OptionView {

    var shouldOnStatus = false
    var once: Once? = null
    var appName: String? = ""
    private var addApplicationEntity = ApplicationEntity()
    private var holderEntity = ApplicationEntity()
    private var optionPresenter: OptionPresenter? = null

    private lateinit var viewModel: ApplicationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
        optionPresenter = OptionPresenter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_speak_options, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
        setListener()
        enableProMode()

        // Initialize ViewModel
        val applicationDao = AppDatabase.getInstance(requireContext()).applicationDao()
        val repository = ApplicationRepository(applicationDao)
        val viewModelFactory = ApplicationViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ApplicationViewModel::class.java)

        val packageName = arguments?.getString(Constants.BundleKeys.PACKAGE_NAME)
        if (!TextUtils.isEmpty(packageName)) {
            viewModel.getAppByPackageName(packageName!!)
        }

        initObserver()
    }


    private fun enableProMode() {
        if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)) {
            switch_vibrate?.isEnabled = true
            txt_pro_vibrate?.visibility = View.GONE
        } else {
            switch_vibrate?.isChecked = false
            switch_vibrate?.isEnabled = false
            txt_pro_vibrate?.visibility = View.VISIBLE
        }
    }

    private fun setListener() {

        btn_close?.setOnClickListener {
            dismiss()
        }

        range_slider?.setLabelFormatter {
            (it.toInt() + 1).toString()
        }

        switch_vibrate?.setOnCheckedChangeListener { buttonView, isChecked ->
            /**
             * set vibrate option to data model
             */
            addApplicationEntity.vibrateOnAlarm = isChecked
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
                addApplicationEntity.soundLevel = progress
                txt_percent_sound_level?.text = "${progress}%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

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

        switch_custom_time?.setOnCheckedChangeListener { buttonView, isChecked ->
            /**
             * set is custom time to data model
             */
            if (!BaseApplication.isHintShowing) {
                addApplicationEntity.isCustomTime = isChecked
                range_slider?.isEnabled = isChecked
            }
        }

        view_custom_time?.setOnClickListener {
            if (!BaseApplication.isHintShowing) {
                switch_custom_time?.performClick()
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
                            // holderEntity.senderNames = "None"

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
                            //  holderEntity.ignored_names = "None"
                        }
                        addApplicationEntity.ignoredNames = "None"
                        txt_exclude_sender_name_value?.text = "None"
                        btn_exclude_sender_name_clear?.visibility = View.GONE
                    }

                    override fun onNegative() {

                    }
                })
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
                if (txt_message_body_value?.text != "None") {
                    val nameList = txt_message_body_value?.text.toString().split(", ")
                    showMessageKeywordsDialog(nameList.toMutableList() as ArrayList<String>)
                } else {
                    val list = ArrayList<String>()
                    showMessageKeywordsDialog(list)
                }
            }
        }

        btn_save?.setOnClickListener {
            //save the app
            try {
                addApplicationEntity.isSpeakEnabled = true
                //save application and turn switch on
                addApplicationEntity.runningStatus = true

                if (TextUtils.isEmpty(addApplicationEntity.packageName)) {
                    saveBitmap()
                } else {
                    viewModel.update(addApplicationEntity)
                }

            } catch (e: java.lang.NullPointerException) {
                e.printStackTrace()
            }
        }
    }

    private fun initObserver() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.applicationByPackageObserver.collectLatest {
                    addApplicationEntity = it ?: ApplicationEntity()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.applicationInsertObserver.collectLatest {
                    if (it == true) {
                        hideProgressBar()
                        requireActivity().runOnUiThread {
                            Toasty.success(
                                requireActivity(),
                                getString(R.string.application_save_success)
                            ).show()
                            dismissAllowingStateLoss()
                            requireActivity().setResult(Activity.RESULT_OK)
                            requireActivity().finish()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.applicationUpdateObserver.collectLatest {
                    if (it == true) {
                        hideProgressBar()
                        requireActivity().runOnUiThread {
                            Toasty.success(
                                requireActivity(),
                                getString(R.string.update_successful)
                            ).show()
                            requireActivity().onBackPressed()
                        }
                    }
                }
            }
        }
    }

    private fun showProgressBar() {
        progress_bar_option?.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progress_bar_option?.visibility = View.INVISIBLE
    }


    private fun saveBitmap() {
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
                Thread(Runnable {
                    try {
                        val bitmap = app.drawableIcon
                        optionPresenter?.saveBitmapToFile(
                            requireActivity(),
                            app.packageName,
                            bitmap.toBitmap()
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }).start()
            }
        } catch (e: NullPointerException) {
            //skip the crash
            e.printStackTrace()
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
                if (arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
                    //  holderEntity.senderNames = name
                }
                dialog.dismiss()
            } else {
                btn_sender_name_clear?.visibility = View.GONE
                txt_sender_name_value?.text = "None"
                addApplicationEntity.senderNames = "None"
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
                addApplicationEntity.ignoredNames = name
                if (arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
                    //holderEntity.ignored_names = name
                }
                dialog.dismiss()
            } else {
                btn_exclude_sender_name_clear?.visibility = View.GONE
                txt_exclude_sender_name_value?.text = "None"
                addApplicationEntity.ignoredNames = "None"
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
                if (arguments?.getBoolean(Constants.BundleKeys.IS_EDIT_MODE)!!) {
                    // holderEntity.messageBody = name
                }
                dialog.dismiss()
            } else {
                btn_message_body_clear?.visibility = View.GONE
                txt_message_body_value?.text = "None"
                addApplicationEntity.messageBody = "None"
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

    private fun isProModeEnabled(): Boolean {
        return SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)
    }

    fun init() {
        var updateTimer: Timer? = null
        val timeZone = TimeZone.getDefault()
        // Create a SimpleDateFormat with the desired output format
        range_slider?.isEnabled = false
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

    }

    override fun onApplicationUpdateSuccess() {

    }

    override fun onApplicationUpdateError(message: String) {

    }

    override fun onBitmapSaveSuccess(path: String) {
        addApplicationEntity.bitmapPath = path
        /**
         * End of other values
         */
        viewModel.insert(addApplicationEntity)

        if (isAdded) {
            requireActivity().runOnUiThread {
                hideProgressBar()
            }
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

    override fun onApplicationGetSuccess(app: ApplicationEntity) {

    }

    override fun onApplicationGetError(message: String) {

    }

    override fun onIllegalState() {

    }

}