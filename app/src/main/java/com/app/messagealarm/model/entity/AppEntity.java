package com.app.messagealarm.model.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "messaging_app")
public class AppEntity {

    @ColumnInfo(name = "app_name")
    private String appName;
    @PrimaryKey
    @ColumnInfo(name = "app_id")
    private int appId;
    @ColumnInfo(name = "app_package_name")
    private String appPackageName;

    public AppEntity(String appName, int appId, String appPackageName) {
        this.appName = appName;
        this.appId = appId;
        this.appPackageName = appPackageName;
    }

    public String getAppName() {
        return appName;
    }

    public int getAppId() {
        return appId;
    }

    public String getAppPackageName() {
        return appPackageName;
    }
}
