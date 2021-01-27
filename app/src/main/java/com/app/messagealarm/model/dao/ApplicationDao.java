package com.app.messagealarm.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.app.messagealarm.model.entity.ApplicationEntity;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ApplicationDao {
    @Insert
    void insertApplication(ApplicationEntity applicationEntity);

    @Query("SELECT * FROM applications")
    List<ApplicationEntity> getAllApplicationList();

    @Update
    int updateApplication(ApplicationEntity applicationEntity);

    @Query("UPDATE applications SET running_status = :status WHERE id = :id")
    void updateAppStatus(boolean status, int id);

    @Query("UPDATE applications SET vibrate_on_alarm = :status")
    void disableVibrateToAllApp(boolean status);

    @Query("UPDATE applications SET just_vibrate = :status")
    void disableJustVibrateToAllApp(boolean status);

    @Delete
    void deleteApplication(ApplicationEntity applicationEntity);

    @Query("SELECT * FROM applications WHERE package_name = :packageName")
    ApplicationEntity getAppByPackageName(String packageName);

}
