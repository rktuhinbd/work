package com.app.messagealarm.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = ApplicationTable.TABLE_NAME,
    indices = [Index(
        value = [ApplicationTable.PACKAGE_NAME,
            ApplicationTable.APP_NAME, ApplicationTable.IS_ALARM_ENABLED,
            ApplicationTable.IS_SPEAK_ENABLED],
        unique = true
    )]
)
data class ApplicationEntityNew(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ApplicationTable.ID)
    var id: Int = 0,

    @ColumnInfo(name = ApplicationTable.RUNNING_STATUS)
    var isRunningStatus: Boolean = false,

    @ColumnInfo(name = ApplicationTable.IS_ALARM_ENABLED)
    var isAlarmEnabled: Boolean? = false,

    @ColumnInfo(name = ApplicationTable.IS_SPEAK_ENABLED)
    var isSpeakEnabled: Boolean? = false,

    @ColumnInfo(name = ApplicationTable.APP_NAME)
    var appName: String? = null,

    @ColumnInfo(name = ApplicationTable.PACKAGE_NAME)
    var packageName: String? = null,

    @ColumnInfo(name = ApplicationTable.ALARM_REPEAT)
    var alarmRepeat: String? = null,

    @ColumnInfo(name = ApplicationTable.RINGTONE)
    var ringTone: String? = null,

    @ColumnInfo(name = ApplicationTable.VIBRATE_ON_ALARM)
    var isVibrateOnAlarm: Boolean = false,

    @ColumnInfo(name = ApplicationTable.JUST_VIBRATE)
    var isJustVibrate: Boolean = false,

    @ColumnInfo(name = ApplicationTable.CUSTOM_TIME)
    var isCustomTime: Boolean = false,

    @ColumnInfo(name = ApplicationTable.START_TIME)
    var startTime: String? = null,

    @ColumnInfo(name = ApplicationTable.END_TIME)
    var endTime: String? = null,

    @ColumnInfo(name = ApplicationTable.NUMBER_OF_PLAY)
    var numberOfPlay: Int = 0,

    @ColumnInfo(name = ApplicationTable.SENDER_NAMES)
    var senderNames: String? = null,

    @ColumnInfo(name = ApplicationTable.MESSAGE_BODY)
    var messageBody: String? = null,

    @ColumnInfo(name = ApplicationTable.REPEAT_DAYS)
    var repeatDays: String? = null,

    @ColumnInfo(name = ApplicationTable.TONE_PATH)
    var tonePath: String? = null,

    @ColumnInfo(name = ApplicationTable.BITMAP_PATH)
    var bitmapPath: String? = null,

    @ColumnInfo(name = ApplicationTable.IGNORED_NAMES, defaultValue = "None")
    var ignoredNames: String? = null,

    @ColumnInfo(name = ApplicationTable.SOUND_LEVEL, defaultValue = "100")
    var soundLevel: Int = 100,

    @ColumnInfo(name = ApplicationTable.IS_FLASH_ON, defaultValue = "false")
    var isFlashOn: Boolean = false
)