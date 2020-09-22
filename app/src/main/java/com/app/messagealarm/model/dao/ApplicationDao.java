package com.app.messagealarm.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;

import com.app.messagealarm.model.entity.ApplicationEntity;

@Dao
public interface ApplicationDao {
    @Insert
    void insertApplication(ApplicationEntity applicationEntity);

}
