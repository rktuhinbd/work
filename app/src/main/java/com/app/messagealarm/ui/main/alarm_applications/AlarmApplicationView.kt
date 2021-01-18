package com.app.messagealarm.ui.main.alarm_applications

import com.app.messagealarm.model.entity.ApplicationEntity

interface AlarmApplicationView {
    fun onGetAlarmApplicationSuccess(appsList:ArrayList<ApplicationEntity>)
    fun onGetAlarmApplicationError()
    fun onApplicationDeleteSuccess(position:Int)
    fun onApplicationDeleteError()
    fun onAppStatusUpdateSuccess()
    fun onAppStatusUpdateError(message:String)
    fun onRemovedFromSnoozeSuccess()
    fun onTablesSizeRequestSuccess(appSize:Int, langSize:Int, appConstrainSize:Int)
}