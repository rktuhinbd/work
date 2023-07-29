package com.app.messagealarm.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.app.messagealarm.model.entity.ApplicationEntity;
import com.app.messagealarm.model.response.AppTypes;

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

    @Query("DELETE FROM applications")
    void deleteAllAppsWithoutTheFirstOne();

    @Query("UPDATE applications SET sound_level = :sound_level")
    void rollBackAppsFromDefaultSoundLevel(int sound_level);

    @Delete
    void deleteApplication(ApplicationEntity applicationEntity);

    @Query("SELECT * FROM applications WHERE package_name = :packageName")
    ApplicationEntity getAppByPackageName(String packageName);

    @Query("SELECT * FROM applications WHERE package_name = :packageName AND is_alarm_enabled = :isAlarmEnabled")
    ApplicationEntity getAppByPackageNameAndAlarm(String packageName, Boolean isAlarmEnabled);

    @Query("SELECT * FROM applications WHERE package_name = :packageName AND is_speak_enabled = :isSpeakEnabled")
    ApplicationEntity getAppByPackageNameAndSpeak(String packageName, Boolean isSpeakEnabled);

    @Query("SELECT package_name, MAX(is_alarm_enabled = 'true') as isAlarmConfigured, " +
            "MAX(is_speak_enabled = 'true') as isSpeakConfigured" +
            " FROM applications WHERE package_name = :packageName GROUP BY package_name")
    AppTypes getAppTypesByPackage(String packageName);

    @Query("SELECT COUNT(*) FROM applications")
    int getAddedAppCount();

}
