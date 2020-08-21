package com.app.messagealarm.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.app.messagealarm.BaseApplication
import timber.log.Timber
import kotlin.math.roundToInt

/**
 * This is a class that contains utils for the view
 * @author Al Mujahid Khan
 * */
class ViewUtils {
    companion object {
        /**
         * This method returns local resources
         *
         * @return desired resources
         * */
        fun getResources(): Resources {
            return BaseApplication.getBaseApplicationContext().resources
        }

        /**
         * This method returns locally stored dimension
         *
         * @return desired dimension
         * */
        fun getDimension(resourceId: Int): Float {
            return getResources().getDimension(resourceId)
        }

        /**
         * This method returns locally stored dimension in pixel
         *
         * @return desired dimension in pixel
         * */
        fun getPixel(resourceId: Int): Int {
            return getResources().getDimensionPixelSize(resourceId)
        }

        /**
         * This method returns a local font
         *
         * @param resourceId desired resource id
         * @return desired font
         * */
        fun getFont(resourceId: Int): Typeface? {
            return ResourcesCompat.getFont(
                BaseApplication.getBaseApplicationContext(),
                resourceId
            )
        }

        /**
         * This method returns a local drawable
         *
         * @param resourceId desired resource
         * @return drawable of the resource
         * */
        fun getDrawable(resourceId: Int): Drawable? {
            return ContextCompat.getDrawable(
                BaseApplication.getBaseApplicationContext(),
                resourceId
            )
        }

        /**
         * This method returns a local color id
         *
         * @param colorResourceId desired color resource
         * @return color id
         * */
        fun getColor(colorResourceId: Int): Int {
            return ContextCompat.getColor(
                BaseApplication.getBaseApplicationContext(),
                colorResourceId
            )
        }

        /**
         * This method converts pixels to dp
         *
         * @param px desired pixels
         * @return amount in dp
         * */
        fun pxToDp(px: Float): Float {
            val densityDpi = Resources.getSystem().displayMetrics.densityDpi.toFloat()
            return px / (densityDpi / 160f)
        }

        /**
         * This method converts dp to pixels
         *
         * @param dp desired amount of dp
         * @return amount in pixels
         * */
        fun dpToPx(dp: Int): Float {
            val density = Resources.getSystem().displayMetrics.density
            return (dp * density).roundToInt().toFloat()
        }

        /**
         * This method returns bitmap from the view
         *
         * @param view desired view
         * @return bitmap of the view
         * */
        fun getBitmapFromView(view: View): Bitmap? {
            val bitmap =
                Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))

            return bitmap
        }






        /**
         * This method sets status bar color
         *
         * @param activity current activity
         * @param colorResourceId color resource id
         * */
        fun setStatusBarColor(activity: Activity, colorResourceId: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && colorResourceId > -1) {
                val window = activity.window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = getColor(colorResourceId)
            }
        }

        /**
         * This method sets toolbar color
         *
         * @param activity current activity
         * @param colorResourceId color resource id
         * */
        fun setToolbarColor(activity: AppCompatActivity, colorResourceId: Int) {
            if (colorResourceId > -1) {
                activity.supportActionBar?.setBackgroundDrawable(
                    ColorDrawable(getColor(colorResourceId))
                )
            }
        }

        /**
         * This method provides width of the screen
         *
         * @param context UI context
         * @return [Int] screen width in pixels
         * */
        fun getScreenWidth(context: Context): Int {
            val windowManager = context
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val dm = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(dm)
            return dm.widthPixels
        }

        /**
         * This method provides height of the screen
         *
         * @param context UI context
         * @return [Int] screen height in pixels
         * */
        fun getScreenHeight(context: Context): Int {
            val windowManager = context
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val dm = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(dm)
            return dm.heightPixels
        }

        /**
         * This method provides the height of status bar
         *
         * @return [Int] status bar height in pixel
         * */
        fun getStatusBarHeight(): Int {
            var result = 0
            val resourceId = getResources()
                .getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = getResources().getDimensionPixelSize(resourceId)
            }
            return result
        }

        /**
         * This method sets the color tint of the drawable
         *
         * @param drawable working drawable
         * @param colorResourceId resource id of the color
         * */
        fun setColorTint(drawable: Drawable, colorResourceId: Int) {
            DrawableCompat.setTint(drawable, getColor(colorResourceId))
        }

        /**
         * This method sets the color tint of the drawable
         *
         * @param drawable working drawable
         * @param colorString string of the color
         * */
        fun setColorTint(drawable: Drawable, colorString: String) {
            try {
                DrawableCompat.setTint(drawable, Color.parseColor(colorString))
            } catch (e: Exception) {
                Timber.d(e)
            }
        }

        /**
         * This method sets the color tint of the ImageView
         *
         * @param imageView working ImageView
         * @param colorResourceId resource id of the color
         * */
        fun setColorTint(imageView: ImageView, colorResourceId: Int) {
            setColorTint(imageView.drawable, colorResourceId)
        }

        /**
         * This method clears the light state of status bar
         *
         * @param activity current activity
         * */
        fun clearLightStatusBar(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val window = activity.window
                val flags = window.decorView.systemUiVisibility
                val modifiedFlags = flags and (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv())
                window.decorView.systemUiVisibility = modifiedFlags
            }
        }

        /**
         * This method sets the light state of status bar
         *
         * @param activity current activity
         * */
        fun setLightStatusBar(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val window = activity.window
                val flags = window.decorView.systemUiVisibility
                val modifiedFlags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.decorView.systemUiVisibility = modifiedFlags
            }
        }

        /**
         * This method clears the light state of system navigation bar
         *
         * @param activity current activity
         * */
        fun clearLightSystemNavigationBar(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                val window = activity.window
                val flags = window.decorView.systemUiVisibility
                val modifiedFlags = flags and (View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv())
                window.decorView.systemUiVisibility = modifiedFlags
            }
        }

        /**
         * This method sets the light state of system navigation bar
         *
         * @param activity current activity
         * */
        fun setLightSystemNavigationBar(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                val window = activity.window
                val flags = window.decorView.systemUiVisibility
                val modifiedFlags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                window.decorView.systemUiVisibility = modifiedFlags
            }
        }

        /**
         * This method sets system navigation bar color
         *
         * @param activity current activity
         * @param colorResourceId color resource id
         * */
        fun setSystemNavigationBarColor(activity: AppCompatActivity, colorResourceId: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && colorResourceId > -1) {
                val window = activity.window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.navigationBarColor = getColor(colorResourceId)
            }
        }
    }
}