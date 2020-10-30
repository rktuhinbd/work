package com.app.messagealarm.model.response;

import com.google.gson.annotations.SerializedName;

public class AllAppItem{

	@SerializedName("app_name")
	private String appName;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("app_package_name")
	private String appPackageName;

	@SerializedName("create_at")
	private String createAt;

	@SerializedName("app_id")
	private int appId;

	public String getAppName(){
		return appName;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}

	public String getAppPackageName(){
		return appPackageName;
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
			"AllAppItem{" + 
			"app_name = '" + appName + '\'' + 
			",updated_at = '" + updatedAt + '\'' + 
			",app_package_name = '" + appPackageName + '\'' + 
			",create_at = '" + createAt + '\'' + 
			",app_id = '" + appId + '\'' + 
			"}";
		}
}