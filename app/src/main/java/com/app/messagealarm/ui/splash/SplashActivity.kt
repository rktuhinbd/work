package com.app.messagealarm.ui.splash

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.TransitionManager
import com.app.messagealarm.R
import com.app.messagealarm.utils.makeItVisible
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()
        val animation = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top)
        txt_title?.startAnimation(animation)
        progress_bar_splash?.startAnimation(animation)
        runProgressWithSteps()
    }

    private fun runProgressWithSteps(){
        var progress = 0
        val total = 3000
        object : CountDownTimer(total.toLong(),25) {
            override fun onFinish() {
                //take user to app
            }
            override fun onTick(millisUntilFinished: Long) {
                progress += 1
                progress_bar_splash?.progress = progress
            }

        }.start()
    }
}
