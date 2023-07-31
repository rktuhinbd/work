package com.app.messagealarm.ui.main.configure_options.presenter

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.Telephony
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.model.response.LatestInfo
import com.app.messagealarm.networking.RetrofitClient
import com.app.messagealarm.ui.main.configure_options.view.OptionView
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.DataUtils
import com.app.messagealarm.utils.SharedPrefUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.JsonParseException
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException

class OptionPresenter(private val optionView: OptionView) {

    fun saveApplication(
        addApplicationEntity: ApplicationEntity,
        firebaseAnalytics: FirebaseAnalytics?
    ) {
        //send data to analytics
        if (firebaseAnalytics != null) {
            val bundle = Bundle()
            bundle.putString("app_name", addApplicationEntity.appName)
            bundle.putString("package_name", addApplicationEntity.packageName)
            bundle.putString("alarm_repeat", addApplicationEntity.alarmRepeat)
            bundle.putString("repeat_days", addApplicationEntity.repeatDays)
            bundle.putString("sender_names", addApplicationEntity.senderNames)
            bundle.putString("message_body", addApplicationEntity.messageBody)
            bundle.putString("is_custom_time", addApplicationEntity.isCustomTime.toString())
            bundle.putString("is_just_vibrate", addApplicationEntity.isJustVibrate.toString())
            bundle.putString("is_vibrate", addApplicationEntity.isVibrateOnAlarm.toString())
            bundle.putString("start_time", addApplicationEntity.startTime.toString())
            bundle.putString("end_time", addApplicationEntity.endTime.toString())
            bundle.putString("number_of_play", addApplicationEntity.numberOfPlay.toString())
            bundle.putString("sound_level", addApplicationEntity.sound_level.toString())
            firebaseAnalytics.logEvent("save_application", bundle)
        }
        //save application
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(
            Runnable {
                try {
                    appDatabase.applicationDao().insertApplication(addApplicationEntity)
                    optionView.onApplicationSaveSuccess()
                } catch (e: NullPointerException) {
                    optionView.onApplicationSaveError(DataUtils.getString(R.string.something_wrong))
                } catch (e: SQLiteConstraintException) {
                    /**
                     * If the app is already in database then just update it
                     */
                    updateApplication(addApplicationEntity)
                } catch (e: SQLiteException) {
                    optionView.onApplicationSaveError(DataUtils.getString(R.string.something_wrong))
                }
            }
        ).start()
    }


    fun getAppByPackageNameAndAlarm(packageName: String?) {
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            try {
                if (packageName != null) {
                    optionView.onApplicationGetSuccess(
                        appDatabase.applicationDao().getAppByPackageNameAndAlarm(packageName, true)
                    )
                }
            } catch (ex: NullPointerException) {
                optionView.onApplicationGetError(DataUtils.getString(R.string.something_wrong))
            } catch (ex: SQLiteException) {
                optionView.onApplicationGetError(DataUtils.getString(R.string.something_wrong))
            } catch (ex: IllegalStateException) {
                optionView.onIllegalState()
            }
        }).start()
    }


    fun getAppByPackageNameAndSpeak(packageName: String?) {
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            try {
                if (packageName != null) {
                    optionView.onApplicationGetSuccess(
                        appDatabase.applicationDao().getAppByPackageNameAndSpeak(packageName, true)
                    )
                }
            } catch (ex: NullPointerException) {
                optionView.onApplicationGetError(DataUtils.getString(R.string.something_wrong))
            } catch (ex: SQLiteException) {
                optionView.onApplicationGetError(DataUtils.getString(R.string.something_wrong))
            } catch (ex: IllegalStateException) {
                optionView.onIllegalState()
            }
        }).start()
    }

    private fun updateApplication(app: ApplicationEntity) {
        val appDatabase = AppDatabase.getInstance(BaseApplication.getBaseApplicationContext())
        Thread(Runnable {
            try {
                appDatabase.applicationDao().updateApplication(app)
                optionView.onApplicationUpdateSuccess()
            } catch (ex: NullPointerException) {
                optionView.onApplicationUpdateError(DataUtils.getString(R.string.update_error))
            } catch (ex: SQLiteException) {
                optionView.onApplicationUpdateError(DataUtils.getString(R.string.update_error))
            }
        }).start()
    }


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

    /*
      * this method save a bitmap to file
      * */
    fun saveBitmapToFile(context: Context, packageName: String, bitmap: Bitmap) {
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
            } else {
                optionView.onBitmapSaveSuccess("$file_path/$imageName")
            }
        } catch (e: IOException) {
            Timber.e(e)
            e.printStackTrace()
            optionView.onBitmapSaveError()
        } catch (ex: NullPointerException) {
            optionView.onBitmapSaveError()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        optionView.onBitmapSaveSuccess("$file_path/$imageName")
    }

}