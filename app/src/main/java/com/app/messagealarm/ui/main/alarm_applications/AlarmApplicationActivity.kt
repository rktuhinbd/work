package com.app.messagealarm.ui.main.alarm_applications

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.service.app_reader_intent_service.AppsReaderIntentService
import com.app.messagealarm.service.notification_service.NotificationListener
import com.app.messagealarm.ui.adapters.AddedAppsListAdapter
import com.app.messagealarm.ui.main.add_apps.AddApplicationActivity
import com.app.messagealarm.ui.main.add_options.AddApplicationOption
import com.app.messagealarm.ui.onboarding.OnboardingDialog
import com.app.messagealarm.ui.setting.SettingsActivity
import com.app.messagealarm.utils.*
import com.app.messagealarm.work_manager.WorkManagerUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import es.dmoral.toasty.Toasty
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_add_app_options.*
import java.io.File
import kotlin.system.exitProcess


class AlarmApplicationActivity : BaseActivity(), AlarmApplicationView,
    AddedAppsListAdapter.ItemClickListener {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    var mMessageReceiver:BroadcastReceiver? = null
    val bottomSheetModel = AddApplicationOption()
    val REQUEST_CODE_PICK_AUDIO = 1
    private val alarmAppPresenter = AlarmApplicationPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        changeTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setToolBar()
        setListener()
        askForPermission()
        handleService()
        setupAppsRecyclerView()
        lookForTablesSize()
        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics

        //schedule quickstart
        if(!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_TUTORIAL_SHOW)){
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                showQuickStartDialog()
            }, 3000)
        }

         mMessageReceiver =  object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                switch_alarm_status?.isChecked = false
            }
        }
    }

    private fun lookForTablesSize(){
        alarmAppPresenter.getRequiredTableSize()
    }

    private fun lookForAlarmApplication() {
        alarmAppPresenter.getApplicationList()
    }

    override fun onResume() {
        super.onResume()
        if(BaseApplication.installedApps.isEmpty()){
            val mIntent = Intent(this, AppsReaderIntentService::class.java)
            AppsReaderIntentService.enqueueWork(this, mIntent)
        }
        lookForAlarmApplication()
        val isServiceStopped =
            SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_SERVICE_STOPPED)
        switch_alarm_status?.isChecked = !isServiceStopped
        this.registerReceiver(mMessageReceiver,  IntentFilter("turn_off_switch"))
    }


    override fun onPause() {
        super.onPause()
        this.unregisterReceiver(mMessageReceiver)
    }


    private fun setToolBar() {
        setSupportActionBar(findViewById(R.id.toolbar))
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item_home, menu)
        if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)) {
            MenuTintUtils.tintAllIcons(menu, Color.WHITE)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnu_setting -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (REQUEST_CODE_PICK_AUDIO == requestCode) {
            if (resultCode == Activity.RESULT_OK && data!!.data != null) {
                val fileName = File(PathUtils.getPath(this, data.data!!)!!).name
                try {
                    if(MediaUtils.getDurationOfMediaFle(PathUtils.getPath(this, data.data!!)!!) >= 30){
                        bottomSheetModel.txt_ringtone_value?.text = fileName
                        bottomSheetModel.setToneName(fileName)
                        bottomSheetModel.alarmTonePath = PathUtils.getPath(this, data.data!!)!!
                    }else{
                        bottomSheetModel.txt_ringtone_value?.text = "Default"
                        bottomSheetModel.setToneName("Default")
                        bottomSheetModel.alarmTonePath = null
                        DialogUtils.showSimpleDialog(
                            this, getString(R.string.txt_wrong_duration),
                            getString(R.string.txt_selected_music_duration)
                        )
                    }
                }catch (e: IllegalArgumentException){
                    bottomSheetModel.txt_ringtone_value?.text = "Default"
                    bottomSheetModel.setToneName("Default")
                    bottomSheetModel.alarmTonePath = null
                    DialogUtils.showSimpleDialog(
                        this, getString(R.string.txt_music),
                        getString(R.string.txt_try_again)
                    )
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
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

    private fun askForPermission() {
        PermissionUtils.requestPermission(
            this, android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        PermissionUtils.requestPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        PermissionUtils.requestPermission(
            this,
            android.Manifest.permission.SYSTEM_ALERT_WINDOW
        )
        PermissionUtils.requestPermission(
            this,
            android.Manifest.permission.RECEIVE_BOOT_COMPLETED
        )
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
            startActivity(Intent(this, AddApplicationActivity::class.java))
        }

        switch_alarm_status?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                ic_alarm_status?.setImageResource(R.drawable.ic_on_button)
                startMagicService()
            } else {
                ic_alarm_status?.setImageResource(R.drawable.ic_off_button)
                stopService()
            }
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
            if (appsList.isNotEmpty()) {
                (rv_application_list?.adapter as AddedAppsListAdapter).addItems(appsList)
                recyclerViewSwipeHandler()
                dataState()
            } else {
                emptyState()
            }
        }
    }

    private fun emptyState() {
        rv_application_list?.visibility = View.GONE
        img_empty_state?.visibility = View.VISIBLE
        txt_empty_state_title?.visibility = View.VISIBLE
        txt_empty_state_desc?.visibility = View.VISIBLE
    }

    private fun dataState() {
        rv_application_list?.visibility = View.VISIBLE
        img_empty_state?.visibility = View.GONE
        txt_empty_state_title?.visibility = View.GONE
        txt_empty_state_desc?.visibility = View.GONE
    }

    override fun onGetAlarmApplicationError() {

    }

    private fun showQuickStartDialog(){
        try {
            val quickStartDialog = OnboardingDialog()
            quickStartDialog.show(supportFragmentManager, "quick_start")
        }catch (e: IllegalStateException){

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

    override fun onItemClick(app: ApplicationEntity) {
        //refresh adapter first
        if (!bottomSheetModel.isAdded) {
            val bundle = Bundle()
            bundle.putBoolean(Constants.BundleKeys.IS_EDIT_MODE, true)
            bundle.putString(Constants.BundleKeys.PACKAGE_NAME, app.packageName)
            bottomSheetModel.arguments = bundle
            bottomSheetModel.show(supportFragmentManager, "MAIN")
        }
    }

    override fun onLongClick(app: ApplicationEntity) {

    }

    override fun onApplicationSwitch(boolean: Boolean, id: Int) {
        alarmAppPresenter.updateAppStatus(boolean, id)
    }

}
