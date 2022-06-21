package com.app.messagealarm.window


import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.IBinder
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.app.messagealarm.R
import com.app.messagealarm.ui.notifications.FloatingNotification
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.MediaUtils
import com.app.messagealarm.utils.Once
import com.app.messagealarm.utils.SharedPrefUtils
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.activity_alarm.*
import java.io.File
import java.lang.NullPointerException
import kotlin.system.exitProcess


class WindowManagerService : Service() {

    private var mWindowManager: WindowManager? = null
    private var mFloatingView: View? = null


    var mMessageReceiver: BroadcastReceiver? = null
    var turnOffReceiver: BroadcastReceiver? = null
    var isIntractive = true
    val once = Once()
    var isSwiped = false
    var intent:Intent? = Intent()


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        isIntractive = isScreenActive(this)
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_window_manager, null)

        val params = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        }
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mWindowManager!!.addView(mFloatingView, params)

        mMessageReceiver =  object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                stopSelf()
            }
        }

        turnOffReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                stopSelf()
            }
        }

    }

    private fun isScreenActive(context: Context): Boolean {
        val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
        return  powerManager.isInteractive
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.intent = intent
        mFloatingView?.findViewById<TextView>(R.id.txt_notification_title)?.text = intent?.extras!!.getString(Constants.IntentKeys.TITLE)
        mFloatingView?.findViewById<TextView>(R.id.txt_notification_desc)?.text = intent?.extras!!.getString(Constants.IntentKeys.DESC)
        mFloatingView?.findViewById<SlideToActView>(R.id.side_to_active)?.text = String.format(
            "Open %s", intent?.extras!!.getString(Constants.IntentKeys.APP_NAME)
        )

        mFloatingView?.findViewById<SlideToActView>(R.id.side_to_active)?.onSlideToActAnimationEventListener =
            object : SlideToActView.OnSlideCompleteListener,
                SlideToActView.OnSlideToActAnimationEventListener {
                override fun onSlideComplete(view: SlideToActView) {

                }

                override fun onSlideCompleteAnimationEnded(view: SlideToActView) {
                    try{
                        if (!SharedPrefUtils.contains(Constants.PreferenceKeys.IS_FIRST_TIME_ALARM_PLAYED)) {
                            SharedPrefUtils.write(
                                Constants.PreferenceKeys.IS_FIRST_TIME_ALARM_PLAYED,
                                true
                            )
                        }
                        FloatingNotification.cancelPageDismissNotification()
                        isSwiped = true
                        MediaUtils.stopAlarm(this@WindowManagerService)
                        stopSelf()
                        openApp()
                    }catch (e:NullPointerException){
                    }
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


        val imagePath = intent?.extras!!.getString(Constants.IntentKeys.IMAGE_PATH)
        if (imagePath != null) {
            mFloatingView?.findViewById<ImageView>(R.id.alarm_image)?.setImageBitmap(
                BitmapFactory.decodeFile(
                    File(imagePath)
                        .absolutePath
                )
            )
        }

//        mFloatingView?.findViewById<FrameLayout>(R.id.root_container)?.setOnClickListener {
//            stopSelf()
//        }

        LocalBroadcastManager.getInstance(this).registerReceiver(turnOffReceiver!!, IntentFilter("turn_off_activity"))
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver!!,  IntentFilter("turn_off_switch"))


        return super.onStartCommand(intent, flags, startId)
    }

    private fun openApp() {
        val launchIntent =
            packageManager.getLaunchIntentForPackage(intent?.extras!!.getString(Constants.IntentKeys.PACKAGE_NAME)!!)
        launchIntent?.let { startActivity(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mFloatingView != null) mWindowManager!!.removeView(mFloatingView)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver!!)
    }



}