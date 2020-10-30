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

    @ColumnInfo(name = "lang_id")
    private int langId;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "app_id")
    private int appId;

    public AppConstrainEntity(int langId, String description, String title, int appId) {
        this.langId = langId;
        this.description = description;
        this.title = title;
        this.appId = appId;
    }

    public int getLangId() {
        return langId;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public int getAppId() {
        return appId;
    }

    public int getId() {
        return id;
    }
}
