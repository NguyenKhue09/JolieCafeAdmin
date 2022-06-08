package com.nt118.joliecafeadmin.data.network

import com.nt118.joliecafeadmin.models.*
import com.nt118.joliecafeadmin.util.Constants.Companion.API_GATEWAY
import retrofit2.Response
import retrofit2.http.*

interface JolieAdminApi {


    @POST("$API_GATEWAY/admin/login-admin")
    suspend fun loginAdmin(
        @Body loginData: Map<String, String>
    ): Response<ApiResponseSingleData<AdminData>>

    @GET("$API_GATEWAY/product/get-admin-products")
    suspend fun getProducts(
        @QueryMap productQuery: Map<String, String>,
        @Header("Authorization") token: String
    ): ApiResponseMultiData<Product>

    @Headers("Content-Type: application/json")
    @POST("$API_GATEWAY/product/admin-update-product")
    suspend fun updateProduct(
        @Body newData: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Product>>

    @Headers("Content-Type: application/json")
    @POST("$API_GATEWAY/product/create-new-product")
    suspend fun addNewProduct(
        @Body productData: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Product>>

    @GET("$API_GATEWAY/product/get-admin-product-detail")
    suspend fun getProductDetail(
        @Query("productId") productId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Product>>

    @GET("$API_GATEWAY/revenue/yearly")
    suspend fun getYearlyRevenue(
        @Header("Authorization") token: String
    ): Response<ApiResponseMultiData<YearlyRevenue>>

    @GET("$API_GATEWAY/revenue/monthly")
    suspend fun getMonthlyRevenue(
        @Header("Authorization") token: String
    ): Response<ApiResponseMultiData<MonthlyRevenue>>

    @GET("$API_GATEWAY/revenue/weekly")
    suspend fun getWeeklyRevenue(
        @Header("Authorization") token: String
    ): Response<ApiResponseMultiData<WeeklyRevenue>>

    @GET("$API_GATEWAY/revenue/best-seller")
    suspend fun getBestSeller(): Response<ApiResponseMultiData<BestSeller>>

    @GET("$API_GATEWAY/revenue/current-week")
    suspend fun getCurrentWeekRevenue(
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Int>>

    @GET("$API_GATEWAY/revenue/current-month")
    suspend fun getCurrentMonthRevenue(
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Int>>

    @GET("$API_GATEWAY/revenue/current-year")
    suspend fun getCurrentYearRevenue(
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Int>>

    @Headers("Content-Type: application/json")
    @POST("$API_GATEWAY/notification/create-new-user-notification")
    suspend fun addNewUserNotification(
        @Body notificationData: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Unit>>

    @Headers("Content-Type: application/json")
    @POST("$API_GATEWAY/notification/create-new-admin-notification")
    suspend fun addNewAdminNotification(
        @Body notificationData: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Unit>>

    @GET("$API_GATEWAY/notification/get-admin-notification")
    suspend fun getNotification(
        @QueryMap notificationQuery: Map<String, String>,
        @Header("Authorization") token: String
    ): ApiResponseMultiData<Notification>

    @GET("$API_GATEWAY/notification/get-detail")
    suspend fun getNotificationDetail(
        @Query("notificationId") notificationId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Notification>>

    // Voucher API
    @POST("$API_GATEWAY/voucher/create-new-voucher")
    suspend fun addNewVoucher(
        @Body voucherData: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Voucher>>

    @GET("$API_GATEWAY/voucher/get-admin-voucher")
    suspend fun getAllVouchers(
        @Header("Authorization") token: String
    ): Response<ApiResponseMultiData<Voucher>>
    // End of Voucher API

    @Headers("Content-Type: application/json")
    @PUT("$API_GATEWAY/notification/update")
    suspend fun updateNotification(
        @Body notificationData: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Notification>>
}