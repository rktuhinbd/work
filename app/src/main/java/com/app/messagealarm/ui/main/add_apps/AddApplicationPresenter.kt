package com.app.messagealarm.ui.main.add_apps

import android.app.Activity
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.ui.service.AppsReader
import com.app.messagealarm.utils.DataUtils

class AddApplicationPresenter(private val addApplicationView: AddApplicationView,
                              private val activity: Activity
                              ) {
    fun getAllApplicationList(){
        Thread(Runnable {
            try{
                addApplicationView.onAllApplicationGetSuccess(
                    AppsReader.getInstalledApps(false, activity)!!
                )
            }catch (e:Exception){
                addApplicationView.onAllApplicationGetError(DataUtils.getString(R.string.something_wrong))
            }
        }).start()
    }
}