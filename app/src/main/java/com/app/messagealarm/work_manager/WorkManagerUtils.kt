package com.app.messagealarm.work_manager

import android.content.Context
import androidx.work.*
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import java.lang.NumberFormatException
import java.util.concurrent.TimeUnit

class WorkManagerUtils {
    companion object{
        fun scheduleWorks(context: Context){
            //get mute time from preferences
            val time = SharedPrefUtils.readString(Constants.PreferenceKeys.MUTE_TIME)
            var duration = 10
            duration = try {
                time.split(" ")[0].toInt()
            }catch (e:NumberFormatException){
                10
            }
            val muteDismissRequest = OneTimeWorkRequest
                .Builder(MuteDismissWorker::class.java)
                .setInitialDelay(duration.toLong(), TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(System.currentTimeMillis().toString(),
                ExistingWorkPolicy.REPLACE, muteDismissRequest)
        }
    }
}