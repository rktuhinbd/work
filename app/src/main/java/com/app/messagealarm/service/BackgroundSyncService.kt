package com.app.messagealarm.service

import com.app.messagealarm.model.response.SyncResponse
import com.app.messagealarm.networking.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BackgroundSyncService {
    companion object{
        fun syncData(appSize:Int, langSize:Int, constrainSize:Int){
                RetrofitClient.getApiService().syncData(appSize, langSize, constrainSize).enqueue(object
                    : Callback<SyncResponse> {
                    override fun onResponse(
                        call: Call<SyncResponse>,
                        response: Response<SyncResponse>
                    ) {
                        TODO("Not yet implemented")
                    }

                    override fun onFailure(call: Call<SyncResponse>, t: Throwable) {
                        TODO("Not yet implemented")
                    }

                })
        }
    }
}