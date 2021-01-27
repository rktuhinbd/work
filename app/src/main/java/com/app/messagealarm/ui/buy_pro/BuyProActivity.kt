package com.app.messagealarm.ui.buy_pro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.app.messagealarm.R
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_buy_pro.*
import kotlinx.android.synthetic.main.activity_buy_pro_new.*


class BuyProActivity : AppCompatActivity() {

    var buyProPresenter: BuyProPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_pro_new)
       // setListener()
        buyProPresenter = BuyProPresenter()

        appBarLayout?.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
            var totalOffset = appBarLayout.totalScrollRange
            Log.e("APP_BAR", "OFF_SET ${calculateAlpha(Math.abs(totalOffset), Math.abs(verticalOffset))}")
            image_king?.alpha = calculateAlpha(Math.abs(totalOffset), Math.abs(verticalOffset))
        })
    }

    private fun calculateAlpha(totalOffset:Int, currentOffset:Int) : Float{
        return (1.0 - ((currentOffset * 0.9999) / totalOffset)).toFloat()
    }

    private fun setListener(){
        btn_buy_pro?.setOnClickListener {
            Toasty.success(this, "Thanks for purchase! You are now pro user!").show()
            setIsPurchased(true)
            finish()
        }

        btn_cancel_pro?.setOnClickListener {
            Toasty.error(this, "Your purchase is canceled!").show()
            setIsPurchased(false)
            buyProPresenter?.turnOfVibrateAndJustVibrateFromAllAddedApp()
            finish()
        }
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
