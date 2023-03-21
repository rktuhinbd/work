package com.app.messagealarm.ui.adapters


import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.ViewUtils
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.activity_buy_pro_new.*
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
                        mExpandedPosition = if (isExpanded) -1 else position
                        notifyDataSetChanged()
                    }

                    itemView.card_alarm?.setOnClickListener {
                        showHideCardView( listOf(itemView.txt_alarm, itemView.txt_speak, itemView.txt_custom),
                            itemView.card_alarm, itemView.card_speak, itemView.card_custom)
                        selectedNotifyOption = Constants.NotifyOptions.ALARM
                        itemView.btn_confirm_app_option?.text = "Configure Alarm"
                    }

                    itemView.card_custom?.setOnClickListener {
                        showHideCardView(listOf(itemView.txt_custom, itemView.txt_speak, itemView.txt_alarm),
                            itemView.card_custom, itemView.card_speak, itemView.card_alarm)
                        selectedNotifyOption = Constants.NotifyOptions.CUSTOM
                        itemView.btn_confirm_app_option?.text = "Setup Custom Actions"
                    }

                    itemView.card_speak?.setOnClickListener {
                        showHideCardView(listOf(itemView.txt_speak, itemView.txt_alarm, itemView.txt_custom),
                            itemView.card_speak, itemView.card_alarm, itemView.card_custom)
                        selectedNotifyOption = Constants.NotifyOptions.SPEAK
                        itemView.btn_confirm_app_option?.text = "Edit Speaking Options"
                    }

                    itemView.btn_confirm_app_option?.setOnClickListener {
                        when(selectedNotifyOption){
                            Constants.NotifyOptions.ALARM -> {
                                mItemClickListener.onItemClick(appsList[adapterPosition])
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
       private fun showHideCardView(texts: List<TextView>, vararg cardAlarms: MaterialCardView){
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