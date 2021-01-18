package com.app.messagealarm.model.response.sync;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class SyncResponse{

	@SerializedName("all_appconstrain")
	private List<AllAppconstrainItem> allAppconstrain;

	@SerializedName("all_app")
	private List<AllAppItem> allApp;

	@SerializedName("success")
	private String success;

	@SerializedName("all_language")
	private List<AllLanguageItem> allLanguage;

	@SerializedName("message")
	private String message;

	public List<AllAppconstrainItem> getAllAppconstrain(){
		return allAppconstrain;
	}

	public List<AllAppItem> getAllApp(){
		return allApp;
	}

	public String getSuccess(){
		return success;
	}

	public List<AllLanguageItem> getAllLanguage(){
		return allLanguage;
	}

	public String getMessage(){
		return message;
	}

	@Override
 	public String toString(){
		return 
			"SyncResponse{" + 
			"all_appconstrain = '" + allAppconstrain + '\'' + 
			",all_app = '" + allApp + '\'' + 
			",success = '" + success + '\'' + 
			",all_language = '" + allLanguage + '\'' + 
			",message = '" + message + '\'' + 
			"}";
		}
}