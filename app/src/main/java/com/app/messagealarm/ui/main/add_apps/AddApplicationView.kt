package com.app.messagealarm.ui.main.add_apps

import com.app.messagealarm.model.InstalledApps

interface AddApplicationView {
    fun onAllApplicationGetSuccess(list: ArrayList<InstalledApps>)
    fun onAllApplicationGetError(message: String)
    fun onApplicationFiltered(list: ArrayList<InstalledApps>)
    fun onSyncFailed(message:String)
}