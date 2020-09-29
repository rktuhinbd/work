package com.app.messagealarm.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class InstalledApps implements Serializable {

    private String appName;
    private String packageName;
    private String versionName;
    private Drawable drawableIcon;

    public InstalledApps(String appName, String packageName, String versionName, Drawable drawableIcon) {
        this.appName = appName;
        this.packageName = packageName;
        this.versionName = versionName;
        this.drawableIcon = drawableIcon;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getVersionName() {
        return versionName;
    }

    public Drawable getDrawableIcon() {
        return drawableIcon;
    }
}
