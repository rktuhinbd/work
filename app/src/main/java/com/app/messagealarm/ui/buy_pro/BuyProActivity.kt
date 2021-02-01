package com.app.messagealarm.ui.buy_pro

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.app.messagealarm.R
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_buy_pro.*
import kotlinx.android.synthetic.main.activity_buy_pro_new.*
import kotlin.math.abs


class BuyProActivity : AppCompatActivity() {

    var buyProPresenter: BuyProPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_pro_new)
        changeStatusBarColorInLightMode()
       // setListener()
        buyProPresenter = BuyProPresenter()
        appBarLayout?.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalOffset = appBarLayout.totalScrollRange
            image_king?.alpha = calculateAlpha(abs(totalOffset), abs(verticalOffset))
        })
        setListener()
    }

    private fun changeStatusBarColorInLightMode(){
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
    }

    private fun calculateAlpha(totalOffset:Int, currentOffset:Int) : Float{
        return (1.0 - ((currentOffset * 0.9999) / totalOffset)).toFloat()
    }

    private fun setListener(){
        btn_buy_pro_user?.setOnClickListener {
            setIsPurchased(true)
            finish()
        }

       txt_learn_more?.setOnClickListener {
           Toasty.error(this, "Your purchase is canceled!").show()
            setIsPurchased(false)
           buyProPresenter?.turnOfVibrateAndJustVibrateFromAllAddedApp()
            finish()
       }

        toolbar?.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
      finish()
    }

    private fun setIsPurchased(boolean: Boolean){
        SharedPrefUtils.write(Constants.PreferenceKeys.IS_PURCHASED, boolean)
    }

    private fun takeToHome(purchased: Boolean){
        val intent = Intent(this, AlarmApplicationActivity::class.java)
        intent.putExtra(Constants.IntentKeys.IS_PURCHASED, purchased)
        startActivity(intent)
        finish()
    }

}
