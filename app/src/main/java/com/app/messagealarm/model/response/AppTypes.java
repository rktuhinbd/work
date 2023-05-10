package com.app.messagealarm.model.response;

import androidx.room.Ignore;

public class AppTypes {
    @Ignore
    private String packageName;
    private boolean isAlarmConfigured;
    private boolean isSpeakConfigured;
    private boolean isCustomConfigured;

    public AppTypes(boolean isAlarmConfigured, boolean isSpeakConfigured, boolean isCustomConfigured) {
        this.isAlarmConfigured = isAlarmConfigured;
        this.isSpeakConfigured = isSpeakConfigured;
        this.isCustomConfigured = isCustomConfigured;
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

    public boolean isCustomConfigured() {
        return isCustomConfigured;
    }
}
