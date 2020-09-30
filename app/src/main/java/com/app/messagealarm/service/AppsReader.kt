package com.app.messagealarm.service

import android.content.Context
import android.content.pm.PackageInfo
import com.app.messagealarm.model.InstalledApps


class AppsReader {

    companion object{
         fun getInstalledApps(getSysPackages: Boolean, context:Context): ArrayList<InstalledApps>? {
            val res: ArrayList<InstalledApps> = ArrayList()
            val packs: List<PackageInfo> =
                context.packageManager.getInstalledPackages(0)
            for (i in packs.indices) {
                val p = packs[i]
                if (!getSysPackages && p.versionName == null) {
                    continue
                }
                val app = InstalledApps(p.applicationInfo.loadLabel(context.packageManager).toString(),
                    p.packageName,
                    p.versionName,
                    p.applicationInfo.loadIcon(context.packageManager)
                    )
                res.add(app)
            }
            return res
        }
    }
}