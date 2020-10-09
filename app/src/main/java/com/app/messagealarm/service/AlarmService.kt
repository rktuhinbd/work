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

        var isPlayAble = true
        var isThreadExecuted = false
        private const val MESSENGER_PKG = "com.facebook.orca"
        private const val WHATSAPP_PKG = "com.whatsapp"
        private const val VIBER_PKG = ""
        private const val IMO_PKG = ""

        fun playAlarmOnNotification(sbn: StatusBarNotification?, appsList:List<ApplicationEntity>, service: Service){
            when(sbn!!.packageName){
                MESSENGER_PKG ->{
                    messengerFilter(sbn.notification.extras["android.title"].toString())
                }
                WHATSAPP_PKG ->{
                    whatsAppFilter(
                        sbn.notification.extras["android.title"].toString(),
                        sbn.notification.extras["android.text"].toString())
                }
                IMO_PKG -> imoFilter()

                else -> isPlayAble = true
            }
            for (app in appsList){
                if(sbn.packageName != null){
                    if(sbn.packageName == app.packageName){
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
                    FloatingNotification.showFloatingNotification(service, app.tone_path)
               }
            }else{
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                    startAlarmActivity(service,null, sbn, app)
                }else{
                    FloatingNotification.showFloatingNotification(service, null)
                }
            }
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
                    intent.putExtra(Constants.IntentKeys.PACKAGE_NAME, app.packageName)
                    intent.putExtra(Constants.IntentKeys.TONE, tone)
                    intent.putExtra(Constants.IntentKeys.IMAGE_PATH, app.bitmapPath)
                    intent.putExtra(Constants.IntentKeys.TITLE, title)
                    intent.putExtra(Constants.IntentKeys.DESC, desc)
                    service.startActivity(intent)
        }



        private fun whatsAppFilter(title:String, desc:String){
            isPlayAble = !(title == "WhatsApp" ||
                    desc == "Checking for new messages" ||
                    desc == "8 new messages")
        }

        private fun messengerFilter(title:String){
            isPlayAble = title != "Chat heads active"
        }


        private fun viberFilter(){

        }

        private fun upworkFilter(){

        }

        private fun fiverrFilter(){

        }

        private fun freelancerFilter(){

        }

        private fun imoFilter(){

        }
    }
}