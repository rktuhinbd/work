package com.app.messagealarm.ui.alarm

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.app.messagealarm.BaseActivity
import com.app.messagealarm.BaseApplication
import com.app.messagealarm.R
import com.app.messagealarm.ui.notifications.FloatingNotification
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.MediaUtils
import com.app.messagealarm.utils.Once
import com.app.messagealarm.utils.SharedPrefUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.activity_alarm.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import kotlin.system.exitProcess


class AlarmActivity : BaseActivity() {

    var mMessageReceiver: BroadcastReceiver? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    var isIntractive = true
    var focus = true
    val once = Once()
    var isSwiped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isIntractive = isScreenActive(this)
        setContentView(R.layout.activity_alarm)
        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics
        setupViews()
        val runnable = Runnable(){
            playMedia()
        }
        once.run(runnable)
        tiltAnimation()
        val bundle = Bundle()
        bundle.putString("alarm_by_activity", "true")
        firebaseAnalytics.logEvent("alarm_type", bundle)

        mMessageReceiver =  object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                finishAffinity()
                exitProcess(0)
            }
        }
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
        var thread:Thread? = null
        thread = Thread(Runnable {
            //here i need run the loop of how much time need to play
            val numberOfPLay = intent?.extras!!.getInt(Constants.IntentKeys.NUMBER_OF_PLAY)
            for (x in 0 until numberOfPLay) {
                if (SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_STOPPED)) {
                    break
                }
                val once = Once()
                once.run(Runnable {
                    MediaUtils.playAlarm(
                        thread!!,
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
        })
        thread.start()
    }

    private fun isDeviceLocked(): Boolean{
        val myKM = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        return  myKM.isDeviceLocked
    }


    private fun showPageDismissNotification() {
        FloatingNotification.showPageDismissNotification(
            intent?.extras!!.getString(Constants.IntentKeys.TITLE)!!,
            this,
            intent?.extras!!.getString(Constants.IntentKeys.PACKAGE_NAME)!!,
            intent?.extras!!.getString(Constants.IntentKeys.APP_NAME)!!
        )
    }

    override fun onPause() {
        if(!isSwiped){
            if(isIntractive){
                //we now know that only few devices getting the dismiss notification function called at lock screen startup
                //need to know how much devices creating this issue
                    showPageDismissNotification()
                }
            }
        super.onPause()
        this.unregisterReceiver(mMessageReceiver)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        isIntractive = isScreenActive(this)
        super.onWindowFocusChanged(hasFocus)
    }


    private fun isScreenActive(context: Context): Boolean {
        val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
        return  powerManager.isInteractive
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
        this.registerReceiver(mMessageReceiver,  IntentFilter("turn_off_switch"))
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

   /* override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        finish()
        startActivity(Intent(intent))
    }*/
}