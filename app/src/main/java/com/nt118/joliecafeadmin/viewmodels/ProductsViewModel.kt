package com.nt118.joliecafeadmin.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nt118.joliecafeadmin.data.DataStoreRepository
import com.nt118.joliecafeadmin.data.Repository
import com.nt118.joliecafeadmin.models.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    dataStoreRepository: DataStoreRepository
) : BaseViewModel(application, dataStoreRepository) {

    private var _tabSelected = MutableLiveData<String>()
    val tabSelected: LiveData<String> = _tabSelected


    fun getProducts(
        productQuery: Map<String, String>
    ): Flow<PagingData<Product>> {
        return if (adminToken.isNotEmpty()) {
            try {
                repository.remote.getProducts(
                    productQuery = productQuery,
                    token = adminToken
                ).cachedIn(viewModelScope)
            } catch (e: Exception) {
                e.printStackTrace()
                flowOf(PagingData.empty())
            }
        } else {
            println("Token empty")
            flowOf(PagingData.empty())
        }
    }

    fun setTabSelected(tab: String) {
        _tabSelected.value = tab
    }
}