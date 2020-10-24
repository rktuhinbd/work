package com.app.messagealarm.utils

import android.database.sqlite.SQLiteException
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import java.lang.NullPointerException
import java.lang.NumberFormatException
import java.util.*
import java.util.concurrent.TimeUnit

class SnoozeUtils {
    companion object {
        fun activateSnoozeMode(packageName: String) {
            var snoozeInterval = 20
            snoozeInterval = try {
                SharedPrefUtils.readString(Constants.PreferenceKeys.SNOOZE_TIME)
                    .split(" ")[0].toInt()
            } catch (e: NumberFormatException) {
                20
            }
            val dateMilli =
                System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(snoozeInterval.toLong())
            val appDataBase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
            Thread(Runnable {
                try {
                    appDataBase.applicationDao().addSnooze(packageName, dateMilli.toString())
                } catch (e: SQLiteException) {

                } catch (e: NullPointerException) {

                }
            }).start()
        }

        fun isSnoozedModeActivate(): Boolean {
            return false
        }
    }
}