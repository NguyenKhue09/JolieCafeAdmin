package com.nt118.joliecafeadmin.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nt118.joliecafeadmin.data.DataStoreRepository
import com.nt118.joliecafeadmin.data.Repository
import com.nt118.joliecafeadmin.models.BestSeller
import com.nt118.joliecafeadmin.models.MonthlyRevenue
import com.nt118.joliecafeadmin.models.WeeklyRevenue
import com.nt118.joliecafeadmin.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RevenueViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    dataStoreRepository: DataStoreRepository
) : BaseViewModel(application, dataStoreRepository) {

    companion object {
        const val WEEKLY_REVENUE = 0
        const val MONTHLY_REVENUE = 1
    }

    val monthlyRevenueResponse: MutableLiveData<ApiResult<List<MonthlyRevenue>>> = MutableLiveData()
    val weeklyRevenueResponse: MutableLiveData<ApiResult<List<WeeklyRevenue>>> = MutableLiveData()
    val bestSellerResponse: MutableLiveData<ApiResult<List<BestSeller>>> = MutableLiveData()
    val selectedTab: MutableLiveData<Int> = MutableLiveData(WEEKLY_REVENUE)

    fun getMonthlyRevenue() {
        viewModelScope.launch {
            monthlyRevenueResponse.value = ApiResult.Loading()
            try {
                val response = repository.remote.getMonthlyRevenue(adminToken)
                monthlyRevenueResponse.value = handleApiMultiResponse(response)
            } catch (e: Exception) {
                e.printStackTrace()
                monthlyRevenueResponse.value = ApiResult.Error(e.message.toString())
            }
        }
    }

    fun getWeeklyRevenue() = viewModelScope.launch {
        weeklyRevenueResponse.value = ApiResult.Loading()
        try {
            val response = repository.remote.getWeeklyRevenue(adminToken)
            weeklyRevenueResponse.value = handleApiMultiResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
            weeklyRevenueResponse.value = ApiResult.Error(e.message.toString())
        }
    }

    fun getBestSeller() = viewModelScope.launch {
        bestSellerResponse.value = ApiResult.Loading()
        try {
            val response = repository.remote.getBestSeller()
            bestSellerResponse.value = handleApiMultiResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
            bestSellerResponse.value = ApiResult.Error(e.message.toString())
        }
    }

}