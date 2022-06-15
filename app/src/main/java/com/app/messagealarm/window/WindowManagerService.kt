package com.app.messagealarm.window


import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import com.app.messagealarm.R
import com.app.messagealarm.ui.notifications.FloatingNotification
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.MediaUtils
import com.app.messagealarm.utils.SharedPrefUtils
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.activity_alarm.*
import java.lang.NullPointerException


class WindowManagerService : Service() {

    private var mWindowManager: WindowManager? = null
    private var mFloatingView: View? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onCreate() {
        super.onCreate()
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
//
//        mFloatingView?.findViewById<FrameLayout>(R.id.root_container)?.setOnClickListener {
//            stopSelf()
//        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

//        val soundLevel: Int = intent?.extras!!.getInt(Constants.IntentKeys.SOUND_LEVEL)
//        val isJustVibrate: Boolean = intent.extras!!.getBoolean(Constants.IntentKeys.IS_JUST_VIBRATE)
//        val isVibrate: Boolean = intent.extras!!.getBoolean(Constants.IntentKeys.IS_VIBRATE)
//        val mediaPath: String? = intent.extras!!.getString(Constants.IntentKeys.TONE)
//        val packageName: String? = intent.extras!!.getString(Constants.IntentKeys.PACKAGE_NAME)
//        val appName: String? = intent.extras!!.getString(Constants.IntentKeys.APP_NAME)

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
                        stopSelf()
                    }catch (e: NullPointerException){
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

        mFloatingView?.findViewById<FrameLayout>(R.id.root_container)?.setOnClickListener {
            stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mFloatingView != null) mWindowManager!!.removeView(mFloatingView)
    }

}