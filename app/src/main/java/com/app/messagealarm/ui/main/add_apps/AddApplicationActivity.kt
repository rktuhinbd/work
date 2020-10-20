package com.app.messagealarm.ui.main.add_apps

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.ui.adapters.AllAppsListAdapter
import com.app.messagealarm.ui.main.add_options.AddApplicationOption
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.PathUtils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_add_application.*
import kotlinx.android.synthetic.main.dialog_add_app_options.*
import java.io.File
import java.io.Serializable


class AddApplicationActivity : AppCompatActivity(), AddApplicationView,
    AllAppsListAdapter.ItemClickListener {

    val bottomSheetModel = AddApplicationOption()
    val REQUEST_CODE_PICK_AUDIO = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_application)
        //setup toolbar
        toolBarSetup()
        //setup presenter
        val addApplicationPresenter = AddApplicationPresenter(this, this)
        addApplicationPresenter.getAllApplicationList()
        filterListener()
    }

    private fun initAllAppsRecyclerView(list: ArrayList<InstalledApps>) {
        rv_apps_list?.layoutManager = LinearLayoutManager(this)
        rv_apps_list?.setHasFixedSize(true)
        rv_apps_list?.adapter = AllAppsListAdapter(list, this)
    }

    override fun onAllApplicationGetSuccess(list: ArrayList<InstalledApps>) {
        runOnUiThread {
            progress_bar_add_app?.visibility = View.GONE
            rv_apps_list?.visibility = View.VISIBLE
            initAllAppsRecyclerView(list)
        }
    }

    private fun filterListener() {
        spinner_filter?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

            }
        }
    }

    private fun toolBarSetup() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = getString(R.string.txt_add_app)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onAllApplicationGetError(message: String) {
        runOnUiThread {
            Toasty.info(this, message).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        menuInflater.inflate(R.menu.menu_add_app, menu)
        val searchItem: MenuItem? = menu?.findItem(R.id.mnu_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView: SearchView? = searchItem?.actionView as SearchView
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        setSearchViewEditTextBackgroundColor(this, searchView!!)
        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener,
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                (rv_apps_list?.adapter as AllAppsListAdapter).filter(query!!)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                (rv_apps_list?.adapter as AllAppsListAdapter).filter(newText!!)
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (REQUEST_CODE_PICK_AUDIO == requestCode) {
            if (resultCode == Activity.RESULT_OK && data!!.data != null) {
                val fileName = File(PathUtils.getPath(this, data.data!!)!!).name
                bottomSheetModel.txt_ringtone_value?.text = fileName
                bottomSheetModel.setToneName(fileName)
                bottomSheetModel.alarmTonePath = PathUtils.getPath(this, data.data!!)!!
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
            bottomSheetModel.show(supportFragmentManager, "OPTIONS")
        }
    }

    override fun onLongClick(app: InstalledApps) {
        Toasty.info(this, app.appName).show()
    }


    override fun onBackPressed() {
        super.onBackPressed()
    }

}
