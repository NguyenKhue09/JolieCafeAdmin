package com.nt118.joliecafeadmin.data.network

import com.nt118.joliecafeadmin.models.ApiResponseSingleData
import com.nt118.joliecafeadmin.models.PushNotification
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface FCMApi {

    @Headers("Content-Type: application/json")
    @POST("fcm/send")
    suspend fun sendNotification(
        @Body notificationData: PushNotification,
        @Header("Authorization") key: String
    ): Response<ApiResponseSingleData<Unit>>

}