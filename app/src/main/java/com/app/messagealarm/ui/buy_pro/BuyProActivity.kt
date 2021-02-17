package com.app.messagealarm.ui.buy_pro

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.SkuType
import com.app.messagealarm.R
import com.app.messagealarm.utils.*
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_buy_pro_new.*
import java.io.IOException
import java.util.*
import kotlin.math.abs


class BuyProActivity : AppCompatActivity(), PurchasesUpdatedListener, BuyProView {

    private var billingClient: BillingClient? = null
    var buyProPresenter: BuyProPresenter? = null
    var flowParams:BillingFlowParams? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_pro_new)
        changeStatusBarColorInLightMode()
        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics
        //log about screen open log
        val bundle = Bundle()
        bundle.putString("open_buy_page", "yes")
        firebaseAnalytics.logEvent("open_buy_page", bundle)
       // setListener()
        buyProPresenter = BuyProPresenter(this, firebaseAnalytics)
        appBarLayout?.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalOffset = appBarLayout.totalScrollRange
            image_king?.alpha = calculateAlpha(abs(totalOffset), abs(verticalOffset))
        })
        setListener()
        buyingProcess()
    }

    override fun onStart() {
        super.onStart()

    }


    private fun buyingProcess(){
        if(AndroidUtils.isOnline(this)){
            progress_purchase?.visibility = View.VISIBLE
            checkPurchaseStatus()
        }else{
            btn_buy_pro_user.text = "No Internet!"
            progress_purchase?.visibility = View.GONE
        }
    }

    private fun checkPurchaseStatus(){
        // Establish connection to billing client
        //check purchase status from google play store cache
        //to check if item already Purchased previously or refunded
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases().setListener(this).build()
        billingClient!!.startConnection(object : BillingClientStateListener{
            override fun onBillingServiceDisconnected() {

            }
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    initiatePurchase()
                    val queryPurchase = billingClient!!.queryPurchases(SkuType.INAPP)
                    val queryPurchases =
                        queryPurchase.purchasesList
                    if (queryPurchases != null && queryPurchases.size > 0) {
                        handlePurchases(queryPurchases)
                    } else {
                        setIsPurchased(false)
                    }

                }
            }
        })
    }


    private fun isPurchased(): Boolean{
        return SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)
    }

    @SuppressLint("SetTextI18n")
    private fun initiatePurchase() {
        val skuList: MutableList<String> =
            ArrayList()
        skuList.add(Constants.Purchase.PRODUCT_ID)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(SkuType.INAPP)
        billingClient!!.querySkuDetailsAsync(
            params.build()
        ) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (skuDetailsList != null && skuDetailsList.size > 0) {
                   flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetailsList[0])
                        .build()
                    //set price
                    btn_buy_pro_user?.text =
                        "Buy For ${skuDetailsList[0].price}"
                    progress_purchase?.visibility = View.GONE
                } else {
                    //try to add item/product id "purchase" inside managed product in google play console
                    Toast.makeText(
                        applicationContext,
                        "Purchase Item not Found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    " Error " + billingResult.debugMessage, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun handlePurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            //if item is purchased
            if (Constants.Purchase.PRODUCT_ID == purchase.sku && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                buyProPresenter?.verifyPurchase(purchase.originalJson, purchase.signature, purchase)
            } else if (Constants.Purchase.PRODUCT_ID == purchase.sku && purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                Toast.makeText(
                    applicationContext,
                    "Purchase is Pending. Please complete Transaction", Toast.LENGTH_SHORT
                ).show()
            } else if (Constants.Purchase.PRODUCT_ID == purchase.sku && purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                setIsPurchased(false)
                Toast.makeText(
                    applicationContext,
                    "Purchase Status Unknown",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    var ackPurchase =
        AcknowledgePurchaseResponseListener { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                //if purchase is acknowledged
                // Grant entitlement to the user. and restart activity
                setIsPurchased(true)
                finish()
            }
        }


    private fun changeStatusBarColorInLightMode(){
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
    }

    private fun calculateAlpha(totalOffset:Int, currentOffset:Int) : Float{
        return (1.0 - ((currentOffset * 0.9999) / totalOffset)).toFloat()
    }

    private fun setListener(){
        txt_learn_more?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("click_on_learn_more", "yes")
            firebaseAnalytics.logEvent("click_on_learn_more", bundle)
            VisitUrlUtils.visitWebsite(this, "https://www.mk7lab.com/charity")
        }

        btn_buy_pro_user?.setOnClickListener {
            if(AndroidUtils.isOnline(this)){
                val bundle = Bundle()
                bundle.putString("click_on_buy_button", "yes")
                firebaseAnalytics.logEvent("click_on_buy_button", bundle)
                if(billingClient?.isReady!!){
                    if(flowParams != null){
                        billingClient!!.launchBillingFlow(this, flowParams!!)
                    }
                }else{
                    Toasty.success(this, "here").show()
                    billingClient =
                        BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build()
                    billingClient!!.startConnection(object : BillingClientStateListener {
                        override fun onBillingSetupFinished(billingResult: BillingResult) {
                            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                initiatePurchase()
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "Error " + billingResult.debugMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onBillingServiceDisconnected() {}
                    })
                }
            }else{
                Toasty.info(this, "No Internet!").show()
                checkPurchaseStatus()
            }

        }

        toolbar?.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
      finish()
    }

    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     *
     * Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     *
     */
    private fun verifyValidSignature(
        signedData: String,
        signature: String
    ): Boolean {
        return try {
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
            val base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArYJvmLyxsJEJTWGhsAoJJCtDRYHk6iM94tsW6U61xkDnbmeyJzi43bhE8clftwbeWhsg67tbjHi1KgvV17Nn+jRbCSGvrkkRY9l9Uz2FdfiSC3UD7Lh9RGc7ZU0zy93Acj6ELvg71B+vZCm/wlZ2rPtaSpE+nhm+fJh887RReb5Rv1a69EFc8pq7IvVdeTVOVABD22ZELTciyM3BybasAwrzcKQ9FbUKdVuDm5Lzq+AlktXea95Wuhfh4NA82zk3uYO5xsXeFWhV9+uboewYGwADrrm+3Y7LmuMldOiDONScwDkOPdayiWRKGqFGAWYxs9udFWUGIzQ0HVycuT9F0wIDAQAB"
            Security.verifyPurchase(base64Key, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }

    private fun setIsPurchased(boolean: Boolean){
        SharedPrefUtils.write(Constants.PreferenceKeys.IS_PURCHASED, boolean)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        //if item newly purchased
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            val queryAlreadyPurchasesResult =
                billingClient!!.queryPurchases(SkuType.INAPP)
            val alreadyPurchases =
                queryAlreadyPurchasesResult.purchasesList
            alreadyPurchases?.let { handlePurchases(it) }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(applicationContext, "Purchase Canceled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                applicationContext,
                "Error " + billingResult.debugMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (billingClient != null) {
            billingClient!!.endConnection()
        }
    }

    override fun verifyPurchaseStatus(boolean: Boolean, purchase: Purchase) {
       if(boolean){
           //complete purchase
           //if item is purchased and not acknowledged
           if (!purchase.isAcknowledged) {
               val acknowledgePurchaseParams =
                   AcknowledgePurchaseParams.newBuilder()
                       .setPurchaseToken(purchase.purchaseToken)
                       .build()
               billingClient!!.acknowledgePurchase(acknowledgePurchaseParams, ackPurchase)
           } else {
               // Grant entitlement to the user on item purchase
               // restart activity
               if (!isPurchased()) {
                   val bundle = Bundle()
                   bundle.putString("item_sold", "yes")
                   firebaseAnalytics.logEvent("item_sold", bundle)
                   setIsPurchased(true)
                   finish()
               }
           }
       }else{
           val bundle = Bundle()
           bundle.putString("invalid_purchase", "yes")
           firebaseAnalytics.logEvent("invalid_purchase", bundle)
           // Invalid purchase
           // show error to user
           Toast.makeText(
               applicationContext,
               "Error : Invalid Purchase",
               Toast.LENGTH_SHORT
           ).show()
           return
       }
    }

}
