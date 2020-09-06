package com.app.messagealarm.ui.main.add_apps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toolbar
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
        supportActionBar?.title = getString(R.string.txt_add_app)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onAllApplicationGetError(message: String) {
        runOnUiThread {
                Toasty.info(this, message).show()
        }
    }
}
