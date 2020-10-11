package com.app.messagealarm.service.notification_service

import android.database.sqlite.SQLiteException
import android.service.notification.StatusBarNotification
import android.util.Log
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import java.lang.NullPointerException

class NotificationListenerPresenter (private val view: NotificationListenerView){

    var isExecuted = false

    fun getApplicationList(sbn:StatusBarNotification?){
        Log.e("BEFORE", "TRUE")
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
                try{
                    if(!isExecuted){
                        Log.e("AFTER", "TRUE")
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