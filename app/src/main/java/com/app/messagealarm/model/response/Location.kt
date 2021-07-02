package com.app.messagealarm.model.response


import com.google.gson.annotations.SerializedName

data class Location(
    @SerializedName("calling_code")
    var callingCode: String,
    @SerializedName("capital")
    var capital: String,
    @SerializedName("country_flag")
    var countryFlag: String,
    @SerializedName("country_flag_emoji")
    var countryFlagEmoji: String,
    @SerializedName("country_flag_emoji_unicode")
    var countryFlagEmojiUnicode: String,
    @SerializedName("geoname_id")
    var geonameId: Int,
    @SerializedName("is_eu")
    var isEu: Boolean,
    @SerializedName("languages")
    var languages: List<Language>
)