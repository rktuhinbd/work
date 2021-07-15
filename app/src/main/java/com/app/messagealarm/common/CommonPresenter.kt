package com.app.messagealarm.common

import com.app.messagealarm.model.response.UserInfoGlobal
import com.app.messagealarm.networking.RetrofitClient
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.NullPointerException

class CommonPresenter(private var commonView: CommonView) {
    /**
     * Save to shared preference user from which country
     */
    fun knowUserFromWhichCountry() {
        if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_USER_INFO_SAVED)) {
            Thread {
                val ipAddress = RetrofitClient.getExternalIpAddress()
                val url = String.format(
                    "https://api.ipstack.com/%s?access_key=7abb122c84ee6a620119f0566fa0b620/",
                    ipAddress
                )
                RetrofitClient.getApiService().getCurrentUserInfo(url).enqueue(object :
                    Callback<UserInfoGlobal> {
                    override fun onResponse(
                        call: Call<UserInfoGlobal>,
                        response: Response<UserInfoGlobal>
                    ) {
                        if (response.isSuccessful) {
                            /**
                             * got the response
                             */
                            //save the country code
                            //save the currency code
                            //save the currency symbol
                            try {
                                val userInfo = response.body()
                                if (userInfo != null) {
                                    SharedPrefUtils.write(
                                        Constants.PreferenceKeys.COUNTRY_CODE,
                                        userInfo.countryCode
                                    )
                                    SharedPrefUtils.write(
                                        Constants.PreferenceKeys.CURRENCY_CODE,
                                        userInfo.currency.code
                                    )
                                    SharedPrefUtils.write(
                                        Constants.PreferenceKeys.CURRENCY_SYMBOL,
                                        userInfo.currency.symbolNative
                                    )
                                    SharedPrefUtils.write(
                                        Constants.PreferenceKeys.COUNTRY,
                                        userInfo.countryName
                                    )
                                    //save user info saved
                                    SharedPrefUtils.write(
                                        Constants.PreferenceKeys.IS_USER_INFO_SAVED,
                                        true
                                    )
                                }
                            }catch (e:NullPointerException){

                            }
                            commonView.onSuccess()
                        } else {
                            commonView.onError()
                        }
                    }

                    override fun onFailure(call: Call<UserInfoGlobal>, t: Throwable) {
                        commonView.onError()
                    }
                })
            }.start()
        }
    }

    /**
     * Save to shared preference user from which country
     */
    fun knowUserFromWhichCountry(token: String) {
        if (!SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_USER_INFO_SAVED)) {
            Thread {
                val ipAddress = RetrofitClient.getExternalIpAddress()
                val url = String.format(
                    "https://api.ipstack.com/%s?access_key=7abb122c84ee6a620119f0566fa0b620/",
                    ipAddress
                )
                RetrofitClient.getApiService().getCurrentUserInfo(url).enqueue(object :
                    Callback<UserInfoGlobal> {
                    override fun onResponse(
                        call: Call<UserInfoGlobal>,
                        response: Response<UserInfoGlobal>
                    ) {
                        if (response.isSuccessful) {
                            /**
                             * got the response
                             */
                            //save the country code
                            //save the currency code
                            //save the currency symbol
                            try {
                                val userInfo = response.body()
                                if (userInfo != null) {
                                    SharedPrefUtils.write(
                                        Constants.PreferenceKeys.COUNTRY_CODE,
                                        userInfo.countryCode
                                    )
                                    SharedPrefUtils.write(
                                        Constants.PreferenceKeys.CURRENCY_CODE,
                                        userInfo.currency.code
                                    )
                                    SharedPrefUtils.write(
                                        Constants.PreferenceKeys.CURRENCY_SYMBOL,
                                        userInfo.currency.symbolNative
                                    )
                                    SharedPrefUtils.write(
                                        Constants.PreferenceKeys.COUNTRY,
                                        userInfo.countryName
                                    )
                                    //save user info saved
                                    SharedPrefUtils.write(
                                        Constants.PreferenceKeys.IS_USER_INFO_SAVED,
                                        true
                                    )
                                }
                            }catch (e:NullPointerException){

                            }
                            commonView.onSuccess(token)
                        } else {
                            commonView.onError()
                        }
                    }

                    override fun onFailure(call: Call<UserInfoGlobal>, t: Throwable) {
                        commonView.onError()
                    }
                })
            }.start()
        }
    }
}