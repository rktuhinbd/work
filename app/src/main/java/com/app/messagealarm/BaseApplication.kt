package com.app.messagealarm

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.app.messagealarm.BaseApplication.Companion.sInstance
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.model.response.UserInfoGlobal
import com.app.messagealarm.networking.RetrofitClient
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.LanguageUtils
import com.app.messagealarm.utils.SharedPrefUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.*

/**
 * This is the Application class of the project. As we want to enable multi-dex inorder to
 * have greater quantity of methods, we are extending [MultiDexApplication] class here.
 * @property sInstance an instance of this Application class
 * @author Al Mujahid Khan
 */
class BaseApplication : MultiDexApplication(){

    init {
        sInstance = this
    }

    companion object {

        /**
         * Save to shared preference user from which country
         */
        fun knowUserFromWhichCountry(){
            if(!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_USER_INFO_SAVED)){
                Thread{
                    val ipAddress = RetrofitClient.getExternalIpAddress()
                    val url = String.format("https://api.ipstack.com/%s?access_key=7abb122c84ee6a620119f0566fa0b620", ipAddress)
                    RetrofitClient.getApiService().getCurrentUserInfo(url).enqueue(object :
                        Callback<UserInfoGlobal> {
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

        var isHintShowing: Boolean = false
        var installedApps: List<InstalledApps> = ArrayList()
        /**
         * check if the alarm activity is active or not
         */
        private lateinit var sInstance: BaseApplication

        /**
         * This method provides the Application context
         * @return [Context] application context
         */
        fun getBaseApplicationContext(): Context {
            return sInstance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        // DataUtils.getAndroidHashKey()

        if (applicationContext != null) {
            initiate(applicationContext)
        }
    }

    /**
     * This method only executes in debug build. Therefore, we can place our debug build specific
     * initialization here. i.e, logging library, app data watcher library etc.
     * */
    private fun initiateOnlyInDebugMode() {
        Timber.plant(object : Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String? {
                return super.createStackElementTag(element) +
                        " - Method:${element.methodName} - Line:${element.lineNumber}"
            }
        })
    }

    /**
     * This method executes in every build mode. Therefore, we can place our essential and common
     * initialization here. i.e, base repository, usable libraries etc
     * */
    private fun initiate(context: Context) {

    }

    override fun attachBaseContext(base: Context?) {
        SharedPrefUtils.init(base!!)
        val context = LanguageUtils.onAttach(base, Constants.Default.DEFAULT_LANGUAGE)
        super.attachBaseContext(context)
        MultiDex.install(context)
    }
}