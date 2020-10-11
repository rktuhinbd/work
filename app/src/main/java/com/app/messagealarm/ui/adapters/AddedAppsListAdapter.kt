package com.app.messagealarm.ui.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.model.entity.ApplicationEntity
import kotlinx.android.synthetic.main.dialog_add_app_options.*
import kotlinx.android.synthetic.main.item_added_applications.view.*
import java.io.File

class AddedAppsListAdapter(
    private val appsList: ArrayList<ApplicationEntity>,
    val mItemClickListener: ItemClickListener
) :
    RecyclerView.Adapter<AddedAppsListAdapter.AddedAppsViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddedAppsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_added_applications, parent, false)
        return AddedAppsViewHolder(view)
    }

    interface ItemClickListener {
        fun onItemClick(app: ApplicationEntity)
        fun onLongClick(app: ApplicationEntity)
        fun onApplicationSwitch(boolean: Boolean, id:Int)
    }

    override fun getItemCount(): Int {
        return appsList.size
    }

    fun addItems(list:ArrayList<ApplicationEntity>){
        appsList.clear()
        appsList.addAll(list)
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        appsList.removeAt(position)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): ApplicationEntity {
        return appsList[position]
    }

    override fun onBindViewHolder(holder: AddedAppsViewHolder, position: Int) {
        holder.bindItems(appsList[position])
    }

    inner class AddedAppsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        fun bindItems(app: ApplicationEntity) {
            itemView.tv_app_name?.text = app.appName
            itemView.switch_app_status?.isChecked = app.isRunningStatus
            itemView.switch_app_status?.setOnCheckedChangeListener { buttonView, isChecked ->
               mItemClickListener.onApplicationSwitch(isChecked, app.id)
            }
            itemView.iv_app_icon?.setImageBitmap(
                BitmapFactory.decodeFile(
                    File(app.bitmapPath)
                        .absolutePath
                )
            )


        }

        override fun onClick(v: View?) {
            mItemClickListener.onItemClick(appsList[adapterPosition])
        }

    }

}