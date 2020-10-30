package com.app.messagealarm.model.response;

import com.google.gson.annotations.SerializedName;

public class AllAppconstrainItem{

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("lang_id")
	private int langId;

	@SerializedName("description")
	private String description;

	@SerializedName("title")
	private String title;

	@SerializedName("create_at")
	private String createAt;

	@SerializedName("app_id")
	private int appId;

	public String getUpdatedAt(){
		return updatedAt;
	}

	public int getLangId(){
		return langId;
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

	public int getAppId(){
		return appId;
	}

	@Override
 	public String toString(){
		return 
			"AllAppconstrainItem{" + 
			"updated_at = '" + updatedAt + '\'' + 
			",lang_id = '" + langId + '\'' + 
			",description = '" + description + '\'' + 
			",title = '" + title + '\'' + 
			",create_at = '" + createAt + '\'' + 
			",app_id = '" + appId + '\'' + 
			"}";
		}
}