package com.app.messagealarm.model.response;

import com.google.gson.annotations.SerializedName;

public class LatestInfo{

	@SerializedName("success")
	private boolean success;

	@SerializedName("total_constraints")
	private int totalConstraints;

	@SerializedName("current_version")
	private String currentVersion;

	public boolean isSuccess(){
		return success;
	}

	public int getTotalConstraints(){
		return totalConstraints;
	}

	public String getCurrentVersion(){
		return currentVersion;
	}
}