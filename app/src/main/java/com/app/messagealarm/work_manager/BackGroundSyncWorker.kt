package com.app.messagealarm.work_manager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.networking.RetrofitClient
import com.app.messagealarm.service.BGSyncDataSavingService
import com.app.messagealarm.utils.AndroidUtils
import com.app.messagealarm.utils.Constants

class BackGroundSyncWorker(context: Context, workerParams: WorkerParameters) : Worker(
    context,
    workerParams
) {


    override fun doWork(): Result {
        val appSize = inputData.getInt(Constants.InputData.APP_SIZE, 0)
        val langSize = inputData.getInt(Constants.InputData.LANG_SIZE, 0)
        val constrainSize = inputData.getInt(Constants.InputData.CONSTRAIN_SIZE, 0)
        val call =
            RetrofitClient.getApiService().syncData(appSize, langSize, constrainSize,
                AndroidUtils.getCurrentLangCode(BaseApplication.getBaseApplicationContext()))
        val response = call.execute()
        return if (response.isSuccessful) {
            BGSyncDataSavingService.saveData(response.body()!!)
            Result.success()
        } else {
            Result.retry()
        }
    }
}