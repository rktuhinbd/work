package com.app.messagealarm.service.notification_service

import android.service.notification.StatusBarNotification
import com.app.messagealarm.model.entity.ApplicationEntity

interface NotificationListenerView {
    fun onApplicationListGetSuccess(list:List<ApplicationEntity>, sbn:StatusBarNotification?)
    fun onApplicationListGetError()
    fun isPlayAbleSuccess(sbn: StatusBarNotification?)
}