package com.app.messagealarm.ui.main.add_apps

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Telephony
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.ui.adapters.AllAppsListAdapter
import com.app.messagealarm.ui.main.configure_options.add_options_alarm.AlarmOptionDialog
import com.app.messagealarm.utils.*
import com.google.android.material.appbar.MaterialToolbar
import com.greentoad.turtlebody.mediapicker.MediaPicker
import com.greentoad.turtlebody.mediapicker.core.MediaPickerConfig
import es.dmoral.toasty.Toasty
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_add_application.*
import kotlinx.android.synthetic.main.dialog_add_app_options.*
import net.frakbot.jumpingbeans.JumpingBeans
import xyz.aprildown.ultimateringtonepicker.RingtonePickerActivity
import java.io.File
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.lang.IndexOutOfBoundsException
import java.lang.NullPointerException
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


class AddApplicationActivity : AppCompatActivity(), AddApplicationView,
    AllAppsListAdapter.ItemClickListener {

    var textSync = "Searching apps"
    var addApplicationPresenter:AddApplicationPresenter? = null
    val bottomSheetModel = AlarmOptionDialog()
    val REQUEST_CODE_PICK_AUDIO = 1
    var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        changeTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_application)
        setupSpinner()
        //setup toolbar
        toolBarSetup()
        //hide spinner
        card_view_filter?.visibility = View.INVISIBLE
        txt_filter_by?.visibility = View.INVISIBLE
        spinner_filter?.visibility = View.INVISIBLE
        spinner_drop_down?.visibility = View.INVISIBLE
        JumpingBeans.with(animated_dots)
            .appendJumpingDots()
            .build()
        //setup presenter
        addApplicationPresenter = AddApplicationPresenter(this, this)
        filterListener()
        darkModePre()
        setListener()
        handleRefund()

    }

    private fun handleRefund(){
        //Check if user has opted for any refund
        //if yes, then disable PRO feature option for that user
        //if no, keep the access

    }

    private fun deleteList(){
        (rv_apps_list?.adapter as AllAppsListAdapter).cleanList()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(rv_apps_list != null){
            if(rv_apps_list?.adapter != null){
                deleteList()
            }
        }

    }

    private fun darkModePre(){
        if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)){
            spinner_drop_down?.setImageResource(R.drawable.ic_arrow_drop_down_white)
            search_not_found?.setImageResource(R.drawable.ic_search_no_found_dark)
        }else{
            spinner_drop_down?.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp)
            search_not_found?.setImageResource(R.drawable.ic_search_no_found_white)
        }
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

    private fun setListener(){
        btn_sync_now?.setOnClickListener {
            if(AndroidUtils.isOnline(this)){
                textSync = "Syncing data"
                animated_dots.text = textSync
                JumpingBeans.with(animated_dots)
                    .appendJumpingDots()
                    .build()
                Toasty.success(this, "Sync started, please hold on!").show()
            }
            hideNotSyncedSuccess()
            progress_bar_add_app?.visibility = View.VISIBLE
            animated_dots?.visibility = View.VISIBLE
            addApplicationPresenter?.sync()
        }
    }



    private fun initAllAppsRecyclerView(list: ArrayList<InstalledApps>) {
        rv_apps_list?.layoutManager = LinearLayoutManager(this)
        rv_apps_list?.setHasFixedSize(true)
        rv_apps_list?.adapter = AllAppsListAdapter(list, this)
    }

    override fun onAllApplicationGetSuccess(list: ArrayList<InstalledApps>) {
        try{
            list.sortWith { lhs, rhs -> lhs.appName.compareTo(rhs.appName) }
            runOnUiThread {
                progress_bar_add_app?.visibility = View.GONE
                animated_dots?.visibility = View.GONE
                rv_apps_list?.visibility = View.VISIBLE
                initAllAppsRecyclerView(list)
                spinner_filter?.visibility = View.VISIBLE
                txt_filter_by?.visibility = View.VISIBLE
                card_view_filter?.visibility = View.VISIBLE
                spinner_drop_down?.visibility = View.VISIBLE
            }

        }catch (e:TypeCastException){
            e.printStackTrace()
        }catch (e:NullPointerException){
            e.printStackTrace()
        }
    }


    @SuppressLint("ResourceType")
    private fun setupSpinner(){
        val spinnerList = ArrayList<String>()
        spinnerList.add("All Apps")
        spinnerList.add("Messaging")
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, R.layout.spinner_item, spinnerList)
        spinner_filter?.adapter = adapter
    }

    private fun filterListener() {
        spinner_filter?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                try{
                    if(p2 == 0){
                        hideNotSyncedSuccess()
                        //clear search view
                        searchView?.setQuery("", false)
                        searchView?.isIconified = true
                        rv_apps_list?.visibility = View.GONE
                        progress_bar_add_app?.visibility = View.VISIBLE
                        animated_dots?.visibility = View.VISIBLE
                        addApplicationPresenter!!.getAllApplicationList()
                    }else{
                        //clear search view
                        spinner_filter?.isEnabled = false
                        spinner_filter?.isClickable = false
                        searchView?.setQuery("", false)
                        searchView?.isIconified = true
                        rv_apps_list?.visibility = View.GONE
                        progress_bar_add_app?.visibility = View.VISIBLE
                        animated_dots?.visibility = View.VISIBLE
                        addApplicationPresenter!!.filterByMessaging()
                    }
                }catch (e:NullPointerException){
                    //skip the crash
                }
            }
        }
    }

    private fun toolBarSetup() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.txt_add_app)
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



    override fun onAllApplicationGetError(message: String) {
        runOnUiThread {
            Toasty.info(this, message).show()
        }
    }

    override fun onApplicationFiltered(list: List<InstalledApps>) {
        val mainList = ArrayList<InstalledApps>()
        mainList.addAll(list)
        val holderList = ArrayList<InstalledApps>()
        holderList.addAll(list)
        //there should be a logic that if internet is off then show handle sync not success
        //or if loading is happening for more than 6 sec, then show sync not
        runOnUiThread {
            textSync = "Searching apps"
            animated_dots.text = textSync
            JumpingBeans.with(animated_dots)
                .appendJumpingDots()
                .build()
            val countDownTimer = object  : CountDownTimer(5000, 1000){
                override fun onTick(millisUntilFinished: Long) {
                    //skipping the milliseconds as not needed
                }
                override fun onFinish() {
                    if(holderList.isEmpty()){
                        handleSyncedNotSuccess()
                    }
                }
            }
            countDownTimer.start()
        }
        try{
            mainList.sortWith(Comparator { lhs, rhs -> lhs.appName.compareTo(rhs.appName) })
                runOnUiThread {
                    (rv_apps_list?.adapter as AllAppsListAdapter).updateData(mainList)
                    if(holderList.size == 1 &&
                            holderList[0].packageName ==
                            Telephony.Sms.getDefaultSmsPackage(this@AddApplicationActivity)){
                            handleSyncedNotSuccess()
                        }else if(holderList.size > 1){
                        progress_bar_add_app?.visibility = View.GONE
                        animated_dots?.visibility = View.GONE
                        rv_apps_list?.visibility = View.VISIBLE
                    }
                    spinner_filter?.isEnabled = true
                    spinner_filter?.isClickable = true
                }
        }catch (e:NullPointerException){
            e.printStackTrace()
        }catch (e:TypeCastException){
            e.printStackTrace()
        }
    }

    override fun onSyncFailed(message: String) {
        runOnUiThread {
            textSync = "Searching apps"
            animated_dots.text = textSync
            JumpingBeans.with(animated_dots)
                .appendJumpingDots()
                .build()
            progress_bar_add_app?.visibility = View.GONE
            animated_dots?.visibility = View.GONE
            Toasty.error(this, message).show()
        }
        handleSyncedNotSuccess()
    }

    override fun onResume() {
        super.onResume()
        //clear search view
        searchView?.setQuery("", false)
        searchView?.isIconified = true
    }

    private fun hideNotSyncedSuccess(){
        runOnUiThread {
            gif_no_internet?.visibility = View.GONE
            txt_no_internet?.visibility = View.GONE
            btn_sync_now?.visibility = View.GONE
        }
    }

    private fun showSearchNotFound(){
        search_not_found?.visibility = View.VISIBLE
        txt_search_no_found?.visibility = View.VISIBLE
    }

    private fun hideSearchNotFound(){
        search_not_found?.visibility = View.GONE
        txt_search_no_found?.visibility = View.GONE
    }

    private fun handleSyncedNotSuccess(){
        runOnUiThread {
            rv_apps_list?.visibility = View.GONE
            progress_bar_add_app?.visibility = View.GONE
            animated_dots?.visibility = View.GONE
            gif_no_internet?.visibility = View.VISIBLE
            txt_no_internet?.visibility = View.VISIBLE
            btn_sync_now?.visibility = View.VISIBLE
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        menuInflater.inflate(R.menu.menu_add_app, menu)
        val searchItem: MenuItem? = menu?.findItem(R.id.mnu_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
         searchView = searchItem?.actionView as SearchView
        // Assumes current activity is the searchable activity
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        setSearchViewEditTextBackgroundColor(this, searchView!!)
        searchView?.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener,
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                try{
                    (rv_apps_list?.adapter as AllAppsListAdapter).filter(query!!)
                    if((rv_apps_list?.adapter as AllAppsListAdapter).adapterSize() == 0 &&
                        !gif_no_internet?.isVisibile()!! &&
                        !progress_bar_add_app?.isVisibile()!!){
                        showSearchNotFound()
                    }else{
                        hideSearchNotFound()
                    }
                }catch (e:TypeCastException){
                    e.printStackTrace()
                }catch (e:NullPointerException){
                    e.printStackTrace()
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                try{
                    (rv_apps_list?.adapter as AllAppsListAdapter).filter(newText!!)
                    if((rv_apps_list?.adapter as AllAppsListAdapter).adapterSize() == 0 &&
                        !gif_no_internet?.isVisibile()!! &&
                            !progress_bar_add_app?.isVisibile()!!){
                        showSearchNotFound()
                    }else{
                        hideSearchNotFound()
                    }
                }catch (e:TypeCastException){
                    e.printStackTrace()
                }catch (e:NullPointerException){
                    e.printStackTrace()
                }
                return true
            }
        })
        if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)){
            val clearButton = searchView?.context?.resources?.getIdentifier("android:id/search_close_btn",null,null)
            val buttonId = searchView?.context?.resources?.getIdentifier("android:id/search_button",null, null)
            val id = searchView?.context?.resources?.getIdentifier("android:id/search_src_text", null, null);
            val textView = searchView?.findViewById<TextView>(id!!)
            val imageView = searchView?.findViewById<ImageView>(buttonId!!)
            val clearImage = searchView?.findViewById<ImageView>(clearButton!!)
            clearImage?.setColorFilter(Color.WHITE)
            imageView?.setColorFilter(Color.WHITE)
            textView?.setTextColor(Color.WHITE)
            textView?.hint = getString(R.string.txt_search_app)
            textView?.setHintTextColor(Color.GRAY)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (REQUEST_CODE_PICK_AUDIO == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val alarmTone = RingtonePickerActivity.getPickerResult(data!!)
                    val fileName = File(PathUtils.getPath(this, alarmTone[0].uri)!!).name

                    /***
                     * Remove the 30 second and more audio restriction
                     * @author Mortuza Hossain
                     * To go back to previous code uncomment the if..else condition
                     */

//                    if(MediaUtils.getDurationOfMediaFle(PathUtils.getPath(this, alarmTone[0].uri)!!) >= 30){
                        bottomSheetModel.txt_ringtone_value?.text = fileName
                        bottomSheetModel.setToneName(fileName)
                        bottomSheetModel.alarmTonePath = PathUtils.getPath(this, alarmTone[0].uri)!!
//                    }else{
//                        bottomSheetModel.txt_ringtone_value?.text = "Default"
//                        bottomSheetModel.setToneName("Default")
//                        bottomSheetModel.alarmTonePath = null
//                        DialogUtils.showSimpleDialog(this, getString(R.string.txt_wrong_duration),
//                            getString(R.string.txt_selected_music_duration))
//                    }
                }catch (e:IllegalArgumentException){
                    bottomSheetModel.txt_ringtone_value?.text = "Default"
                    bottomSheetModel.setToneName("Default")
                    bottomSheetModel.alarmTonePath = null
                    DialogUtils.showSimpleDialog(this, getString(R.string.txt_music),
                        getString(R.string.txt_try_again))
                }catch (e:IndexOutOfBoundsException){
                    bottomSheetModel.txt_ringtone_value?.text = "Default"
                    bottomSheetModel.setToneName("Default")
                    bottomSheetModel.alarmTonePath = null
                    bottomSheetModel.askForPermission()
                }
            }
        }else if(requestCode == Constants.ACTION.ACTION_PURCHASE_FROM_ADD){
            //purchased
            if(isPurchased()){
                Toasty.success(this, "Thanks for purchase! You are now pro user!").show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    @SuppressLint("CheckResult")
    fun pickAudioFromStorage(){
        val pickerConfig = MediaPickerConfig()
            .setAllowMultiSelection(false)
            .setUriPermanentAccess(false)
            .setShowConfirmationDialog(true)
            .setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        MediaPicker.with(this, MediaPicker.MediaTypes.AUDIO)
            .setConfig(pickerConfig)
            .setFileMissingListener(object : MediaPicker.MediaPickerImpl.OnMediaListener{
                override fun onMissingFileWarning() {
                    bottomSheetModel.txt_ringtone_value?.text = "Default"
                    bottomSheetModel.setToneName("Default")
                    bottomSheetModel.alarmTonePath = null
                    DialogUtils.showSimpleDialog(this@AddApplicationActivity, getString(R.string.missing_file),
                        getString(R.string.txt_try_again))
                }
            })
            .onResult()
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val fileName = File(PathUtils.getPath(this, it[0])!!).name
                if(MediaUtils.getDurationOfMediaFle(PathUtils.getPath(this, it[0])!!) >= 30){
                    bottomSheetModel.txt_ringtone_value?.text = fileName
                    bottomSheetModel.setToneName(fileName)
                    bottomSheetModel.alarmTonePath = PathUtils.getPath(this, it[0])!!
                }else{
                    bottomSheetModel.txt_ringtone_value?.text = "Default"
                    bottomSheetModel.setToneName("Default")
                    bottomSheetModel.alarmTonePath = null
                    DialogUtils.showSimpleDialog(this, getString(R.string.txt_wrong_duration),
                        getString(R.string.txt_selected_music_duration))
                }
            },{
                bottomSheetModel.txt_ringtone_value?.text = "Default"
                bottomSheetModel.setToneName("Default")
                bottomSheetModel.alarmTonePath = null
                DialogUtils.showSimpleDialog(this, it.message!!,
                    getString(R.string.txt_try_again))
            },{

            },{

            })
    }

   private fun isPurchased() : Boolean{
       return SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)
   }

    private fun setSearchViewEditTextBackgroundColor(
        context: Context,
        searchView: SearchView
    ) {
        val searchPlateId =
            context.resources.getIdentifier("android:id/search_plate", null, null)
        val viewGroup =
            searchView.findViewById<View>(searchPlateId) as ViewGroup
        viewGroup.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onItemClick(app: InstalledApps, type:String) {
        when(type){
            Constants.NotifyOptions.ALARM -> {
                if (!bottomSheetModel.isAdded) {
                    val bundle = Bundle()
                    bundle.putBoolean(Constants.BundleKeys.IS_EDIT_MODE, false)
                    bundle.putSerializable(Constants.BundleKeys.APP, app as Serializable)
                    bottomSheetModel.arguments = bundle
                    bottomSheetModel.isCancelable = false
                    bottomSheetModel.show(supportFragmentManager, "OPTIONS")
                }
            }
            Constants.NotifyOptions.SPEAK -> {

            }
            Constants.NotifyOptions.CUSTOM -> {

            }
        }

    }

    override fun onLongClick(app: InstalledApps) {
        Toasty.info(this, app.appName).show()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var isGranted = true
        if (requestCode == PermissionUtils.REQUEST_CODE_PERMISSION_DEFAULT) {
            for (element in grantResults) {
                if (element != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false
                }
            }
            if (isGranted) {
                bottomSheetModel.pickAudioFromStorage()
            }
        }
    }

}
