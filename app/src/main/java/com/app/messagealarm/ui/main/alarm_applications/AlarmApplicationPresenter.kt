package com.app.messagealarm.ui.main.alarm_applications

import android.content.Context
import android.database.sqlite.SQLiteException
import android.os.Build
import android.os.PowerManager
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.BuildConfig
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.model.response.TokenResponse
import com.app.messagealarm.networking.RetrofitClient
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import com.app.messagealarm.work_manager.WorkManagerUtils
import com.judemanutd.autostarter.AutoStartPermissionHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.NullPointerException

class AlarmApplicationPresenter(private val alarmApplicationView: AlarmApplicationView) {

    fun getApplicationList() {
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            try {
                alarmApplicationView.onGetAlarmApplicationSuccess(
                    appDatabase.applicationDao().allApplicationList
                            as ArrayList<ApplicationEntity>
                )
            } catch (e: NullPointerException) {
                alarmApplicationView.onGetAlarmApplicationError()
            } catch (e: SQLiteException) {
                alarmApplicationView.onGetAlarmApplicationError()
            }
        }).start()
    }


    fun dbRollBackForSoundLevelFromDefault() {
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread {
            try {
                appDatabase.applicationDao().rollBackAppsFromDefaultSoundLevel(70)
                SharedPrefUtils.write(Constants.PreferenceKeys.IS_DB_ROLLED_BACK, true)
            } catch (e: NullPointerException) {

            } catch (e: SQLiteException) {

            }
        }.start()
    }

    /**
     * sync heroku token when not done by callback from push class
     */
    fun syncFirebaseTokenToHeroku() {
        if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_HEROKU_TOKEN_SYNCED)) {
            if (!BuildConfig.DEBUG) {
                RetrofitClient.getApiServiceHeroku().registerTokenForHeroku(
                    SharedPrefUtils.readString(Constants.PreferenceKeys.FIREBASE_TOKEN)
                ).enqueue(object : Callback<TokenResponse> {
                    override fun onResponse(
                        call: Call<TokenResponse>,
                        response: Response<TokenResponse>
                    ) {
                        if (response.isSuccessful) {
                            //done
                            SharedPrefUtils.write(
                                Constants.PreferenceKeys.IS_HEROKU_TOKEN_SYNCED,
                                true
                            )
                        }
                    }

                    override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                        //failed to sync
                    }
                })
            }
        }
    }

    /**
     * return true if in App's Battery settings "Not optimized" and false if "Optimizing battery use"
     */
    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pwrm = context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = context.applicationContext.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pwrm.isIgnoringBatteryOptimizations(name)
        }
        return true
    }


    fun isAutoStartPermissionAvailable(context: Context) {
        Thread {
            try {
                if (SharedPrefUtils.readInt(Constants.PreferenceKeys.ALARM_COUNT) > 0) {
                    /**
                     * handle auto start
                     */
                    if (AutoStartPermissionHelper.getInstance()
                            .isAutoStartPermissionAvailable(context, true)
                    ) {
                        if(!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_AUTO_STARTED)){
                            alarmApplicationView.onAutoStartTextShow()
                        }else{
                            alarmApplicationView.onAutoStartTextHide()
                        }
                    }else{
                        alarmApplicationView.onAutoStartTextHide()
                    }
                    /**
                     * handle battery restriction
                     */
                    if(!isIgnoringBatteryOptimizations(context)){
                        if(!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_BATTERY_RESTRICTED)){
                            alarmApplicationView.onBatteryTextShow()
                        }else{
                            alarmApplicationView.onBatteryTextHide()
                        }
                    }else{
                        alarmApplicationView.onBatteryTextHide()
                    }
                }else{
                    //no app added yet
                    alarmApplicationView.onAutoStartTextHide()
                    alarmApplicationView.onBatteryTextHide()
                }
            }catch (e:Exception){
                //skip crashes
            }
        }.start()
    }

    fun updateAppStatus(boolean: Boolean, id: Int) {
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            try {
                appDatabase.applicationDao().updateAppStatus(boolean, id)
                alarmApplicationView.onAppStatusUpdateSuccess()
            } catch (e: NullPointerException) {
                e.printStackTrace()
                alarmApplicationView.onAppStatusUpdateError(e.message.toString())
            } catch (e: SQLiteException) {
                alarmApplicationView.onAppStatusUpdateError(e.message.toString())
                e.printStackTrace()
            }
        }).start()
    }

    /**
     * I know the constrain count is less than the server, sync from server
     * Need to test
     */
    fun getSyncedLowerLoaded(context: Context) {
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread {
            try {
                val appSize = appDatabase.appDao().totalCountOfApp
                val langSize = appDatabase.languageDao().totalCountOfLanguage
                val appConstrainSize = appDatabase.appConstrainDao().totalCountOfAppConstrain
                if (!WorkManagerUtils.isWorkScheduled(context, Constants.Default.WORK_SYNC)) {
                    if (SharedPrefUtils.contains(Constants.PreferenceKeys.CONSTRAIN_COUNT)) {
                        if (appConstrainSize < SharedPrefUtils.readInt(Constants.PreferenceKeys.CONSTRAIN_COUNT)) {
                            alarmApplicationView.onTablesSizeRequestSuccess(
                                appSize,
                                langSize,
                                appConstrainSize
                            )
                        }
                    }
                }
            } catch (e: SQLiteException) {

            } catch (e: NullPointerException) {

            }
        }.start()
    }

    fun getRequiredTableSize() {
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            try {
                val appSize = appDatabase.appDao().totalCountOfApp
                val langSize = appDatabase.languageDao().totalCountOfLanguage
                val appConstrainSize = appDatabase.appConstrainDao().totalCountOfAppConstrain
                if (appSize == 0 || appConstrainSize == 0) {
                    alarmApplicationView.onTablesSizeRequestSuccess(
                        appSize,
                        langSize,
                        appConstrainSize
                    )
                }
            } catch (e: SQLiteException) {

            } catch (e: NullPointerException) {

            }
        }).start()
    }


    fun deleteApplication(applicationEntity: ApplicationEntity, position: Int) {
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            try {
                appDatabase.applicationDao().deleteApplication(applicationEntity)
                alarmApplicationView.onApplicationDeleteSuccess(position)
            } catch (e: NullPointerException) {
                alarmApplicationView.onApplicationDeleteError()
            } catch (e: SQLiteException) {
                alarmApplicationView.onApplicationDeleteError()
            }
        }).start()
    }
}