package com.app.messagealarm.ui.main.add_options

import com.app.messagealarm.model.entity.ApplicationEntity

interface AddApplicationOptionView{
    fun onApplicationSaveSuccess()
    fun onApplicationSaveError(message:String)
    fun onApplicationUpdateSuccess();
    fun onApplicationUpdateError(message: String);
    fun onBitmapSaveSuccess(path:String)
    fun onBitmapSaveError()
    fun onApplicationGetSuccess(app:ApplicationEntity)
    fun onApplicationGetError(message: String)
}