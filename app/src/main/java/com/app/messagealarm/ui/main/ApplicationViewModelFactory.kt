package com.app.messagealarm.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ApplicationViewModelFactory(private val applicationRepository: ApplicationRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ApplicationViewModel::class.java)) {
            return ApplicationViewModel(applicationRepository) as T
        }
        throw IllegalArgumentException("Application ViewModel class")
    }
}