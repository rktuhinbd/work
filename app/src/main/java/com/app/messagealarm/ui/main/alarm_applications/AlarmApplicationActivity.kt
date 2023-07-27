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
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.databinding.ActivityMainBinding
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.service.app_reader_intent_service.AppsReaderIntentService
import com.app.messagealarm.service.notification_service.NotificationListener
import com.app.messagealarm.ui.adapters.AddedAppsListAdapterNew
import com.app.messagealarm.ui.buy_pro.BuyProActivity
import com.app.messagealarm.ui.main.add_apps.AddApplicationActivity
import com.app.messagealarm.ui.main.configure_options.add_options_alarm.AlarmOptionDialog
import com.app.messagealarm.ui.main.add_website.AddWebsiteActivity
import com.app.messagealarm.ui.main.configure_options.add_options_speak.SpeakOptionDialog
import com.app.messagealarm.ui.setting.SettingsActivity
import com.app.messagealarm.ui.widget.BottomSheetFragmentLang
import com.app.messagealarm.ui.widget.TutorialBottomSheetDialog
import com.app.messagealarm.utils.*
import com.app.messagealarm.window.WindowManagerService
import com.app.messagealarm.work_manager.WorkManagerUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import es.dmoral.toasty.Toasty
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_alarm_options.*
import kotlinx.android.synthetic.main.item_added_applications.view.*
import org.jetbrains.anko.toast
import xyz.aprildown.ultimateringtonepicker.RingtonePickerActivity
import java.io.File


