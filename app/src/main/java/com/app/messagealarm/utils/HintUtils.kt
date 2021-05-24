package com.app.messagealarm.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.app.messagealarm.R
import com.app.messagealarm.model.Hint
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.Circle

class HintUtils {

    companion object {

        fun showHintsToUser(activity: FragmentActivity, map:List<Hint>,
                            spotLayout:Int, dialogTag:String, vararg viewsList: View) {
            //iterate over all the views and show hint
            val targets = ArrayList<Target>()
            val views = ArrayList<View>()
            val rootView: ViewGroup = if (dialogTag == null) {
                activity
                    .findViewById<ViewGroup?>(android.R.id.content)
                    ?: return
            } else {
                (activity.supportFragmentManager
                    .findFragmentByTag(dialogTag) as? DialogFragment)
                    ?.dialog
                    ?.window
                    ?.findViewById<ViewGroup?>(android.R.id.content)
                    ?: return
            }
            var count = 0
            viewsList.forEach {
                val firstRoot = FrameLayout(activity)
                val first = activity.layoutInflater.inflate(spotLayout, firstRoot)
                val title = first.findViewById<TextView>(R.id.custom_text)
                val desc = first.findViewById<TextView>(R.id.custom_desc)
                title.text = map[count].title
                desc.text = map[count].desc
                count++
                val firstTarget = Target.Builder()
                    .setAnchor(it)
                    .setShape(Circle(70f))
                    .setOverlay(first)
                    .setOnTargetListener(object : OnTargetListener {
                        override fun onStarted() {

                        }
                        override fun onEnded() {
                            count = 0
                        }
                    })
                    .build()
                targets.add(firstTarget)
                views.add(first)
            }

            // create spotlight
            val spotlight = Spotlight.Builder(activity)
                .setTargets(targets)
                .setDuration(1000L)
                .setAnimation(DecelerateInterpolator(1f))
                .setOnSpotlightListener(object : OnSpotlightListener {
                    override fun onStarted() {

                    }
                    override fun onEnded() {

                    }
                })
                .setContainer(rootView)
                .build()
            spotlight.start()
            val nextTarget = View.OnClickListener { spotlight.next() }
            val closeSpotlight = View.OnClickListener { spotlight.finish() }
            //listener
            views.forEach {
                it.findViewById<View>(R.id.close_target).setOnClickListener(nextTarget)
                it.findViewById<View>(R.id.close_spotlight).setOnClickListener(closeSpotlight)
            }
        }
    }
}