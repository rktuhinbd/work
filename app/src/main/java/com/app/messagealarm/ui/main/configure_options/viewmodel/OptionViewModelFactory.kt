package com.app.messagealarm.ui.main.configure_options.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.messagealarm.model.repository.ApplicationRepository

class OptionViewModelFactory(private val applicationRepository: ApplicationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OptionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OptionViewModel(applicationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}