class AlarmApplicationActivity : BaseActivity(), AlarmApplicationView, PurchasesUpdatedListener,
    AddedAppsListAdapterNew.ItemClickListener {

    private lateinit var binding : ActivityMainBinding

    private lateinit var billingClient: BillingClient
    var mMessageReceiver: BroadcastReceiver? = null
    val bottomSheetModel = AlarmOptionDialog()
    val bottomSpeakOptionDialog = SpeakOptionDialog()
    val REQUEST_CODE_PICK_AUDIO = 1
    var menu: Menu? = null
    val CODE_DRAW_OVER_OTHER_APP_PERMISSION = 111
    var isFromLauncher = false

    private val alarmAppPresenter = AlarmApplicationPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        changeTheme()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setToolBar()
        setListener()
        lookForTablesSize()
        handleService()
        setupAppsRecyclerView()
        showLanguageDoesNotSupported()
        triggerBuyProDialog()
        handlePushNotificationData()
        Thread {
            handlePurchaseState()
        }.start()

        /**
         * Use this below function for battery and auto-launch enable
         */
        //showWarningDialog()
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
        isFromLauncher = true
    }


    private fun handlePurchaseState() {
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(this)
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Billing client is ready
                    /**
                     * Check for subscription
                     */
                    billingClient.queryPurchasesAsync(
                        BillingClient.SkuType.SUBS
                    ) { p0, p1 ->
                        if (p1.size > 0) {
                            handlePurchase(p1)
                        } else {
                            if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.WAS_SUBSCRIBED)) {
                                runOnUiThread {
                                    //first time after subscription is cancelled
                                    Toasty.info(
                                        this@AlarmApplicationActivity,
                                        "Sorry to see you go, Let us know why you canceled your subscription!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                SharedPrefUtils.write(
                                    Constants.PreferenceKeys.WAS_SUBSCRIBED,
                                    false
                                )
                            }
                            setIsPurchased(false)
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to the billing client
            }
        })
    }


    /**
     * Removed by MK
     */
    private fun showFirstSavedAnimation() {
        /*  viewKonfetti.build()
              .setDirection(0.0, 359.0)
              .setSpeed(1f, 2f)
              .setFadeOutEnabled(true)
              .setTimeToLive(500L)
              .addColors(
                  Color.parseColor("#FF3F62"),
                  Color.parseColor("#307E45")
              )
              .addShapes(
                  Shape.DrawableShape(
                      ResourcesCompat.getDrawable(
                          resources, R.drawable.ic_leaf, null
                      )!!
                  ), Shape.DrawableShape(
                      ResourcesCompat.getDrawable(
                          resources, R.drawable.ic_flower, null
                      )!!
                  )
              )
              .addSizes(Size(18))
              .setPosition(-50f, viewKonfetti.width + 50f, -50f, -50f)
              .streamFor(100, 1500)*/
    }


    /**
     * show congratulations dialog
     */
    private fun showCongratulationDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_congrats_layout)
        val materialButton = dialog.findViewById<MaterialButton>(R.id.text_done)
        val textView = dialog.findViewById<TextView>(R.id.text_sub_message)
        try {
            val appEntity = (rv_application_list?.adapter as AddedAppsListAdapterNew).getItem(
                0
            )
            val html = String.format(
                "<b>%s</b> has been successfully added! You will now receive alarm as needed.",
                appEntity.appName
            )
            textView.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(html)
            }
        } catch (e: IndexOutOfBoundsException) {
            /**
             * Here is a bug, handle it by reactive programming. Note by MK
             */
            textView.text =
                "Your app has been successfully added, You will receive alarm as needed!"
        }
        materialButton.setOnClickListener {
            if (dialog.isShowing) {
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
                btnBuyPro.gone(false)
                btnExplore.visible(false)
            } else if (isBuyPRO && !isWebUrl) {
                btnBuyPro.visible(false)
                btnExplore.gone(false)
            } else {
                btnBuyPro.gone(false)
                btnExplore.gone(false)
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
        // alarmAppPresenter.getRequiredTableSize()
        alarmAppPresenter.getSyncedLowerLoaded(this)
    }

    private fun lookForAlarmApplication() {
        if (!SharedPrefUtils.contains(Constants.PreferenceKeys.SOUND_LEVEL) && !SharedPrefUtils.contains(
                Constants.PreferenceKeys.DEFAULT_SOUND_LEVEL
            )
        ) {
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

    override fun onStop() {
        super.onStop()
        isFromLauncher = false
    }

    private fun setToolBar() {
        setSupportActionBar(binding.toolbar)
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
        alarmAppPresenter.isAutoStartPermissionAvailable(this)
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

                    /***
                     * Remove the 30 second and more audio restriction
                     * @author Mortuza Hossain
                     * To go back to previous code uncomment the if..else condition
                     */

//                    if (MediaUtils.getDurationOfMediaFle(
//                            PathUtils.getPath(
//                                this,
//                                alarmTone[0].uri
//                            )!!
//                        ) >= 30
//                    ) {
                    bottomSheetModel.txt_ringtone_value?.text = fileName
                    bottomSheetModel.setToneName(fileName)
                    bottomSheetModel.alarmTonePath = PathUtils.getPath(this, alarmTone[0].uri)!!
//                    } else {
//                        bottomSheetModel.txt_ringtone_value?.text = "Default"
//                        bottomSheetModel.setToneName("Default")
//                        bottomSheetModel.alarmTonePath = null
//                        DialogUtils.showSimpleDialog(
//                            this, getString(R.string.txt_wrong_duration),
//                            getString(R.string.txt_selected_music_duration)
//                        )
//                    }
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
        } else if (requestCode == Constants.ACTION.ACTION_SAVE_APPLICATION) {
            if (resultCode == Activity.RESULT_OK) {
                if (rv_application_list?.adapter?.itemCount!! == 0) {
                    //   showFirstSavedAnimation()
                    Handler(Looper.myLooper()!!).postDelayed({
                        showCongratulationDialog()
                    }, 2500)
                }
            }
        }

        // Use This callback if want to show notification after Draw over other app permission
        /*else if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {

            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) {
                Toast.makeText(
                    this,
                    "Permission granted.",
                    Toast.LENGTH_SHORT
                ).show()
            } else { //Permission is not available
                Toast.makeText(
                    this,
                    "Draw over other app permission not available.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }*/

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
                                (rv_application_list?.adapter as AddedAppsListAdapterNew).getItem(
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
                    startActivityForResult(
                        Intent(this, AddApplicationActivity::class.java),
                        Constants.ACTION.ACTION_SAVE_APPLICATION
                    )
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
                startActivityForResult(
                    Intent(this, AddApplicationActivity::class.java),
                    Constants.ACTION.ACTION_SAVE_APPLICATION
                )
            }
        }

        /**
         * setup the SpeedDialView
         */

        if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_VIDEO_SHOWED)) {
            /**
             * Tutorial Button
             */
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

            /**
             * Add Website Button
             */
            val drawableThree = AppCompatResources.getDrawable(this, R.drawable.web)
            val itemThree = SpeedDialActionItem.Builder(R.id.fab_action3, drawableThree)
                .setFabImageTintColor(
                    ResourcesCompat.getColor(
                        resources, R.color.color_white,
                        theme
                    )
                )
                .setLabel("Add Website")
                .setContentDescription("Webhook alarm")
                .setLabelColor(Color.WHITE)
                .setFabSize(FloatingActionButton.SIZE_MINI)
                .setFabBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.colorOfNavigationBar,
                        theme
                    )
                )
                .setLabelBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.colorOfNavigationBar,
                        theme
                    )
                )
                .setLabelClickable(true)
                .create()

            /**
             * Add App Button
             */
            val drawable = AppCompatResources.getDrawable(this, R.drawable.apps)
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

            try {
                speedDial.addActionItem(itemTwo)
                speedDial.addActionItem(itemThree)
                speedDial.addActionItem(itemOne)
            } catch (e: NullPointerException) {

            }

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

                    R.id.fab_action3 -> {
                        startActivity(
                            Intent(this, AddWebsiteActivity::class.java),
                        )
                        speedDial.close()
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
                "If you enable AutoStart option, it will help Zaxroid to run more " +
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


    /**
     * show warning dialog for battery and autostart
     */
    private fun showWarningDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_warning_layout)
        val fabClose = dialog.findViewById<FloatingActionButton>(R.id.fabClose)
        fabClose.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }

        val cardAutoStart = dialog.findViewById<CardView>(R.id.cardAutoStart)
        cardAutoStart.setOnClickListener {
            try {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
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

        val cardBatteryOptimization = dialog.findViewById<CardView>(R.id.cardBatteryOptimization)
        cardBatteryOptimization.setOnClickListener {
            try {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
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


        val window: Window = dialog.window!!
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun changeStateOfSpeedDial() {
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
     * BUG: If comes from previous version it's showing sound level reduced by -0 percent, Note:
     * As the previous version doesn't have some variables
     * Note: Same for PRO Dialog showing
     * @Shutdown: By MK at 15th Jun 22
     */

    private fun showLowVolumeWarning() {
        if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED) &&
            SharedPrefUtils.readInt(Constants.PreferenceKeys.SHOW_SOUND_WARNING_COUNT) <
            SharedPrefUtils.readInt(Constants.PreferenceKeys.SHOW_PRO_DIALOG_COUNT)
        ) {
            SharedPrefUtils.write(
                Constants.PreferenceKeys.SHOW_SOUND_WARNING_COUNT,
                SharedPrefUtils.readInt(Constants.PreferenceKeys.SHOW_PRO_DIALOG_COUNT)
            )
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.layout_dialog_warning)
            val txtMsg = dialog.findViewById<TextView
                    >(R.id.text_warning_sub_title)
            val html = String.format(
                "Your alarm volume getting low by : <font color='red'><b>- %d%%</b></font>",
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
                if (dialog.isShowing) {
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
                    SharedPrefUtils.readInt(Constants.PreferenceKeys.MAIN_SCREEN_OPENED)) >= 5
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
                    SharedPrefUtils.write(
                        Constants.PreferenceKeys.SHOW_PRO_DIALOG_COUNT,
                        SharedPrefUtils.readInt(Constants.PreferenceKeys.SHOW_PRO_DIALOG_COUNT) + 1
                    )
                }
            }
        }
        /**
         * Turned off Sound warning by MK at 15th Jun 22
         */
        /*else{
                showLowVolumeWarning()
            }*/
    }


    private fun setupAppsRecyclerView() {
        rv_application_list?.isVerticalScrollBarEnabled = true
        val arraylist = ArrayList<ApplicationEntity>()
        rv_application_list?.adapter = AddedAppsListAdapterNew(arraylist, this)
    }


    override fun onGetAlarmApplicationSuccess(appsList: ArrayList<ApplicationEntity>) {
        runOnUiThread {
            try {
                if (appsList.isNotEmpty()) {

                    Log.d("MainActivity", "onGetAlarmApplicationSuccess: ${GsonBuilder().setPrettyPrinting().create().toJson(appsList)}")

                    val applicationList : ArrayList<ApplicationEntity> = arrayListOf()

                    for(i in 0 until appsList.size){

                        if(applicationList.isEmpty()){
                            applicationList.add(appsList[i])
                        } else {
                            for(j in 0 until applicationList.size){
                                if(appsList[i].packageName != applicationList[j].packageName){
                                    applicationList.add(appsList[i])
                                }
                            }
                        }
                    }

                    Log.d("MainActivity", "Manipulated List: ${GsonBuilder().setPrettyPrinting().create().toJson(applicationList)}")

                    dataState()
                    (rv_application_list?.adapter as AddedAppsListAdapterNew).addItems(applicationList)
                    recyclerViewSwipeHandler()
                } else {
                    emptyState()
                }
            } catch (_: NullPointerException) {

            }
            //check if any app is added, if added then ask
            //only if app is opened, not from coming from different activity
            if (isFromLauncher && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(
                    this
                ) && rv_application_list?.adapter?.itemCount!! > 0
            ) {
                //for better alarming please enable the permission to draw message alarm on top of other apps
                DialogUtils.showDialogDrawOverApp(this,
                    "Better Alarming",
                    "Please enable draw over other app " +
                            "permission for better alarming experience",
                    object : DialogUtils.Callback {
                        override fun onPositive() {
                            getWindowManagerPermission()
                        }

                        override fun onNegative() {

                        }
                    })
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
        txt_hint_home?.gone(false)
        rv_application_list?.gone(false)
        img_empty_state?.visible(false)
        transparent_status_bar?.gone(false)
        binding.toolbar.visible(false)
        txt_applications?.gone(false)
        layout_top_part?.gone(false)
        txt_empty_state_title?.visible(false)
        txt_empty_state_desc?.visible(false)
    }

    private fun dataState() {
        rv_application_list?.visible(false)
        txt_hint_home?.visible(false)
        img_empty_state?.gone(false)
        layout_top_part?.visible(true)
        transparent_status_bar?.visible(false)
        binding.toolbar.gone(false)
        txt_applications?.visible(false)
        txt_empty_state_title?.gone(false)
        txt_empty_state_desc?.gone(false)
    }

    override fun onGetAlarmApplicationError() {

    }


    private fun showVideoTutorial() {
        /**
         * Updating the tutorial viewing experience at this branch
         */
        try {
            val bottomSheet = TutorialBottomSheetDialog(this)
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        } catch (e: IllegalStateException) {

        }
    }


    override fun onApplicationDeleteSuccess(position: Int) {
        runOnUiThread {
            (rv_application_list?.adapter as AddedAppsListAdapterNew).deleteItem(position)
            Toasty.success(this, getString(R.string.app_delete_success)).show()
            if ((rv_application_list?.adapter as AddedAppsListAdapterNew).itemCount == 0) {
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
//        runOnUiThread {
//            txt_auto_start_detail?.visible(false)
//            txt_auto_start_enable?.visible(false)
//        }
        runOnUiThread {
            menu?.getItem(1)?.isVisible = true
        }
    }

    override fun onAutoStartTextHide() {
//        runOnUiThread {
//            txt_auto_start_detail?.gone(false)
//            txt_auto_start_enable?.gone(false)
//        }
        runOnUiThread {
            menu?.getItem(1)?.isVisible = false
        }
    }

    override fun onBatteryTextShow() {
//        runOnUiThread {
//            try {
//                if (txt_auto_start_detail.isVisibile()) {
//                    //make txt battery detail top margin only 8 dp
//                    (txt_battery_detail.layoutParams as ConstraintLayout.LayoutParams).apply {
//                        topMargin = ViewUtils.dpToPx(6).toInt()
//                    }
//                } else {
//                    //make txt battery detail top margin only 12 dp
//                    (txt_battery_detail.layoutParams as ConstraintLayout.LayoutParams).apply {
//                        topMargin = ViewUtils.dpToPx(12).toInt()
//                    }
//                }
//            } catch (e: Exception) {
//                //skip it to default
//            }
//            txt_battery_detail?.visible(false)
//            txt_battery_enable?.visible(false)
//        }
//
        runOnUiThread {
            menu?.getItem(1)?.isVisible = true
        }
    }

    override fun onBatteryTextHide() {
//        runOnUiThread {
//            txt_battery_detail?.gone(false)
//            txt_battery_enable?.gone(false)
//        }
        runOnUiThread {
            menu?.getItem(1)?.isVisible = false
        }
    }

    private fun showEditDialog(app: ApplicationEntity,selectedNotifyOption:String) {
        //refresh adapter first
        if (!isFinishing) {
            try {
                if (selectedNotifyOption == Constants.NotifyOptions.ALARM) {
                    if (!bottomSheetModel.isAdded) {
                        val bundle = Bundle()
                        bundle.putBoolean(Constants.BundleKeys.IS_EDIT_MODE, true)
                        bundle.putString(Constants.BundleKeys.PACKAGE_NAME, app.packageName)
                        bottomSheetModel.arguments = bundle
                        bottomSheetModel.show(supportFragmentManager, "OPTIONS")
                    }
                } else if (selectedNotifyOption == Constants.NotifyOptions.SPEAK) {
                    if (!bottomSpeakOptionDialog.isAdded) {
                        val bundle = Bundle()
                        bundle.putBoolean(Constants.BundleKeys.IS_EDIT_MODE, true)
                        bundle.putString(Constants.BundleKeys.PACKAGE_NAME, app.packageName)
                        bottomSpeakOptionDialog.arguments = bundle
                        bottomSpeakOptionDialog.show(supportFragmentManager, "OPTIONS")
                    }
                } else if (selectedNotifyOption == Constants.NotifyOptions.CUSTOM) {
                    toast("Not available now")
                }
            } catch (e: java.lang.IllegalStateException) {
                //skip the crash
            }
        }
    }


    override fun onItemClick(app: ApplicationEntity, selectedNotifyOption: String) {
        //refresh adapter first
        showEditDialog(app,selectedNotifyOption)
    }

    override fun onItemDeleteClick(app: ApplicationEntity, id: Int) {
        DialogUtils.showDialog(this@AlarmApplicationActivity,
            getString(R.string.delete_app_title),
            getString(R.string.delete_app_message),
            object : DialogUtils.Callback {
                override fun onPositive() {
                    alarmAppPresenter.deleteApplication(
                        app,
                        id
                    )
                }

                override fun onNegative() {
                    rv_application_list?.adapter?.notifyDataSetChanged()
                }
            })
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

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchase(purchases)
        }
    }

    private fun handlePurchase(purchases: MutableList<Purchase>) {
        for (purchase in purchases) {
            // Check if the purchase has been completed
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                // The purchase has been completed
                if (purchase.isAcknowledged
                ) {
                    // The user has acknowledged the purchase, so it is not canceled
                    // Handle the completed purchase
                    //first time app open
                    setIsPurchased(true)
                    SharedPrefUtils.write(Constants.PreferenceKeys.WAS_SUBSCRIBED, true)
                    if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_RESTORED_SHOWED)) {
                        Thread.sleep(2000)
                        runOnUiThread {
                            Toasty.success(
                                applicationContext,
                                "Your subscription is restored!", Toast.LENGTH_LONG
                            ).show()
                        }
                        SharedPrefUtils.write(Constants.PreferenceKeys.IS_RESTORED_SHOWED, true)
                    }
                } else {
                    //show a dialog that sorry to see you go and know why the user canceled the purchase
                    // The user has not acknowledged the purchase, so assume it has been canceled
                    // Handle the canceled purchase
                    setIsPurchased(false)
                    runOnUiThread {
                        Toasty.info(
                            applicationContext,
                            "Your purchase was not acknowledged!", Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                // The purchase is still pending
                // Handle the pending purchase
                runOnUiThread {
                    Toasty.success(
                        applicationContext,
                        "Your purchase is processing, Please wait a bit!", Toast.LENGTH_LONG
                    ).show()
                }
            } else if (purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                // The purchase state could not be determined
                // Handle the unspecified state
                setIsPurchased(false)
                runOnUiThread {
                    Toasty.error(
                        applicationContext,
                        "Something wrong with your subscription, Contact us!", Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setIsPurchased(boolean: Boolean) {
        SharedPrefUtils.write(Constants.PreferenceKeys.IS_PURCHASED, boolean)
        //re-create the menu
        invalidateOptionsMenu()
    }


    /**
     * FOR TESTING THE ALARM WINDOW
     * */
    private fun getWindowManagerPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION)
    }

    @Deprecated("Do not use this in production code")
    private fun testWindowNotification() {
        val intent = Intent(this@AlarmApplicationActivity, WindowManagerService::class.java)
        intent.putExtra(Constants.IntentKeys.APP_NAME, "WhatsApp")
        intent.putExtra(Constants.IntentKeys.PACKAGE_NAME, "com.whatsapp")
        intent.putExtra(Constants.IntentKeys.TITLE, "Sender name")
        intent.putExtra(Constants.IntentKeys.DESC, "says hi")

        intent.putExtra(Constants.IntentKeys.NUMBER_OF_PLAY, 1)
        intent.putExtra(Constants.IntentKeys.IS_VIBRATE, true)
//        intent.putExtra(Constants.IntentKeys.TONE, "")
        intent.putExtra(Constants.IntentKeys.IS_JUST_VIBRATE, false)
        intent.putExtra(
            Constants.IntentKeys.IMAGE_PATH,
            "/storage/emulated/0/Android/data/com.app.messagealarm/files/.message_alarm/com.whatsapp.png"
        )
        intent.putExtra(Constants.IntentKeys.SOUND_LEVEL, 100)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        startService(intent)
        finish()
    }


}
