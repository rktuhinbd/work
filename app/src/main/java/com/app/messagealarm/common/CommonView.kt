package com.app.messagealarm.common

interface CommonView {
    fun onSuccess()
    fun onSuccess(token:String)
    fun onError()
}