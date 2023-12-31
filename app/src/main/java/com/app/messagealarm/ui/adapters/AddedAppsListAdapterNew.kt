package com.app.messagealarm.ui.adapters

import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.model.entity.ApplicationEntity
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.ViewUtils
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.activity_main.rv_application_list
import kotlinx.android.synthetic.main.dialog_alarm_options.*
import kotlinx.android.synthetic.main.item_added_application_new.view.*
import java.io.File

class AddedAppsListAdapterNew(
    private val appsList: ArrayList<ApplicationEntity>,
    val mItemClickListener: ItemClickListener
) :
    RecyclerView.Adapter<AddedAppsListAdapterNew.AddedAppsViewHolderNew>() {

    private var mExpandedPosition = -1
    private var selectedNotifyOption = Constants.NotifyOptions.ALARM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddedAppsViewHolderNew {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_added_application_new, parent, false)
        return AddedAppsViewHolderNew(view)
    }

    interface ItemClickListener {
        fun onItemClick(app: ApplicationEntity, selectedNotifyOption: String)
        fun onItemDeleteClick(app: ApplicationEntity, id: Int)
        fun onLongClick(app: ApplicationEntity)
        fun onApplicationSwitch(boolean: Boolean, id: Int)
    }

    override fun getItemCount(): Int {
        return appsList.size
    }

    fun addItems(list: ArrayList<ApplicationEntity>) {
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

    override fun onBindViewHolder(holder: AddedAppsViewHolderNew, position: Int) {
        holder.bindItems(appsList[position])
    }

    inner class AddedAppsViewHolderNew(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        fun bindItems(installedApps: ApplicationEntity) {
            try{
                val isExpanded = position == mExpandedPosition
                itemView.tv_app_name?.text = installedApps.appName
                if (isExpanded) {
                    itemView.layout_expand_section.animate().alpha(1.0f).duration = 220
                    itemView.layout_expand_section.visibility = View.VISIBLE
                    itemView.dotted_condom.visibility = View.VISIBLE
                } else{
                    itemView.layout_expand_section.animate().alpha(0.0f).duration = 220
                    itemView.layout_expand_section.visibility = View.GONE
                    itemView.dotted_condom.visibility = View.GONE
                }

                    if(installedApps.isAlarmEnabled == true){
                        itemView.img_save_flag.visibility = View.VISIBLE
                        itemView.btn_confirm_app_option?.text = "Edit Alarm"
                    }
                    if(installedApps.isSpeakEnabled == true){
                        itemView.img_save_flag_speak.visibility = View.VISIBLE
                        itemView.btn_confirm_app_option?.text = "Edit Speak"
                    }

                itemView.base_part_of_item.setOnClickListener {
                    if (isExpanded){
                        mExpandedPosition =  -1
                        //If collapsed, reset the selected item to Alarm again
                        selectedNotifyOption = Constants.NotifyOptions.ALARM
                    } else{
                        mExpandedPosition =  position
                        //should show a loader
                      //  mItemClickListener.onDataRequest(appsList[position],position)
                    }
                    notifyDataSetChanged()
                }

                itemView.card_alarm?.setOnClickListener {
                    itemView.txt_option_notice.text = "Set an alarm to notify you when you receive an important message"
                    selectedNotifyOption = Constants.NotifyOptions.ALARM
                    itemView.btn_confirm_app_option?.text = "Edit Alarm"
                    itemView.img_info?.visibility = View.GONE
                    itemView.txt_option_three_notice.visibility = View.GONE

                    setSelectedOption(listOf(itemView.txt_speak, itemView.txt_alarm), itemView.card_alarm, itemView.card_speak)
                }

                itemView.card_speak?.setOnClickListener {
                    selectedNotifyOption = Constants.NotifyOptions.SPEAK
                    itemView.txt_option_notice.text = "Have the app speak the contents of the message out loud"
                    itemView.btn_confirm_app_option?.text =  "Edit Speaking"
                    itemView.img_info?.visibility = View.VISIBLE
                    itemView.txt_option_three_notice.visibility = View.VISIBLE

                    setSelectedOption(listOf(itemView.txt_alarm, itemView.txt_speak), itemView.card_speak, itemView.card_alarm)
                }

                itemView.btn_confirm_app_option?.setOnClickListener {
                    mItemClickListener.onItemClick(appsList[adapterPosition],selectedNotifyOption )
                }

                itemView.btn_confirm_delete?.setOnClickListener {
                    mItemClickListener.onItemDeleteClick(appsList[adapterPosition],adapterPosition)
                }

            }catch (e:IndexOutOfBoundsException){
                //skip the crash
            }
            itemView.switch_app_status?.isChecked = installedApps.runningStatus == true
            itemView.switch_app_status?.setOnCheckedChangeListener { buttonView, isChecked ->
                installedApps.id?.let { mItemClickListener.onApplicationSwitch(isChecked, it) }
            }
            itemView.iv_app_icon?.setImageBitmap(
                BitmapFactory.decodeFile(
                    File(installedApps.bitmapPath)
                        .absolutePath
                )
            )
        }

        override fun onClick(v: View?) {

        }


        /**
         * First one will be selected, others one will be unselected
         */
        private fun setSelectedOption(texts: List<TextView>, vararg cardAlarms: MaterialCardView){
            for (i in cardAlarms.indices){
                if(i == 0){
                    cardAlarms[i].strokeWidth = ViewUtils.dpToPx(3).toInt()
                    cardAlarms[i].setStrokeColor(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                BaseApplication.getBaseApplicationContext(),
                                R.color.colorAccent
                            )
                        )
                    )
                    texts[i].setTextColor(ContextCompat.getColor(BaseApplication.getBaseApplicationContext(),R.color.colorTextRegular))
                    texts[i].setTypeface(null, Typeface.BOLD)
                }else{
                    cardAlarms[i].strokeWidth = 0
                    cardAlarms[i].setStrokeColor(null)
                    texts[i].setTextColor(ContextCompat.getColor(BaseApplication.getBaseApplicationContext(),R.color.default_text))
                    texts[i].setTypeface(null, Typeface.NORMAL)
                }
            }
        }

    }

}