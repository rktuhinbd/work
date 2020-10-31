package com.app.messagealarm.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.app.messagealarm.model.entity.AppConstrainEntity;

import java.util.List;

@Dao
public interface AppConstrainDao {

    @Insert
    void insertAppConstrain(AppConstrainEntity appConstrainEntity);

    @Query("SELECT COUNT(*) FROM app_constrain")
    int getTotalCountOfAppConstrain();


    @Query("SELECT * FROM app_constrain WHERE app_package_name = :packageName AND lang_code = :langCode")
    List<AppConstrainEntity> getAppConstrains(String packageName, String langCode);

}
