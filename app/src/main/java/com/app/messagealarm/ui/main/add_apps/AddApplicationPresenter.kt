package com.app.messagealarm.ui.main.add_apps

import android.app.Activity
import android.os.Looper
import android.provider.Telephony
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.model.response.sync.SyncResponse
import com.app.messagealarm.networking.RetrofitClient
import com.app.messagealarm.service.AppsReader
import com.app.messagealarm.service.BGSyncDataSavingService
import com.app.messagealarm.utils.AndroidUtils
import com.app.messagealarm.utils.DataUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddApplicationPresenter(

    private val addApplicationView: AddApplicationView,
    private val activity: Activity
) {

    fun getAllApplicationList() {
        Thread(Runnable {
            try {
                if (BaseApplication.installedApps.isEmpty()) {
                    addApplicationView.onAllApplicationGetSuccess(
                        AppsReader.getInstalledApps(false, activity)!!
                    )
                } else {
                    addApplicationView.onAllApplicationGetSuccess(
                        BaseApplication.installedApps as ArrayList<InstalledApps>
                    )
                }
            } catch (e: Exception) {
                addApplicationView.onAllApplicationGetError(DataUtils.getString(R.string.something_wrong))
            }
        }).start()
    }

    fun getRequestAppFlagAdded(installedApps: InstalledApps, position: Int) {
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            try {
                val appTypes =
                    appDatabase.applicationDao().getAppTypesByPackage(installedApps.packageName)
                installedApps.isAlarmConfigured = appTypes.isAlarmConfigured
                installedApps.isCustomConfigured = appTypes.isCustomConfigured
                installedApps.isSpeakConfigured = appTypes.isSpeakConfigured
                addApplicationView.onAdapterRequestedDataReturn(installedApps, position)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

        }).start()
    }

    fun sync() {
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            appDatabase.languageDao().cleanLanguage()
            appDatabase.appDao().cleanMessagingApp()
            appDatabase.appConstrainDao().cleanAppConstrain()
            RetrofitClient.getApiService().syncData(
                0, 0, 0,
                AndroidUtils.getCurrentLangCode(BaseApplication.getBaseApplicationContext())
            )
                .enqueue(object : Callback<SyncResponse> {
                    override fun onResponse(
                        call: Call<SyncResponse>,
                        response: Response<SyncResponse>
                    ) {
                        if (response.isSuccessful) {
                            BGSyncDataSavingService.saveData(response.body()!!)
                            //wait let's get it saved
                            android.os.Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    filterByMessaging()
                                }, 5000
                            )
                        }
                    }

                    override fun onFailure(call: Call<SyncResponse>, t: Throwable) {
                        addApplicationView.onSyncFailed("Sync not success, Try again!")
                    }
                })
        }).start()
    }

    fun filterByMessaging() {
        Thread(Runnable {
            val installedAppsList = if (BaseApplication.installedApps.isEmpty()) {
                AppsReader.getInstalledApps(false, activity)!!
            } else {
                BaseApplication.installedApps
            }
            try {
                val appDatabase =
                    AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
                val appList = appDatabase.appDao().appsList.associateBy { it.appPackageName }
                val holder = HashSet<InstalledApps>()
                for (x in installedAppsList) {
                    if (appList.containsKey(x.packageName)) {
                        holder.add(x)
                    } else if (x.packageName == Telephony.Sms.getDefaultSmsPackage(activity)) {
                        holder.add(x)
                    }
                }
                holder.removeIf { it.packageName == "com.android.mms" }
                addApplicationView.onApplicationFiltered(holder.toList())
            } catch (e: ConcurrentModificationException) {
                //skip the crash
            }
        }).start()
    }

}