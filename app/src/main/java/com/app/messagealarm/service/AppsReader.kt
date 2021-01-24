package com.app.messagealarm.service

import android.content.Context
import android.content.pm.PackageInfo
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.local_database.AppDatabase
import com.app.messagealarm.model.InstalledApps


class AppsReader {

    companion object {
        fun getInstalledApps(getSysPackages: Boolean, context: Context): ArrayList<InstalledApps>? {
            val res: ArrayList<InstalledApps> = ArrayList()
            val packs: List<PackageInfo> =
                context.packageManager.getInstalledPackages(0)
            for (i in packs.indices) {
                val p = packs[i]
                if (!getSysPackages && p.versionName == null) {
                    continue
                }
                if(p.packageName == "com.app.messagealarm"){
                    continue
                }
                val app = InstalledApps(
                    p.applicationInfo.loadLabel(context.packageManager).toString(),
                    p.packageName,
                    p.versionName,
                    p.applicationInfo.loadIcon(context.packageManager)
                )
                if (app.appName.contains(".")) {
                    continue
                }
                res.add(app)
            }
            return res
        }
    }

}