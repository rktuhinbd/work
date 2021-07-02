package com.app.messagealarm.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "applications", indices = {@Index(value = {"package_name", "app_name"}, unique = true)})
public class ApplicationEntity implements Serializable {

    /**
     * Entity fields
     */

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "running_status")
    private boolean runningStatus;
    @ColumnInfo(name = "app_name")
    private String appName;
    @ColumnInfo(name = "package_name")
    private String packageName;
    @ColumnInfo(name = "alarm_repeat")
    private String alarmRepeat;
    @ColumnInfo(name = "ringtone")
    private String ringTone;
    @ColumnInfo(name = "vibrate_on_alarm")
    private boolean vibrateOnAlarm;
    @ColumnInfo(name = "just_vibrate")
    private boolean justVibrate;
    @ColumnInfo(name = "custom_time")
    private boolean customTime;
    @ColumnInfo(name = "start_time")
    private String startTime;
    @ColumnInfo(name = "end_time")
    private String endTime;
    @ColumnInfo(name = "number_of_play")
    private int numberOfPlay;
    @ColumnInfo(name = "sender_names")
    private String senderNames;
    @ColumnInfo(name = "message_body")
    private String messageBody;
    @ColumnInfo(name = "repeat_days")
    private String repeatDays;
    @ColumnInfo(name = "tone_path")
    private String tone_path;
    @ColumnInfo(name = "bitmap_path")
    private String bitmapPath;
    @ColumnInfo(name = "ignored_names", defaultValue = "None")
    private String ignored_names;
    @ColumnInfo(name = "sound_level")
    private int soundLevel;


    /**
     * Getter methods
     * @return
     */
    public int getSoundLevel() {return soundLevel;}

    public String getIgnored_names(){
        return ignored_names;
    }

    public String getBitmapPath() {
        return bitmapPath;
    }

    public int getId() {
        return id;
    }

    public boolean isRunningStatus() {
        return runningStatus;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAlarmRepeat() {
        return alarmRepeat;
    }

    public String getRingTone() {
        return ringTone;
    }

    public boolean isVibrateOnAlarm() {
        return vibrateOnAlarm;
    }

    public boolean isCustomTime() {
        return customTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public int getNumberOfPlay() {
        return numberOfPlay;
    }

    public String getSenderNames() {
        return senderNames;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getRepeatDays() {
        return repeatDays;
    }

    public String getTone_path() {
        return tone_path;
    }

    public boolean isJustVibrate() {
        return justVibrate;
    }



    /**
     * Setter methods
     */

    public void setSoundLevel(int soundLevel) {this.soundLevel = soundLevel;}

    public void setIgnored_names(String ignored_names){
        this.ignored_names = ignored_names;
    }

    public void setJustVibrate(boolean justVibrate) {
        this.justVibrate = justVibrate;
    }

    public void setBitmapPath(String bitmapPath) {
        this.bitmapPath = bitmapPath;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRunningStatus(boolean runningStatus) {
        this.runningStatus = runningStatus;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setAlarmRepeat(String alarmRepeat) {
        this.alarmRepeat = alarmRepeat;
    }

    public void setRingTone(String ringTone) {
        this.ringTone = ringTone;
    }

    public void setVibrateOnAlarm(boolean vibrateOnAlarm) {
        this.vibrateOnAlarm = vibrateOnAlarm;
    }

    public void setCustomTime(boolean customTime) {
        this.customTime = customTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setNumberOfPlay(int numberOfPlay) {
        this.numberOfPlay = numberOfPlay;
    }

    public void setSenderNames(String senderNames) {
        this.senderNames = senderNames;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public void setRepeatDays(String repeatDays) {
        this.repeatDays = repeatDays;
    }

    public void setTone_path(String tone_path) {
        this.tone_path = tone_path;
    }

    @Override
    public String toString() {
        return "ApplicationEntity{" +
                "id=" + id +
                ", runningStatus=" + runningStatus +
                ", appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", alarmRepeat='" + alarmRepeat + '\'' +
                ", ringTone='" + ringTone + '\'' +
                ", vibrateOnAlarm=" + vibrateOnAlarm +
                ", customTime=" + customTime +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", numberOfPlay=" + numberOfPlay +
                ", senderNames='" + senderNames + '\'' +
                ", messageBody='" + messageBody + '\'' +
                ", repeatDays='" + repeatDays + '\'' +
                ", tone_path='" + tone_path + '\'' +
                ", bitmapPath='" + bitmapPath + '\'' +
                '}';
    }
}
