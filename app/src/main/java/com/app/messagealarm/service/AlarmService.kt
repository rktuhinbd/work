package com.app.messagealarm.service

import android.app.Service
import android.os.Build
import android.service.notification.StatusBarNotification
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.ui.notifications.FloatingNotification
import com.app.messagealarm.utils.MediaUtils

class AlarmService() {
    companion object{

        private const val MESSENGER_PKG = ""
        private const val WHATSAPP_PKG = ""

        fun playAlarmOnNotification(sbn: StatusBarNotification?, appsList:List<ApplicationEntity>, service: Service){
            appsList.forEach {
                if(sbn?.packageName != null){
                    if(sbn.packageName == it.packageName){
                        if(sbn.notification.extras["android.title"] != null){
                            when(sbn.packageName){
                                MESSENGER_PKG -> messengerFilter(sbn.notification.extras["android.title"].toString())
                                WHATSAPP_PKG -> whatsAppFilter()
                            }
                            messengerFilter(sbn.notification.extras["android.title"].toString())
                                if(!MediaUtils.isPlaying()){
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        FloatingNotification.showFloatingNotification(service)
                                    }
                                }
                        }
                    }
                }
            }
        }

        private fun messengerFilter(title:String){
            if(title == "Chat heads active"){
                return
            }
        }

        private fun whatsAppFilter(){

        }

        private fun viberFilter(){

        }

        private fun upworkFilter(){

        }

        private fun fiverrFilter(){

        }

        private fun freelancerFilter(){

        }
    }
}