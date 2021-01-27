package com.app.messagealarm.ui.buy_pro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.messagealarm.R
import com.app.messagealarm.ui.main.add_apps.AddApplicationPresenter
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_buy_pro.*

class BuyProActivity : AppCompatActivity() {

    var buyProPresenter: BuyProPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_pro)
        setListener()
        buyProPresenter = BuyProPresenter()
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

    private fun takeToHome(purchased:Boolean){
        val intent = Intent(this, AlarmApplicationActivity::class.java)
        intent.putExtra(Constants.IntentKeys.IS_PURCHASED, purchased)
        startActivity(intent)
        finish()
    }

}
