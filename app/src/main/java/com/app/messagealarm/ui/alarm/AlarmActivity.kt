package com.app.messagealarm.ui.alarm

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.R
import com.app.messagealarm.ui.notifications.FloatingNotification
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.MediaUtils
import com.app.messagealarm.utils.Once
import com.app.messagealarm.utils.SharedPrefUtils
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.activity_alarm.*
import java.io.File


class AlarmActivity : BaseActivity() {

    var isIntractive = true
    var focus = true
    val once = Once()
    var isSwiped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isIntractive = isScreenActive(this)
        setContentView(R.layout.activity_alarm)
        setupViews()
        val runnable = Runnable(){
            playMedia()
        }
        once.run(runnable)
        tiltAnimation()
    }

    override fun onStart() {
        super.onStart()
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
            this.window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        } else {
            this.window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    private fun playMedia() {
        SharedPrefUtils.write(Constants.PreferenceKeys.IS_NOTIFICATION_SWIPED, false)
        SharedPrefUtils.write(Constants.PreferenceKeys.IS_ACTIVITY_STARTED, true)
        if (intent?.extras!!.getString(Constants.IntentKeys.TONE) != null) {
            startPlaying(intent?.extras!!.getString(Constants.IntentKeys.TONE))
        } else {
            startPlaying(null)
        }
    }


    private fun startPlaying(tone: String?) {
        Thread(Runnable {
            //here i need run the loop of how much time need to play
            val numberOfPLay = intent?.extras!!.getInt(Constants.IntentKeys.NUMBER_OF_PLAY)
            for (x in 0 until numberOfPLay) {
                if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_STOPPED)) {
                    break
                }
                val once = Once()
                once.run(Runnable {
                    MediaUtils.playAlarm(
                        intent?.extras!!.getBoolean(Constants.IntentKeys.IS_JUST_VIBRATE),
                        intent?.extras!!.getBoolean(Constants.IntentKeys.IS_VIBRATE),
                        this, tone,
                        (x == (numberOfPLay - 1)),
                        intent?.extras!!.getString(Constants.IntentKeys.PACKAGE_NAME)!!,
                        intent?.extras!!.getString(Constants.IntentKeys.APP_NAME)!!
                    )
                    if (x == numberOfPLay - 1) {
                        //done playing dismiss the activity now
                        //send a notification that you missed the alarm
                        finish()
                        SharedPrefUtils.write(Constants.PreferenceKeys.IS_MUTED, true)
                        FloatingNotification.notifyMute(true)
                        FloatingNotification.cancelPageDismissNotification()
                    }
                })
            }
        }).start()
    }


    private fun showPageDismissNotification() {
        FloatingNotification.showPageDismissNotification(
            this,
            intent?.extras!!.getString(Constants.IntentKeys.PACKAGE_NAME)!!,
            intent?.extras!!.getString(Constants.IntentKeys.APP_NAME)!!
        )
    }

    override fun onPause() {
        if(!isSwiped){
            if(isIntractive){
                showPageDismissNotification()
            }
        }
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        isIntractive = isScreenActive(this)
        super.onWindowFocusChanged(hasFocus)
    }


    fun isScreenActive(context: Context): Boolean {
        val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            powerManager.isInteractive
        } else {
            powerManager.isScreenOn
        }
    }

    private fun tiltAnimation() {
        val ranim =
            AnimationUtils.loadAnimation(this, R.anim.tilt_animation) as Animation
        ranim.repeatCount = Animation.INFINITE
        img_app_icon?.animation = ranim
    }

    private fun setupViews() {
        txt_message_from?.text = intent?.extras!!.getString(Constants.IntentKeys.TITLE)
        txt_message_desc?.text = intent?.extras!!.getString(Constants.IntentKeys.DESC)
        side_to_active?.text = String.format(
            "Open %s", intent?.extras!!
                .getString(Constants.IntentKeys.APP_NAME)
        )
        val imagePath = intent?.extras!!.getString(Constants.IntentKeys.IMAGE_PATH)
        if (imagePath != null) {
            img_app_icon?.setImageBitmap(
                BitmapFactory.decodeFile(
                    File(imagePath)
                        .absolutePath
                )
            )
        }
        side_to_active?.onSlideToActAnimationEventListener =
            object : SlideToActView.OnSlideCompleteListener,
                SlideToActView.OnSlideToActAnimationEventListener {
                override fun onSlideComplete(view: SlideToActView) {

                }

                override fun onSlideCompleteAnimationEnded(view: SlideToActView) {
                    isSwiped = true
                    MediaUtils.stopAlarm()
                    openApp()
                    finish()
                }

                override fun onSlideCompleteAnimationStarted(
                    view: SlideToActView,
                    threshold: Float
                ) {

                }

                override fun onSlideResetAnimationEnded(view: SlideToActView) {

                }

                override fun onSlideResetAnimationStarted(view: SlideToActView) {

                }

            }
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {

    }


    private fun openApp() {
        val launchIntent =
            packageManager.getLaunchIntentForPackage(intent?.extras!!.getString(Constants.IntentKeys.PACKAGE_NAME)!!)
        launchIntent?.let { startActivity(it) }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        finish()
        startActivity(Intent(intent))
    }
}