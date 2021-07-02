package com.app.messagealarm.model.response


import com.google.gson.annotations.SerializedName

data class Language(
    @SerializedName("code")
    var code: String,
    @SerializedName("name")
    var name: String,
    @SerializedName("native")
    var native: String
)