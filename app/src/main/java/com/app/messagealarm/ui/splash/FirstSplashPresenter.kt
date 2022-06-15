package com.app.messagealarm.ui.splash

import android.database.sqlite.SQLiteException
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationView
import java.lang.NullPointerException

class FirstSplashPresenter(private val alarmApplicationView: FirstSplashView) {
    fun getRequiredTableSize() {
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread {
            try {
                val appSize = appDatabase.appDao().totalCountOfApp
                val langSize = appDatabase.languageDao().totalCountOfLanguage
                val appConstrainSize = appDatabase.appConstrainDao().totalCountOfAppConstrain
                if (appSize == 0 || appConstrainSize == 0) {
                    alarmApplicationView.onTablesSizeRequestSuccess(
                        appSize,
                        langSize,
                        appConstrainSize
                    )
                }
            } catch (e: SQLiteException) {

            } catch (e: NullPointerException) {

            }
        }.start()
    }
}