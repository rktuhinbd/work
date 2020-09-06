package com.app.messagealarm.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import kotlinx.android.synthetic.main.item_all_apps.view.*

class AllAppsListAdapter( private val appsList: ArrayList<InstalledApps>) :
    RecyclerView.Adapter<AllAppsListAdapter.AllAppsViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllAppsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_all_apps, parent, false)
        return AllAppsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return appsList.size
    }

    override fun onBindViewHolder(holder: AllAppsViewHolder, position: Int) {
        holder.bindItems(appsList[position])
    }

    class AllAppsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bindItems(installedApps: InstalledApps){
                itemView.tv_app_name?.text = installedApps.appName
                itemView.iv_app_icon?.setImageDrawable(installedApps.drawableIcon)
            }
    }

}