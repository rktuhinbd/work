package com.app.messagealarm.ui.main.alarm_applications

import android.database.sqlite.SQLiteException
import android.util.Log
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.model.response.TokenResponse
import com.app.messagealarm.networking.RetrofitClient
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.NullPointerException

class AlarmApplicationPresenter(private val alarmApplicationView: AlarmApplicationView){

    fun getApplicationList(){
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            try{
                alarmApplicationView.onGetAlarmApplicationSuccess(appDatabase.applicationDao().allApplicationList
                        as ArrayList<ApplicationEntity>)
            }catch (e:NullPointerException){
                alarmApplicationView.onGetAlarmApplicationError()
            }catch (e:SQLiteException){
                alarmApplicationView.onGetAlarmApplicationError()
            }
        }).start()
    }


    /**
     * sync heroku token when not done by callback from push class
     */
    fun syncFirebaseTokenToHeroku(){
        if(!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_HEROKU_TOKEN_SYNCED)){
           RetrofitClient.getApiServiceHeroku().registerTokenForHeroku(
               SharedPrefUtils.readString(Constants.PreferenceKeys.FIREBASE_TOKEN)
           ) .enqueue(object : Callback<TokenResponse> {
               override fun onResponse(
                   call: Call<TokenResponse>,
                   response: Response<TokenResponse>
               ) {
                   if(response.isSuccessful){
                       //done
                       SharedPrefUtils.write(Constants.PreferenceKeys.IS_HEROKU_TOKEN_SYNCED, true)
                   }
               }

               override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                 //failed to sync
               }
           })
        }
    }

    fun updateAppStatus(boolean: Boolean, id:Int){
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            try{
                appDatabase.applicationDao().updateAppStatus(boolean, id)
                alarmApplicationView.onAppStatusUpdateSuccess()
            }catch (e:NullPointerException){
                e.printStackTrace()
                alarmApplicationView.onAppStatusUpdateError(e.message.toString())
            }catch (e:SQLiteException){
                alarmApplicationView.onAppStatusUpdateError(e.message.toString())
                e.printStackTrace()
            }
        }).start()
    }

    fun getRequiredTableSize(){
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            try {
                val appSize = appDatabase.appDao().totalCountOfApp
                val langSize = appDatabase.languageDao().totalCountOfLanguage
                val appConstrainSize = appDatabase.appConstrainDao().totalCountOfAppConstrain
                if(appSize == 0 || appConstrainSize == 0){
                    alarmApplicationView.onTablesSizeRequestSuccess(appSize, langSize, appConstrainSize)
                }
            }catch (e:SQLiteException){

            }catch (e:NullPointerException){

            }
        }).start()
    }


    fun  deleteApplication(applicationEntity: ApplicationEntity, position:Int){
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            try{
                appDatabase.applicationDao().deleteApplication(applicationEntity)
                alarmApplicationView.onApplicationDeleteSuccess(position)
            }catch (e:NullPointerException){
                alarmApplicationView.onApplicationDeleteError()
            }catch (e:SQLiteException){
                alarmApplicationView.onApplicationDeleteError()
            }
        }).start()
    }
}