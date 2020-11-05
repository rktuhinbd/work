package com.app.messagealarm.ui.main.add_options

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.graphics.Bitmap
import android.os.Environment
import android.os.Handler
import android.util.Log
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.utils.DataUtils
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalStateException
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
                   /**
                    * If the app is already in database then just update it
                    */
                  updateApplication(addApplicationEntity)
               }catch (e:SQLiteException){
                   addApplicationOptionView.onApplicationSaveError(DataUtils.getString(R.string.something_wrong))
               }
            }
        ).start()
    }


    fun getAppByPackageName(packageName:String?){
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            try {
                if(packageName != null){
                    addApplicationOptionView.onApplicationGetSuccess(
                        appDatabase.applicationDao().getAppByPackageName(packageName)
                    )
                }
            }catch (ex:NullPointerException){
                addApplicationOptionView.onApplicationGetError(DataUtils.getString(R.string.something_wrong))
            }catch (ex:SQLiteException){
                addApplicationOptionView.onApplicationGetError(DataUtils.getString(R.string.something_wrong))
            }catch (ex:IllegalStateException){
                addApplicationOptionView.onIllegalState()
            }
        }).start()
    }

   private fun updateApplication(app:ApplicationEntity){
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            try{
                appDatabase.applicationDao().updateApplication(app)
                addApplicationOptionView.onApplicationUpdateSuccess()
            }catch (ex:NullPointerException){
                addApplicationOptionView.onApplicationUpdateError(DataUtils.getString(R.string.update_error))
            }catch (ex:SQLiteException){
                addApplicationOptionView.onApplicationUpdateError(DataUtils.getString(R.string.update_error))
            }
        }).start()
    }


    /*
      * this method save a bitmap to file
      * */
    fun saveBitmapToFile(context: Context, packageName:String, bitmap: Bitmap) {
        var file_path = ""
        var imageName = ""
        try {
            file_path = context.getExternalFilesDir(null)!!.absolutePath +
                    "/.message_alarm"
            val dir = File(file_path)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            Thread.sleep(1000)
            imageName = "$packageName.png"
            val file = File(dir, imageName)
            if (!file.exists()) {
                val fOut = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fOut)
                fOut.flush()
                fOut.close()
            }else{
                addApplicationOptionView.onBitmapSaveSuccess("$file_path/$imageName")
            }
        } catch (e: IOException) {
            Timber.e(e)
            e.printStackTrace()
            addApplicationOptionView.onBitmapSaveError()
        }catch (ex:NullPointerException){
            addApplicationOptionView.onBitmapSaveError()
        }catch (e:Exception){
            e.printStackTrace()
        }
        addApplicationOptionView.onBitmapSaveSuccess("$file_path/$imageName")
    }

}