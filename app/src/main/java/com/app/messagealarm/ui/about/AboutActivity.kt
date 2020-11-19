package com.app.messagealarm.ui.about

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.R
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.MenuTintUtils
import com.app.messagealarm.utils.SharedPrefUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : BaseActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setToolBar()
        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics
        //log about screen open log
        val bundle = Bundle()
        bundle.putString("open_about_screen", "yes")
        firebaseAnalytics.logEvent("about_page", bundle)
    }

    private fun setToolBar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)){
            img_company_logo?.setImageResource(R.drawable.company_logo_white)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                toolbar.navigationIcon?.setTint(resources.getColor(R.color.color_white, theme))
                toolbar.collapseIcon?.setTint(resources.getColor(R.color.color_white, theme))
            }else{
                img_company_logo?.setImageResource(R.drawable.company_logo)
                toolbar.navigationIcon?.setTint(resources.getColor(R.color.color_white))
                toolbar.collapseIcon?.setTint(resources.getColor(R.color.color_white))
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }
}