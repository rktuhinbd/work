package com.app.messagealarm.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.app.messagealarm.model.entity.ApplicationEntity;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ApplicationDao {
    @Insert
    void insertApplication(ApplicationEntity applicationEntity);

    @Query("SELECT * FROM applications")
    List<ApplicationEntity> getAllApplicationList();

    @Delete
    void deleteApplication(ApplicationEntity applicationEntity);
}
