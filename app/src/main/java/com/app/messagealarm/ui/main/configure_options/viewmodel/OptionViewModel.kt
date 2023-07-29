package com.app.messagealarm.ui.main.configure_options.viewmodel

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.Telephony
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.entity.ApplicationEntityNew
import com.app.messagealarm.model.repository.ApplicationRepository
import com.app.messagealarm.model.response.LatestInfo
import com.app.messagealarm.networking.ApiService
import com.app.messagealarm.networking.RetrofitClient
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.JsonParseException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException

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

    // Get application by package name
    fun getAppByPackageName(packageName: String) {
        viewModelScope.launch {
            try {
                packageName?.let {
                    val result = applicationRepository.getAppByPackageName(it)
                    application.postValue(result)
                }
            } catch (e: Exception) {
                error.postValue(e.message ?: "Something went wrong")
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

    /**
     * Check for unknown app
     */
    fun checkForUnknownApp(context: Activity, appName: String, packageName: String) {
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        var count = 0
        Thread(Runnable {
            val appList = appDatabase.appDao().appsList
            appList.forEach {
                if (it.appPackageName == packageName) {
                    count++
                }
            }
            if (count == 0) {
                //check if first time app was synced
                //it's an unknown app
                if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.FIRST_APP_SYNC_FINISHED) ||
                    appDatabase.appConstrainDao().totalCountOfAppConstrain == SharedPrefUtils.readInt(
                        Constants.PreferenceKeys.CONSTRAIN_COUNT
                    )
                ) {
                    if (packageName != Telephony.Sms.getDefaultSmsPackage(context)) {
                        sendUnknownAppNameToServer(appName, packageName)
                    }
                }
            }
        }).start()
    }

    private fun sendUnknownAppNameToServer(appName: String, packageName: String) {
        try {
            RetrofitClient.getApiService().notifyUnknownApp(
                appName, packageName,
                SharedPrefUtils.readString(Constants.PreferenceKeys.FIREBASE_TOKEN, "not_found")
            ).execute()
        } catch (e: JsonParseException) {

        } catch (e: SocketTimeoutException) {

        } catch (e: IOException) {

        } catch (e: JSONException) {

        }
    }


    /**
     * Check for updated version
     */
    fun checkForLatestUpdate() {
        RetrofitClient.getApiService().latestVersion.enqueue(object : Callback<LatestInfo> {
            override fun onResponse(call: Call<LatestInfo>, response: Response<LatestInfo>) {
                if (response.isSuccessful) {
                    //successful call
                    val latestInfo = response.body()
                    if (latestInfo!!.isSuccess) {
                        SharedPrefUtils.write(
                            Constants.PreferenceKeys.CONSTRAIN_COUNT,
                            latestInfo.totalConstraints
                        )
                        SharedPrefUtils.write(
                            Constants.PreferenceKeys.UPDATED_VERSION,
                            latestInfo.currentVersion
                        )
                    }
                }
            }

            override fun onFailure(call: Call<LatestInfo>, t: Throwable) {

            }
        })
    }
}
