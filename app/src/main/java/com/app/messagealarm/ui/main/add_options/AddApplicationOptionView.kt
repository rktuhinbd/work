package com.app.messagealarm.ui.main.add_options

interface AddApplicationOptionView{
    fun onApplicationSaveSuccess()
    fun onApplicationSaveError(message:String)
    fun onBitmapSaveSuccess(path:String)
}