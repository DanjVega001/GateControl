package com.gatecontrol.app.network

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface GateApiService {

    @FormUrlEncoded
    @POST("open-gate")
    suspend fun openGate(@Field("user_id") userID:String, @Field("gate_id") gateID:String)
            : Response<Map<String, Any>>

    @FormUrlEncoded
    @POST("close-gate")
    suspend fun closeGate(@Field("user_id") userID:String, @Field("gate_id") gateID:String)
            : Response<Map<String, Any>>
}