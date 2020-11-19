package com.app.messagealarm.ui.setting

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.WorkManager
import com.app.messagealarm.R
import com.app.messagealarm.ui.about.AboutActivity
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import com.app.messagealarm.utils.SupportUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dev.doubledot.doki.api.extensions.DONT_KILL_MY_APP_DEFAULT_MANUFACTURER
import dev.doubledot.doki.ui.DokiActivity
import kotlinx.android.synthetic.main.settings_activity.*


class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        toolBarSetup()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
    }

    private fun toolBarSetup() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                toolbar.navigationIcon?.setTint(resources.getColor(R.color.color_white, theme))
                toolbar.collapseIcon?.setTint(resources.getColor(R.color.color_white, theme))
            }else{
                toolbar.navigationIcon?.setTint(resources.getColor(R.color.color_white))
                toolbar.collapseIcon?.setTint(resources.getColor(R.color.color_white))
            }
        }
    }


    class SettingsFragment : PreferenceFragmentCompat() {

        lateinit var firebaseAnalytics: FirebaseAnalytics

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setListener()
            // Obtain the FirebaseAnalytics instance.
            firebaseAnalytics = Firebase.analytics
        }

        private fun setListener(){

            val notWorkingBackground =
                findPreference("background_not_working") as Preference?
            notWorkingBackground!!.layoutResource = R.layout.layout_preference
            notWorkingBackground.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val bundle = Bundle()
                bundle.putString("background_tutorial", "yes")
                firebaseAnalytics.logEvent("background_not_working_tutorial", bundle)
                //open browser or intent here
                DokiActivity.start(requireActivity(), DONT_KILL_MY_APP_DEFAULT_MANUFACTURER)
                true
            }

            val aboutPre =
                findPreference("about") as Preference?
          aboutPre!!.layoutResource = R.layout.layout_preference
            aboutPre.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                //open browser or intent here
                startActivity(Intent(activity, AboutActivity::class.java))
                true
            }


            val emailPre =
                findPreference("email") as Preference?
            emailPre!!.layoutResource = R.layout.layout_preference
            emailPre.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                //open browser or intent here
                SupportUtils.sendEmail(requireActivity())
                true
            }


            val sharePre =
                findPreference("share") as Preference?
            sharePre!!.layoutResource = R.layout.layout_preference
            sharePre.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                //open browser or intent here
                SupportUtils.shareApp(requireActivity())
                //log this event
                val bundle = Bundle()
                bundle.putString("did_shared", "yes")
                firebaseAnalytics.logEvent("share_app_to_friends", bundle)
                true
            }


            val themePre = findPreference("theme") as ListPreference?
            themePre!!.layoutResource = R.layout.layout_preference
            themePre.setOnPreferenceChangeListener(object :Preference.OnPreferenceChangeListener{
                val bundle = Bundle()
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    if(newValue == "Dark"){
                        //enable dark mode
                        SharedPrefUtils.write(Constants.PreferenceKeys.IS_DARK_MODE, true)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        bundle.putString("theme", "dark")
                        firebaseAnalytics.logEvent("app_theme", bundle)
                    }else if(newValue == "Light"){
                        //enable light mode
                        SharedPrefUtils.write(Constants.PreferenceKeys.IS_DARK_MODE, false)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        bundle.putString("theme", "light")
                        firebaseAnalytics.logEvent("app_theme", bundle)
                    }
                    return true
                }

            })


            val snoozePre = findPreference("mute") as ListPreference?
            snoozePre!!.layoutResource = R.layout.layout_preference
            snoozePre.setOnPreferenceChangeListener(object :Preference.OnPreferenceChangeListener{
                val bundle = Bundle()
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    bundle.putString("mute_status", newValue.toString())
                    firebaseAnalytics.logEvent("mute_options", bundle)
                    if(newValue == "Manual"){
                        WorkManager.getInstance(requireActivity()).cancelAllWorkByTag("MUTE")
                    }
                    return true
                }

            })


        }
    }
}