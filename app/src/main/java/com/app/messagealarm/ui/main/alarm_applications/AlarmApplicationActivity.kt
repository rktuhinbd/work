package com.app.messagealarm.ui.main.alarm_applications

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.R
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.service.notification_service.NotificationListener
import com.app.messagealarm.ui.adapters.AddedAppsListAdapter
import com.app.messagealarm.ui.main.add_apps.AddApplicationActivity
import com.app.messagealarm.ui.main.add_options.AddApplicationOption
import com.app.messagealarm.ui.setting.SettingsActivity
import com.app.messagealarm.utils.*
import es.dmoral.toasty.Toasty
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_add_app_options.*
import java.io.File
import java.lang.NumberFormatException
import java.util.*
import kotlin.collections.ArrayList


class AlarmApplicationActivity : BaseActivity(), AlarmApplicationView,
    AddedAppsListAdapter.ItemClickListener {

    val bottomSheetModel = AddApplicationOption()
    val REQUEST_CODE_PICK_AUDIO = 1
    private val alarmAppPresenter = AlarmApplicationPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setToolBar()
        setListener()
        askForPermission()
        handleService()
        setupAppsRecyclerView()
    }


    private fun lookForAlarmApplication() {
        alarmAppPresenter.getApplicationList()
    }

    override fun onResume() {
        super.onResume()
        lookForAlarmApplication()
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
                bottomSheetModel.txt_ringtone_value?.text = fileName
                bottomSheetModel.setToneName(fileName)
                bottomSheetModel.alarmTonePath = PathUtils.getPath(this, data.data!!)!!
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun handleService() {
        val isServiceStopped =
            SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_SERVICE_STOPPED)
        if (SharedPrefUtils.contains(Constants.PreferenceKeys.IS_SERVICE_STOPPED)) {
            if (isServiceStopped) {
                switch_alarm_status?.isChecked = false
            } else {
                switch_alarm_status?.isChecked = true
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
                        R.color.colorAccent
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

    override fun onSnoozeClick(app: ApplicationEntity) {
        //click event from an item
        try {
            snoozeRemoveDialog(timeDifference(app.snoozedTime.toLong()), app.appName, app.packageName)
        } catch (e: NumberFormatException) {
            Toasty.info(this, getString(R.string.txt_invalid_request)).show()
        }
    }

    private fun timeDifference(time: Long): String {
        return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), 0).toString()
    }

    private fun snoozeRemoveDialog(difference: String, appName:String, packageName:String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_snooze_timer)
        val txtTitle = dialog.findViewById<TextView>(R.id.txt_discard)
        val txtDesc = dialog.findViewById<TextView>(R.id.txt_changes)
        txtDesc.text  = String.format("%s snooze has %s remaining, do you want to remove it now?", appName, difference)
        val txtCancel = dialog.findViewById<TextView>(R.id.btn_cancel)
        val txtDiscard = dialog.findViewById<TextView>(R.id.btn_discard)
        Objects.requireNonNull(dialog.window!!)
            .setBackgroundDrawableResource(android.R.color.transparent)
        txtCancel.setOnClickListener {
            if (dialog.isShowing) {
                dialog.cancel()
            }
        }
        txtDiscard.setOnClickListener {
            if (dialog.isShowing) {
                dialog.cancel()
            }
            alarmAppPresenter.removeFromSnooze(packageName)
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
