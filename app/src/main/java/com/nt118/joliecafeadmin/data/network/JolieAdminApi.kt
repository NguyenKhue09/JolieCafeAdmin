package com.nt118.joliecafeadmin.data.network

import com.nt118.joliecafeadmin.models.ApiResponseMultiData
import com.nt118.joliecafeadmin.models.Product
import com.nt118.joliecafeadmin.util.Constants.Companion.API_GATEWAY
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.QueryMap

interface JolieAdminApi {

    @GET("$API_GATEWAY/product/get-admin-products")
    suspend fun getProducts(
        @QueryMap productQuery: Map<String, String>,
        @Header("Authorization") token: String
    ): ApiResponseMultiData<Product>

}