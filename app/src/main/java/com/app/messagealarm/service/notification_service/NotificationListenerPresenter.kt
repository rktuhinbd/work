package com.app.messagealarm.service.notification_service

import android.database.sqlite.SQLiteException
import android.service.notification.StatusBarNotification
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import java.lang.NullPointerException

class NotificationListenerPresenter (private val view: NotificationListenerView){

    var isExecuted = false

    fun getApplicationList(sbn:StatusBarNotification?){
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
                try{
                    if(!isExecuted){
                        view.onApplicationListGetSuccess(
                            appDatabase.applicationDao().allApplicationList,
                            sbn
                        )
                        isExecuted = true
                    }

                }catch (e: NullPointerException){
                    view.onApplicationListGetError()
                }catch (e: SQLiteException){
                    view.onApplicationListGetError()
                }
        }).start()
    }
}