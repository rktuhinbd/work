package com.app.messagealarm.model.response;

import com.google.gson.annotations.SerializedName;

public class UnknownAppResponse{

	@SerializedName("success")
	private boolean success;

	public boolean isSuccess(){
		return success;
	}
}