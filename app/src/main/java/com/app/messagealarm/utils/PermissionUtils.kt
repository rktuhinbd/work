package com.app.messagealarm.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import com.app.messagealarm.BaseApplication
import java.util.*

/**
 * This is a class that contains utils for the runtime permissions
 * @author Al Mujahid Khan
 * */
object PermissionUtils {

    const val REQUEST_CODE_PERMISSION_DEFAULT = 1
    const val REQUEST_CODE_PERMISSION_LOCATION = 2
    const val REQUEST_CODE_PERMISSION_PICK_PHOTO = 3

    /**
     * This method is to get specific permission/s from user in the provided activity
     * with default request code and
     * returns true/false depending on the allowance of the requested permissions
     *
     * @param activity current activity
     * @param permissions required permission/s
     * @return true if the requested permissions are already allowed otherwise false
     * */
    @Synchronized
    fun requestPermission(activity: Activity, vararg permissions: String): Boolean {
        return requestPermission(null, activity,
                REQUEST_CODE_PERMISSION_DEFAULT, permissions.toList())
    }

    /**
     * This method is to get specific permission/s from user in the provided fragment
     * with default request code and
     * returns true/false depending on the allowance of the requested permissions
     *
     * @param fragment current fragment
     * @param permissions required permission/s
     * @return true if the requested permissions are already allowed otherwise false
     * */
    @Synchronized
    fun requestPermission(fragment: Fragment, vararg permissions: String): Boolean {
        return requestPermission(fragment, null,
                REQUEST_CODE_PERMISSION_DEFAULT, permissions.toList())
    }

    /**
     * This method is to get specific permission/s from user in the provided activity
     * with custom request code and
     * returns true/false depending on the allowance of the requested permissions
     *
     * @param activity current activity
     * @param requestCode custom request code
     * @param permissions required permission/s
     * @return true if the requested permissions are already allowed otherwise false
     * */
    @Synchronized
    fun requestPermission(activity: Activity, requestCode: Int,
                          vararg permissions: String): Boolean {
        return requestPermission(null, activity, requestCode, permissions.toList())
    }

    /**
     * This method is to get specific permission/s from user in the provided fragment
     * with custom request code and
     * returns true/false depending on the allowance of the requested permissions
     *
     * @param fragment current fragment
     * @param requestCode custom request code
     * @param permissions required permission/s
     * @return true if the requested permissions are already allowed otherwise false
     * */
    @Synchronized
    fun requestPermission(fragment: Fragment, requestCode: Int,
                          vararg permissions: String): Boolean {
        return requestPermission(fragment, null, requestCode, permissions.toList())
    }

    private fun requestPermission(fragment: Fragment?, activity: Activity?,
                                  requestCode: Int, permissions: List<String>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        val permissionsNotTaken = ArrayList<String>()

        for (i in permissions.indices) {
            if (!isAllowed(permissions[i])) {
                permissionsNotTaken.add(permissions[i])
            }
        }

        if (permissionsNotTaken.isEmpty()) {
            return true
        }

        if (fragment == null) {
            activity?.requestPermissions(permissionsNotTaken.toTypedArray(), requestCode)
        } else {
            fragment.requestPermissions(permissionsNotTaken.toTypedArray(), requestCode)
        }

        return false
    }

    /**
     * This method returns the allowance state of a permission
     *
     * @param permission permission to be checked
     * @return true if the permission is already granted, else false
     * */
    fun isAllowed(permission: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        return BaseApplication.getBaseApplicationContext()
                .checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * This function return the state of notification permission of app
     */
    fun isNotificationAllowed(): Boolean{
        var isAllowed = false
        val packageList =
            NotificationManagerCompat.getEnabledListenerPackages(BaseApplication.getBaseApplicationContext())
        for (s in packageList) {
            if(s == BaseApplication.getBaseApplicationContext().packageName){
                isAllowed = true
            }
        }
        return isAllowed
    }

    /**
     * This function takes useful permission of autostart, battery etc
     */
    fun takePhoneRequiredPermission(activity:Activity){
        val POWERMANAGER_INTENTS = arrayOf(
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.letv.android.letvsafe",
                    "com.letv.android.letvsafe.AutobootManageActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.startupapp.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.oppo.safe",
                    "com.oppo.safe.permission.startup.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.samsung.android.lool",
                    "com.samsung.android.sm.ui.battery.BatteryActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.htc.pitroad",
                    "com.htc.pitroad.landingpage.activity.LandingPageActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.asus.mobilemanager",
                    "com.asus.mobilemanager.MainActivity"
                )
            )
        )

        for (intent in POWERMANAGER_INTENTS) if (BaseApplication.getBaseApplicationContext()
                .packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            ) != null
        ) {
            activity.startActivity(intent)
            break
        }
    }
}