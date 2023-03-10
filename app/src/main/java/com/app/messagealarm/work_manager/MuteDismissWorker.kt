package com.app.messagealarm.work_manager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.app.messagealarm.ui.notifications.FloatingNotification.Companion.notifyMute
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils.write

class MuteDismissWorker(context: Context, workerParams: WorkerParameters) : Worker(
    context,
    workerParams
) {
    override fun doWork(): Result {
        write(Constants.PreferenceKeys.IS_MUTED, false)
        notifyMute(false)
       return Result.success()
    }
}