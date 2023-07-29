package com.app.messagealarm.model.response;

import androidx.room.Ignore;

public class AppTypes {
    @Ignore
    private String packageName;
    private boolean isAlarmConfigured;
    private boolean isSpeakConfigured;

    public AppTypes(boolean isAlarmConfigured, boolean isSpeakConfigured) {
        this.isAlarmConfigured = isAlarmConfigured;
        this.isSpeakConfigured = isSpeakConfigured;
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean isAlarmConfigured() {
        return isAlarmConfigured;
    }

    public boolean isSpeakConfigured() {
        return isSpeakConfigured;
    }

}
