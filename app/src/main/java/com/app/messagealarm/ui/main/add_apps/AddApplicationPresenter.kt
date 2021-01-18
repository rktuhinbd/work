package com.app.messagealarm.ui.main.add_apps

import android.app.Activity
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.service.AppsReader
import com.app.messagealarm.utils.DataUtils

class AddApplicationPresenter(
    private val addApplicationView: AddApplicationView,
    private val activity: Activity
) {


    fun getAllApplicationList() {
        Thread(Runnable {
            try {
                addApplicationView.onAllApplicationGetSuccess(
                    AppsReader.getInstalledApps(true, activity)!!
                )
            } catch (e: Exception) {
                addApplicationView.onAllApplicationGetError(DataUtils.getString(R.string.something_wrong))
            }
        }).start()
    }

    fun filterByMessaging(
    ) {
        Thread(Runnable {
            val installedAppsList = AppsReader.getInstalledApps(true, activity)
            val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
            val appList = appDatabase.appDao().appsList
            val holder = ArrayList<InstalledApps>()
            for (x in installedAppsList!!) {
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