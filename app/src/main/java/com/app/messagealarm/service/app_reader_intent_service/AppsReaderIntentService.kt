package com.app.messagealarm.service.app_reader_intent_service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.service.AppsReader

class AppsReaderIntentService : JobIntentService() {

    private val TAG = "MyJobIntentService"

    override fun onHandleWork(intent: Intent) {
        /**
         * send the task to background thread, fix by Mujahid : 18 Jun 2021
         */
        Thread {
            BaseApplication.installedApps = AppsReader.getInstalledApps(false, this)!!
        }.start()
    }

    companion object{
        /**
         * Unique job ID for this service.
         */
        val JOB_ID = 2
        fun enqueueWork(context: Context?, intent: Intent?) {
            enqueueWork(context!!, AppsReaderIntentService::class.java, JOB_ID, intent!!)
        }
    }

}