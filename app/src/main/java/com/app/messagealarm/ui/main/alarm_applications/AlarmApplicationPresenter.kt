package com.app.messagealarm.ui.main.alarm_applications

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.model.response.TokenResponse
import com.app.messagealarm.model.response.UserInfoGlobal
import com.app.messagealarm.networking.RetrofitClient
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import com.app.messagealarm.work_manager.WorkManagerUtils
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
     * Save to shared preference user from which country
     */
     fun knowUserFromWhichCountry(){
        if(!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_USER_INFO_SAVED)){
            Thread{
                val ipAddress = RetrofitClient.getExternalIpAddress()
                val url = String.format("https://api.ipstack.com/%s?access_key=7abb122c84ee6a620119f0566fa0b620", ipAddress)
                RetrofitClient.getApiService().getCurrentUserInfo(url).enqueue(object : Callback<UserInfoGlobal>{
                    override fun onResponse(
                        call: Call<UserInfoGlobal>,
                        response: Response<UserInfoGlobal>
                    ) {
                        if(response.isSuccessful){
                            /**
                             * got the response
                             */
                            //save the country code
                            //save the currency code
                            //save the currency symbol
                            val userInfo = response.body()
                            SharedPrefUtils.write(Constants.PreferenceKeys.COUNTRY_CODE,
                                userInfo?.countryCode!!
                            )
                            SharedPrefUtils.write(Constants.PreferenceKeys.CURRENCY_CODE,
                                userInfo.currency.code
                            )
                            SharedPrefUtils.write(Constants.PreferenceKeys.CURRENCY_SYMBOL,
                                userInfo.currency.symbolNative
                            )
                            //save user info saved
                            SharedPrefUtils.write(Constants.PreferenceKeys.IS_USER_INFO_SAVED, true)
                        }
                    }

                    override fun onFailure(call: Call<UserInfoGlobal>, t: Throwable) {

                    }

                })
            }.start()
        }
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

    /**
     * 2.0.1 user to 2.0.2 sending token and status to server so we can know user status
     */
    fun updateUserToken(isPaid:Boolean){
        Thread{
            if(!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_FIREBASE_TOKEN_SYNCED_2_0_2)){
                RetrofitClient.getApiService().updateCurrentToken(
                    SharedPrefUtils.readString(Constants.PreferenceKeys.FIREBASE_TOKEN),
                    RetrofitClient.getExternalIpAddress(),
                    isPaid
                    ).execute()
            }
        }.start()

    }

    /**
     * I know the constrain count is less than the server, sync from server
     * Need to test
     */
    fun getSyncedLowerLoaded(context: Context){
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