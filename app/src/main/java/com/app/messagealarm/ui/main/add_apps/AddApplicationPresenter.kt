package com.app.messagealarm.ui.main.add_apps

import android.app.Activity
import android.content.Intent
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.model.response.sync.SyncResponse
import com.app.messagealarm.networking.RetrofitClient
import com.app.messagealarm.service.AppsReader
import com.app.messagealarm.service.BGSyncDataSavingService
import com.app.messagealarm.service.app_reader_intent_service.AppsReaderIntentService
import com.app.messagealarm.utils.AndroidUtils
import com.app.messagealarm.utils.DataUtils
import com.app.messagealarm.work_manager.WorkManagerUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.logging.Handler

class AddApplicationPresenter(

    private val addApplicationView: AddApplicationView,
    private val activity: Activity
) {

    fun getAllApplicationList() {
        Thread(Runnable {
            try {
                if(BaseApplication.installedApps.isEmpty()){
                    addApplicationView.onAllApplicationGetSuccess(
                        AppsReader.getInstalledApps(false, activity)!!
                    )
                }else{
                    addApplicationView.onAllApplicationGetSuccess(
                        BaseApplication.installedApps as ArrayList<InstalledApps>
                    )
                }
            } catch (e: Exception) {
                addApplicationView.onAllApplicationGetError(DataUtils.getString(R.string.something_wrong))
            }
        }).start()
    }

    fun sync(){
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            appDatabase.languageDao().cleanLanguage()
            appDatabase.appDao().cleanMessagingApp()
            appDatabase.appConstrainDao().cleanAppConstrain()
            RetrofitClient.getApiService().syncData(0, 0, 0,
                AndroidUtils.getCurrentLangCode(BaseApplication.getBaseApplicationContext()))
                .enqueue(object : Callback<SyncResponse>{
                    override fun onResponse(
                        call: Call<SyncResponse>,
                        response: Response<SyncResponse>
                    ) {
                        if(response.isSuccessful){
                            BGSyncDataSavingService.saveData(response.body()!!)
                            //wait let's get it saved
                            android.os.Handler(Looper.getMainLooper()).postDelayed(
                                Runnable {
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

    fun filterByMessaging(
    ) {
        Thread(Runnable {
            var installedAppsList = emptyList<InstalledApps>()
            installedAppsList = if(BaseApplication.installedApps.isEmpty()){
                AppsReader.getInstalledApps(false, activity)!!
            }else{
                BaseApplication.installedApps
            }
            try {
                val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
                val appList = appDatabase.appDao().appsList
                val holder = ArrayList<InstalledApps>()
                for (x in installedAppsList) {
                    for (y in appList) {
                        if (x.packageName == y.appPackageName) {
                                holder.add(x)
                        }
                    }
                    if(x.packageName != "com.android.mms"){
                        if(x.packageName == Telephony.Sms.getDefaultSmsPackage(activity)){
                            holder.add(x)
                        }
                    }
                }
                addApplicationView.onApplicationFiltered(holder.distinct())
            }catch (e:ConcurrentModificationException){
                //skip the crash
            }
        }).start()

    }
}