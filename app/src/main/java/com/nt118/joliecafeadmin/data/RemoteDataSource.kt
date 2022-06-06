package com.nt118.joliecafeadmin.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.nt118.joliecafeadmin.data.network.FCMApi
import com.nt118.joliecafeadmin.data.network.JolieAdminApi
import com.nt118.joliecafeadmin.data.paging_source.ProductPagingSource
import com.nt118.joliecafeadmin.models.*
import com.nt118.joliecafeadmin.util.Constants.Companion.PAGE_SIZE
import com.nt118.joliecafeadmin.util.Constants.Companion.SERVER_KEY
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.QueryMap
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val jolieAdminApi: JolieAdminApi,
    private val fcmApi: FCMApi
) {

    fun getProducts(productQuery: Map<String, String>, token: String): Flow<PagingData<Product>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                ProductPagingSource(jolieAdminApi, "Bearer $token", productQuery)
            }
        ).flow
    }

    suspend fun updateProduct(
        newData: Map<String, String>,
        token: String
    ): Response<ApiResponseSingleData<Product>> {
        return jolieAdminApi.updateProduct(newData = newData, token = "Bearer $token")
    }

    suspend fun addNewProduct(
        productData: Map<String, String>,
        token: String
    ): Response<ApiResponseSingleData<Product>> {
        return jolieAdminApi.addNewProduct(productData = productData, token = "Bearer $token")
    }

    suspend fun getProductDetail(
        productId: String,
        token: String
    ): Response<ApiResponseSingleData<Product>> {
        return jolieAdminApi.getProductDetail(productId = productId, token = "Bearer $token")
    }

    suspend fun getMonthlyRevenue(
        token: String
    ): Response<ApiResponseMultiData<MonthlyRevenue>> {
        return jolieAdminApi.getMonthlyRevenue(token = "Bearer $token")
    }

    suspend fun getWeeklyRevenue(
        token: String
    ): Response<ApiResponseMultiData<WeeklyRevenue>> {
        return jolieAdminApi.getWeeklyRevenue(token = "Bearer $token")
    }

    suspend fun getBestSeller(): Response<ApiResponseMultiData<BestSeller>> {
        return jolieAdminApi.getBestSeller()
    }

    suspend fun addNewUserNotification(
        token: String,
        notificationData: Map<String, String>
    ): Response<ApiResponseSingleData<Unit>> {
        return jolieAdminApi.addNewUserNotification(token = "Bearer $token", notificationData = notificationData)
    }

    suspend fun addNewAdminNotification(
        token: String,
        notificationData: Map<String, String>
    ): Response<ApiResponseSingleData<Unit>> {
        return jolieAdminApi.addNewAdminNotification(token = "Bearer $token", notificationData = notificationData)
    }

    suspend fun sendNotification(
        key: String = SERVER_KEY,
        notificationData: PushNotification
    ): Response<ApiResponseSingleData<Unit>> {
        return fcmApi.sendNotification(key = "key=$key", notificationData = notificationData)
    }
}