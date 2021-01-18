package com.app.messagealarm.service

import android.database.sqlite.SQLiteException
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import java.lang.NullPointerException

class AlarmServicePresenter {
    companion object{
        fun updateAppStatus(boolean: Boolean, id:Int){
            val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
            Thread(Runnable {
                try{
                    appDatabase.applicationDao().updateAppStatus(boolean, id)
                }catch (e: NullPointerException){
                    e.printStackTrace()
                }catch (e: SQLiteException){
                    e.printStackTrace()
                }
            }).start()
        }
    }
}