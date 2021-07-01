package com.app.messagealarm.work_manager

import android.content.Context
import androidx.work.*
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit


class WorkManagerUtils {

    companion object{

        fun scheduleWorks(context: Context){
            //get mute time from preferences
            val time = SharedPrefUtils.readString(Constants.PreferenceKeys.MUTE_TIME)
            if(time != Constants.Default.MANUAL){
                var duration = 10
                duration = try {
                    time.split(" ")[0].toInt()
                }catch (e: NumberFormatException){
                    10
                }
                val muteDismissRequest = OneTimeWorkRequest
                    .Builder(MuteDismissWorker::class.java)
                    .addTag("MUTE")
                    .setInitialDelay(duration.toLong(), TimeUnit.MINUTES)
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork(
                        Constants.Default.MUTE_TIMER,
                        ExistingWorkPolicy.REPLACE, muteDismissRequest
                    )
            }
        }

        fun scheduleWorkWithTime(time: String, context: Context){
            //get mute time from preferences
            if(time != Constants.Default.MANUAL){
                var duration = 10
                duration = try {
                    time.split(" ")[0].toInt()
                }catch (e: NumberFormatException){
                    10
                }
                val muteDismissRequest = OneTimeWorkRequest
                    .Builder(MuteDismissWorker::class.java)
                    .addTag("MUTE")
                    .setInitialDelay(duration.toLong(), TimeUnit.MINUTES)
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork(
                        Constants.Default.MUTE_TIMER,
                        ExistingWorkPolicy.REPLACE, muteDismissRequest
                    )
            }
        }

         fun isWorkScheduled(context: Context, tag: String): Boolean {
            val instance = WorkManager.getInstance(context)
            val statuses: ListenableFuture<List<WorkInfo>> = instance.getWorkInfosByTag(tag)
            return try {
                var running = false
                val workInfoList: List<WorkInfo> = statuses.get()
                for (workInfo in workInfoList) {
                    val state = workInfo.state
                    running = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED
                }
                running
            } catch (e: ExecutionException) {
                e.printStackTrace()
                false
            } catch (e: InterruptedException) {
                e.printStackTrace()
                false
            }
        }

        fun scheduleSyncWork(context: Context, appSize: Int, langSize: Int, constrainSize: Int){
            val inputData = Data.Builder()
                .putInt(Constants.InputData.APP_SIZE, appSize)
                .putInt(Constants.InputData.LANG_SIZE, langSize)
                .putInt(Constants.InputData.CONSTRAIN_SIZE, constrainSize)
                .build()
            val networkConstrain = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build()
           val syncRequest = OneTimeWorkRequest
               .Builder(BackGroundSyncWorker::class.java)
               .addTag(Constants.Default.WORK_SYNC)
               .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
               .setInputData(inputData)
               .setConstraints(networkConstrain)
               .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                Constants.Default.SYNC,
                ExistingWorkPolicy.REPLACE, syncRequest
            )
        }
    }


}