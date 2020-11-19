package com.app.messagealarm.ui.main.add_apps

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.ui.adapters.AllAppsListAdapter
import com.app.messagealarm.ui.main.add_options.AddApplicationOption
import com.app.messagealarm.utils.*
import com.google.android.material.appbar.MaterialToolbar
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_add_application.*
import kotlinx.android.synthetic.main.dialog_add_app_options.*
import java.io.File
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.lang.NullPointerException
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


class AddApplicationActivity : AppCompatActivity(), AddApplicationView,
    AllAppsListAdapter.ItemClickListener {

    var addApplicationPresenter:AddApplicationPresenter? = null
    val bottomSheetModel = AddApplicationOption()
    val REQUEST_CODE_PICK_AUDIO = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_application)
        setupSpinner()
        //setup toolbar
        toolBarSetup()
        //hide spinner
        spinner_filter?.visibility = View.INVISIBLE
        spinner_drop_down?.visibility = View.INVISIBLE
        //setup presenter
        addApplicationPresenter = AddApplicationPresenter(this, this)
        filterListener()
        darkModePre()
    }


    private fun darkModePre(){
        if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)){
            spinner_drop_down?.setImageResource(R.drawable.ic_arrow_drop_down_white)
        }else{
            spinner_drop_down?.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp)
        }
    }



    private fun initAllAppsRecyclerView(list: ArrayList<InstalledApps>) {
        rv_apps_list?.layoutManager = LinearLayoutManager(this)
        rv_apps_list?.setHasFixedSize(true)
        rv_apps_list?.adapter = AllAppsListAdapter(list, this)
    }

    override fun onAllApplicationGetSuccess(list: ArrayList<InstalledApps>) {
        try{
            Collections.sort(list,
                Comparator<InstalledApps> { lhs, rhs -> lhs.appName.compareTo(rhs.appName) })
            runOnUiThread {
                progress_bar_add_app?.visibility = View.GONE
                rv_apps_list?.visibility = View.VISIBLE
                initAllAppsRecyclerView(list)
                spinner_filter?.visibility = View.VISIBLE
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
                if(p2 == 0){
                    rv_apps_list?.visibility = View.GONE
                    progress_bar_add_app?.visibility = View.VISIBLE
                    addApplicationPresenter!!.getAllApplicationList()
                }else{
                    rv_apps_list?.visibility = View.GONE
                    progress_bar_add_app?.visibility = View.VISIBLE
                    addApplicationPresenter!!.filterByMessaging()
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

    override fun onApplicationFiltered(list: ArrayList<InstalledApps>) {
        try{
            Collections.sort(list,
                Comparator<InstalledApps> { lhs, rhs -> lhs.appName.compareTo(rhs.appName) })
            runOnUiThread {
                (rv_apps_list?.adapter as AllAppsListAdapter).updateData(list)
                progress_bar_add_app?.visibility = View.GONE
                rv_apps_list?.visibility = View.VISIBLE
            }
        }catch (e:NullPointerException){
            e.printStackTrace()
        }catch (e:TypeCastException){
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        menuInflater.inflate(R.menu.menu_add_app, menu)
        val searchItem: MenuItem? = menu?.findItem(R.id.mnu_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView: SearchView? = searchItem?.actionView as SearchView
        // Assumes current activity is the searchable activity
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        setSearchViewEditTextBackgroundColor(this, searchView!!)
        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener,
            SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                try{
                    (rv_apps_list?.adapter as AllAppsListAdapter).filter(query!!)
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
                }catch (e:TypeCastException){
                    e.printStackTrace()
                }catch (e:NullPointerException){
                    e.printStackTrace()
                }

                return true
            }
        })
        if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)){
            val clearButton = searchView.context.resources.getIdentifier("android:id/search_close_btn",null,null)
            val buttonId = searchView.context.resources.getIdentifier("android:id/search_button",null, null)
            val id = searchView.context.resources.getIdentifier("android:id/search_src_text", null, null);
            val textView = searchView.findViewById<TextView>(id)
            val imageView = searchView.findViewById<ImageView>(buttonId)
            val clearImage = searchView.findViewById<ImageView>(clearButton)
            clearImage.setColorFilter(Color.WHITE)
            imageView.setColorFilter(Color.WHITE)
            textView.setTextColor(Color.WHITE)
            textView.hint = getString(R.string.txt_search_app)
            textView.setHintTextColor(Color.GRAY)
        }
        return super.onCreateOptionsMenu(menu)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (REQUEST_CODE_PICK_AUDIO == requestCode) {
            if (resultCode == Activity.RESULT_OK && data!!.data != null) {
                val fileName = File(PathUtils.getPath(this, data.data!!)!!).name
                try {
                    if(MediaUtils.getDurationOfMediaFle(PathUtils.getPath(this, data.data!!)!!) >= 30){
                        bottomSheetModel.txt_ringtone_value?.text = fileName
                        bottomSheetModel.setToneName(fileName)
                        bottomSheetModel.alarmTonePath = PathUtils.getPath(this, data.data!!)!!
                    }else{
                        bottomSheetModel.txt_ringtone_value?.text = "Default"
                        bottomSheetModel.setToneName("Default")
                        bottomSheetModel.alarmTonePath = null
                        DialogUtils.showSimpleDialog(this, getString(R.string.txt_wrong_duration),
                            getString(R.string.txt_selected_music_duration))
                    }
                }catch (e:IllegalArgumentException){
                    bottomSheetModel.txt_ringtone_value?.text = "Default"
                    bottomSheetModel.setToneName("Default")
                    bottomSheetModel.alarmTonePath = null
                    DialogUtils.showSimpleDialog(this, getString(R.string.txt_music),
                        getString(R.string.txt_try_again))
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
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

    override fun onItemClick(app: InstalledApps) {
        if (!bottomSheetModel.isAdded) {
            val bundle = Bundle()
            bundle.putBoolean(Constants.BundleKeys.IS_EDIT_MODE, false)
            bundle.putSerializable(Constants.BundleKeys.APP, app as Serializable)
            bottomSheetModel.arguments = bundle
            bottomSheetModel.isCancelable = false
            bottomSheetModel.show(supportFragmentManager, "OPTIONS")
        }
    }

    override fun onLongClick(app: InstalledApps) {
        Toasty.info(this, app.appName).show()
    }


}
