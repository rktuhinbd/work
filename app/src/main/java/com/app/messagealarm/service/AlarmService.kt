package com.app.messagealarm.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.service.notification.StatusBarNotification
import android.util.Log
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.ui.alarm.AlarmActivity
import com.app.messagealarm.ui.notifications.FloatingNotification
import com.app.messagealarm.utils.*
import com.judemanutd.autostarter.AutoStartPermissionHelper
import java.lang.Exception
import javax.security.auth.login.LoginException


class AlarmService {

    companion object{

        var isPlayAble = false

        fun playAlarmOnNotification(sbn: StatusBarNotification?, appsList:List<ApplicationEntity>, service: Service){
           //filter for apps
          //  filterApps(sbn)
            //find app and play
                findOutAppToPlay(sbn, appsList, service)
        }

        private fun findOutAppToPlay(sbn: StatusBarNotification?, appsList: List<ApplicationEntity>, service: Service){
            for (app in appsList){
                if(sbn?.packageName != null){
                    if(sbn.packageName == app.packageName){
                        //alarm repeat
                        alarmRepeatOutput(app.alarmRepeat, app)
                        if(sbn.notification.extras["android.title"] != null){
                            if(!ExoPlayerUtils.isPlaying()){
                                if(isPlayAble){
                                    magicPlay(app.ringTone, service, sbn, app)
                                }
                            }
                        }
                        break
                    }
                }
            }
        }

        private fun magicPlay(ringtone:String, service: Service, sbn: StatusBarNotification?,
                              app:ApplicationEntity){
            if(!ringtone.contains("Default")){
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                    startAlarmActivity(service, app.tone_path, sbn, app)
                }else{
                    //check if activity is not open
                    if(!BaseApplication.isActivityRunning()){
                        FloatingNotification.showFloatingNotification(service, app.tone_path)
                    }
               }
            }else{
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                    startAlarmActivity(service,null, sbn, app)
                }else{
                    //check activity is not open
                    if(!BaseApplication.isActivityRunning()){
                        FloatingNotification.showFloatingNotification(service, null)
                    }

                }
            }
        }

        private fun alarmRepeatOutput(repeat:String, app:ApplicationEntity){
            when {
                repeat.contains("Once") -> {
                    //play one time and switch off the status
                    if(app.isRunningStatus){
                        isPlayAble = true
                        AlarmServicePresenter.updateAppStatus(false, app.id)
                    }
                }
                repeat.contains("Daily") -> {
                    //play every date and every time
                    if(app.isRunningStatus){
                        isPlayAble = true
                    }
                }
                repeat.contains("Custom") -> {
                    //check the for the days, if the day match then please
                    if(app.isRunningStatus){
                        if(checkWithCurrentDay(app.repeatDays)){
                            isPlayAble = true
                        }
                    }
                }
            }
        }

        private fun checkWithCurrentDay(days:String) : Boolean{
            val list = days.split(", ")
            var isToday = false
            for (x in list){
                if(x == TimeUtils.getCurrentDayName()){
                    Log.e("CHECK", x + "==" + TimeUtils.getCurrentDayName())
                    isToday = true
                    break
                }
            }
            return isToday
        }

        private fun startAlarmActivity(service: Service, tone:String?, sbn: StatusBarNotification?, app:ApplicationEntity){
                AlarmCheckerThread(AlarmCheckerThread.PlayListener { s ->
                    if(!s){
                        FloatingNotification.showFloatingNotification(service, app.tone_path)
                    }
                }).execute()
            val title = sbn?.notification!!.extras["android.title"].toString()
                    val desc = sbn.notification!!.extras["android.text"].toString()
                    val intent = Intent(service, AlarmActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra(Constants.IntentKeys.APP_NAME, app.appName)
                    intent.putExtra(Constants.IntentKeys.PACKAGE_NAME, app.packageName)
                    intent.putExtra(Constants.IntentKeys.TONE, tone)
                    intent.putExtra(Constants.IntentKeys.IMAGE_PATH, app.bitmapPath)
                    intent.putExtra(Constants.IntentKeys.TITLE, title)
                    intent.putExtra(Constants.IntentKeys.DESC, desc)
                    service.startActivity(intent)
        }
    }
}