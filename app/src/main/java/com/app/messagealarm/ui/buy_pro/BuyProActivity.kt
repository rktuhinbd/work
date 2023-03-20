package com.app.messagealarm.ui.buy_pro

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import java.lang.Exception
import java.util.*
import kotlin.math.abs
import kotlin.math.round


/**
 * @author Al Mujahid Khan
 * Note: This class codes are un-stable and not fully tested, need to test and make the architecture better
 */
class BuyProActivity : AppCompatActivity(), PurchasesUpdatedListener, BuyProView {

    private var billingClient: BillingClient? = null
    var buyProPresenter: BuyProPresenter? = null
    //var flowParams: BillingFlowParams? = null
    var flowparamSubscription: BillingFlowParams? = null
    var flowparamSubscriptionYearly: BillingFlowParams? = null
    var offerString = "Subscribe Now"

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: ReviewAdapter
    var isYearlySubscription = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_pro_new)
        changeStatusBarColorInLightMode()
        recyclerView = findViewById(R.id.recycler_review_view)
        /**
         * uzzal code for recycler view..
         */
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        mAdapter = ReviewAdapter(this, generateReviewList());
        recyclerView.adapter = mAdapter
        /**
         * init views
         */
        txt_terms_condition?.movementMethod = LinkMovementMethod.getInstance()
        txt_privacy_policy?.movementMethod = LinkMovementMethod.getInstance()
        txt_privacy_policy?.setLinkTextColor(ContextCompat.getColor(this, R.color.colorAccent))
        txt_terms_condition?.setLinkTextColor(ContextCompat.getColor(this, R.color.colorAccent))


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


    /**
     * Initiate the state of In App Subscription for recurring payments
     */
    private fun initSubscriptionYearly() {
        isYearlySubscription = true
        //UI Changes
        card_subscription?.strokeWidth = ViewUtils.dpToPx(3).toInt()
        card_subscription?.setStrokeColor(
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    this,
                    R.color.purchase_stroke
                )
            )
        )
        card_in_app_purchase?.strokeWidth = 0
        card_in_app_purchase.setStrokeColor(null)
        btn_buy_pro_user?.text = offerString
        txt_package_details?.text = getString(R.string.txt_details_subscribe)
    }


    /**
     * Calculate the offer of yearly subscription
     */

    private fun calculateYearlyOffer(monthPrice: Double, yearPrice:Double) : Double {
        val monthPackageYearTotal = monthPrice * 12
        val offerAmount = monthPackageYearTotal - yearPrice
        return if(offerAmount <= 0){
            //no offer
            0.0
        }else{
            round((offerAmount * 100)/ monthPackageYearTotal)
        }
    }

    /**
     * Initiate the state of In App Purchase for a flat price
     */
    private fun initSubscriptionMonthly() {
        isYearlySubscription = false
        //UI Changes
        card_in_app_purchase?.strokeWidth = ViewUtils.dpToPx(3).toInt()
        card_in_app_purchase?.setStrokeColor(
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    this,
                    R.color.purchase_stroke
                )
            )
        )
        card_subscription?.strokeWidth = 0
        card_subscription?.setStrokeColor(null)
        btn_buy_pro_user?.text = getString(R.string.txt_subs_button)
        txt_package_details?.text = getString(R.string.txt_details_subscribe)
    }




    private fun buyingProcess() {
        if (AndroidUtils.isOnline(this)) {
            progress_purchase?.visibility = View.VISIBLE
            Thread{
                checkPurchaseStatus()
            }.start()
        } else {
            btn_buy_pro_user.text = "No Internet!"
            progress_purchase?.visibility = View.GONE
        }
    }

    private fun checkPurchaseStatus() {
        // Establish connection to billing client
        //check purchase status from google play store cache
        //to check if item already Purchased previously or refunded
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases().setListener(this).build()

        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        initiateInAppSubscription()
                    /**
                     * Check for subscription
                     */
                    billingClient!!.queryPurchasesAsync(
                        SkuType.SUBS
                    ) { p0, p1 ->
                        if (p1.size > 0) {
                            handleGenericPurchase(p1)
                        }/* else {
                            *//**
                             * Check for in-app-purchase
                             *//*
                                billingClient!!.queryPurchasesAsync(
                                    SkuType.INAPP
                                ) { pp0, pp1 ->
                                    if (pp1.size > 0) {
                                        handleInAppPurchase(pp1)
                                    } else {
                                        setIsPurchased(false)
                                    }
                                }
                        }*/
                    }



                }
            }
        })
    }


    private fun isPurchased(): Boolean {
        return SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_PURCHASED)
    }


