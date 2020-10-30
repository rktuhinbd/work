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

        fun scheduleSyncWork(context: Context, appSize:Int, langSize:Int, constrainSize:Int){
            val inputData = Data.Builder()
                .putInt(Constants.InputData.APP_SIZE, appSize)
                .putInt(Constants.InputData.LANG_SIZE, langSize)
                .putInt(Constants.InputData.CONSTRAIN_SIZE, constrainSize)
                .build()

            val networkConstrain = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build()

           val syncRequest = OneTimeWorkRequest
               .Builder(BackGroundSyncWorker::class.java)
               .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
               .setInputData(inputData)
               .setConstraints(networkConstrain)
               .build()

            WorkManager.getInstance(context).enqueueUniqueWork(System.currentTimeMillis().toString(),
            ExistingWorkPolicy.REPLACE, syncRequest
                )
        }
    }


}