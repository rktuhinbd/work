package com.app.messagealarm.ui.about

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatDelegate
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.BuildConfig
import com.app.messagealarm.R
import com.app.messagealarm.utils.*
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_about.*


class AboutActivity : BaseActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        changeTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setToolBar()
        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics
        //log about screen open log
        val bundle = Bundle()
        bundle.putString("open_about_screen", "yes")
        firebaseAnalytics.logEvent("about_page", bundle)
        setListener()
        //set version
        txt_version_name?.text = "Version : ${BuildConfig.VERSION_NAME}"
    }

    private fun changeTheme() {
        if (!SharedPrefUtils.contains(Constants.PreferenceKeys.IS_DARK_MODE)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }


    private fun setToolBar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)) {
            img_company_logo?.setImageResource(R.drawable.company_logo_white)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                toolbar.navigationIcon?.setTint(resources.getColor(R.color.color_white, theme))
                toolbar.collapseIcon?.setTint(resources.getColor(R.color.color_white, theme))
            } else {
                img_company_logo?.setImageResource(R.drawable.company_logo)
                toolbar.navigationIcon?.setTint(resources.getColor(R.color.color_white))
                toolbar.collapseIcon?.setTint(resources.getColor(R.color.color_white))
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    private fun setListener() {
        btn_facebook?.setOnClickListener {
            VisitUrlUtils.visitWebsite(this, "https://www.facebook.com/messagealarm")
        }
        btn_website?.setOnClickListener {
            VisitUrlUtils.visitWebsite(this, "https://www.messagealarm.app")
        }
        btn_instagram?.setOnClickListener {
            VisitUrlUtils.visitWebsite(this, "https://www.instagram.com/message_alarm/")
        }
        btn_linked_in?.setOnClickListener {
            VisitUrlUtils.visitWebsite(
                this,
                "https://www.linkedin.com/company/message-alarm-never-miss-an-important-message"
            )
        }
        btn_website_mk7lab?.setOnClickListener {
            VisitUrlUtils.visitWebsite(this, "https://www.mk7lab.com")
        }
        btn_contact_us?.setOnClickListener {
            SupportUtils.sendEmailSell(this)
        }
        btn_library?.setOnClickListener {
            // When the user selects an option to see the licenses:
            // When the user selects an option to see the licenses:
            OssLicensesMenuActivity.setActivityTitle("Used Libraries Licenses")
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
        }
    }
}