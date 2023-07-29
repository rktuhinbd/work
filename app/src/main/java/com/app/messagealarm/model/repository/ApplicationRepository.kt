package com.app.messagealarm.model.repository

import androidx.lifecycle.LiveData
import com.app.messagealarm.model.dao.ApplicationDaoNew
import com.app.messagealarm.model.entity.ApplicationEntityNew

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
}