package com.app.messagealarm.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.messagealarm.model.entity.ApplicationEntityNew
import com.app.messagealarm.model.entity.ApplicationTable

@Dao
interface ApplicationDaoNew {

    @Query("SELECT * FROM ${ApplicationTable.TABLE_NAME}")
    fun getAll(): LiveData<List<ApplicationEntityNew>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(application: ApplicationEntityNew)

    @Update
    suspend fun update(application: ApplicationEntityNew)

    @Delete
    suspend fun delete(application: ApplicationEntityNew)

    @Query("SELECT * FROM ${ApplicationTable.TABLE_NAME} WHERE ${ApplicationTable.PACKAGE_NAME} = :packageName")
    suspend fun getAppByPackageName(packageName: String): ApplicationEntityNew
}