package com.app.messagealarm.networking;

import com.app.messagealarm.BuildConfig;
import com.app.messagealarm.R;
import com.app.messagealarm.utils.DataUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.List;
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
        /**
         * if debug mode then use logging interceptor or if release mode don't use it
         */
        OkHttpClient client;
        if(BuildConfig.DEBUG){
             client = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();
        }else{
             client = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .build();
        }
        return new Retrofit.Builder()
                .baseUrl(DataUtils.Companion.getString(com.app.messagealarm.R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(client)
                .build();
    }

   // Use this method in order to get your IpAddress
   public static String getExternalIpAddress() throws Exception {
       URL whatismyip = new URL("http://checkip.amazonaws.com");
       BufferedReader in = null;
       try {
           in = new BufferedReader(new InputStreamReader(
                   whatismyip.openStream()));
           return in.readLine();
       }catch (Exception e){
           return "Unknown";
       }finally {
           if (in != null) {
               try {
                   in.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       }
   }

    /**
     * Get Retrofit In
     */

    private static Retrofit getRetrofitInstanceHeroku() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        /**
         * if debug mode then use logging interceptor or if release mode don't use it
         */
        OkHttpClient client;
        if(BuildConfig.DEBUG){
            client = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();
        }else{
            client = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .build();
        }
        return new Retrofit.Builder()
                .baseUrl(DataUtils.Companion.getString(com.app.messagealarm.R.string.base_url_heroku))
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

    /**
     * Get API Service Heroku
     * @return API Service Heroku
     */
    public static ApiService getApiServiceHeroku() {
        return getRetrofitInstanceHeroku().create(ApiService.class);
    }

}
