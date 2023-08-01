package com.app.messagealarm.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.messagealarm.model.entity.ApplicationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ApplicationViewModel(private val repository: ApplicationRepository) : ViewModel() {

    // Using LiveData and caching what getAllApplications returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allApplications: LiveData<List<ApplicationEntity>> = repository.allApplications

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */

    private val _applicationInsertEntity = MutableStateFlow<Boolean?>(null)
    val applicationInsertObserver: StateFlow<Boolean?> = _applicationInsertEntity

    fun insert(application: ApplicationEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(application)
            _applicationInsertEntity.emit(true)
        }
    }

    /**
     * Launching a new coroutine to update the data in a non-blocking way
     */

    private val _applicationUpdateEntity = MutableStateFlow<Boolean?>(null)
    val applicationUpdateObserver: StateFlow<Boolean?> = _applicationUpdateEntity

    fun update(application: ApplicationEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val applicationEntity = repository.update(application)
            _applicationUpdateEntity.emit(applicationEntity)
        }
    }

    /**
     * Launching a new coroutine to delete the data in a non-blocking way
     */
    fun delete(application: ApplicationEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(application)
//            deletionStatusLiveData.postValue(true)
        }
    }

    /**
     * Launching a new coroutine to get an application by package name in a non-blocking way
     */
    private val _applicationEntity = MutableStateFlow<ApplicationEntity?>(null)
    val applicationByPackageObserver: StateFlow<ApplicationEntity?> = _applicationEntity

    fun getAppByPackageName(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val applicationEntity = repository.getAppByPackageName(packageName)
            _applicationEntity.emit(applicationEntity) // post the fetched entity to LiveData
        }
    }
    /**
     * Launching a new coroutine to get an application by id in a non-blocking way
     */
    fun getAppById(id: Int) {
        viewModelScope.launch {
            val applicationEntity = repository.getAppById(id)
//            optionView.onApplicationGetSuccess(applicationEntity)
//            applicationEntityByIdLiveData.postValue(applicationEntity)
        }
    }
}
