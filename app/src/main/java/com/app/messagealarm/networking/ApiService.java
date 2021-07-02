package com.app.messagealarm.networking;

import com.app.messagealarm.model.response.LatestInfo;
import com.app.messagealarm.model.response.TokenResponse;
import com.app.messagealarm.model.response.UnknownAppResponse;
import com.app.messagealarm.model.response.UserInfoGlobal;
import com.app.messagealarm.model.response.VerifyPurchaseResponse;
import com.app.messagealarm.model.response.sync.SyncResponse;
import com.app.messagealarm.utils.Constants;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiService {

    /**
     * @param country
     * @param token
     * @return
     */
    @Headers({Constants.API.ResponseFormat.JSON_RESPONSE})
    @FormUrlEncoded
    @POST(Constants.API.REGISTER_TOKEN)
    Call<TokenResponse> registerToken(@Field(Constants.API.Body.TOKEN) String token,
                                      @Field(Constants.API.Body.COUNTRY) String country
                                      );

    @Headers({Constants.API.ResponseFormat.JSON_RESPONSE})
    @GET(Constants.API.SYNC)
    Call<SyncResponse> syncData(@Query(Constants.API.Body.APP_SIZE)int appSize,
                                @Query(Constants.API.Body.LANG_SIZE) int langSize,
                                @Query(Constants.API.Body.CONSTRAIN_SIZE) int constrainSize,
                                @Query(Constants.API.Body.LANG_CODE) String langCode
                                );

    @Headers({Constants.API.ResponseFormat.JSON_RESPONSE})
    @FormUrlEncoded
    @POST(Constants.API.UNKNOWN_APP)
    Call<UnknownAppResponse> notifyUnknownApp(@Field(Constants.API.Body.APP_NAME)String appName,
                                              @Field(Constants.API.Body.PACKAGE_NAME) String packageName,
                                              @Field(Constants.API.Body.TOKEN) String token
                                              );

    /**
     * For version 2.0.2 we are adding firebase token in the api
     * @param receipt
     * @param signature
     * @return
     */
    @Headers({Constants.API.ResponseFormat.JSON_RESPONSE})
    @FormUrlEncoded
    @POST(Constants.API.VERIFY_PURCHASE)
    Call<VerifyPurchaseResponse> verifyPurchase(@Field(Constants.API.Body.RECEIPT)String receipt,
                                                @Field(Constants.API.Body.SIGNATURE) String signature,
                                                @Field(Constants.API.Body.USER_TOKEN) String token
                                                );
    /**
     *Register token for Heroku
     *
     */
    @Headers({Constants.API.ResponseFormat.JSON_RESPONSE})
    @FormUrlEncoded
    @POST(Constants.API.REGISTER_TOKEN)
    Call<TokenResponse> registerTokenForHeroku(@Field(Constants.API.Body.TOKEN) String token);

    /**
     * Get latest version info
     */
    @Headers({Constants.API.ResponseFormat.JSON_RESPONSE})
    @GET(Constants.API.LATEST_VERSION)
    Call<LatestInfo> getLatestVersion();

    /**
     * Update existing users token
     */
    @Headers({Constants.API.ResponseFormat.JSON_RESPONSE})
    @FormUrlEncoded
    @POST(Constants.API.UPDATE_TOKEN)
    Call<TokenResponse> updateCurrentToken(
            @Field(Constants.API.Body.USER_TOKEN) String userToken,
            @Field(Constants.API.Body.COUNTRY) String country,
            @Field(Constants.API.Body.IS_PAID) Boolean isPaid
    );

    /**
     * get user country
     */
    @Headers({Constants.API.ResponseFormat.JSON_RESPONSE})
    @GET
    Call<UserInfoGlobal> getCurrentUserInfo(@Url String url);
}
