package com.app.messagealarm.service.notification_service

import android.database.sqlite.SQLiteException
import android.service.notification.StatusBarNotification
import android.util.Log
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import java.lang.NullPointerException

class NotificationListenerPresenter (private val view: NotificationListenerView){

    fun getApplicationList(sbn:StatusBarNotification?){
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
                try{
                        view.onApplicationListGetSuccess(
                            appDatabase.applicationDao().allApplicationList,
                            sbn
                        )
                }catch (e: NullPointerException){
                    view.onApplicationListGetError()
                }catch (e: SQLiteException){
                    view.onApplicationListGetError()
                }
        }).start()
    }
}