package com.app.messagealarm.ui.main.add_options

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.graphics.Bitmap
import android.os.Environment
import android.os.Handler
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.utils.DataUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
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
    /*
      * this method save a bitmap to file
      * */
    fun saveBitmapToFile(bitmap: Bitmap) {
        var file_path = ""
        var imageName = ""
        try {
            file_path = Environment.getExternalStorageDirectory().absolutePath +
                    "/.message_alarm"
            val dir = File(file_path)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            Thread.sleep(1000)
            val image = System.currentTimeMillis().toString()
            imageName = "$image.png"
            val file = File(dir, imageName)
            if (!file.exists()) {
                val fOut = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fOut)
                fOut.flush()
                fOut.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            addApplicationOptionView.onBitmapSaveError()
        }catch (ex:NullPointerException){
            addApplicationOptionView.onBitmapSaveError()
        }
        addApplicationOptionView.onBitmapSaveSuccess("$file_path/$imageName")
    }

}