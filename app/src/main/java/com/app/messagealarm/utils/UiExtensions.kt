package com.app.messagealarm.utils

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView


/**
 * This file is the collection of all the extension functions to be used in the UI
 * @author Al Mujahid Khan
 * */

fun View.isVisibile(): Boolean = visibility == View.VISIBLE

fun View.isGone(): Boolean = visibility == View.GONE

fun View.isInvisible(): Boolean = visibility == View.INVISIBLE

fun View.makeItVisible() {
    visibility = View.VISIBLE
}

fun View.makeItInvisible() {
    visibility = View.INVISIBLE
}

fun View.makeItGone() {
    visibility = View.GONE
}



fun ViewGroup.inflate(layoutId: Int): View? {
    return if (layoutId > Constants.Invalid.INVALID_INTEGER) {
        LayoutInflater.from(context).inflate(layoutId, this, false)
    } else {
        null
    }
}

fun View.setRipple(colorResourceId: Int) {
    background = RippleDrawable(
        ColorStateList.valueOf(ViewUtils.getColor(colorResourceId)),
        if (this is ImageView) null else background, null
    )
}

fun Activity.setLightStatusBar() {
    ViewUtils.setLightStatusBar(this)
}

fun Activity.clearLightStatusBar() {
    ViewUtils.clearLightStatusBar(this)
}

fun Fragment.setLightStatusBar() {
    if (activity != null) {
        ViewUtils.setLightStatusBar(activity!!)
    }
}

fun Fragment.clearLightStatusBar() {
    if (activity != null) {
        ViewUtils.clearLightStatusBar(activity!!)
    }
}
