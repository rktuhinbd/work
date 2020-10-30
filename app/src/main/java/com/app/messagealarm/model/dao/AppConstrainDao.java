package com.app.messagealarm.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.app.messagealarm.model.entity.AppConstrainEntity;

@Dao
public interface AppConstrainDao {
    @Insert
    void insertAppConstrain(AppConstrainEntity appConstrainEntity);

    @Query("SELECT COUNT(*) FROM app_constrain")
    int getTotalCountOfAppConstrain();
}
