package com.app.messagealarm.ui.alarm

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.R
import com.app.messagealarm.ui.notifications.FloatingNotification
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.MediaUtils
import com.app.messagealarm.utils.Once
import com.app.messagealarm.utils.SharedPrefUtils
import com.application.isradeleon.notify.Notify
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.activity_alarm.*
import java.io.File


class AlarmActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        setupViews()
        playMedia()
        tiltAnimation()
    }

    private fun playMedia() {
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
                        intent?.extras!!.getBoolean(Constants.IntentKeys.IS_VIBRATE),
                        this, tone,
                        (x == (numberOfPLay - 1))
                    )
                    if (x == numberOfPLay - 1) {
                        //done playing dismiss the activity now
                        //send a notification that you missed the alarm
                        finish()
                        SharedPrefUtils.write(Constants.PreferenceKeys.IS_MUTED, true)
                        FloatingNotification.notifyMute(true)
                    }
                })
            }
        }).start()
    }

    private fun showYouMissedAlarmNotification(app: String) {

    }



    private fun showPageDismissNotification() {
        val pattern = longArrayOf(0, 100, 500, 100, 500, 100, 500, 100, 500, 100, 500)
        Notify.create(this)
            .setChannelId(getString(R.string.notify_channel_id))
            .setChannelName(getString(R.string.notify_channel_name))
            .setChannelDescription(getString(R.string.notify_channel_description))
            .setTitle("You have a message from ${intent.extras?.getString(Constants.IntentKeys.APP_NAME)}")
            .setContent("Swipe to dismiss the alarm!")
            .setVibrationPattern(pattern)
            .setImportance(Notify.NotificationImportance.HIGH)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .show()
    }

    override fun onPause() {
        super.onPause()
        showPageDismissNotification()
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
        SharedPrefUtils.write(Constants.PreferenceKeys.IS_ACTIVITY_STARTED, false)
    }

    override fun onBackPressed() {

    }


    private fun openApp() {
        val launchIntent =
            packageManager.getLaunchIntentForPackage(intent?.extras!!.getString(Constants.IntentKeys.PACKAGE_NAME)!!)
        launchIntent?.let { startActivity(it) }
    }

}