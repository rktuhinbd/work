package com.app.messagealarm.model.response;

import com.google.gson.annotations.SerializedName;

public class VerifyPurchaseResponse {

    @SerializedName("success")
    private boolean success;

    public boolean isSuccess(){
        return success;
    }

}
