package com.app.messagealarm.service

import android.database.sqlite.SQLiteException
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.entity.AppConstrainEntity
import com.app.messagealarm.model.entity.AppEntity
import com.app.messagealarm.model.entity.LanguageEntity
import com.app.messagealarm.model.response.sync.SyncResponse

class BGSyncDataSavingService {

    companion object{

        fun saveData(syncResponse: SyncResponse){
            val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
            //save language
            Thread(Runnable {
                try {
                    //init language entity
                    val languageList = syncResponse.allLanguage
                    for (x in languageList) {
                        Thread.sleep(200)
                        appDatabase.languageDao().insertLanguage(
                            LanguageEntity(
                                x.langName,
                                x.langCode,
                                x.langId
                            )
                        )
                    }
                } catch (e: SQLiteException) {

                } catch (e: NullPointerException) {

                } catch (e: InterruptedException){

                }
            }).start()

            //save app
            Thread(Runnable {
                try {
                    //init language entity
                    val appList = syncResponse.allApp
                    for (x in appList) {
                        Thread.sleep(200)
                        appDatabase.appDao().insertApp(
                            AppEntity(
                               x.appName,
                                x.appId,
                                x.appPackageName
                            )
                        )
                    }
                } catch (e: SQLiteException) {

                } catch (e: NullPointerException) {

                } catch (e: InterruptedException){

                }
            }).start()

            //save app constrain
            Thread(Runnable {
                try {
                    //init language entity
                    val appConstrain = syncResponse.allAppconstrain
                    for (x in appConstrain) {
                        Thread.sleep(200)
                        appDatabase.appConstrainDao().insertAppConstrain(
                            AppConstrainEntity(
                              x.langCode,
                                x.description,
                                x.title,
                                x.appPackageName
                            )
                        )
                    }
                } catch (e: SQLiteException) {

                } catch (e: NullPointerException) {

                } catch (e: InterruptedException){

                }
            }).start()

        }
    }

}