package com.nt118.joliecafeadmin.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nt118.joliecafeadmin.data.DataStoreRepository
import com.nt118.joliecafeadmin.data.Repository
import com.nt118.joliecafeadmin.models.MonthlyRevenue
import com.nt118.joliecafeadmin.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RevenueViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    dataStoreRepository: DataStoreRepository
) : BaseViewModel(application, dataStoreRepository) {

    val revenueResponse: MutableLiveData<ApiResult<List<MonthlyRevenue>>> = MutableLiveData()

    fun getRevenue() {
        viewModelScope.launch {
            revenueResponse.value = ApiResult.Loading()
            try {
                val response = repository.remote.getMonthlyRevenue(adminToken)
                revenueResponse.value = handleApiMultiResponse(response)
            } catch (e: Exception) {
                e.printStackTrace()
                revenueResponse.value = ApiResult.Error(e.message.toString())
            }
        }
    }

}