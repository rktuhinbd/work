package com.app.messagealarm.ui.main.configure_options.viewmodel

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.graphics.Bitmap
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.messagealarm.model.entity.ApplicationEntityNew
import com.app.messagealarm.model.repository.ApplicationRepository
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class OptionViewModel(private val applicationRepository: ApplicationRepository) : ViewModel() {

    // LiveData to handle errors
    val error = MutableLiveData<String>()

    // LiveData to handle success state
    val success = MutableLiveData<Boolean>()

    // LiveData to handle application data
    val application = MutableLiveData<ApplicationEntityNew>()

    // LiveData to handle the application save success state
    val applicationSaveSuccess = MutableLiveData<Boolean>()

    // LiveData to handle the application update success state
    val applicationUpdateSuccess = MutableLiveData<Boolean>()

    // LiveData to handle the bitmap save success path
    val bitmapSaveSuccess = MutableLiveData<String>()

    // Save application
    fun saveApplication(applicationEntity: ApplicationEntityNew, firebaseAnalytics: FirebaseAnalytics?) {
        viewModelScope.launch {
            try {
                // send data to analytics
                firebaseAnalytics?.logEvent(
                    "save_application", Bundle().apply {
                        putString("app_name", applicationEntity.appName)
                        //... other properties
                    }
                )

                // save application
                applicationRepository.insert(applicationEntity)
                applicationSaveSuccess.postValue(true)
            } catch (e: SQLiteConstraintException) {
                // If the app is already in the database then just update it
                updateApplication(applicationEntity)
            } catch (e: Exception) {
                error.postValue(e.message ?: "Something went wrong")
            }
        }
    }

    // Update application
    private fun updateApplication(app: ApplicationEntityNew) {
        viewModelScope.launch {
            try {
                applicationRepository.update(app)
                applicationUpdateSuccess.postValue(true)
            } catch (ex: Exception) {
                error.postValue(ex.message ?: "Something went wrong")
            }
        }
    }

    // Save bitmap to file
    fun saveBitmapToFile(context: Context, packageName: String, bitmap: Bitmap) {
        viewModelScope.launch {
            var file_path = ""
            var imageName = ""
            try {
                file_path = context.getExternalFilesDir(null)!!.absolutePath +
                        "/.message_alarm"
                val dir = File(file_path)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                delay(1000)
                imageName = "$packageName.png"
                val file = File(dir, imageName)
                if (!file.exists()) {
                    val fOut = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fOut)
                    fOut.flush()
                    fOut.close()
                }
            } catch (e: IOException) {
                error.postValue(e.message ?: "Something went wrong")
            }
            bitmapSaveSuccess.postValue("$file_path/$imageName")
        }
    }
}
