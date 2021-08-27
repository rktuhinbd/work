package com.app.messagealarm.ui.buy_pro

import com.android.billingclient.api.Purchase

interface BuyProView {
    fun verifyPurchaseStatus(boolean: Boolean, purchase: Purchase?)
}