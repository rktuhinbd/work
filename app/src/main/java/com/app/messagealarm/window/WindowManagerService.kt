package com.app.messagealarm.window


import android.R
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.*
import android.view.*
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.app.messagealarm.ui.notifications.FloatingNotification
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.MediaUtils
import com.app.messagealarm.utils.Once
import com.app.messagealarm.utils.SharedPrefUtils
import com.ncorti.slidetoact.SlideToActView
import java.io.File


class WindowManagerService : Service() {

    private var mWindowManager: WindowManager? = null
    private var mFloatingWidgetView: View? = null
    private var collapsedView: View? = null
    private var expandedView: View? = null
    private var remove_image_view: ImageView? = null
    private var szWindow: Point = Point()
    private var removeFloatingWidgetView: View? = null

    private var x_init_cord = 0
    private var y_init_cord: Int = 0
    private var x_init_margin: Int = 0
    private var y_init_margin: Int = 0
    private var isLeft = true


    var mMessageReceiver: BroadcastReceiver? = null
    var turnOffReceiver: BroadcastReceiver? = null
    var isIntractive = true
    val once = Once()
    var isSwiped = false
    var intent: Intent? = Intent()


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        getWindowManagerDefaultDisplay()
        addRemoveView(inflater)
        addFloatingWidgetView(inflater)
        implementTouchListenerToFloatingWidgetView()
        implementClickListeners()

//        mFloatingView = LayoutInflater.from(this)
//            .inflate(com.app.messagealarm.R.layout.layout_window_manager, null)


//        mWindowManager!!.addView(mFloatingView, params)

        mMessageReceiver = object : BroadcastReceiver() {
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

    private fun implementClickListeners() {
        mFloatingWidgetView!!.findViewById<View>(com.app.messagealarm.R.id.close_floating_view)
            .setOnClickListener {
                stopSelf()
            }
    }

    private fun getWindowManagerDefaultDisplay() {
        mWindowManager!!.defaultDisplay.getSize(
            szWindow
        )
    }

    private fun addRemoveView(inflater: LayoutInflater): View? {
        //Inflate the removing view layout we created
        removeFloatingWidgetView =
            inflater.inflate(com.app.messagealarm.R.layout.remove_floating_widget_layout, null)

        //Add the view to the window.
        val paramRemove: WindowManager.LayoutParams
        paramRemove = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        }

        //Specify the view position
        paramRemove.gravity = Gravity.TOP or Gravity.LEFT

        //Initially the Removing widget view is not visible, so set visibility to GONE
        removeFloatingWidgetView!!.visibility = View.GONE
        remove_image_view =
            removeFloatingWidgetView!!.findViewById<View>(com.app.messagealarm.R.id.remove_img) as ImageView
        mWindowManager!!.addView(removeFloatingWidgetView, paramRemove)
        return remove_image_view
    }

    private fun addFloatingWidgetView(inflater: LayoutInflater) {
        //Inflate the floating view layout we created
        mFloatingWidgetView =
            inflater.inflate(com.app.messagealarm.R.layout.layout_window_manager, null)

        //Add the view to the window.
        val params: WindowManager.LayoutParams
        params = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        }

        params.gravity = Gravity.TOP or Gravity.LEFT

        params.x = 0
        params.y = 100

        //Add the view to the window
        mWindowManager!!.addView(mFloatingWidgetView, params)

        //find id of collapsed view layout
        collapsedView = mFloatingWidgetView?.findViewById(com.app.messagealarm.R.id.collapse_view)

