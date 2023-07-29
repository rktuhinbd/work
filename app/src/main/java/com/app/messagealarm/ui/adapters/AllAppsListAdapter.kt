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
import kotlinx.android.synthetic.main.item_all_apps.view.base_part_of_item
import kotlinx.android.synthetic.main.item_all_apps.view.btn_confirm_app_option
import kotlinx.android.synthetic.main.item_all_apps.view.card_alarm
import kotlinx.android.synthetic.main.item_all_apps.view.card_speak
import kotlinx.android.synthetic.main.item_all_apps.view.dotted_condom
import kotlinx.android.synthetic.main.item_all_apps.view.img_info
import kotlinx.android.synthetic.main.item_all_apps.view.img_save_flag
import kotlinx.android.synthetic.main.item_all_apps.view.img_save_flag_speak
import kotlinx.android.synthetic.main.item_all_apps.view.indicator_item
import kotlinx.android.synthetic.main.item_all_apps.view.iv_app_icon
import kotlinx.android.synthetic.main.item_all_apps.view.layout_expand_section
import kotlinx.android.synthetic.main.item_all_apps.view.tv_app_name
import kotlinx.android.synthetic.main.item_all_apps.view.txt_option_notice
import kotlinx.android.synthetic.main.item_all_apps.view.txt_option_three_notice
import java.util.Locale


/**
 * @author Mujahid Khan
 */
