package com.app.messagealarm.ui.buy_pro

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.messagealarm.R

/**
 * Created by Android Dev on 29-Aug-21 Aug, 2021
 */

@Suppress("UNREACHABLE_CODE")
class ReviewAdapter(val context: Context, val reviewDataList: List<MyReviewView>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val inflater = LayoutInflater.from(context)
        val itemView: View = inflater.inflate(R.layout.item_review_buy_pro, parent, false)
        return ReviewViewHolder(itemView);

    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {

        holder.circleImageView.setImageResource(reviewDataList[position].circleImage)
        holder.reviewUserName.text = reviewDataList[position].name
        holder.imageFlag.setImageResource(reviewDataList[position].imageFlag)
        holder.reviewContentText.text = reviewDataList[position].reviewContent

    }

    override fun getItemCount() = reviewDataList.size

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         val circleImageView: ImageView =
            itemView.findViewById(R.id.review_profile_image_view)
         val reviewUserName: TextView = itemView.findViewById(R.id.review_text_user_name)
         val imageFlag: ImageView = itemView.findViewById(R.id.review_image_view_flag)
         val reviewContentText: TextView = itemView.findViewById(R.id.text_review_content)
    }
}

