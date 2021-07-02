package com.app.messagealarm.model.response


import com.google.gson.annotations.SerializedName

data class UserInfoGlobal(
    @SerializedName("city")
    var city: String,
    @SerializedName("continent_code")
    var continentCode: String,
    @SerializedName("continent_name")
    var continentName: String,
    @SerializedName("country_code")
    var countryCode: String,
    @SerializedName("country_name")
    var countryName: String,
    @SerializedName("currency")
    var currency: Currency,
    @SerializedName("ip")
    var ip: String,
    @SerializedName("latitude")
    var latitude: Double,
    @SerializedName("location")
    var location: Location,
    @SerializedName("longitude")
    var longitude: Double,
    @SerializedName("region_code")
    var regionCode: String,
    @SerializedName("region_name")
    var regionName: String,
    @SerializedName("time_zone")
    var timeZone: TimeZone,
    @SerializedName("type")
    var type: String,
    @SerializedName("zip")
    var zip: String
)