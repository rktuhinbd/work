package com.app.messagealarm.ui.buy_pro

import com.app.messagealarm.R

/**
 * Created by Android Dev on 29-Aug-21 Aug, 2021
 */
fun generateReviewList(): List<MyReviewView> {
    val reviewList = ArrayList<MyReviewView>()
    reviewList.add(MyReviewView(R.drawable.image_brain, "Brain Baur", R.drawable.flag_america,
        "Fantastic app!! Thanks to the premium version you will say goodbye to the announces forever!!"))
    reviewList.add(MyReviewView(R.drawable.image_brain, "Brain Baur", R.drawable.flag_america,
        "Fantastic app!! Thanks to the premium version you will say goodbye to the announces forever!!"))
    reviewList.add(MyReviewView(R.drawable.image_brain, "Brain Baur", R.drawable.flag_america,
        "Fantastic app!! Thanks to the premium version you will say goodbye to the announces forever!!"))

    return reviewList;
}
