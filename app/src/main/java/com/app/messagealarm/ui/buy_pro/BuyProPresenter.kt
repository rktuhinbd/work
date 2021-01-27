package com.app.messagealarm.ui.buy_pro

import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase

class BuyProPresenter() {
   public fun turnOfVibrateAndJustVibrateFromAllAddedApp(){
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            appDatabase.applicationDao().disableJustVibrateToAllApp(false)
            appDatabase.applicationDao().disableVibrateToAllApp(false)
        }).start()
    }
}