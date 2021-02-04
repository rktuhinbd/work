package com.app.messagealarm.ui.buy_pro

import androidx.appcompat.app.AppCompatDelegate
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils

class BuyProPresenter {

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