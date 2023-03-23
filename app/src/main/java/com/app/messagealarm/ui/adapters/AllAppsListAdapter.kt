package com.app.messagealarm.ui.adapters


import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.ViewUtils
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.item_all_apps.view.*
import java.util.*


/**
 * @author Mujahid Khan
 */
class AllAppsListAdapter (private var appsList: ArrayList<InstalledApps>,
                          val mItemClickListener: ItemClickListener
):
    RecyclerView.Adapter<AllAppsListAdapter.AllAppsViewHolder>(){

    private var itemsCopy: ArrayList<InstalledApps> = ArrayList()
    private var mExpandedPosition = -1
    private var selectedNotifyOption = Constants.NotifyOptions.ALARM

    interface ItemClickListener{
        fun onItemClick(app: InstalledApps, type:String)
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

    fun updateData(list:ArrayList<InstalledApps>){
        //On any update disable any expanded view
        mExpandedPosition = -1
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
        //disable expanded view in search option
        mExpandedPosition = -1
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
                    val rotate = RotateAnimation(
                        270f,
                        360f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f
                    )
                    rotate.duration = 400
                    rotate.interpolator = LinearInterpolator()

                    val isExpanded = position == mExpandedPosition
                    itemView.tv_app_name?.text = installedApps.appName
                    itemView.iv_app_icon?.setImageDrawable(installedApps.drawableIcon)
                        if (isExpanded) {
                            itemView.layout_expand_section.animate().alpha(1.0f).duration = 150
                            itemView.indicator_item?.startAnimation(rotate)
                            itemView.layout_expand_section.visibility = View.VISIBLE
                            itemView.indicator_item?.rotation = 360F
                            itemView.dotted_condom.visibility = View.VISIBLE
                        } else{
                            itemView.layout_expand_section.animate().alpha(0.0f).duration = 150
                            itemView.indicator_item?.rotation = 270F
                            itemView.layout_expand_section.visibility = View.GONE
                            itemView.dotted_condom.visibility = View.GONE
                        }
                    itemView.base_part_of_item.setOnClickListener {
                         if (isExpanded){
                             mExpandedPosition =  -1
                             //If collapsed, reset the selected item to Alarm again
                            selectedNotifyOption = Constants.NotifyOptions.ALARM
                        } else{
                             mExpandedPosition =  position
                        }
                        notifyDataSetChanged()
                    }

                    itemView.card_alarm?.setOnClickListener {
                        setSelectedOption( listOf(itemView.txt_alarm, itemView.txt_speak, itemView.txt_custom),
                            itemView.card_alarm, itemView.card_speak, itemView.card_custom)
                        itemView.txt_option_notice.text = "Set an alarm to notify you when you receive an important message"
                        selectedNotifyOption = Constants.NotifyOptions.ALARM
                        itemView.btn_confirm_app_option?.text = "Configure Alarm Options"
                    }

                    itemView.card_custom?.setOnClickListener {
                        setSelectedOption(listOf(itemView.txt_custom, itemView.txt_speak, itemView.txt_alarm),
                            itemView.card_custom, itemView.card_speak, itemView.card_alarm)
                        selectedNotifyOption = Constants.NotifyOptions.CUSTOM
                        itemView.btn_confirm_app_option?.text = "Configure Custom Actions"
                        itemView.txt_option_notice.text = "Enhance functionality by," +
                                " triggering actions based on message content, integrating custom APIs," +
                                " providing a visual editor, and a marketplace for pre-made workflows."
                    }

                    itemView.card_speak?.setOnClickListener {
                        setSelectedOption(listOf(itemView.txt_speak, itemView.txt_alarm, itemView.txt_custom),
                            itemView.card_speak, itemView.card_alarm, itemView.card_custom)
                        selectedNotifyOption = Constants.NotifyOptions.SPEAK
                        itemView.txt_option_notice.text = "Have the app speak the contents of the message out loud when your headphones are connected"
                        itemView.btn_confirm_app_option?.text = "Configure Speaking Options"
                    }

                    itemView.btn_confirm_app_option?.setOnClickListener {
                        when(selectedNotifyOption){
                            Constants.NotifyOptions.ALARM -> {
                                mItemClickListener.onItemClick(appsList[adapterPosition], selectedNotifyOption)
                            }

                            Constants.NotifyOptions.SPEAK -> {

                            }

                            Constants.NotifyOptions.CUSTOM -> {

                            }
                        }
                    }

                }catch (e:IndexOutOfBoundsException){
                   //skip the crash
                }
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