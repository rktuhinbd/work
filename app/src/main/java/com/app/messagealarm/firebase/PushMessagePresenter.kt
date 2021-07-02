package com.app.messagealarm.firebase

import android.database.sqlite.SQLiteException
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase

class PushMessagePresenter(private val pushMessageView: PushMessageView)  {

    fun cleanDb(){
        val appdatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            try {
                appdatabase.appConstrainDao().cleanAppConstrain()
                appdatabase.appDao().cleanMessagingApp()
                appdatabase.languageDao().cleanLanguage()
                pushMessageView.onDbCleanSuccess()
            }catch (e:SQLiteException){

            }
        }).start()
    }

}