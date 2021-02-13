package com.app.messagealarm.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import kotlinx.android.synthetic.main.item_all_apps.view.*
import java.lang.IndexOutOfBoundsException
import java.util.*
import kotlin.collections.ArrayList


class AllAppsListAdapter (private var appsList: ArrayList<InstalledApps>,
                          val mItemClickListener: ItemClickListener
):
    RecyclerView.Adapter<AllAppsListAdapter.AllAppsViewHolder>(){

    private var itemsCopy: ArrayList<InstalledApps> = ArrayList()


    interface ItemClickListener{
        fun onItemClick(app: InstalledApps)
        fun onLongClick(app: InstalledApps)
    }

    init {
       itemsCopy.addAll(appsList)
   }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllAppsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_all_apps, parent, false)
        return AllAppsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return appsList.size
    }

    public fun updateData(list:ArrayList<InstalledApps>){
        appsList.clear()
        appsList = list
        itemsCopy.clear()
        itemsCopy.addAll(appsList)
        notifyDataSetChanged()
    }

    public fun cleanList(){
        appsList.clear()
        itemsCopy.clear()
    }

    fun filter(text: String) {
        var text = text
        appsList.clear()
        if (text.isEmpty()) {
            appsList.addAll(itemsCopy)
        } else {
            text = text.toLowerCase(Locale.getDefault())
            for (item in itemsCopy) {
                if (item.appName.toLowerCase(Locale.getDefault()).contains(text) || item.appName.toLowerCase(
                        Locale.getDefault()).contains(text)) {
                    appsList.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    public fun adapterSize() : Int{
        return appsList.size
    }


    override fun onBindViewHolder(holder: AllAppsViewHolder, position: Int) {
        holder.bindItems(appsList[position])
    }

   inner class AllAppsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bindItems(installedApps: InstalledApps){
                try{
                    itemView.tv_app_name?.text = installedApps.appName
                    itemView.iv_app_icon?.setImageDrawable(installedApps.drawableIcon)
                    itemView.card_view_all_app.setOnClickListener {
                        mItemClickListener.onItemClick(appsList[adapterPosition])
                    }
                }catch (e:IndexOutOfBoundsException){
                   //skip the crash
                }
            }
    }

}