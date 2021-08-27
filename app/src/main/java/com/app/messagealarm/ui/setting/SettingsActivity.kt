package com.app.messagealarm.ui.setting

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import androidx.work.WorkManager
import com.app.messagealarm.R
import com.app.messagealarm.ui.about.AboutActivity
import com.app.messagealarm.ui.buy_pro.BuyProActivity
import com.app.messagealarm.ui.onboarding.OnboardingDialog
import com.app.messagealarm.utils.*
import com.app.messagealarm.work_manager.WorkManagerUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dev.doubledot.doki.api.extensions.DONT_KILL_MY_APP_DEFAULT_MANUFACTURER
import dev.doubledot.doki.ui.DokiActivity
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.settings_activity.*
import java.lang.IllegalStateException


class SettingsActivity : AppCompatActivity() {

    var settingFragment:SettingsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        changeTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        toolBarSetup()
        settingFragment = SettingsFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, settingFragment!!)
            .commit()
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

    private fun isPurchased() : Boolean{
        return SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == Constants.ACTION.ACTION_PURCHASE_FROM_SETTING){
            if(isPurchased()){
                Toasty.success(this, "Thanks for purchase! You are now pro user!").show()
            }
            recreate()
            supportFragmentManager.beginTransaction()
                .detach(SettingsFragment())
                .attach(SettingsFragment())
                .commitAllowingStateLoss()
        }
        super.onActivityResult(requestCode, resultCode, data)
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

       private lateinit var firebaseAnalytics: FirebaseAnalytics

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setListener()
            // Obtain the FirebaseAnalytics instance.
            firebaseAnalytics = Firebase.analytics
        }

        private fun showQuickStartDialog() {
            try {
                val quickStartDialog = OnboardingDialog()
                quickStartDialog.show(requireActivity().supportFragmentManager, "quick_start")
            } catch (e: IllegalStateException) {

            }
        }

        private fun isPurchased() : Boolean{
            return SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)
        }

        private fun setListener(){
            //hide top line of preference category
          /*  val themeCategory = findPreference("theme_cat") as PreferenceCategory?
            themeCategory?.isEnabled = isPurchased()*/
            val chatFeature = findPreference("chat") as Preference?
            if(TimeUtils.getPossibleReplyTime().contains("Sleeping")){
                chatFeature!!.layoutResource = R.layout.layout_preference_chat_offline
            }else{
                chatFeature!!.layoutResource = R.layout.layout_preference_chat
            }
                chatFeature.title = "Live Chat"
                chatFeature.summary = TimeUtils.getPossibleReplyTime()

            chatFeature.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val bundle = Bundle()
                bundle.putString("live_chat", "yes")
                firebaseAnalytics.logEvent("live_chat", bundle)
                /**
                 * change by Mujahid
                 */
                LiveChatUtils.openWhatsApp(requireActivity())
                true
            }

            val tutorial =
                findPreference("tutorial") as Preference?
            tutorial!!.layoutResource = R.layout.layout_preference
            tutorial.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val bundle = Bundle()
                bundle.putString("tutorial", "yes")
                firebaseAnalytics.logEvent("tutorial", bundle)
                //open browser or intent here
                showQuickStartDialog()
                true
            }

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


            /**
             * theme section
             */

            val themeSectionPro = findPreference("theme_pro") as Preference?
            themeSectionPro?.isVisible  = !isPurchased()
            themeSectionPro!!.layoutResource = R.layout.layout_preference_theme

            themeSectionPro.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                 val intent = Intent(activity, BuyProActivity::class.java)
                 requireActivity().startActivityForResult(intent,
                     Constants.ACTION.ACTION_PURCHASE_FROM_SETTING)
                true
            }


            val themePre = findPreference("theme") as ListPreference?
            themePre?.isVisible = isPurchased()
            try {
                if(!isPurchased()){
                    themePre?.setValueIndex(0)
                }
            }catch (e:IllegalStateException){

            }
            themePre!!.layoutResource = R.layout.layout_preference
                themePre.onPreferenceChangeListener = object :Preference.OnPreferenceChangeListener{
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
                }

            val snoozePre = findPreference("mute") as ListPreference?
            snoozePre!!.layoutResource = R.layout.layout_preference
            snoozePre.onPreferenceChangeListener = object :Preference.OnPreferenceChangeListener{
                val bundle = Bundle()
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    bundle.putString("mute_status", newValue.toString())
                   // Log.e("mute_status", newValue.toString())
                    firebaseAnalytics.logEvent("mute_options", bundle)
                    if(newValue == "Until I unmute it" || newValue == "Never"){
                        if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_MUTED)){
                                Toasty.info(requireActivity(), "App will not unmute automatically," +
                                        " unmute from notification bar!").show()
                            WorkManager.getInstance(requireActivity()).cancelAllWorkByTag("MUTE")
                        }else{
                            if(newValue == "Never"){
                                Toasty.info(requireActivity(), "App will never mute after alarm!").show()
                            }else{
                                Toasty.info(requireActivity(), "App will not unmute until you do it!").show()
                            }
                            WorkManager.getInstance(requireActivity()).cancelAllWorkByTag("MUTE")
                        }
                    }else{
                      //  val time = SharedPrefUtils.readString(Constants.PreferenceKeys.MUTE_TIME)
                          if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_MUTED)){
                              Toasty.info(requireActivity(), String.format("App will unmute after %s", newValue.toString())).show()
                              WorkManagerUtils.scheduleWorkWithTime(newValue.toString(), requireActivity())
                          }else{
                              Toasty.info(requireActivity(), String.format("Mute time changed to %s", newValue.toString())).show()
                              WorkManager.getInstance(requireActivity()).cancelAllWorkByTag("MUTE")
                          }
                    }
                    return true
                }
            }
        }
    }
}