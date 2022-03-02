package com.app.messagealarm.model.response

import com.google.gson.annotations.SerializedName

data class RegisterResponse(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("success")
	val success: Boolean? = null
)

data class Data(

	/**
	 * Need alarm count, pro_dialog_shown_count, sound_level and variables
	 * that we track to update the user status
	 */

	@field:SerializedName("country")
	val country: String? = null,

	@field:SerializedName("isPaid")
	val isPaid: Boolean? = null,

	@field:SerializedName("isOfferClaim")
	val isOfferClaim: Boolean? = null,

	@field:SerializedName("isFreelauncer")
	val isFreelauncer: Boolean? = null,

	@field:SerializedName("timeZone")
	val timeZone: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("installTime")
	val installTime: Int? = null,

	@field:SerializedName("uuid")
	val uuid: String? = null,

	@field:SerializedName("token")
	val token: String? = null
)
