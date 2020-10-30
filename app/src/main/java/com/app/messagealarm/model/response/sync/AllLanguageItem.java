package com.app.messagealarm.model.response.sync;

import com.google.gson.annotations.SerializedName;

public class AllLanguageItem{

	@SerializedName("lang_name")
	private String langName;

	@SerializedName("lang_code")
	private String langCode;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("lang_id")
	private int langId;

	@SerializedName("create_at")
	private String createAt;

	public String getLangName(){
		return langName;
	}

	public String getLangCode(){
		return langCode;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}

	public int getLangId(){
		return langId;
	}

	public String getCreateAt(){
		return createAt;
	}

	@Override
 	public String toString(){
		return 
			"AllLanguageItem{" + 
			"lang_name = '" + langName + '\'' + 
			",lang_code = '" + langCode + '\'' + 
			",updated_at = '" + updatedAt + '\'' + 
			",lang_id = '" + langId + '\'' + 
			",create_at = '" + createAt + '\'' + 
			"}";
		}
}