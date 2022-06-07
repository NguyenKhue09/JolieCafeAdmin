package com.nt118.joliecafeadmin.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nt118.joliecafeadmin.data.DataStoreRepository
import com.nt118.joliecafeadmin.data.Repository
import com.nt118.joliecafeadmin.models.BestSeller
import com.nt118.joliecafeadmin.models.MonthlyRevenue
import com.nt118.joliecafeadmin.models.WeeklyRevenue
import com.nt118.joliecafeadmin.models.YearlyRevenue
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
        const val YEARLY_REVENUE = 2
    }

    val monthlyRevenueResponse: MutableLiveData<ApiResult<List<MonthlyRevenue>>> = MutableLiveData()
    val weeklyRevenueResponse: MutableLiveData<ApiResult<List<WeeklyRevenue>>> = MutableLiveData()
    val yearlyRevenueResponse: MutableLiveData<ApiResult<List<YearlyRevenue>>> = MutableLiveData()
    val bestSellerResponse: MutableLiveData<ApiResult<List<BestSeller>>> = MutableLiveData()
    val selectedTab: MutableLiveData<Int> = MutableLiveData(WEEKLY_REVENUE)
    val totalRevenue: MutableLiveData<Int> = MutableLiveData(0)

    fun getYearlyRevenue() = viewModelScope.launch {
        yearlyRevenueResponse.value = ApiResult.Loading()
        try {
            val response = repository.remote.getYearlyRevenue(adminToken)
            yearlyRevenueResponse.value = handleApiMultiResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
            yearlyRevenueResponse.value = ApiResult.Error(e.message.toString())
        }
    }

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

    fun getCurrentYearRevenue() = viewModelScope.launch {
        try {
            val response = repository.remote.getCurrentYearRevenue(adminToken)
            totalRevenue.value = handleApiResponse(response).data ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentMonthRevenue() = viewModelScope.launch {
        try {
            val response = repository.remote.getCurrentMonthRevenue(adminToken)
            totalRevenue.value = handleApiResponse(response).data ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentWeekRevenue() = viewModelScope.launch {
        try {
            val response = repository.remote.getCurrentWeekRevenue(adminToken)
            totalRevenue.value = handleApiResponse(response).data ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
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