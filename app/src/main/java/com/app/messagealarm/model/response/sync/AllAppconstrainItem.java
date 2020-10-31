package com.app.messagealarm.model.response.sync;

import com.google.gson.annotations.SerializedName;

public class AllAppconstrainItem{

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("lang_code")
	private String langCode;

	@SerializedName("description")
	private String description;

	@SerializedName("title")
	private String title;

	@SerializedName("create_at")
	private String createAt;

	@SerializedName("app_package_name")
	private String appPackageName;

	@SerializedName("is_constains")
	private boolean isContains;

	public boolean isContains() {
		return isContains;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}

	public String getDescription(){
		return description;
	}

	public String getTitle(){
		return title;
	}

	public String getCreateAt(){
		return createAt;
	}

	public String getLangCode() {
		return langCode;
	}

	public String getAppPackageName() {
		return appPackageName;
	}
}