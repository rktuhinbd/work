package com.app.messagealarm.model.response


import com.google.gson.annotations.SerializedName

data class TimeZone(
    @SerializedName("code")
    var code: String,
    @SerializedName("current_time")
    var currentTime: String,
    @SerializedName("gmt_offset")
    var gmtOffset: Int,
    @SerializedName("id")
    var id: String,
    @SerializedName("is_daylight_saving")
    var isDaylightSaving: Boolean
)