package com.app.messagealarm.networking;

import com.app.messagealarm.R;
import com.app.messagealarm.utils.DataUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static android.os.Build.VERSION_CODES.R;

public class RetrofitClient {

    /**
     * Get Retrofit Instance
     */
    private static Retrofit getRetrofitInstance() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                /*.addInterceptor(new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY))*/
                .build();
        return new Retrofit.Builder()
                .baseUrl(DataUtils.Companion.getString(com.app.messagealarm.R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(client)
                .build();
    }

    /**
     * Get API Service
     *
     * @return API Service
     */
    public static ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);
    }
}
