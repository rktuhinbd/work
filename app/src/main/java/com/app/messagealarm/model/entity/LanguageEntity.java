package com.app.messagealarm.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "language")
public class LanguageEntity {

    @ColumnInfo(name = "lang_name")
    private String langName;

    @ColumnInfo(name = "lang_code")
    private String langCode;

    @PrimaryKey
    @ColumnInfo(name = "lang_id")
    private int langId;


    public LanguageEntity(String langName, String langCode, int langId) {
        this.langName = langName;
        this.langCode = langCode;
        this.langId = langId;
    }

    public String getLangName() {
        return langName;
    }

    public String getLangCode() {
        return langCode;
    }

    public int getLangId() {
        return langId;
    }
}
