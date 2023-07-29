package com.app.messagealarm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.messagealarm.model.entity.ApplicationEntityNew
import com.app.messagealarm.model.repository.ApplicationRepository
import kotlinx.coroutines.launch

class ApplicationViewModel(private val repository: ApplicationRepository) : ViewModel() {

    val allApplications: LiveData<List<ApplicationEntityNew>> = repository.allApplications

    fun insert(application: ApplicationEntityNew) = viewModelScope.launch {
        repository.insert(application)
    }

    fun update(application: ApplicationEntityNew) = viewModelScope.launch {
        repository.update(application)
    }

    fun delete(application: ApplicationEntityNew) = viewModelScope.launch {
        repository.delete(application)
    }
}