package com.app.messagealarm.ui.main.alarm_applications

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.service.app_reader_intent_service.AppsReaderIntentService
import com.app.messagealarm.service.notification_service.NotificationListener
import com.app.messagealarm.ui.adapters.AddedAppsListAdapter
import com.app.messagealarm.ui.buy_pro.BuyProActivity
import com.app.messagealarm.ui.main.add_apps.AddApplicationActivity
import com.app.messagealarm.ui.main.add_options.AddApplicationOption
import com.app.messagealarm.ui.setting.SettingsActivity
import com.app.messagealarm.ui.widget.BottomSheetFragmentLang
import com.app.messagealarm.ui.widget.TutorialBottomSheetDialog
import com.app.messagealarm.utils.*
import com.app.messagealarm.work_manager.WorkManagerUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import es.dmoral.toasty.Toasty
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_add_app_options.*
import kotlinx.android.synthetic.main.item_added_applications.view.*
import xyz.aprildown.ultimateringtonepicker.RingtonePickerActivity
import java.io.File


class AlarmApplicationActivity : BaseActivity(), AlarmApplicationView, PurchasesUpdatedListener,
    AddedAppsListAdapter.ItemClickListener {

    var mMessageReceiver: BroadcastReceiver? = null
    val bottomSheetModel = AddApplicationOption()
    val REQUEST_CODE_PICK_AUDIO = 1
    var menu: Menu? = null

    private val alarmAppPresenter = AlarmApplicationPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        changeTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setToolBar()
        setListener()
        handleService()
        setupAppsRecyclerView()
        lookForTablesSize()
        showLanguageDoesNotSupported()
        triggerBuyProDialog()
        handlePushNotificationData()
        /**
         * check for review
         */
        if (SharedPrefUtils.readInt(Constants.PreferenceKeys.ALARM_COUNT) >= 5) {
            askForReview()
        }
        mMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                switch_alarm_status?.isChecked = false
            }
        }
    }


    /**
     * handle push notification
     */
    private fun handlePushNotificationData() {
        if (intent.extras != null) {
            if (intent.hasExtra(Constants.IntentKeys.PUSH_IMAGE)) {
                //image available
                showPushNotificationDialog(
                    intent.getBooleanExtra(Constants.IntentKeys.IS_PUSH_URL, false),
                    intent.getBooleanExtra(Constants.IntentKeys.IS_BUY_PRO, false),
                    intent.getStringExtra(Constants.IntentKeys.PUSH_TITLE)!!,
                    intent.getStringExtra(Constants.IntentKeys.PUSH_DESC)!!,
                    intent.getByteArrayExtra(Constants.IntentKeys.PUSH_IMAGE)!!,
                    intent.getStringExtra(Constants.IntentKeys.PUSH_URL)
                )
            } else {
                //no image
                showPushNotificationDialog(
                    intent.getBooleanExtra(Constants.IntentKeys.IS_PUSH_URL, false),
                    intent.getBooleanExtra(Constants.IntentKeys.IS_BUY_PRO, false),
                    intent.getStringExtra(Constants.IntentKeys.PUSH_TITLE)!!,
                    intent.getStringExtra(Constants.IntentKeys.PUSH_DESC)!!,
                    null,
                    intent.getStringExtra(Constants.IntentKeys.PUSH_URL)
                )
            }
        }
    }

    private fun showPushNotificationDialog(
        isWebUrl: Boolean,
        isBuyPRO: Boolean, title: String,
        desc: String, image: ByteArray?, webUrl: String?
    ) {
        var bitmap: Bitmap? = null
        if (image != null) {
            //convert the bitmap data to bitmap
            try {
                bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        runOnUiThread {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_push_notification)
            val imageView = dialog.findViewById<ImageView>(R.id.image_logo)
            val fabCloseButton = dialog.findViewById<FloatingActionButton>(R.id.fab_close_push)
            val btnBuyPro = dialog.findViewById<MaterialButton>(R.id.button_pro)
            val btnExplore = dialog.findViewById<MaterialButton>(R.id.button_web)
            if (isWebUrl && !isBuyPRO) {
                btnBuyPro.visibility = View.GONE
                btnExplore.visibility = View.VISIBLE
            } else if (isBuyPRO && !isWebUrl) {
                btnBuyPro.visibility = View.VISIBLE
                btnExplore.visibility = View.GONE
            } else {
                btnBuyPro.visibility = View.GONE
                btnExplore.visibility = View.GONE
            }
            btnExplore.setOnClickListener {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
                if (webUrl != null) {
                    VisitUrlUtils.visitWebsite(this, webUrl)
                }
            }
            btnBuyPro.setOnClickListener {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
                val intent = Intent(this, BuyProActivity::class.java)
                startActivityForResult(
                    intent,
                    Constants.ACTION.ACTION_PURCHASE_FROM_MAIN
                )
            }
            fabCloseButton.setOnClickListener {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            }
            val txtTitle = dialog.findViewById<TextView>(R.id.text_title)
            txtTitle?.text = title
            val subTitle = dialog.findViewById<TextView>(R.id.text_sub_title)
            subTitle?.text = desc
            val window: Window = dialog.window!!
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            if (!isFinishing && !dialog.isShowing) {
                dialog.show()
            }
        }
    }

    /**
     * end of handling push notification
     */


    private fun lookForTablesSize() {
        alarmAppPresenter.getRequiredTableSize()
        alarmAppPresenter.getSyncedLowerLoaded(this)
    }

    private fun lookForAlarmApplication() {
        if(!SharedPrefUtils.contains(Constants.PreferenceKeys.SOUND_LEVEL) && !SharedPrefUtils.contains(
                Constants.PreferenceKeys.DEFAULT_SOUND_LEVEL
        )){
            AndroidUtils.getDefaultSoundLevel()
        }
        alarmAppPresenter.getApplicationList()
        alarmAppPresenter.syncFirebaseTokenToHeroku()
        alarmAppPresenter.isAutoStartPermissionAvailable(this)
        /**
         * rollback db to 80 if user is unpaid and from previous version and from outside bd
         */
        if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)) {
            if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_FIREBASE_TOKEN_SYNCED_2_0_2)) {
                if (SharedPrefUtils.readString(Constants.PreferenceKeys.COUNTRY_CODE) != "BD") {
                    if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DB_ROLLED_BACK)) {
                        alarmAppPresenter.dbRollBackForSoundLevelFromDefault()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        changeStateOfSpeedDial()
        lookForAlarmApplication()
        if (BaseApplication.installedApps.isEmpty()) {
            val mIntent = Intent(this, AppsReaderIntentService::class.java)
            AppsReaderIntentService.enqueueWork(this, mIntent)
        }
        val isServiceStopped =
            SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_SERVICE_STOPPED)
        switch_alarm_status?.isChecked = !isServiceStopped
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver!!, IntentFilter("turn_off_switch"))
        /**
         * if user is paid user remove the buy bro menu item
         */
        if (isPurchased()) {
            menu?.getItem(0)?.isVisible = false
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver!!)
    }


    private fun setToolBar() {
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item_home, menu)
        if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)) {
            MenuTintUtils.tintAllIcons(menu, Color.WHITE)
        }
        this.menu = menu
        /**
         * if user is paid user remove the buy bro menu item
         */
        if (isPurchased()) {
            menu?.getItem(0)?.isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnu_setting -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.mnu_buy_pro -> {
                //one app added now take user to buy
                val intent = Intent(this, BuyProActivity::class.java)
                startActivityForResult(
                    intent,
                    Constants.ACTION.ACTION_PURCHASE_FROM_MAIN
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (REQUEST_CODE_PICK_AUDIO == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val alarmTone = RingtonePickerActivity.getPickerResult(data!!)
                    val fileName = File(PathUtils.getPath(this, alarmTone[0].uri)!!).name
                    if (MediaUtils.getDurationOfMediaFle(
                            PathUtils.getPath(
                                this,
                                alarmTone[0].uri
                            )!!
                        ) >= 30
                    ) {
                        bottomSheetModel.txt_ringtone_value?.text = fileName
                        bottomSheetModel.setToneName(fileName)
                        bottomSheetModel.alarmTonePath = PathUtils.getPath(this, alarmTone[0].uri)!!
                    } else {
                        bottomSheetModel.txt_ringtone_value?.text = "Default"
                        bottomSheetModel.setToneName("Default")
                        bottomSheetModel.alarmTonePath = null
                        DialogUtils.showSimpleDialog(
                            this, getString(R.string.txt_wrong_duration),
                            getString(R.string.txt_selected_music_duration)
                        )
                    }
                } catch (e: IllegalArgumentException) {
                    bottomSheetModel.txt_ringtone_value?.text = "Default"
                    bottomSheetModel.setToneName("Default")
                    bottomSheetModel.alarmTonePath = null
                    DialogUtils.showSimpleDialog(
                        this, getString(R.string.txt_music),
                        getString(R.string.txt_try_again)
                    )
                } catch (e: IndexOutOfBoundsException) {
                    bottomSheetModel.txt_ringtone_value?.text = "Default"
                    bottomSheetModel.setToneName("Default")
                    bottomSheetModel.alarmTonePath = null
                    bottomSheetModel.askForPermission()
                }
            }
        } else if (requestCode == Constants.ACTION.ACTION_PURCHASE_FROM_MAIN) {
            //purchased
            if (isPurchased()) {
                Toasty.success(this, "Thanks for purchase! You are now pro user!").show()
                /**
                 * if user is paid user remove the buy bro menu item
                 */
                if (isPurchased()) {
                    menu?.getItem(0)?.isVisible = false
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun isPurchased(): Boolean {
        return SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)
    }

    private fun changeTheme() {
        if (!SharedPrefUtils.contains(Constants.PreferenceKeys.IS_DARK_MODE)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }


    private fun handleService() {
        val isServiceStopped =
            SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_SERVICE_STOPPED)
        if (SharedPrefUtils.contains(Constants.PreferenceKeys.IS_SERVICE_STOPPED)) {
            if (!isServiceStopped) {
                startMagicService()
            }
        } else {
            startMagicService()
        }
    }


    private fun recyclerViewSwipeHandler() {
        val callback: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                ).addSwipeLeftBackgroundColor(
                    ContextCompat.getColor(
                        this@AlarmApplicationActivity,
                        R.color.delete_item
                    )
                )
                    .addSwipeLeftActionIcon(R.drawable.ic_delete_black_24dp)
                    .create()
                    .decorate()
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) { // Take action for the swiped item
                DialogUtils.showDialog(this@AlarmApplicationActivity,
                    getString(R.string.delete_app_title),
                    getString(R.string.delete_app_message),
                    object : DialogUtils.Callback {
                        override fun onPositive() {
                            alarmAppPresenter.deleteApplication(
                                (rv_application_list?.adapter as AddedAppsListAdapter).getItem(
                                    viewHolder.adapterPosition
                                ),
                                viewHolder.adapterPosition
                            )
                        }

                        override fun onNegative() {
                            rv_application_list?.adapter?.notifyDataSetChanged()
                        }
                    })
            }
        }
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(rv_application_list)
    }


    private fun stopService() {
        if (AndroidUtils.isServiceRunning(this, NotificationListener::class.java)) {
            val intent = Intent(this, NotificationListener::class.java)
            intent.action = NotificationListener.ACTION_STOP_FOREGROUND_SERVICE
            startService(intent)
        }
    }

    private fun startMagicService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(this, NotificationListener::class.java)
            startForegroundService(intent)
        } else {
            val intent = Intent(this, NotificationListener::class.java)
            startService(intent)
        }
    }


    private fun setListener() {
        fab_button_add_application?.setOnClickListener {
            if (!isPurchased()) {
                if (rv_application_list?.adapter?.itemCount!! < 3) {
                    startActivity(Intent(this, AddApplicationActivity::class.java))
                } else {
                    Toasty.info(this, "Please buy pro version to add more apps!").show()
                    //one app added now take user to buy
                    val intent = Intent(this, BuyProActivity::class.java)
                    startActivityForResult(
                        intent,
                        Constants.ACTION.ACTION_PURCHASE_FROM_MAIN
                    )
                }
            } else {
                startActivity(Intent(this, AddApplicationActivity::class.java))
            }
        }

        /**
         * setup the SpeedDialView
         */

        if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_VIDEO_SHOWED)) {
            val drawableOne = AppCompatResources.getDrawable(this, R.drawable.ic_youtube)
            val itemOne = SpeedDialActionItem.Builder(R.id.fab_action1, drawableOne)
                .setFabImageTintColor(
                    ResourcesCompat.getColor(
                        resources, R.color.color_white,
                        theme
                    )
                )
                .setLabel("Tutorial")
                .setContentDescription("Tutorial of the app")
                .setLabelColor(Color.WHITE)
                .setFabSize(FloatingActionButton.SIZE_MINI)
                .setFabBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.success_color,
                        theme
                    )
                )
                .setLabelBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.success_color,
                        theme
                    )
                )
                .setLabelClickable(true)
                .create()
            val drawable = AppCompatResources.getDrawable(this, R.drawable.ic_add)

            val itemTwo = SpeedDialActionItem.Builder(R.id.fab_action2, drawable)
                .setFabImageTintColor(
                    ResourcesCompat.getColor(
                        resources, R.color.color_white,
                        theme
                    )
                )
                .setLabel("Add Application")
                .setFabSize(FloatingActionButton.SIZE_MINI)
                .setContentDescription("Add application for playing alarm")
                .setLabelColor(Color.WHITE)
                .setFabBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.floating,
                        theme
                    )
                )
                .setLabelBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.floating,
                        theme
                    )
                )
                .setLabelClickable(true)
                .create()


            speedDial.addActionItem(itemTwo)
            speedDial.addActionItem(itemOne)

            // Set option fabs clicklisteners.
            speedDial.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
                when (actionItem.id) {
                    R.id.fab_action1 -> {
                        speedDial.close()
                        showVideoTutorial()
                        return@OnActionSelectedListener true // false will close it without animation
                    }
                    R.id.fab_action2 -> {
                        speedDial.close()
                        fab_button_add_application?.performClick()
                        return@OnActionSelectedListener true
                    }
                }
                false // To keep the Speed Dial open
            })
        } else {
            try {
                speedDial.clearActionItems()
            } catch (e: Exception) {

            }
        }


        // Set main action checklist.
        speedDial.setOnChangeListener(object : SpeedDialView.OnChangeListener {
            override fun onMainActionSelected(): Boolean {
                if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_VIDEO_SHOWED)) {
                    fab_button_add_application?.performClick()
                }
                return false // True to keep the Speed Dial open
            }

            override fun onToggleChanged(isOpen: Boolean) {

            }
        })

        /**
         * end of SpeedDialView
         */

        switch_alarm_status?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                ic_alarm_status?.setImageResource(R.drawable.ic_on_button)
                startMagicService()
            } else {
                ic_alarm_status?.setImageResource(R.drawable.ic_off_button)
                stopService()
            }
        }

        txt_auto_start_enable?.setOnClickListener {
            DialogUtils.showOnlyPositiveDialog(this, "AutoStart",
                "If you enable AutoStart option, it will help Message Alarm to run more " +
                        "smoothly in your phone. As it's help not to get killed by the OS",
                object : DialogUtils.Callback {
                    override fun onPositive() {
                        try {
                            val isOpened =
                                AutoStartPermissionHelper.getInstance().getAutoStartPermission(
                                    this@AlarmApplicationActivity,
                                    open = true,
                                    newTask = true
                                )
                            if (isOpened) {
                                SharedPrefUtils.write(
                                    Constants.PreferenceKeys.IS_AUTO_STARTED,
                                    true
                                )
                            }
                        } catch (e: Exception) {
                            //having exception hide it permanently
                            SharedPrefUtils.write(
                                Constants.PreferenceKeys.IS_AUTO_STARTED,
                                true
                            )
                        }
                    }

                    override fun onNegative() {

                    }

                })

        }

        txt_battery_enable?.setOnClickListener {
            DialogUtils.showOnlyPositiveDialog(this, "Battery Restriction",
                "We recommend you to disable battery restriction on our app, as it kills" +
                        " our app sometime from background. For further help, please visit 'Not working sometime' from (Setting) page",
                object : DialogUtils.Callback {
                    override fun onPositive() {
                        //open app info screen
                        try {
                            //Open the specific App Info page:
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.parse("package:$packageName")
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            SharedPrefUtils.write(
                                Constants.PreferenceKeys.IS_BATTERY_RESTRICTED,
                                true
                            )
                        } catch (e: ActivityNotFoundException) {
                            //e.printStackTrace();
                            //Open the generic Apps page:
                            val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            SharedPrefUtils.write(
                                Constants.PreferenceKeys.IS_BATTERY_RESTRICTED,
                                true
                            )
                        }
                    }
                    override fun onNegative() {

                    }
                })
        }
    }


    private fun showOfferDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_buy_pro_layout)
        val videoView = dialog.findViewById<VideoView>(R.id.button_vibrate)
        val window: Window = dialog.window!!
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    public fun changeStateOfSpeedDial() {
        try {
            if (speedDial != null) {
                if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_VIDEO_SHOWED)) {
                    speedDial.clearActionItems()
                }
            }
        } catch (e: java.lang.Exception) {

        }
    }




    /**
     * low sound volume warning
     */

    private fun showLowVolumeWarning(){
        if(!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED) &&
            SharedPrefUtils.readInt(Constants.PreferenceKeys.SHOW_SOUND_WARNING_COUNT)  <
                SharedPrefUtils.readInt(Constants.PreferenceKeys.SHOW_PRO_DIALOG_COUNT)
                ){
                    SharedPrefUtils.write(Constants.PreferenceKeys.SHOW_SOUND_WARNING_COUNT,
                            SharedPrefUtils.readInt(Constants.PreferenceKeys.SHOW_PRO_DIALOG_COUNT)
                        )
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.layout_dialog_warning)
            val txtMsg = dialog.findViewById<TextView
                    >(R.id.text_warning_sub_title)
            val html = String.format("Your alarm volume getting low by : <font color='red'><b>- %d%%</b></font>",
                (SharedPrefUtils.readInt(Constants.PreferenceKeys.DEFAULT_SOUND_LEVEL) -
                       SharedPrefUtils.readInt(Constants.PreferenceKeys.SOUND_LEVEL))
                )
            txtMsg?.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(html)
            }
            val closeButton = dialog.findViewById<FloatingActionButton>(R.id.fab_close_warning)
            closeButton.setOnClickListener {
                if(dialog.isShowing){
                    dialog.dismiss()
                }
            }
            val window: Window = dialog.window!!
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            if (!dialog.isShowing) {
                dialog.show()
            }
        }
    }




    @SuppressLint("SetTextI18n")
    private fun triggerBuyProDialog() {
        /**
         * Every 10 times show buy pro dialog with
         */
        if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED) &&
            (SharedPrefUtils.readInt(Constants.PreferenceKeys.ALARM_COUNT) -
                    SharedPrefUtils.readInt(Constants.PreferenceKeys.MAIN_SCREEN_OPENED)) >= 2
        ) {
            SharedPrefUtils.write(
                Constants.PreferenceKeys.MAIN_SCREEN_OPENED,
                SharedPrefUtils.readInt(Constants.PreferenceKeys.ALARM_COUNT)
            )
            if (!isFinishing) {
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.dialog_buy_pro_layout)
                //init
                val txtMainTitle = dialog.findViewById<TextView>(R.id.text_vibrate_sub_title)
                val imgAppLogo = dialog.findViewById<ImageView>(R.id.image_just_vibrate)
                val txtAlarmCount = dialog.findViewById<TextView>(R.id.text_sale)
                val btnBuyPro = dialog.findViewById<MaterialButton>(R.id.button_sound)
                btnBuyPro?.setOnClickListener {
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                    val bundle = Bundle()
                    bundle.putString("clicked_go_to_pro", "yes")
                    Firebase.analytics.logEvent("pro_dialog_status", bundle)
                    //one app added now take user to buy
                    val intent = Intent(this, BuyProActivity::class.java)
                    startActivityForResult(
                        intent,
                        Constants.ACTION.ACTION_PURCHASE_FROM_MAIN
                    )
                }
                //bind view
                val text = "" +
                        "You were alarmed, when ${SharedPrefUtils.readString(Constants.PreferenceKeys.LAST_SENDER_NAME)} " +
                        "sent you message via ${
                            SharedPrefUtils.readString(
                                Constants.PreferenceKeys.LAST_APP_NAME
                            )
                        }"


                val bundle = Bundle()
                bundle.putString("message_popped_up", text)
                Firebase.analytics.logEvent("pro_dialog_status", bundle)

                val spannable: Spannable = SpannableString(text)
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorGolden)),
                    22,
                    22 + SharedPrefUtils.readString(Constants.PreferenceKeys.LAST_SENDER_NAME).length + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                txtMainTitle?.setText(spannable, TextView.BufferType.SPANNABLE)
                txtAlarmCount?.text =
                    "${SharedPrefUtils.readInt(Constants.PreferenceKeys.ALARM_COUNT)} times"
                imgAppLogo?.setImageBitmap(
                    BitmapFactory.decodeFile(
                        File(SharedPrefUtils.readString(Constants.PreferenceKeys.LAST_APP_ICON_NAME))
                            .absolutePath
                    )
                )
                val window: Window = dialog.window!!
                window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                dialog.setOnKeyListener { arg0, keyCode, event -> // TODO Auto-generated method stub
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        val bundle = Bundle()
                        bundle.putString("clicked_back_button", "yes")
                        Firebase.analytics.logEvent("pro_dialog_status", bundle)
                        dialog.dismiss()
                    }
                    true
                }
                if (!dialog.isShowing) {
                    dialog.show()
                    SharedPrefUtils.write(Constants.PreferenceKeys.SHOW_PRO_DIALOG_COUNT,
                            SharedPrefUtils.readInt(Constants.PreferenceKeys.SHOW_PRO_DIALOG_COUNT) + 1
                        )
                }
            }
        }else{
            showLowVolumeWarning()
        }
    }


    private fun setupAppsRecyclerView() {
        rv_application_list?.layoutManager = LinearLayoutManager(this)
        rv_application_list?.isVerticalScrollBarEnabled = true
        val arraylist = ArrayList<ApplicationEntity>()
        rv_application_list?.adapter = AddedAppsListAdapter(arraylist, this)
    }


    override fun onGetAlarmApplicationSuccess(appsList: ArrayList<ApplicationEntity>) {
        runOnUiThread {
            try {
                if (appsList.isNotEmpty()) {
                    (rv_application_list?.adapter as AddedAppsListAdapter).addItems(appsList)
                    recyclerViewSwipeHandler()
                    dataState()
                } else {
                    emptyState()
                }
            } catch (e: NullPointerException) {

            }
        }
    }

    /**
     * Ask for review to user after first time alarm played
     */
    private fun askForReview() {
        if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_REVIEW_SCREEN_SHOWN)) {
            val manager = ReviewManagerFactory.create(this)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    // We got the ReviewInfo object
                    val reviewInfo = request.result
                    val flow = manager.launchReviewFlow(this, reviewInfo)
                    val bundle = Bundle()
                    bundle.putString("showed_review_pop_up", "yes")
                    Firebase.analytics.logEvent("review_popup", bundle)
                    Toasty.info(this, "You can always see the video from setting!").show()
                    flow.addOnCompleteListener { _ ->
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                        SharedPrefUtils.write(Constants.PreferenceKeys.IS_REVIEW_SCREEN_SHOWN, true)
                    }
                    flow.addOnSuccessListener {
                        SharedPrefUtils.write(Constants.PreferenceKeys.IS_REVIEW_SCREEN_SHOWN, true)
                    }
                    flow.addOnFailureListener {
                        SharedPrefUtils.write(
                            Constants.PreferenceKeys.IS_REVIEW_SCREEN_SHOWN,
                            false
                        )
                    }
                }
            }
        }

    }

    private fun emptyState() {
        txt_hint_home?.visibility = View.GONE
        rv_application_list?.visibility = View.GONE
        img_empty_state?.visibility = View.VISIBLE
        txt_empty_state_title?.visibility = View.VISIBLE
        txt_empty_state_desc?.visibility = View.VISIBLE
    }

    private fun dataState() {
        rv_application_list?.visibility = View.VISIBLE
        txt_hint_home?.visibility = View.VISIBLE
        img_empty_state?.visibility = View.GONE
        txt_empty_state_title?.visibility = View.GONE
        txt_empty_state_desc?.visibility = View.GONE
    }

    override fun onGetAlarmApplicationError() {

    }


    private fun showVideoTutorial() {
        try {
            val bottomSheet = TutorialBottomSheetDialog(this)
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        } catch (e: IllegalStateException) {

        }
    }


    override fun onApplicationDeleteSuccess(position: Int) {
        runOnUiThread {
            (rv_application_list?.adapter as AddedAppsListAdapter).deleteItem(position)
            Toasty.success(this, getString(R.string.app_delete_success)).show()
            if ((rv_application_list?.adapter as AddedAppsListAdapter).itemCount == 0) {
                emptyState()
            }
        }
    }

    override fun onApplicationDeleteError() {
        runOnUiThread {
            Toasty.error(this, getString(R.string.app_delete_error)).show()
            rv_application_list?.adapter?.notifyDataSetChanged()
        }
    }

    fun notifyCurrentAdapter() {
        //at this point getting the concurrent modification exception
        Handler(Looper.getMainLooper()).postDelayed({
            alarmAppPresenter.getApplicationList()
        }, 1500)
    }


    override fun onAppStatusUpdateSuccess() {

    }

    override fun onAppStatusUpdateError(message: String) {
        runOnUiThread {
            rv_application_list?.adapter?.notifyDataSetChanged()
            Toasty.error(this, message).show()
        }
    }

    override fun onRemovedFromSnoozeSuccess() {
        alarmAppPresenter.getApplicationList()
    }

    override fun onTablesSizeRequestSuccess(appSize: Int, langSize: Int, appConstrainSize: Int) {
        WorkManagerUtils.scheduleSyncWork(this, appSize, langSize, appConstrainSize)
    }

    override fun onAutoStartTextShow() {
        runOnUiThread {
            txt_auto_start_detail?.visibility = View.VISIBLE
            txt_auto_start_enable?.visibility = View.VISIBLE
        }
    }

    override fun onAutoStartTextHide() {
        runOnUiThread {
            txt_auto_start_detail?.visibility = View.GONE
            txt_auto_start_enable?.visibility = View.GONE
        }
    }

    override fun onBatteryTextShow() {
        runOnUiThread {
            try {
                if (txt_auto_start_detail.isVisibile()) {
                    //make txt battery detail top margin only 8 dp
                    (txt_battery_detail.layoutParams as ConstraintLayout.LayoutParams).apply {
                        topMargin = ViewUtils.dpToPx(6).toInt()
                    }
                } else {
                    //make txt battery detail top margin only 12 dp
                    (txt_battery_detail.layoutParams as ConstraintLayout.LayoutParams).apply {
                        topMargin = ViewUtils.dpToPx(12).toInt()
                    }
                }
            } catch (e: Exception) {
                //skip it to default
            }
            txt_battery_detail?.visibility = View.VISIBLE
            txt_battery_enable?.visibility = View.VISIBLE
        }
    }

    override fun onBatteryTextHide() {
        runOnUiThread {
            txt_battery_detail?.visibility = View.GONE
            txt_battery_enable?.visibility = View.GONE
        }
    }

    private fun showEditDialog(app: ApplicationEntity) {
        //refresh adapter first
        if (!isFinishing) {
            try {
                if (!bottomSheetModel.isAdded) {
                    val bundle = Bundle()
                    bundle.putBoolean(Constants.BundleKeys.IS_EDIT_MODE, true)
                    bundle.putString(Constants.BundleKeys.PACKAGE_NAME, app.packageName)
                    bottomSheetModel.arguments = bundle
                    bottomSheetModel.show(supportFragmentManager, "OPTIONS")
                }
            } catch (e: java.lang.IllegalStateException) {
                //skip the crash
            }
        }
    }


    override fun onItemClick(app: ApplicationEntity) {
        //refresh adapter first
        showEditDialog(app)
    }

    override fun onLongClick(app: ApplicationEntity) {

    }

    override fun onApplicationSwitch(boolean: Boolean, id: Int) {
        alarmAppPresenter.updateAppStatus(boolean, id)
    }

    private fun showLanguageDoesNotSupported() {
        if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_LANG_WARNING_SHOWED)) {
            if (AndroidUtils.getCurrentLangCode(this) != "en") {
                val bottomSheet = BottomSheetFragmentLang()
                bottomSheet.show(supportFragmentManager, bottomSheet.tag)
                SharedPrefUtils.write(Constants.PreferenceKeys.IS_LANG_WARNING_SHOWED, true)
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var isGranted = true
        if (requestCode == PermissionUtils.REQUEST_CODE_PERMISSION_DEFAULT) {
            for (element in grantResults) {
                if (element != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false
                }
            }
            if (isGranted) {
                bottomSheetModel.pickAudioFromStorage()
            }
        }
    }

    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {

    }


}
