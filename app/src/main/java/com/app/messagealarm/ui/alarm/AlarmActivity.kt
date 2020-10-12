package com.app.messagealarm.ui.alarm

import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.utils.*
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.activity_alarm.*

import java.io.File


class AlarmActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        setupViews()
        playMedia()
    }

    private fun playMedia(){
        if (intent?.extras!!.getString(Constants.IntentKeys.TONE) != null){
            Thread(Runnable {
                ExoPlayerUtils.playAudio(this, intent?.extras!!.getString(Constants.IntentKeys.TONE))
                VibratorUtils.startVibrate(this)
            }).start()
        }else{
            Thread(Runnable {
                ExoPlayerUtils.playAudio(this, null)
                VibratorUtils.startVibrate(this)
            }).start()

        }
    }

    private fun setupViews(){
        txt_message_from?.text = intent?.extras!!.getString(Constants.IntentKeys.TITLE)
        txt_message_desc?.text = intent?.extras!!.getString(Constants.IntentKeys.DESC)
        side_to_active?.text  = String.format("Slide to open %s", intent?.extras!!
            .getString(Constants.IntentKeys.APP_NAME))
        val imagePath = intent?.extras!!.getString(Constants.IntentKeys.IMAGE_PATH)
        if(imagePath != null){
            img_app_icon?.setImageBitmap(
                BitmapFactory.decodeFile(
                    File(imagePath)
                        .absolutePath
                )
            )
        }
        side_to_active?.onSlideToActAnimationEventListener = object :SlideToActView.OnSlideCompleteListener,
            SlideToActView.OnSlideToActAnimationEventListener {
            override fun onSlideComplete(view: SlideToActView) {

            }

            override fun onSlideCompleteAnimationEnded(view: SlideToActView) {
                Thread(Runnable {
                    ExoPlayerUtils.stopAlarm()
                    VibratorUtils.stopVibrate()
                }).start()
                SnoozeUtils.activateSnoozeMode(true)
                openApp()
                finish()
            }

            override fun onSlideCompleteAnimationStarted(view: SlideToActView, threshold: Float) {

            }

            override fun onSlideResetAnimationEnded(view: SlideToActView) {

            }

            override fun onSlideResetAnimationStarted(view: SlideToActView) {

            }

        }
    }

    override fun onResume() {
        super.onResume()
        BaseApplication.activityRunning()
    }


    override fun onDestroy() {
        super.onDestroy()
        BaseApplication.activityStopped()
    }



    private fun openApp(){
        val launchIntent =
            packageManager.getLaunchIntentForPackage(intent?.extras!!.getString(Constants.IntentKeys.PACKAGE_NAME)!!)
        launchIntent?.let { startActivity(it) }
    }

}