class AllAppsListAdapter(
    private var appsList: ArrayList<InstalledApps>,
    val mItemClickListener: ItemClickListener
) :
    RecyclerView.Adapter<AllAppsListAdapter.AllAppsViewHolder>() {

    private var itemsCopy: ArrayList<InstalledApps> = ArrayList()
    private var mExpandedPosition = -1
    private var selectedNotifyOption = Constants.NotifyOptions.ALARM

    interface ItemClickListener {
        fun onItemClick(app: InstalledApps, type: String)
        fun onLongClick(app: InstalledApps)
        fun onDataRequest(app: InstalledApps, position: Int)
    }


    init {
        itemsCopy.addAll(appsList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllAppsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_all_apps, parent, false)
        return AllAppsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return appsList.size
    }


    fun onRequestedDataReturn(app: InstalledApps, position: Int) {
        appsList[position].isAlarmConfigured = app.isAlarmConfigured
        appsList[position].isSpeakConfigured = app.isSpeakConfigured
    }

    fun updateData(list: ArrayList<InstalledApps>) {
        //On any update disable any expanded view
        mExpandedPosition = -1
        appsList.clear()
        appsList = list
        itemsCopy.clear()
        itemsCopy.addAll(appsList)
        notifyDataSetChanged()
    }

    public fun cleanList() {
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
                if (item.appName.toLowerCase(Locale.getDefault())
                        .contains(text) || item.appName.toLowerCase(
                        Locale.getDefault()
                    ).contains(text)
                ) {
                    appsList.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    public fun adapterSize(): Int {
        return appsList.size
    }


    override fun onBindViewHolder(holder: AllAppsViewHolder, position: Int) {
        holder.bindItems(appsList[position])
    }

    inner class AllAppsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(installedApps: InstalledApps) {
            try {
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
                    itemView.layout_expand_section.animate().alpha(1.0f).duration = 220
                    itemView.indicator_item?.startAnimation(rotate)
                    itemView.layout_expand_section.visibility = View.VISIBLE
                    itemView.indicator_item?.rotation = 360F
                    itemView.dotted_condom.visibility = View.VISIBLE
                } else {
                    itemView.layout_expand_section.animate().alpha(0.0f).duration = 220
                    itemView.indicator_item?.rotation = 270F
                    itemView.layout_expand_section.visibility = View.GONE
                    itemView.dotted_condom.visibility = View.GONE
                }

                if (installedApps.isAlarmConfigured) {
                    itemView.img_save_flag.visibility = View.VISIBLE
                    itemView.card_alarm.setStrokeColor(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                BaseApplication.getBaseApplicationContext(),
                                R.color.colorAccent
                            )
                        )
                    )
                    itemView.card_alarm.strokeWidth = ViewUtils.dpToPx(2).toInt()

                    itemView.btn_confirm_app_option?.text = "Edit Alarm Options"
                } else {
                    itemView.card_alarm.setStrokeColor(null)
                    itemView.card_alarm.strokeWidth = 0
                    itemView.img_save_flag.visibility = View.GONE
                }


                if (installedApps.isSpeakConfigured) {
                    itemView.card_speak.setStrokeColor(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                BaseApplication.getBaseApplicationContext(),
                                R.color.colorAccent
                            )
                        )
                    )
                    itemView.card_speak.strokeWidth = ViewUtils.dpToPx(2).toInt()
                    itemView.img_save_flag_speak.visibility = View.VISIBLE

                    itemView.btn_confirm_app_option?.text = "Edit Speak Options"
                } else {
                    itemView.card_speak.setStrokeColor(null)
                    itemView.card_speak.strokeWidth = 0
                    itemView.img_save_flag_speak.visibility = View.GONE
                }

                itemView.base_part_of_item.setOnClickListener {
                    if (isExpanded) {
                        mExpandedPosition = -1
                        //If collapsed, reset the selected item to Alarm again
                        selectedNotifyOption = Constants.NotifyOptions.ALARM
                    } else {
                        mExpandedPosition = position
                        //should show a loader
                        mItemClickListener.onDataRequest(appsList[position], position)
                    }
                    notifyDataSetChanged()
                }

                itemView.card_alarm?.setOnClickListener {
                    itemView.txt_option_notice.text =
                        "Set an alarm to notify you when you receive an important message"
                    selectedNotifyOption = Constants.NotifyOptions.ALARM
                    itemView.btn_confirm_app_option?.text =
                        if (installedApps.isAlarmConfigured) "Edit Alarm Options" else "Setup Alarm"
                    itemView.img_info?.visibility = View.GONE
                    itemView.txt_option_three_notice.visibility = View.GONE
                }

                itemView.card_speak?.setOnClickListener {
                    selectedNotifyOption = Constants.NotifyOptions.SPEAK
                    itemView.txt_option_notice.text =
                        "Have the app speak the contents of the message out loud"
                    itemView.btn_confirm_app_option?.text =
                        if (installedApps.isSpeakConfigured) "Edit Speaking Options" else "Setup Speaking Options"
                    itemView.img_info?.visibility = View.VISIBLE
                    itemView.txt_option_three_notice.visibility = View.VISIBLE
                }

                itemView.btn_confirm_app_option?.setOnClickListener {
                    mItemClickListener.onItemClick(appsList[adapterPosition], selectedNotifyOption)
                }

            } catch (e: IndexOutOfBoundsException) {
                //skip the crash
            }
        }

        /**
         * First one will be selected, others one will be unselected
         */
        private fun setSelectedOption(texts: List<TextView>, vararg cardAlarms: MaterialCardView) {
            for (i in cardAlarms.indices) {
                if (i == 0) {
                    cardAlarms[i].strokeWidth = ViewUtils.dpToPx(2).toInt()
                    cardAlarms[i].setStrokeColor(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                BaseApplication.getBaseApplicationContext(),
                                R.color.colorAccent
                            )
                        )
                    )
                    texts[i].setTextColor(
                        ContextCompat.getColor(
                            BaseApplication.getBaseApplicationContext(),
                            R.color.colorTextRegular
                        )
                    )
                    texts[i].setTypeface(null, Typeface.BOLD)
                } else {
                    cardAlarms[i].strokeWidth = 0
                    cardAlarms[i].setStrokeColor(null)
                    texts[i].setTextColor(
                        ContextCompat.getColor(
                            BaseApplication.getBaseApplicationContext(),
                            R.color.default_text
                        )
                    )
                    texts[i].setTypeface(null, Typeface.NORMAL)
                }
            }
        }
    }
}