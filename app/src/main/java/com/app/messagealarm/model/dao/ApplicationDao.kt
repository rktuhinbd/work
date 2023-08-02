package com.app.messagealarm.model.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.model.entity.ApplicationTable

@Dao
interface ApplicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(application: ApplicationEntity)

    @Update
    fun update(application: ApplicationEntity)

    @Delete
    fun delete(application: ApplicationEntity)

    @Query("UPDATE applications SET running_status = :status WHERE id = :id")
    fun updateAppStatus(status: Boolean, id: Int)

    @Query("SELECT * FROM ${ApplicationTable.TABLE_NAME}")
    fun allApplications(): LiveData<List<ApplicationEntity>>

    @Query("SELECT * FROM ${ApplicationTable.TABLE_NAME}")
    fun getAllApplications(): List<ApplicationEntity>

    @Query("SELECT * FROM ${ApplicationTable.TABLE_NAME} WHERE ${ApplicationTable.ID} = :id")
    fun getApplication(id: Int): ApplicationEntity

    @Query("SELECT * FROM ${ApplicationTable.TABLE_NAME} WHERE ${ApplicationTable.PACKAGE_NAME} = :packageName")
    fun getAppByPackageName(packageName: String): ApplicationEntity

    @Query("DELETE FROM ${ApplicationTable.TABLE_NAME}")
    fun deleteAllApplications()
}
