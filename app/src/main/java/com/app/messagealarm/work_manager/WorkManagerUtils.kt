package com.app.messagealarm.work_manager

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class WorkManagerUtils {
    companion object{
        fun scheduleWorks(context: Context){
            val muteDismissRequest = OneTimeWorkRequest
                .Builder(MuteDismissWorker::class.java)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(System.currentTimeMillis().toString(),
                ExistingWorkPolicy.REPLACE, muteDismissRequest)
        }
    }
}