package com.app.messagealarm.ui.buy_pro

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.android.billingclient.api.Purchase
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.response.VerifyPurchaseResponse
import com.app.messagealarm.networking.RetrofitClient
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BuyProPresenter(private val buyProView: BuyProView) {

    /*
    * delete the turned of vibrate option
    * */
    private fun turnOfVibrateAndJustVibrateFromAllAddedApp(){
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            appDatabase.applicationDao().disableJustVibrateToAllApp(false)
            appDatabase.applicationDao().disableVibrateToAllApp(false)
        }).start()
    }


    fun cancelPurchase(){
        turnOfVibrateAndJustVibrateFromAllAddedApp()
        deleteAddedApps()
        //delete preference
        SharedPrefUtils.delete(Constants.PreferenceKeys.IS_DARK_MODE)
        changeTheme()
    }

    /**
     * verify purchase
     */
     fun verifyPurchase(
        receipt: String,
        signature: String,
        purchase: Purchase
    ){
        Log.e("RECEIPT", receipt)
        Log.e("SIGNATURE", signature)
        RetrofitClient.getApiService().verifyPurchase(receipt, signature).enqueue(
            object : Callback<VerifyPurchaseResponse>{
                override fun onFailure(call: Call<VerifyPurchaseResponse>, t: Throwable) {
                    buyProView.verifyPurchaseStatus(false, purchase)
                }
                override fun onResponse(
                    call: Call<VerifyPurchaseResponse>,
                    response: Response<VerifyPurchaseResponse>
                ) {
                   if(response.isSuccessful){
                       if(response.body()!!.isSuccess){
                           buyProView.verifyPurchaseStatus(true, purchase)
                       }else{
                           buyProView.verifyPurchaseStatus(false, purchase)
                       }
                   }else{
                       buyProView.verifyPurchaseStatus(false, purchase)
                   }
                }

            }
        )
    }


/*
* change theme to white as purchase is canceled
* */
     private fun changeTheme() {
        if (!SharedPrefUtils.contains(Constants.PreferenceKeys.IS_DARK_MODE)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    /*
    * Delete added application without the first one
    * */
    private fun deleteAddedApps(){
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            appDatabase.applicationDao().deleteAllAppsWithoutTheFirstOne()
        }).start()
    }

}