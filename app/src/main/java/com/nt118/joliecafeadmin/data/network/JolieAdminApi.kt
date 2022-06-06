package com.nt118.joliecafeadmin.data.network

import com.nt118.joliecafeadmin.models.*
import com.nt118.joliecafeadmin.util.Constants.Companion.API_GATEWAY
import retrofit2.Response
import retrofit2.http.*

interface JolieAdminApi {

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
    suspend fun getMonthlyRevenue(
        @Header("Authorization") token: String
    ): Response<ApiResponseMultiData<MonthlyRevenue>>

    @GET("$API_GATEWAY/revenue/weekly")
    suspend fun getWeeklyRevenue(
        @Header("Authorization") token: String
    ): Response<ApiResponseMultiData<WeeklyRevenue>>

    @GET("$API_GATEWAY/revenue/best-seller")
    suspend fun getBestSeller(): Response<ApiResponseMultiData<BestSeller>>
}