/*
    @SuppressLint("SetTextI18n")
    private fun initiateInAppPurchase() {
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
                    //used for launching the payment flow
                    flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetailsList[0])
                        .build()
                    runOnUiThread {
                        //set price
                        if (SharedPrefUtils.contains(Constants.PreferenceKeys.CURRENCY_CODE) &&
                            SharedPrefUtils.contains(Constants.PreferenceKeys.CURRENCY_SYMBOL)
                            && skuDetailsList[0].priceCurrencyCode ==
                            SharedPrefUtils.readString(Constants.PreferenceKeys.CURRENCY_CODE)
                        ) {
                            try {
                                txt_in_app_price?.text =
                                    "Onetime Payment \n${skuDetailsList[0].price} " + SharedPrefUtils.readString(
                                        Constants.PreferenceKeys.CURRENCY_SYMBOL
                                    )
                            } catch (e: Exception) {
                                txt_in_app_price?.text =
                                    "Onetime Payment \n${skuDetailsList[0].price}"
                            }
                        } else {
                            txt_in_app_price?.text =
                                "Onetime Payment \n${skuDetailsList[0].price}"
                        }
                        progress_purchase?.visibility = View.GONE
                    }
                } else {
                    //try to add item/product id "purchase" inside managed product in google play console
                    runOnUiThread {
                        Toasty.error(
                            applicationContext,
                            "Purchase Item not Found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                runOnUiThread {
                    Toasty.error(
                        applicationContext,
                        " Error " + billingResult.debugMessage, Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

*/

    @SuppressLint("SetTextI18n")
    private fun initiateInAppSubscription() {
        val skuList: MutableList<String> =
            ArrayList()
        skuList.add(Constants.Purchase.SUBSCRIPTION_ID)
        skuList.add(Constants.Purchase.SUBSCRIPTION_YEARLY_ID)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(SkuType.SUBS)
        billingClient!!.querySkuDetailsAsync(
            params.build()
        ) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (skuDetailsList != null && skuDetailsList.size > 0) {
                    //used for launching the payment flow
                    flowparamSubscription = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetailsList[0])
                        .build()
                    flowparamSubscriptionYearly = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetailsList[1])
                        .build()
                    runOnUiThread {
                        //set offer percent
                            val offer = calculateYearlyOffer(
                                (skuDetailsList[0].priceAmountMicros / 1000000).toDouble(),
                                (skuDetailsList[1].priceAmountMicros / 1000000).toDouble())
                            if(offer > 0.0) {
                                btn_buy_pro_user?.text = "Subscribe Now - Save ${offer.toInt()}%"
                                offerString = "Subscribe Now - Save ${offer.toInt()}%"
                            }else{
                                btn_buy_pro_user?.text = "Subscribe Now"
                                offerString = "Subscribe Now"
                            }
                        /**
                         * Monthly
                         */
                        //set price
                        if (SharedPrefUtils.contains(Constants.PreferenceKeys.CURRENCY_CODE) &&
                            SharedPrefUtils.contains(Constants.PreferenceKeys.CURRENCY_SYMBOL)
                            && skuDetailsList[0].priceCurrencyCode ==
                            SharedPrefUtils.readString(Constants.PreferenceKeys.CURRENCY_CODE)
                        ) {
                            try {
                                txt_in_app_price?.text =
                                    "Subscribe For \n${skuDetailsList[0].price}/Mo"
                            } catch (e: Exception) {
                                txt_in_app_price?.text =
                                    "Subscribe For \n${skuDetailsList[0].price}/Mo"
                            }
                        } else {
                            txt_in_app_price?.text =
                                "Subscribe For \n${skuDetailsList[0].price}/Mo"
                        }

                        /**
                         * Yearly
                         */
                        //set price
                        if (SharedPrefUtils.contains(Constants.PreferenceKeys.CURRENCY_CODE) &&
                            SharedPrefUtils.contains(Constants.PreferenceKeys.CURRENCY_SYMBOL)
                            && skuDetailsList[1].priceCurrencyCode ==
                            SharedPrefUtils.readString(Constants.PreferenceKeys.CURRENCY_CODE)
                        ) {
                            try {
                                txt_subscription_price?.text =
                                    "Subscribe For \n${skuDetailsList[1].price}/Yr"
                            } catch (e: Exception) {
                                txt_subscription_price?.text =
                                    "Subscribe For \n${skuDetailsList[1].price}/Yr"
                            }
                        } else {
                            txt_subscription_price?.text =
                                "Subscribe For \n${skuDetailsList[1].price}/Yr"
                        }

                        progress_purchase?.visibility = View.GONE
                    }
                } else {
                    //try to add item/product id "purchase" inside managed product in google play console
                    runOnUiThread {
                        Toasty.error(
                            applicationContext,
                            "Purchase Item not Found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                runOnUiThread {
                    Toasty.error(
                        applicationContext,
                        " Error " + billingResult.debugMessage, Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


 /*   fun handleInAppPurchase(purchases: List<Purchase>) {
        for (purchase in purchases) {
            //if item is purchased
            if (Constants.Purchase.PRODUCT_ID == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                // buyProPresenter?.verifyPurchase(this,purchase.originalJson, purchase.signature, purchase)
            } else if (Constants.Purchase.PRODUCT_ID == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                runOnUiThread {
                    Toasty.success(
                        applicationContext,
                        "Your purchase is processing, Please wait a bit!", Toast.LENGTH_LONG
                    ).show()
                }
            } else if (Constants.Purchase.PRODUCT_ID == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                *//**
                 *refund request
                 *//*
                setIsPurchased(false)
                runOnUiThread {
                    Toasty.error(
                        applicationContext,
                        "Purchase Status Unknown",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }*/


 /*   fun handleInAppSubscription(purchases: List<Purchase>) {
        for (purchase in purchases) {
            //if item is purchased
            if (Constants.Purchase.SUBSCRIPTION_ID == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                buyProPresenter?.verifyPurchase(
                    this,
                    purchase.originalJson,
                    purchase.signature,
                    purchase
                )
            } else if (Constants.Purchase.SUBSCRIPTION_ID == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                runOnUiThread {
                    Toasty.success(
                        applicationContext,
                        "Your purchase is processing, Please wait a bit!", Toast.LENGTH_LONG
                    ).show()
                }
            } else if (Constants.Purchase.SUBSCRIPTION_ID == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                *//**
                 *refund request
                 *//*
                setIsPurchased(false)
                runOnUiThread {
                    Toasty.error(
                        applicationContext,
                        "Purchase Status Unknown",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
*/

    fun handleGenericPurchase(purchases: List<Purchase>) {
        for (purchase in purchases) {
            //if item is purchased
            when (purchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    buyProPresenter?.verifyPurchase(
                        this,
                        purchase.originalJson,
                        purchase.signature,
                        purchase
                    )
                }
                Purchase.PurchaseState.PENDING -> {
                    runOnUiThread {
                        Toasty.success(
                            applicationContext,
                            "Your purchase is processing, Please wait a bit!", Toast.LENGTH_LONG
                        ).show()
                    }
                }
                Purchase.PurchaseState.UNSPECIFIED_STATE -> {
                    /**
                     *refund request
                     */
                    /**
                     *refund request
                     */
                    /**
                     *refund request
                     */
                    setIsPurchased(false)
                    runOnUiThread {
                        Toasty.error(
                            applicationContext,
                            "Purchase Status Unknown",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
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


    private fun changeStatusBarColorInLightMode() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
    }

    private fun calculateAlpha(totalOffset: Int, currentOffset: Int): Float {
        return (1.0 - ((currentOffset * 0.9999) / totalOffset)).toFloat()
    }

    private fun setListener() {
        txt_learn_more?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("click_on_learn_more", "yes")
            firebaseAnalytics.logEvent("click_on_learn_more", bundle)
            VisitUrlUtils.visitWebsite(
                this, /*"https://www.mk7lab.com/Company/charity/"*/
                "https://onetakameal.org/"
            )
        }

        card_in_app_purchase?.setOnClickListener {
            initSubscriptionMonthly()
        }

        card_subscription?.setOnClickListener {
            initSubscriptionYearly()
        }

        /**
         * @exception We ignored it
         */
        /*  txt_restore_purchase?.setOnClickListener {
              val bundle = Bundle()
              bundle.putString("click_on_restore_purchase", "yes")
              firebaseAnalytics.logEvent("restore_purchase", bundle)
              billingClient?.queryPurchaseHistoryAsync("inapp", object : PurchaseHistoryResponseListener{
                  override fun onPurchaseHistoryResponse(
                      p0: BillingResult,
                      p1: MutableList<PurchaseHistoryRecord>?
                  ) {
                      if(p1 != null && p1.size > 0){
                          for(item in p1){
                              buyProPresenter?.verifyPurchase(
                                  this@BuyProActivity,item.originalJson,
                                  item.signature, null)
                              break
                          }
                      }else{
                          Toasty.error(this@BuyProActivity, "No previous purchase found!").show()
                      }
                  }

              })
          }*/

        btn_buy_pro_user?.setOnClickListener {
            if (AndroidUtils.isOnline(this)) {
                val bundle = Bundle()
                bundle.putString("click_on_buy_button", "yes")
                firebaseAnalytics.logEvent("click_on_buy_button", bundle)
                if (billingClient?.isReady!!) {
                    if(isYearlySubscription){
                        if (flowparamSubscriptionYearly != null) {
                            billingClient!!.launchBillingFlow(this, flowparamSubscriptionYearly!!)
                        }
                    }else{
                        if (flowparamSubscription != null) {
                            billingClient!!.launchBillingFlow(this, flowparamSubscription!!)
                        }
                    }
                } else {
                    billingClient =
                        BillingClient.newBuilder(this).enablePendingPurchases().setListener(this)
                            .build()
                    billingClient!!.startConnection(object : BillingClientStateListener {
                        override fun onBillingSetupFinished(billingResult: BillingResult) {
                            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                /**
                                 * Have to test this part wisely
                                 */
                              //  initiateInAppPurchase()
                                initiateInAppSubscription()
                            } else {
                                Toasty.error(
                                    applicationContext,
                                    "Error " + billingResult.debugMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onBillingServiceDisconnected() {

                        }
                    })
                }
            } else {
                Toasty.error(this, "No Internet!").show()
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

    private fun setIsPurchased(boolean: Boolean) {
        SharedPrefUtils.write(Constants.PreferenceKeys.IS_PURCHASED, boolean)
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        //if item newly purchased
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handleGenericPurchase(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            /**
             *  val queryAlreadyPurchasesResult =
            billingClient!!.queryPurchases(SkuType.INAPP)
            val alreadyPurchases =
            queryAlreadyPurchasesResult.purchasesList
            alreadyPurchases?.let { handlePurchases(it) }
             */
            /**
             * The below code is replaced for Billing library 4
             */

            /**
             * Handle already owned subscription
             */
            billingClient!!.queryPurchasesAsync(
                SkuType.SUBS
            ) { p0, p1 ->
                handleGenericPurchase(p1)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toasty.error(applicationContext, "Purchase Canceled", Toast.LENGTH_LONG).show()
        } else {
            Toasty.error(
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

    override fun verifyPurchaseStatus(boolean: Boolean, purchase: Purchase?) {
        runOnUiThread {
            ProgressDialogUtils.on().hideProgressDialog()
        }
        if (boolean) {
            //complete purchase
            //if item is purchased and not acknowledged
            if (purchase != null) {
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
                        SharedPrefUtils.write(Constants.PreferenceKeys.WAS_SUBSCRIBED, true)
                        finish()
                    }
                }
            } else {
                //restore purchase that's why purchase is null
                if (!isPurchased()) {
                    val bundle = Bundle()
                    bundle.putString("restore_purchased", "yes")
                    firebaseAnalytics.logEvent("restore_purchase", bundle)
                    setIsPurchased(true)
                    finish()
                }
            }
        } else {
            val bundle = Bundle()
            bundle.putString("invalid_purchase", "yes")
            firebaseAnalytics.logEvent("invalid_purchase", bundle)
            // Invalid purchase
            // show error to user
            Toasty.error(
                applicationContext,
                "Error : Invalid Purchase",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
    }


}
