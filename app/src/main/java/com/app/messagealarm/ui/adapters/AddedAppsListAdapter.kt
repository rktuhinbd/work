package com.app.messagealarm.ui.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.messagealarm.R
import com.app.messagealarm.model.entity.ApplicationEntity
import kotlinx.android.synthetic.main.item_added_applications.view.*
import java.io.File

class AddedAppsListAdapter(private val appsList:ArrayList<ApplicationEntity>) :
    RecyclerView.Adapter<AddedAppsListAdapter.AddedAppsViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddedAppsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_added_applications, parent, false)
        return AddedAppsViewHolder(view)
    }

    override fun getItemCount(): Int {
       return appsList.size
    }

    fun deleteItem(position: Int){
        appsList.removeAt(position)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: AddedAppsViewHolder, position: Int) {
       holder.bindItems(appsList[position])
    }

    inner class AddedAppsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.setOnClickListener { this }
        }

        fun bindItems(app:ApplicationEntity){
            itemView.tv_app_name?.text = app.appName
            itemView.iv_app_icon?.setImageBitmap(
                BitmapFactory.decodeFile(
                    File(app.bitmapPath)
                        .absolutePath
                ))
            itemView.switch_app_status?.isChecked = app.isRunningStatus
        }

        override fun onClick(v: View?) {

        }

    }

}