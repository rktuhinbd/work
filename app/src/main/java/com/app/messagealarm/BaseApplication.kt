package com.app.messagealarm

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.os.Bundle
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.app.messagealarm.BaseApplication.Companion.sInstance
import com.app.messagealarm.ui.alarm.AlarmActivity
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.LanguageUtils
import com.app.messagealarm.utils.SharedPrefUtils
import timber.log.Timber

/**
 * This is the Application class of the project. As we want to enable multi-dex inorder to
 * have greater quantity of methods, we are extending [MultiDexApplication] class here.
 * @property sInstance an instance of this Application class
 * @author Al Mujahid Khan
 */
class BaseApplication : MultiDexApplication(){

    init {
        sInstance = this
    }

    companion object {

        /**
         * check if the alarm activity is active or not
         */
        private lateinit var sInstance: BaseApplication

        /**
         * This method provides the Application context
         * @return [Context] application context
         */
        fun getBaseApplicationContext(): Context {
            return sInstance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        // DataUtils.getAndroidHashKey()

        if (applicationContext != null) {
            initiate(applicationContext)
        }
    }

    /**
     * This method only executes in debug build. Therefore, we can place our debug build specific
     * initialization here. i.e, logging library, app data watcher library etc.
     * */
    private fun initiateOnlyInDebugMode() {
        Timber.plant(object : Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String? {
                return super.createStackElementTag(element) +
                        " - Method:${element.methodName} - Line:${element.lineNumber}"
            }
        })
    }

    /**
     * This method executes in every build mode. Therefore, we can place our essential and common
     * initialization here. i.e, base repository, usable libraries etc
     * */
    private fun initiate(context: Context) {

    }

    override fun attachBaseContext(base: Context?) {
        SharedPrefUtils.init(base!!)
        val context = LanguageUtils.onAttach(base, Constants.Default.DEFAULT_LANGUAGE)
        super.attachBaseContext(context)
        MultiDex.install(context)
    }
}