package com.app.messagealarm.ui.main.add_apps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
                initAllAppsRecyclerView(list)
       }
    }

    override fun onAllApplicationGetError(message: String) {
        runOnUiThread {
                Toasty.info(this, message).show()
        }
    }
}
