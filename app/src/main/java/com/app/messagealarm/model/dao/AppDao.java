package com.app.messagealarm.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.app.messagealarm.model.entity.AppEntity;

@Dao
public interface AppDao {
    @Insert
    void insertApp(AppEntity appEntity);

    @Query("SELECT COUNT(*) FROM messaging_app")
    int getTotalCountOfApp();
}
