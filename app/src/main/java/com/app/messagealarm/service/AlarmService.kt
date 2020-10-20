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
                        //check for alarm repeat
                        if(alarmRepeatOutput(app.alarmRepeat, app)){
                            //check for title not null
                            if(sbn.notification.extras["android.title"] != null){
                                //check for player not playing
                                if(!ExoPlayerUtils.isPlaying()){
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
                        FloatingNotification.showFloatingNotification(app.numberOfPlay, app.isVibrateOnAlarm,service, app.tone_path)
               }
            }else{
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                    startAlarmActivity(service,null, sbn, app)
                }else{
                    //check activity is not open
                        FloatingNotification.showFloatingNotification(app.numberOfPlay, app.isVibrateOnAlarm, service, null)
                }
            }
        }


        private fun alarmRepeatOutput(repeat:String, app:ApplicationEntity):Boolean{
            var isPlayAble = false
            when (repeat) {
                "Once" -> {
                    //play one time and switch off the status
                    if(app.isRunningStatus){
                        isPlayAble = true
                        AlarmServicePresenter.updateAppStatus(false, app.id)
                    }
                }
                "Daily" -> {
                    //play every date and every time
                    if(app.isRunningStatus){
                        isPlayAble = true
                    }
                }
                "Custom" -> {
                    //check the for the days, if the day match then please
                    if(app.isRunningStatus){
                        if(checkWithCurrentDay(app.repeatDays)){
                            isPlayAble = true
                        }
                    }
                }

                else -> isPlayAble = false
            }
            return isPlayAble
        }

        private fun checkWithCurrentDay(days:String) : Boolean{
            val list = days.split(", ")
            var isToday = false
            for (x in list){
                if(x == TimeUtils.getCurrentDayName()){
                    isToday = true
                    break
                }
            }
            return isToday
        }

        private fun startAlarmActivity(service: Service, tone:String?, sbn: StatusBarNotification?, app:ApplicationEntity){
            //when alarm is playing with activity and the thread is not finished then user dismissed the alarm, then it's playing with notification again
            //temporary fixed by reducing waiting time from 4 sec to 2 sec.
            //need to check device to device for more result
            val once = Once()
            once.run(Runnable {
                AlarmCheckerThread(AlarmCheckerThread.PlayListener { s ->
                    if(!s){
                        FloatingNotification.showFloatingNotification(app.numberOfPlay, app.isVibrateOnAlarm, service, app.tone_path)
                    }
                }).execute()
            })
            val title = sbn?.notification!!.extras["android.title"].toString()
                    val desc = sbn.notification!!.extras["android.text"].toString()
                    val intent = Intent(service, AlarmActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra(Constants.IntentKeys.NUMBER_OF_PLAY, app.numberOfPlay)
                    intent.putExtra(Constants.IntentKeys.APP_NAME, app.appName)
                    intent.putExtra(Constants.IntentKeys.IS_VIBRATE, app.isVibrateOnAlarm)
                    intent.putExtra(Constants.IntentKeys.PACKAGE_NAME, app.packageName)
                    intent.putExtra(Constants.IntentKeys.TONE, tone)
                    intent.putExtra(Constants.IntentKeys.IMAGE_PATH, app.bitmapPath)
                    intent.putExtra(Constants.IntentKeys.TITLE, title)
                    intent.putExtra(Constants.IntentKeys.DESC, desc)
                    service.startActivity(intent)
        }
    }
}