package com.app.messagealarm.utils

import android.content.Context
import android.database.sqlite.SQLiteException
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import es.dmoral.toasty.Toasty
import java.lang.NullPointerException
import java.lang.NumberFormatException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Handler

class SnoozeUtils {
    companion object {
        fun activateSnoozeMode(packageName: String, appName:String, context: Context) {
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
                    val handler = android.os.Handler(context.mainLooper)
                    val runnable = Runnable(){
                        Toasty.info(context, String.format("%s snoozed for %s", appName,
                            SharedPrefUtils.readString(Constants.PreferenceKeys.SNOOZE_TIME))).show()
                    }
                    handler.post(runnable)
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