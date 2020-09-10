package com.app.messagealarm.ui.main.add_apps

import android.app.SearchManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.ui.adapters.AllAppsListAdapter
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_add_application.*



class AddApplicationActivity : AppCompatActivity(), AddApplicationView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_application)
        //setup toolbar
        toolBarSetup()
        //setup presenter
        val addApplicationPresenter = AddApplicationPresenter(this, this)
        addApplicationPresenter.getAllApplicationList()

    }

    private fun initAllAppsRecyclerView(list:ArrayList<InstalledApps>) {
        rv_apps_list?.layoutManager = LinearLayoutManager(this)
        rv_apps_list?.setHasFixedSize(true)
        rv_apps_list?.adapter = AllAppsListAdapter(list)
    }

    override fun onAllApplicationGetSuccess(list: ArrayList<InstalledApps>) {
       runOnUiThread {
                progress_bar_add_app?.visibility = View.GONE
                rv_apps_list?.visibility = View.VISIBLE
                initAllAppsRecyclerView(list)
       }
    }

    private fun toolBarSetup(){
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
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener,
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
}
