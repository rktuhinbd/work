package com.app.messagealarm.ui.main.alarm_applications

import android.content.Intent
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.view.View
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
import com.app.messagealarm.utils.*
import es.dmoral.toasty.Toasty
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.activity_main.*


class AlarmApplicationActivity : BaseActivity(), AlarmApplicationView {

    private val alarmAppPresenter = AlarmApplicationPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setListener()
        askForPermission()
        handleService()
    }

    private fun lookForAlarmApplication(){
        alarmAppPresenter.getApplicationList()
    }

    override fun onResume() {
        super.onResume()
        lookForAlarmApplication()
    }

    private fun handleService(){
        val isServiceStopped = SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_SERVICE_STOPPED)
        if(SharedPrefUtils.contains(Constants.PreferenceKeys.IS_SERVICE_STOPPED)){
            if(isServiceStopped){
                switch_alarm_status?.isChecked = false
            }else{
                switch_alarm_status?.isChecked = true
                startMagicService()
            }
        }else{
            startMagicService()
        }
    }

    private fun askForPermission(){
        PermissionUtils.requestPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
    }

    private fun recyclerViewSwipeHandler(){
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
                ).addSwipeLeftBackgroundColor(ContextCompat.getColor(this@AlarmApplicationActivity, R.color.colorAccent))
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
                DialogUtils.showDialog(this@AlarmApplicationActivity, getString(R.string.delete_app_title),
                    getString(R.string.delete_app_message), object :DialogUtils.Callback{
                        override fun onPositive() {
                            alarmAppPresenter.deleteApplication(
                                (rv_application_list?.adapter as AddedAppsListAdapter).getItem(viewHolder.adapterPosition),
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


    private fun stopService(){
        if(AndroidUtils.isServiceRunning(this, NotificationListener::class.java)){
            val intent = Intent(this, NotificationListener::class.java)
            intent.action = NotificationListener.ACTION_STOP_FOREGROUND_SERVICE
            startService(intent)
        }
    }

    private fun startMagicService(){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(this, NotificationListener::class.java)
                startForegroundService(intent)
            }else{
                val intent = Intent(this, NotificationListener::class.java)
                startService(intent)
            }
    }


    private fun setListener(){

        fab_button_add_application?.setOnClickListener {
            startActivity(Intent(this, AddApplicationActivity::class.java))
        }

        switch_alarm_status?.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
               ic_alarm_status?.setImageResource(R.drawable.ic_on_button)
               startMagicService()
            }else{
                ic_alarm_status?.setImageResource(R.drawable.ic_off_button)
                stopService()
            }
        }
    }

    private fun setupAppsRecyclerView(appsList:ArrayList<ApplicationEntity>){
        rv_application_list?.layoutManager = LinearLayoutManager(this)
        rv_application_list?.isVerticalScrollBarEnabled = true
        rv_application_list?.adapter = AddedAppsListAdapter(appsList)
    }


    override fun onGetAlarmApplicationSuccess(appsList: ArrayList<ApplicationEntity>) {
        runOnUiThread {
            if(appsList.isNotEmpty()){
                setupAppsRecyclerView(appsList)
                recyclerViewSwipeHandler()
                dataState()
            }else{
                emptyState()
            }
        }
    }

    private fun emptyState(){
        rv_application_list?.visibility = View.GONE
        img_empty_state?.visibility = View.VISIBLE
        txt_empty_state_title?.visibility = View.VISIBLE
        txt_empty_state_desc?.visibility = View.VISIBLE
    }

    private fun dataState(){
        rv_application_list?.visibility = View.VISIBLE
        img_empty_state?.visibility = View.GONE
        txt_empty_state_title?.visibility = View.GONE
        txt_empty_state_desc?.visibility = View.GONE
    }

    override fun onGetAlarmApplicationError() {

    }

    override fun onApplicationDeleteSuccess(position:Int) {
        runOnUiThread {
            (rv_application_list?.adapter as AddedAppsListAdapter).deleteItem(position)
            Toasty.success(this, getString(R.string.app_delete_success)).show()
            if((rv_application_list?.adapter as AddedAppsListAdapter).itemCount == 0){
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

}
