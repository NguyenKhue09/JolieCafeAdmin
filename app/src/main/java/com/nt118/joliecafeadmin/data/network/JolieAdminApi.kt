package com.nt118.joliecafeadmin.data.network

import com.nt118.joliecafeadmin.models.ApiResponseMultiData
import com.nt118.joliecafeadmin.models.ApiResponseSingleData
import com.nt118.joliecafeadmin.models.Product
import com.nt118.joliecafeadmin.util.Constants.Companion.API_GATEWAY
import retrofit2.Response
import retrofit2.http.*

interface JolieAdminApi {

    @GET("$API_GATEWAY/product/get-admin-products")
    suspend fun getProducts(
        @QueryMap productQuery: Map<String, String>,
        @Header("Authorization") token: String
    ): ApiResponseMultiData<Product>

    @POST("$API_GATEWAY/product/get-admin-products")
    suspend fun updateProduct(
        @QueryMap newData: Map<String, Any>,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Product>>

    @GET("$API_GATEWAY/product/get-admin-product-detail")
    suspend fun getProductDetail(
        @Query("productId") productId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Product>>

}