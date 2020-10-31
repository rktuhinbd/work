package com.app.messagealarm.networking;

import com.app.messagealarm.model.response.TokenResponse;
import com.app.messagealarm.model.response.sync.SyncResponse;
import com.app.messagealarm.utils.Constants;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @Headers({Constants.API.ResponseFormat.JSON_RESPONSE})
    @FormUrlEncoded
    @POST(Constants.API.REGISTER_TOKEN)
    Call<TokenResponse> registerToken(@Field(Constants.API.Body.TOKEN) String token);


    @Headers({Constants.API.ResponseFormat.JSON_RESPONSE})
    @GET(Constants.API.SYNC)
    Call<SyncResponse> syncData(@Query(Constants.API.Body.APP_SIZE)int appSize,
                                @Query(Constants.API.Body.LANG_SIZE) int langSize,
                                @Query(Constants.API.Body.CONSTRAIN_SIZE) int constrainSize
                                );
}
