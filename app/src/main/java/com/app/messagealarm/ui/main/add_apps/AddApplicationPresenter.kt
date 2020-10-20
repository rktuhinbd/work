package com.app.messagealarm.ui.main.add_apps

import android.app.Activity
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.service.AppsReader
import com.app.messagealarm.utils.DataUtils

class AddApplicationPresenter(
    private val addApplicationView: AddApplicationView,
    private val activity: Activity
) {


    fun getAllApplicationList() {
        Thread(Runnable {
            try {
                addApplicationView.onAllApplicationGetSuccess(
                    AppsReader.getInstalledApps(true, activity)!!
                )
            } catch (e: Exception) {
                addApplicationView.onAllApplicationGetError(DataUtils.getString(R.string.something_wrong))
            }
        }).start()
    }

    fun filterByMessaging(
    ) {
        val installedAppsList = AppsReader.getInstalledApps(true, activity)
        val listOfPackage = ArrayList<String>()
        listOfPackage.add("com.imo.android.imoim")
        listOfPackage.add("org.telegram.messenger")
        listOfPackage.add("com.google.android.talk")
        listOfPackage.add("com.snapchat.android")
        listOfPackage.add("com.Slack")
        listOfPackage.add("com.zoho.meeting")
        listOfPackage.add("com.google.android.apps.tachyon")
        listOfPackage.add("com.snrblabs.grooveip")
        listOfPackage.add("me.dingtone.app.im")
        listOfPackage.add("textnooow.freesmscalltips.nowthetextguide")
        listOfPackage.add("com.calea.echo")
        listOfPackage.add("org.thoughtcrime.securesms")
        listOfPackage.add("com.google.android.apps.dynamite")
        listOfPackage.add("com.bbm.enterprise")
        listOfPackage.add("jp.naver.line.android")
        listOfPackage.add("com.viber.voip")
        listOfPackage.add("com.tencent.mm")
        listOfPackage.add("com.whatsapp")
        listOfPackage.add("com.syncme.syncmeapp")
        listOfPackage.add("com.cloze.app")
        listOfPackage.add("com.covve.android")
        listOfPackage.add("com.contapps.android")
        listOfPackage.add("com.skype.raider")
        listOfPackage.add("com.lmi.g2mmessenger")
        listOfPackage.add("com.gotomeeting")
        listOfPackage.add("com.upwork.android.apps.main")
        listOfPackage.add("com.fiverr.fiverr")
        listOfPackage.add("com.freelancer.android.messenger")
        listOfPackage.add("com.pph")
        listOfPackage.add("com.truelancer.app")
        listOfPackage.add("mathieumaree.rippple")
        listOfPackage.add("com.behance.behance")
        listOfPackage.add("com.facebook.orca")
        listOfPackage.add("com.facebook.mlite")
        listOfPackage.add("com.google.android.apps.messaging")
        listOfPackage.add("com.jb.gosms")
        listOfPackage.add("com.concentriclivers.mms.com.android.mms")
        listOfPackage.add("fr.slvn.mms")
        listOfPackage.add("com.android.mms")
        listOfPackage.add("com.sonyericsson.conversations")
        listOfPackage.add("us.zoom.videomeetings")
        val holder = ArrayList<InstalledApps>()
        for (x in installedAppsList!!) {
            for (y in listOfPackage) {
                if (x.packageName == y) {
                    holder.add(x)
                }
            }
        }
        addApplicationView.onApplicationFiltered(holder)
    }
}