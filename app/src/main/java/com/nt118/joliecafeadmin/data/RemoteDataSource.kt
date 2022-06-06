package com.nt118.joliecafeadmin.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.nt118.joliecafeadmin.data.network.JolieAdminApi
import com.nt118.joliecafeadmin.data.paging_source.ProductPagingSource
import com.nt118.joliecafeadmin.models.ApiResponseMultiData
import com.nt118.joliecafeadmin.models.ApiResponseSingleData
import com.nt118.joliecafeadmin.models.MonthlyRevenue
import com.nt118.joliecafeadmin.models.Product
import com.nt118.joliecafeadmin.util.Constants.Companion.PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.QueryMap
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val jolieAdminApi: JolieAdminApi
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
}