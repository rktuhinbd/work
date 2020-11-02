package com.app.messagealarm.service.notification_service

import android.database.sqlite.SQLiteException
import android.service.notification.StatusBarNotification
import android.util.Log
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import java.lang.NullPointerException

class NotificationListenerPresenter(private val view: NotificationListenerView) {

    fun getApplicationList(sbn: StatusBarNotification?) {
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            try {
                view.onApplicationListGetSuccess(
                    appDatabase.applicationDao().allApplicationList,
                    sbn
                )
            } catch (e: NullPointerException) {
                view.onApplicationListGetError()
            } catch (e: SQLiteException) {
                view.onApplicationListGetError()
            }
        }).start()
    }

    fun filterByAppConstrains(
        packageName: String,
        langCode: String,
        title: String,
        desc: String,
        sbn: StatusBarNotification?
    ) {
        var isPlayAble = true
        val appDataBase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            val constrainList =
                appDataBase.appConstrainDao().getAppConstrains(packageName, langCode)
            for (x in constrainList) {

                if (x.title != null && x.description != null) {
                    if (!x.isContains) {
                        if (x.title == title || x.description == desc) {
                            isPlayAble = false
                            break
                        }
                    } else {
                        if (x.title.contains(title, true) ||
                            x.description.contains(desc, true)
                        ) {
                            isPlayAble = false
                            break
                        }
                    }
                }

                if (x.title == null) {
                    if (!x.isContains) {
                        if (x.description == desc) {
                            isPlayAble = false
                            break
                        }
                    } else {
                        if (x.description.contains(desc, true)) {
                            isPlayAble = false
                            break
                        }
                    }

                }

                if (x.description == null) {
                    if (!x.isContains) {
                        if (x.title == title) {
                            isPlayAble = false
                            break
                        }
                    } else {
                        if (x.title.contains(title, true)) {
                            isPlayAble = false
                            break
                        }
                    }
                }

                if (title.isEmpty()) {
                    isPlayAble = false
                    break
                }

                if (desc.isEmpty()) {
                    isPlayAble = false
                    break
                }

                if (title == "null") {
                    isPlayAble = false
                    break
                }

                if (desc == "null") {
                    isPlayAble = false
                    break
                }

            }

            if (isPlayAble) {
                view.isPlayAbleSuccess(sbn)
            }
        }).start()

    }
}