package com.app.messagealarm.ui.main.add_options

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.utils.DataUtils
import java.lang.NullPointerException

class AddApplicationOptionPresenter(private val addApplicationOptionView: AddApplicationOptionView) {
    fun saveApplication(addApplicationEntity: ApplicationEntity){
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(
            Runnable {
               try{
                    appDatabase.applicationDao().insertApplication(addApplicationEntity)
                   addApplicationOptionView.onApplicationSaveSuccess()
               }catch (e:NullPointerException){
                   addApplicationOptionView.onApplicationSaveError(DataUtils.getString(R.string.something_wrong))
               }catch (e:SQLiteConstraintException){
                   addApplicationOptionView.onApplicationSaveError(DataUtils.getString(R.string.txt_application_exists))
               }catch (e:SQLiteException){
                   addApplicationOptionView.onApplicationSaveError(DataUtils.getString(R.string.something_wrong))
               }
            }
        ).start()
    }
}