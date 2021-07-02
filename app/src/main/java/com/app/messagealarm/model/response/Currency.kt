package com.app.messagealarm.model.response


import com.google.gson.annotations.SerializedName

data class Currency(
    @SerializedName("code")
    var code: String,
    @SerializedName("name")
    var name: String,
    @SerializedName("plural")
    var plural: String,
    @SerializedName("symbol")
    var symbol: String,
    @SerializedName("symbol_native")
    var symbolNative: String
)