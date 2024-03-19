package com.gatecontrol.app.network

import com.gatecontrol.app.util.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AuthApiCliente {
    private val BASE_URL = Constants.URL_CLOUD_FUNCTIONS

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getInstance() : AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

}

