package com.app.messagealarm.ui.buy_pro

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.math.abs


class BuyProActivity : AppCompatActivity(), PurchasesUpdatedListener, BuyProView {

    private var billingClient: BillingClient? = null
    var buyProPresenter: BuyProPresenter? = null
    var flowParams: BillingFlowParams? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: ReviewAdapter


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
        recyclerView.adapter = mAdapter;


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


    private fun buyingProcess() {
        if (AndroidUtils.isOnline(this)) {
            progress_purchase?.visibility = View.VISIBLE
            checkPurchaseStatus()
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


    private fun isPurchased(): Boolean {
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
                    if (SharedPrefUtils.contains(Constants.PreferenceKeys.CURRENCY_CODE) &&
                        SharedPrefUtils.contains(Constants.PreferenceKeys.CURRENCY_SYMBOL)
                        && skuDetailsList[0].priceCurrencyCode ==
                        SharedPrefUtils.readString(Constants.PreferenceKeys.CURRENCY_CODE)
                    ) {
                        try {
                            btn_buy_pro_user?.text =
                                "Buy for ${skuDetailsList[0].price} " + SharedPrefUtils.readString(
                                    Constants.PreferenceKeys.CURRENCY_SYMBOL
                                )
                        } catch (e: Exception) {
                            btn_buy_pro_user?.text =
                                "Buy for ${skuDetailsList[0].price}"
                        }
                    } else {
                        btn_buy_pro_user?.text =
                            "Buy for ${skuDetailsList[0].price}"
                    }
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
                 // buyProPresenter?.verifyPurchase(this,purchase.originalJson, purchase.signature, purchase)
                verifyPurchaseStatus(true, purchase)
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
            VisitUrlUtils.visitWebsite(this, "https://www.mk7lab.com/Company/charity/")
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
                    if (flowParams != null) {
                        billingClient!!.launchBillingFlow(this, flowParams!!)
                    }
                } else {
                    billingClient =
                        BillingClient.newBuilder(this).enablePendingPurchases().setListener(this)
                            .build()
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

                        override fun onBillingServiceDisconnected() {

                        }
                    })
                }
            } else {
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

    private fun setIsPurchased(boolean: Boolean) {
        SharedPrefUtils.write(Constants.PreferenceKeys.IS_PURCHASED, boolean)
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
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

    override fun verifyPurchaseStatus(boolean: Boolean, purchase: Purchase?) {
        ProgressDialogUtils.on().hideProgressDialog()
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
            Toast.makeText(
                applicationContext,
                "Error : Invalid Purchase",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
    }

    /**
     * uzzal create a function for recycler view..
     */
    private fun recyclerInIt() {

        val layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL, false
        )
        recyclerView.layoutManager = layoutManager;
        val adapter = ReviewAdapter(this, generateReviewList());
        recyclerView.adapter = adapter;


    }

}
