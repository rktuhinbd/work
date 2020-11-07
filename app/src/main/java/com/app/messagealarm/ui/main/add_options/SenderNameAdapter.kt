package com.app.messagealarm.ui.main.add_options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import kotlinx.android.synthetic.main.item_all_apps.view.*
import kotlinx.android.synthetic.main.item_sender_name.view.*

class SenderNameAdapter : RecyclerView.Adapter<SenderNameAdapter.SenderNameHolder>(){

    val nameList = ArrayList<String>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SenderNameHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sender_name, parent, false)
        return SenderNameHolder(view)
    }

    override fun onBindViewHolder(holder: SenderNameHolder, position: Int) {
        holder.bindItems(nameList[position])
    }


    fun addName(name:String){
        nameList.add(name)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
       return nameList.size
    }

    inner class SenderNameHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.btn_close.setOnClickListener(this)
        }

        fun bindItems(name: String){
            itemView.txt_name.text = name
        }

        override fun onClick(v: View?) {
            nameList.removeAt(adapterPosition)
            notifyDataSetChanged()
        }
    }

}