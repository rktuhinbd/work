package com.app.messagealarm.service

import android.app.Service
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.service.notification_service.NotificationListener
import com.app.messagealarm.ui.alarm.AlarmActivity
import com.app.messagealarm.ui.notifications.FloatingNotification
import com.app.messagealarm.utils.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import java.lang.NullPointerException
import java.util.*
import java.util.regex.Pattern


class AlarmService {

    companion object {

        private var firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

        fun playAlarmOnNotification(
            sbn: StatusBarNotification?,
            appsList: List<ApplicationEntity>,
            service: Service
        ) {
            //filter for apps
            //filterApps(sbn)
            //find app and play
            findOutAppToPlay(sbn, appsList, service)
        }

        private fun findOutAppToPlay(
            sbn: StatusBarNotification?,
            appsList: List<ApplicationEntity>,
            service: Service
        ) {
            for (app in appsList) {
                if (sbn?.packageName != null) {
                    if (sbn.packageName == app.packageName) {
                        //check for player not playing
                        //check for alarm repeat
                        if (checkByTimeConstrain(app)) {
                            //check for title not null
                            if (sbn.notification.extras["android.title"] != null) {
                                if (checkBySenderName(app, sbn)) {
                                    if (checkByIgnoredName(app, sbn)) {
                                        if (checkByMessageBody(app, sbn)) {
                                            //check if app is in not muted
                                            if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_MUTED)) {
                                                if (alarmRepeatOutput(app.alarmRepeat?:"", app)) {
                                                    if (!MediaUtils.isPlaying()) {
                                                        //save activity started as false
                                                        SharedPrefUtils.write(
                                                            Constants.PreferenceKeys.IS_ACTIVITY_STARTED,
                                                            false
                                                        )
                                                        magicPlay(app.ringtone?:"", service, sbn, app)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break
                    }
                }
            }
        }

        /**
         *
         */
        private fun alarmRecord(
            lastAppName: String,
            lastSenderName: String,
            lastAppIconPath: String
        ) {
            //Alarm Count
            SharedPrefUtils.write(
                Constants.PreferenceKeys.ALARM_COUNT,
                SharedPrefUtils.readInt(Constants.PreferenceKeys.ALARM_COUNT) + 1
            )
            if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)) {
                if (SharedPrefUtils.readInt(Constants.PreferenceKeys.SOUND_LEVEL) > Constants.Default.MIN_SOUND_LEVEL/*50*/) {
                    SharedPrefUtils.write(
                        Constants.PreferenceKeys.SOUND_LEVEL,
                        SharedPrefUtils.readInt(Constants.PreferenceKeys.SOUND_LEVEL) - 1
                    )
                    //start a thread and decrease the sound level of saved app
                    Thread {
                        val appDatabase =
                            AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
                        try {
//                            appDatabase.applicationDao()
//                                .rollBackAppsFromDefaultSoundLevel(AndroidUtils.getSoundLevel())//Todo: check this
                        } catch (e: NullPointerException) {

                        } catch (e: SQLiteException) {

                        }
                    }.start()
                }
            }
            //save last app name
            SharedPrefUtils.write(Constants.PreferenceKeys.LAST_APP_NAME, lastAppName)
            //save last sender_name
            SharedPrefUtils.write(Constants.PreferenceKeys.LAST_SENDER_NAME, lastSenderName)
            //save last app icon path
            SharedPrefUtils.write(Constants.PreferenceKeys.LAST_APP_ICON_NAME, lastAppIconPath)
        }

        /**
         * take decision on the notification or activity alarm for playing
         */
        private fun magicPlay(
            ringtone: String, service: Service, sbn: StatusBarNotification?,
            app: ApplicationEntity
        ) {
            //get sender name
            val title = sbn?.notification?.extras!!["android.title"].toString()
            val desc = sbn?.notification?.extras!!["android.text"].toString()
            /**
             * set alarm record
             */
            Thread {
                alarmRecord(app.appName?:"", title, app.bitmapPath?:"")
            }.start()

            /**
             * play alarm
             */
            if (!ringtone.contains("Default")) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    startAlarmActivity(service, app.tonePath, sbn, app)
                } else {
                    //check if activity is not open
                    FloatingNotification.showFloatingNotification(
                        app.soundLevel?:0,
                        title,
                        app.isJustVibrate?:false,
                        app.appName?:"",
                        app.packageName?:"",
                        app.numberOfPlay?:1,
                        app.vibrateOnAlarm?:false,
                        service,
                        app.tonePath,
                        // Added this 2 extra param for window notification - Mortuza
                        app.isFlashOn?:false,
                        desc,
                        app.bitmapPath?:""
                    )
                }
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    startAlarmActivity(service, null, sbn, app)
                } else {
                    //check activity is not open
                    FloatingNotification.showFloatingNotification(
                        app.soundLevel?:100,
                        title,
                        app.isJustVibrate?:false,
                        app.appName?:"",
                        app.packageName?:"",
                        app.numberOfPlay?:1,
                        app.vibrateOnAlarm?:false,
                        service,
                        null,
                        app.isAlarmEnabled?:false,
                        desc,
                        app.bitmapPath?:""
                    )
                }
            }
        }


        /**
         * Check by time constrain, via start time and end time
         */
        private fun checkByTimeConstrain(app: ApplicationEntity): Boolean {
            return if (app.isCustomTime == true) {
                TimeUtils.isConstrainedByTime(app.startTime?:"", app.endTime?:"")
            } else {
                true
            }
        }

        /**
         * CHECK BY SENDER NAMES FILTER
         */
        private fun checkBySenderName(
            app: ApplicationEntity,
            sbn: StatusBarNotification?
        ): Boolean {
            var result = false
            val title = sbn?.notification?.extras!!["android.title"]
            val nameArray = app.senderNames?.trim()?.split(", ")
            if (app.senderNames != "None") {
                if (nameArray != null) {
                    for (x in nameArray) {
                        if (title.toString().trim().toLowerCase(Locale.getDefault())
                                .contains(
                                    x.trim().toLowerCase(Locale.getDefault())
                                )
                            || x.trim().toLowerCase(Locale.getDefault())
                                .contains(
                                    title.toString().trim().toLowerCase(Locale.getDefault())
                                )
                        ) {
                            result = true
                            break
                        } else {
                            continue
                        }
                    }
                }
            } else {
                result = true
            }
            return result
        }

        /**
         * This function ignore alarm with ignored names
         * @param app app from local db
         * @param sbn notification object
         */
        private fun checkByIgnoredName(
            app: ApplicationEntity,
            sbn: StatusBarNotification?
        ): Boolean {
            var result = true
            val title = sbn?.notification?.extras!!["android.title"]
            val nameArray = app.ignoredNames?.trim()?.split(", ")
            if (app.ignoredNames != "None") {
                if (nameArray != null) {
                    for (x in nameArray) {
                        if (title.toString().trim().toLowerCase(Locale.getDefault())
                                .contains(
                                    x.trim().toLowerCase(Locale.getDefault())
                                )
                            || x.trim().toLowerCase(Locale.getDefault())
                                .contains(
                                    title.toString().trim().toLowerCase(Locale.getDefault())
                                )
                        ) {
                            result = false
                            break
                        } else {
                            continue
                        }
                    }
                }
            } else {
                result = true
            }
            return result
        }

        /**
         *constrained by message body
         */
        private fun checkByMessageBody(
            app: ApplicationEntity,
            sbn: StatusBarNotification?
        ): Boolean {
            var result = false
            val messageBody = sbn?.notification!!.extras["android.text"].toString()
            if (app.messageBody != "None") {
                val keywordArray = app.messageBody?.trim()?.split(", ")
                if (keywordArray != null) {
                    for (x in keywordArray) {
                        if (messageBody.trim().toLowerCase(Locale.getDefault())
                                .contains(
                                    x.trim().toLowerCase(Locale.getDefault())
                                )) {
                            result = true
                            break
                        } else {
                            continue
                        }
                    }
                }
            } else {
                result = true
            }
            return result
        }

        private fun replaceAll(regex: String?, input: String, replacement: String?): String? {
            return Pattern.compile(regex!!).matcher(input).replaceAll(replacement!!)
        }

        /**
         * Alarm repeat section
         */
        private fun alarmRepeatOutput(repeat: String, app: ApplicationEntity): Boolean {
            var isPlayAble = false
            when (repeat) {
                "Once" -> {
                    //play one time and switch off the status
                    if (app.runningStatus == true) {
                        isPlayAble = true
                        app.id?.let { AlarmServicePresenter.updateAppStatus(false, it) }
                    }
                }
                "Always" -> {
                    //play every date and every time
                    if (app.runningStatus == true) {
                        isPlayAble = true
                    }
                }
                "Custom" -> {
                    //check the for the days, if the day match then please
                    if (app.runningStatus == true) {
                        if (checkWithCurrentDay(app.repeatDays?:"")) {
                            isPlayAble = true
                        }
                    }
                }

                else -> isPlayAble = false
            }
            return isPlayAble
        }

        private fun checkWithCurrentDay(days: String): Boolean {
            val list = days.split(", ")
            var isToday = false
            for (x in list) {
                if (x == TimeUtils.getCurrentDayName()) {
                    isToday = true
                    break
                }
            }
            return isToday
        }
        private fun startAlarmActivity(
            service: Service,
            tone: String?,
            sbn: StatusBarNotification?,
            app: ApplicationEntity
        ) {
            var isExecute = false
            val titleName = sbn?.notification?.extras!!["android.title"]
            //when alarm is playing with activity and the thread is not finished then user dismissed the alarm, then it's playing with notification again
            //temporary fixed by reducing waiting time from 4 sec to 2 sec.
            //need to check device to device for more result
            val once = Once()
            once.run(Runnable {
                AlarmCheckerThread(AlarmCheckerThread.PlayListener { s ->
                    if (!s) {
                        val bundle = Bundle()
                        bundle.putString("alarm_by_thread", "true")
                        firebaseAnalytics.logEvent("alarm_type", bundle)

                        val desc = sbn.notification!!.extras["android.text"].toString()
                        FloatingNotification.showFloatingNotification(
                            app.soundLevel?:0,
                            titleName.toString(),
                            app.isJustVibrate?:false,
                            app.appName?:"",
                            app.packageName?:"",
                            app.numberOfPlay?:1,
                            app.vibrateOnAlarm?:false,
                            service,
                            app.tonePath,
                            app.isFlashOn ?:false,
                            desc,
                            app.bitmapPath?:""
                        )
                    }
                }).execute()
            })

            if (!isExecute) {
                isExecute = true
                val title = sbn.notification!!.extras["android.title"].toString()
                val desc = sbn.notification!!.extras["android.text"].toString()
                val intent = Intent(service, AlarmActivity::class.java)
                intent.putExtra(Constants.IntentKeys.NUMBER_OF_PLAY, app.numberOfPlay)
                intent.putExtra(Constants.IntentKeys.APP_NAME, app.appName)
                intent.putExtra(Constants.IntentKeys.IS_VIBRATE, app.vibrateOnAlarm)
                intent.putExtra(Constants.IntentKeys.PACKAGE_NAME, app.packageName)
                intent.putExtra(Constants.IntentKeys.TONE, tone)
                intent.putExtra(Constants.IntentKeys.IS_JUST_VIBRATE, app.isJustVibrate)
                intent.putExtra(Constants.IntentKeys.IS_FLASH_LIGHT, app.isFlashOn)
                intent.putExtra(Constants.IntentKeys.IMAGE_PATH, app.bitmapPath)
                intent.putExtra(Constants.IntentKeys.TITLE, title)
                intent.putExtra(Constants.IntentKeys.DESC, desc)
                intent.putExtra(Constants.IntentKeys.SOUND_LEVEL, app.soundLevel)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                service.startActivity(intent)
            }
        }
    }
}