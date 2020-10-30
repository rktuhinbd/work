package com.app.messagealarm.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.app.messagealarm.model.entity.LanguageEntity;

@Dao
public interface LanguageDao {
    @Insert
    void insertLanguage(LanguageEntity languageEntity);

    @Query("SELECT COUNT(*) FROM language")
    int getTotalCountOfLanguage();

}
