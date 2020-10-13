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
            startPlaying(intent?.extras!!.getString(Constants.IntentKeys.TONE))
        }else{
           startPlaying(null)
        }
    }


    private fun startPlaying(tone:String?){
        Thread(Runnable {
            //here i need run the loop of how much time need to play
            val numberOfPLay = intent?.extras!!.getInt(Constants.IntentKeys.NUMBER_OF_PLAY)
            for (x in 0 until numberOfPLay){
                ExoPlayerUtils.playAudio(
                    intent?.extras!!.getBoolean(Constants.IntentKeys.IS_VIBRATE),
                    this, tone)
                if(x == numberOfPLay - 1){
                    //done playing dismiss the activity now
                    //send a notification that you missed the alarm
                    finish()
                }
            }
        }).start()
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

    override fun onBackPressed() {

    }



    private fun openApp(){
        val launchIntent =
            packageManager.getLaunchIntentForPackage(intent?.extras!!.getString(Constants.IntentKeys.PACKAGE_NAME)!!)
        launchIntent?.let { startActivity(it) }
    }

}