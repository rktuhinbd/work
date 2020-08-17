package com.app.messagealarm.ui.splash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.transition.TransitionManager
import com.app.messagealarm.R
import com.app.messagealarm.utils.makeItVisible
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.activity_splash.view.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed(
            {
                TransitionManager.beginDelayedTransition(layout_base_splash!!)
                txt_title?.makeItVisible()
            },1000)
    }
}
