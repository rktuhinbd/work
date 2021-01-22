package com.app.messagealarm.ui.main.add_apps

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.service.AppsReader
import com.app.messagealarm.service.app_reader_intent_service.AppsReaderIntentService
import com.app.messagealarm.utils.DataUtils

class AddApplicationPresenter(

    private val addApplicationView: AddApplicationView,
    private val activity: Activity
) {

    fun getAllApplicationList() {
        Thread(Runnable {
            try {
                Log.e("APP_L", BaseApplication.installedApps.size.toString())
                if(BaseApplication.installedApps.isEmpty()){
                    addApplicationView.onAllApplicationGetSuccess(
                        AppsReader.getInstalledApps(false, activity)!!
                    )
                }else{
                    addApplicationView.onAllApplicationGetSuccess(
                        BaseApplication.installedApps as ArrayList<InstalledApps>
                    )
                }
            } catch (e: Exception) {
                addApplicationView.onAllApplicationGetError(DataUtils.getString(R.string.something_wrong))
            }
        }).start()
    }

    fun filterByMessaging(
    ) {
        Thread(Runnable {
            var installedAppsList = emptyList<InstalledApps>()
            installedAppsList = if(BaseApplication.installedApps.isEmpty()){
                AppsReader.getInstalledApps(false, activity)!!
            }else{
                BaseApplication.installedApps
            }
            val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
            val appList = appDatabase.appDao().appsList
            val holder = ArrayList<InstalledApps>()
            for (x in installedAppsList) {
                for (y in appList) {
                    if (x.packageName == y.appPackageName) {
                        holder.add(x)
                    }
                }
            }
            addApplicationView.onApplicationFiltered(holder)
        }).start()

    }
}