        //find id of the expanded view layout
        expandedView =
            mFloatingWidgetView?.findViewById(com.app.messagealarm.R.id.expanded_container)
    }

    private fun implementTouchListenerToFloatingWidgetView() {
        mFloatingWidgetView!!.findViewById<View>(com.app.messagealarm.R.id.root_container)
            .setOnTouchListener(object : OnTouchListener {
                var time_start: Long = 0
                var time_end: Long = 0
                var isLongClick = false //variable to judge if user click long press
                var inBounded = false //variable to judge if floating view is bounded to remove view
                var remove_img_width = 0
                var remove_img_height = 0
                var handler_longClick = Handler()
                var runnable_longClick = Runnable { //On Floating Widget Long Click

                    //Set isLongClick as true
                    isLongClick = true

                    //Set remove widget view visibility to VISIBLE
                    removeFloatingWidgetView!!.visibility = View.VISIBLE
                    onFloatingWidgetLongClick()
                }

                override fun onTouch(v: View?, event: MotionEvent): Boolean {

                    //Get Floating widget view params
                    val layoutParams =
                        mFloatingWidgetView!!.layoutParams as WindowManager.LayoutParams

                    //get the touch location coordinates
                    val x_cord = event.rawX.toInt()
                    val y_cord = event.rawY.toInt()
                    val x_cord_Destination: Int
                    var y_cord_Destination: Int
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            time_start = System.currentTimeMillis()
                            handler_longClick.postDelayed(runnable_longClick, 600)
                            remove_img_width = remove_image_view!!.layoutParams.width
                            remove_img_height = remove_image_view!!.layoutParams.height
                            x_init_cord = x_cord
                            y_init_cord = y_cord

                            //remember the initial position.
                            x_init_margin = layoutParams.x
                            y_init_margin = layoutParams.y
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            isLongClick = false
                            removeFloatingWidgetView!!.visibility = View.GONE
                            remove_image_view!!.layoutParams.height = remove_img_height
                            remove_image_view!!.layoutParams.width = remove_img_width
                            handler_longClick.removeCallbacks(runnable_longClick)

                            //If user drag and drop the floating widget view into remove view then stop the service
                            if (inBounded) {
                                stopSelf()
                                inBounded = false;
                                return true
                            }


                            //Get the difference between initial coordinate and current coordinate
                            val x_diff = x_cord - x_init_cord
                            val y_diff = y_cord - y_init_cord

                            //The check for x_diff <5 && y_diff< 5 because sometime elements moves a little while clicking.
                            //So that is click event.
                            if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {
                                time_end = System.currentTimeMillis()

                                //Also check the difference between start time and end time should be less than 300ms
                                if (time_end - time_start < 300) onFloatingWidgetClick()
                            }
                            y_cord_Destination = y_init_margin + y_diff
                            val barHeight = getStatusBarHeight()
                            if (y_cord_Destination < 0) {
                                y_cord_Destination = 0
                            } else if (y_cord_Destination + (mFloatingWidgetView!!.height + barHeight) > szWindow.y) {
                                y_cord_Destination =
                                    szWindow.y - (mFloatingWidgetView!!.height + barHeight)
                            }
                            layoutParams.y = y_cord_Destination
                            inBounded = false

                            //reset position if user drags the floating view
                            resetPosition(x_cord)
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val x_diff_move = x_cord - x_init_cord
                            val y_diff_move = y_cord - y_init_cord
                            x_cord_Destination = x_init_margin + x_diff_move
                            y_cord_Destination = y_init_margin + y_diff_move

                            //If user long click the floating view, update remove view
                            if (isLongClick) {
                                val x_bound_left = szWindow.x / 2 - (remove_img_width * 1.5).toInt()
                                val x_bound_right =
                                    szWindow.x / 2 + (remove_img_width * 1.5).toInt()
                                val y_bound_top = szWindow.y - (remove_img_height * 1.5).toInt()

                                //If Floating view comes under Remove View update Window Manager
                                if (x_cord >= x_bound_left && x_cord <= x_bound_right && y_cord >= y_bound_top) {
                                    inBounded = true
                                    val x_cord_remove =
                                        ((szWindow.x - remove_img_height * 1.5) / 2).toInt()
                                    val y_cord_remove =
                                        (szWindow.y - (remove_img_width * 1.5 + getStatusBarHeight())).toInt()
                                    if (remove_image_view!!.layoutParams.height == remove_img_height) {
                                        remove_image_view!!.layoutParams.height =
                                            (remove_img_height * 1.5).toInt()
                                        remove_image_view!!.layoutParams.width =
                                            (remove_img_width * 1.5).toInt()
                                        val param_remove =
                                            removeFloatingWidgetView!!.layoutParams as WindowManager.LayoutParams
                                        param_remove.x = x_cord_remove
                                        param_remove.y = y_cord_remove
                                        mWindowManager!!.updateViewLayout(
                                            removeFloatingWidgetView,
                                            param_remove
                                        )
                                    }
                                    layoutParams.x = x_cord_remove + Math.abs(
                                        removeFloatingWidgetView!!.width - mFloatingWidgetView!!.width
                                    ) / 2
                                    layoutParams.y = y_cord_remove + Math.abs(
                                        removeFloatingWidgetView!!.height - mFloatingWidgetView!!.height
                                    ) / 2

                                    //Update the layout with new X & Y coordinate
                                    mWindowManager!!.updateViewLayout(
                                        mFloatingWidgetView,
                                        layoutParams
                                    )
                                    return true
                                } else {
                                    //If Floating window gets out of the Remove view update Remove view again
                                    inBounded = false
                                    remove_image_view!!.layoutParams.height = remove_img_height
                                    remove_image_view!!.layoutParams.width = remove_img_width
                                    onFloatingWidgetClick()
                                }
                            }
                            layoutParams.x = x_cord_Destination
                            layoutParams.y = y_cord_Destination

                            //Update the layout with new X & Y coordinate
                            mWindowManager!!.updateViewLayout(mFloatingWidgetView, layoutParams)
                            return true
                        }
                    }
                    return false
                }
            })
    }

    private fun resetPosition(x_cord_now: Int) {
        if (x_cord_now <= szWindow.x / 2) {
            isLeft = true
            moveToLeft(x_cord_now)
        } else {
            isLeft = false
            moveToRight(x_cord_now)
        }
    }

    private fun moveToLeft(current_x_cord: Int) {
        val x = szWindow.x - current_x_cord
        object : CountDownTimer(500, 5) {
            //get params of Floating Widget view
            var mParams = mFloatingWidgetView!!.layoutParams as WindowManager.LayoutParams
            override fun onTick(t: Long) {
                val step = (500 - t) / 5
                mParams.x = 0 - (current_x_cord * current_x_cord * step).toInt()

                //If you want bounce effect uncomment below line and comment above line
                // mParams.x = 0 - (int) (double) bounceValue(step, x);


                //Update window manager for Floating Widget
                mWindowManager!!.updateViewLayout(mFloatingWidgetView, mParams)
            }

            override fun onFinish() {
                mParams.x = 0

                //Update window manager for Floating Widget
                mWindowManager!!.updateViewLayout(mFloatingWidgetView, mParams)
            }
        }.start()
    }

    private fun moveToRight(current_x_cord: Int) {
        object : CountDownTimer(500, 5) {
            //get params of Floating Widget view
            var mParams = mFloatingWidgetView!!.layoutParams as WindowManager.LayoutParams
            override fun onTick(t: Long) {
                val step = (500 - t) / 5
                mParams.x =
                    (szWindow.x + current_x_cord * current_x_cord * step - mFloatingWidgetView!!.width).toInt()

                mWindowManager!!.updateViewLayout(mFloatingWidgetView, mParams)
            }

            override fun onFinish() {
                mParams.x = szWindow.x - mFloatingWidgetView!!.width
                mWindowManager!!.updateViewLayout(mFloatingWidgetView, mParams)
            }
        }.start()
    }

    private fun onFloatingWidgetLongClick() {
        //Get remove Floating view params
        val removeParams = removeFloatingWidgetView!!.layoutParams as WindowManager.LayoutParams

        //get x and y coordinates of remove view
        val x_cord = (szWindow.x - removeFloatingWidgetView!!.width) / 2
        val y_cord: Int = szWindow.y - (removeFloatingWidgetView!!.height + getStatusBarHeight())
        removeParams.x = x_cord
        removeParams.y = y_cord

        //Update Remove view params
        mWindowManager!!.updateViewLayout(removeFloatingWidgetView, removeParams)
    }

    private fun onFloatingWidgetClick() {
        if (isViewCollapsed()) {
            collapsedView!!.visibility = View.GONE
            expandedView!!.visibility = View.VISIBLE
        }
    }

    private fun isViewCollapsed(): Boolean {
        return mFloatingWidgetView == null || mFloatingWidgetView!!.findViewById<View>(com.app.messagealarm.R.id.collapse_view).visibility == View.VISIBLE
    }

    private fun getStatusBarHeight(): Int {
        return Math.ceil((25 * applicationContext.resources.displayMetrics.density).toDouble())
            .toInt()
    }

    private fun isScreenActive(context: Context): Boolean {
        val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
        return powerManager.isInteractive
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.intent = intent
        mFloatingWidgetView?.findViewById<TextView>(com.app.messagealarm.R.id.txt_notification_title)?.text =
            intent?.extras!!.getString(Constants.IntentKeys.TITLE)
        mFloatingWidgetView?.findViewById<TextView>(com.app.messagealarm.R.id.txt_notification_desc)?.text =
            intent.extras!!.getString(Constants.IntentKeys.DESC)
        mFloatingWidgetView?.findViewById<SlideToActView>(com.app.messagealarm.R.id.side_to_active)?.text =
            String.format(
                "Open %s", intent?.extras!!.getString(Constants.IntentKeys.APP_NAME)
            )

        mFloatingWidgetView?.findViewById<SlideToActView>(com.app.messagealarm.R.id.side_to_active)?.onSlideToActAnimationEventListener =
            object : SlideToActView.OnSlideCompleteListener,
                SlideToActView.OnSlideToActAnimationEventListener {
                override fun onSlideComplete(view: SlideToActView) {

                }

                override fun onSlideCompleteAnimationEnded(view: SlideToActView) {
                    try {
                        if (!SharedPrefUtils.contains(Constants.PreferenceKeys.IS_FIRST_TIME_ALARM_PLAYED)) {
                            SharedPrefUtils.write(
                                Constants.PreferenceKeys.IS_FIRST_TIME_ALARM_PLAYED,
                                true
                            )
                        }
                        FloatingNotification.cancelPageDismissNotification()
                        FloatingNotification.cancelAlarmNotification()
                        isSwiped = true
                        MediaUtils.stopAlarm(this@WindowManagerService)
                        stopSelf()
                        openApp()
                    } catch (e: NullPointerException) {
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


        val imagePath = intent.extras!!.getString(Constants.IntentKeys.IMAGE_PATH)
        if (imagePath != null) {
            mFloatingWidgetView?.findViewById<ImageView>(com.app.messagealarm.R.id.alarm_image)?.setImageBitmap(
                BitmapFactory.decodeFile(
                    File(imagePath)
                        .absolutePath
                )
            )
        }

        //        mFloatingView?.findViewById<FrameLayout>(R.id.root_container)?.setOnClickListener {
        //            stopSelf()
        //        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(turnOffReceiver!!, IntentFilter("turn_off_activity"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver!!, IntentFilter("turn_off_switch"))


        return super.onStartCommand(intent, flags, startId)
    }

    private fun openApp() {
        val launchIntent =
            packageManager.getLaunchIntentForPackage(intent?.extras!!.getString(Constants.IntentKeys.PACKAGE_NAME)!!)
        launchIntent?.let { startActivity(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mFloatingWidgetView != null) mWindowManager!!.removeView(mFloatingWidgetView)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver!!)
    }


}