package com.app.messagealarm.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.app.messagealarm.BaseApplication
import java.util.*
import kotlin.system.exitProcess

/**
 * This is a class that contains utils to work with Android os level
 * @author Al Mujahid Khan
 * */
class AndroidUtils private constructor() {
    companion object {
        /**
         * This method provides application id of the application
         *
         * @return [String] application id
         * */
        fun getApplicationId(): String? {
            return getPackageInfo()?.packageName
        }

        fun getCurrentLangCode(context: Context) : String{
            val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales.get(0)
            } else {
                context.resources.configuration.locale
            }
            return locale.language
        }

        /**
         * This method provides version code of the application
         *
         * @return [Int] version code
         * */
        @SuppressLint("NewApi")
        @Suppress("DEPRECATION")
        fun getApplicationVersionCode(): Long {
            return if (getCurrentBuildApiVersion() >= Build.VERSION_CODES.P) {
                getPackageInfo()?.longVersionCode ?: 0
            } else {
                getPackageInfo()?.versionCode as Long? ?: 0
            }
        }

        fun isAppRunning(
            context: Context,
            packageName: String
        ): Boolean {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val procInfos =
                activityManager.runningAppProcesses
            if (procInfos != null) {
                for (processInfo in procInfos) {
                    if (processInfo.processName == packageName) {
                        return true
                    }
                }
            }
            return false
        }

        /**
         * This method provides version name of the application
         *
         * @return [String] version name
         * */
        fun getApplicationVersionName(): String? {
            return getPackageInfo()?.versionName
        }

        /**
         * This method provides package info of the application
         *
         * @return [PackageInfo] package info of the application
         * */
        fun getPackageInfo(): PackageInfo? {
            val context: Context = BaseApplication.getBaseApplicationContext()

            return try {
                context.packageManager.getPackageInfo(context.packageName, 0)
            } catch (nameException: PackageManager.NameNotFoundException) {
                null
            }
        }

        /**
         * This method provides the state if a service is running or not
         *
         * @param context UI context
         * @param serviceClass service class
         * @return [Boolean] if the service is running or not
         * */
        @Suppress("DEPRECATION")
        fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
            return false
        }

        /**
         * This method provides current build API version
         *
         * @return [Int] current build API version
         * */
        fun getCurrentBuildApiVersion(): Int {
            return Build.VERSION.SDK_INT
        }

        /**
         * This method restarts the application
         *
         * @param intent intent with target activity to be restarted
         * @param context UI context
         * */
        fun restartApplication(intent: Intent, context: Context) {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            exitProcess(0)
        }
    }
}