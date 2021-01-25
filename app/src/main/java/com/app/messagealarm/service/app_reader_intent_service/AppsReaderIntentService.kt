package com.app.messagealarm.service.app_reader_intent_service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.service.AppsReader

class AppsReaderIntentService : JobIntentService() {

    private val TAG = "MyJobIntentService"

    override fun onHandleWork(intent: Intent) {
        BaseApplication.installedApps = AppsReader.getInstalledApps(false, this)!!
    }

    companion object{
        /**
         * Unique job ID for this service.
         */
        val JOB_ID = 2
        public fun enqueueWork(context: Context?, intent: Intent?) {
            enqueueWork(context!!, AppsReaderIntentService::class.java, JOB_ID, intent!!)
        }
    }

}