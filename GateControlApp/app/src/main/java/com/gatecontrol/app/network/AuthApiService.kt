package com.gatecontrol.app.network

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApiService{

    @FormUrlEncoded
    @POST("send-number-phone")
    suspend fun sendNumberPhone(@Field("phone") phone:String, @Field("isLogin") isLogin:Boolean)
        : Response<Map<String, Any>>
}