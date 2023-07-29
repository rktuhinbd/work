package com.app.messagealarm.model.repository

import androidx.lifecycle.LiveData
import androidx.room.Query
import com.app.messagealarm.model.dao.ApplicationDaoNew
import com.app.messagealarm.model.entity.ApplicationEntityNew
import com.app.messagealarm.model.entity.ApplicationTable

class ApplicationRepository(private val applicationDao: ApplicationDaoNew) {

    val allApplications: LiveData<List<ApplicationEntityNew>> = applicationDao.getAll()

    suspend fun insert(application: ApplicationEntityNew) {
        applicationDao.insert(application)
    }

    suspend fun update(application: ApplicationEntityNew) {
        applicationDao.update(application)
    }

    suspend fun delete(application: ApplicationEntityNew) {
        applicationDao.delete(application)
    }

    suspend fun getAppByPackageName(packageName: String): ApplicationEntityNew{
        return applicationDao.getAppByPackageName(packageName)
    }
}