package com.app.messagealarm.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "app_constrain")
public class AppConstrainEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    public void setId(int id) {
        this.id = id;
    }

    @ColumnInfo(name = "lang_code")
    private String langCode;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "app_package_name")
    private String appPackageName;

    @ColumnInfo(name = "is_contains")
    private boolean isContains;

    public AppConstrainEntity(String langCode, String description, String title,
                              String appPackageName, boolean  isContains) {
        this.langCode = langCode;
        this.description = description;
        this.title = title;
        this.appPackageName = appPackageName;
        this.isContains  = isContains;
    }

    public boolean isContains() {
        return isContains;
    }

    public String getLangCode() {
        return langCode;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public int getId() {
        return id;
    }
}
