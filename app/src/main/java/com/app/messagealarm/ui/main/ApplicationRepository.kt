package com.app.messagealarm.ui.main

import androidx.lifecycle.LiveData
import com.app.messagealarm.model.dao.ApplicationDao
import com.app.messagealarm.model.entity.ApplicationEntity

class ApplicationRepository(private val applicationDao: ApplicationDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allApplications: LiveData<List<ApplicationEntity>> = applicationDao.allApplications()

    suspend fun insert(application: ApplicationEntity) {
        applicationDao.insert(application)
    }

    suspend fun update(application: ApplicationEntity) {
        applicationDao.update(application)
    }

    suspend fun delete(application: ApplicationEntity) {
        applicationDao.delete(application)
    }

    fun getAppByPackageName(packageName: String): ApplicationEntity {
        return applicationDao.getAppByPackageName(packageName)
    }

    suspend fun getAppById(id: Int): ApplicationEntity {
        return applicationDao.getApplication(id)
    }
}
