package com.app.messagealarm.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,

    @ColumnInfo(name = ApplicationTable.APP_NAME)
    var appName: String? = null,

    @ColumnInfo(name = ApplicationTable.PACKAGE_NAME)
    var packageName: String? = null,

    @ColumnInfo(name = ApplicationTable.IS_ALARM_ENABLED)
    var isAlarmEnabled: Boolean? = false,

    @ColumnInfo(name = ApplicationTable.IS_SPEAK_ENABLED)
    var isSpeakEnabled: Boolean? = false,

    @ColumnInfo(name = ApplicationTable.RUNNING_STATUS)
    var runningStatus: Boolean? = null,

    @ColumnInfo(name = ApplicationTable.ALARM_REPEAT)
    var alarmRepeat: String? = null,

    @ColumnInfo(name = ApplicationTable.REPEAT_DAYS)
    var repeatDays: String? = null,

    @ColumnInfo(name = ApplicationTable.SENDER_NAMES)
    var senderNames: String? = null,

    @ColumnInfo(name = ApplicationTable.MESSAGE_BODY)
    var messageBody: String? = null,

    @ColumnInfo(name = ApplicationTable.CUSTOM_TIME)
    var isCustomTime: Boolean? = false,

    @ColumnInfo(name = ApplicationTable.IS_FLASH_ON)
    var isFlashOn: Boolean? = false,

    @ColumnInfo(name = ApplicationTable.JUST_VIBRATE)
    var isJustVibrate: Boolean? = null,

    @ColumnInfo(name = ApplicationTable.START_TIME)
    var startTime: String? = null,

    @ColumnInfo(name = ApplicationTable.END_TIME)
    var endTime: String? = null,

    @ColumnInfo(name = ApplicationTable.TONE_PATH)
    var tonePath: String? = null,

    @ColumnInfo(name = ApplicationTable.BITMAP_PATH)
    var bitmapPath: String? = null,

    @ColumnInfo(name = ApplicationTable.IGNORED_NAMES)
    var ignoredNames: String? = null,

    @ColumnInfo(name = ApplicationTable.RINGTONE)
    var ringtone: String? = null,

    @ColumnInfo(name = ApplicationTable.VIBRATE_ON_ALARM)
    var vibrateOnAlarm: Boolean? = false,

    @ColumnInfo(name = ApplicationTable.NUMBER_OF_PLAY)
    var numberOfPlay: Int? = null,

    @ColumnInfo(name = ApplicationTable.SOUND_LEVEL)
    var soundLevel: Int? = null,

    ) : Serializable
