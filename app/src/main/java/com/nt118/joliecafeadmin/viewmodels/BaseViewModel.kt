package com.nt118.joliecafeadmin.viewmodels

import android.app.Application
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nt118.joliecafeadmin.data.DataStoreRepository
import com.nt118.joliecafeadmin.models.ApiResponseMultiData
import com.nt118.joliecafeadmin.models.ApiResponseSingleData
import com.nt118.joliecafeadmin.models.CommonFCMResponse
import com.nt118.joliecafeadmin.models.SingleFCMResponse
import com.nt118.joliecafeadmin.util.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.Response

open class BaseViewModel(
    application: Application,
    var dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application){

    val readBackOnline = dataStoreRepository.readBackOnline
    val readAdminToken = dataStoreRepository.readAdminToken

    val networkMessage = MutableLiveData<String>()

    var adminToken = ""
    var networkStatus = false
    var backOnline = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            readAdminToken.collectLatest { token ->
                println("Token: $token")
                adminToken = token
            }
        }
    }

    fun saveAdminToken(adminToken: String) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveAdminToken(userToken = adminToken)
        }

    private fun saveBackOnline(backOnline: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveBackOnline(backOnline)
        }

    fun showNetworkStatus() {
        if (!networkStatus) {
            //Toast.makeText(getApplication(), "No Internet Connection", Toast.LENGTH_SHORT).show()
            networkMessage.value = "No Internet Connection"
            saveBackOnline(true)
        } else if (networkStatus) {
            if (backOnline) {
                //Toast.makeText(getApplication(), "We're back online", Toast.LENGTH_SHORT).show()
                networkMessage.value = "We're back online"
                saveBackOnline(false)
            }
        }
    }

    fun <T> handleApiResponse(response: Response<ApiResponseSingleData<T>>): ApiResult<T> {
        val result = response.body()
        println(response)
        return when {
            response.message().toString().contains("timeout") -> {
                ApiResult.Error("Timeout")
            }
            response.code() == 500 -> {
                ApiResult.Error(response.message())
            }
            response.isSuccessful -> {
                ApiResult.Success(result?.data!!)
            }
            else -> {
                ApiResult.Error(response.message())
            }
        }
    }

    fun <T> handleApiMultiResponse(response: Response<ApiResponseMultiData<T>>): ApiResult<List<T>> {
        val result = response.body()
        println(response)
        return when {
            response.message().toString().contains("timeout") -> {
                ApiResult.Error("Timeout")
            }
            response.code() == 500 -> {
                ApiResult.Error(response.message())
            }
            response.isSuccessful -> {
                ApiResult.Success(result?.data!!)
            }
            else -> {
                ApiResult.Error(response.message())
            }
        }
    }

    fun <T> handleApiNullDataSuccessResponse(response: Response<ApiResponseSingleData<T>>): ApiResult<T> {
        println(response)
        return when {
            response.message().toString().contains("timeout") -> {
                ApiResult.Error("Timeout")
            }
            response.code() == 500 -> {
                ApiResult.Error(response.message())
            }
            response.isSuccessful -> {
                ApiResult.NullDataSuccess()
            }
            else -> {
                ApiResult.Error(response.message())
            }
        }
    }

    fun <T> handleFCMCommonApiResponse(response: Response<CommonFCMResponse>): ApiResult<T> {
        return when {
            response.message().toString().contains("timeout") -> {
                ApiResult.Error("Timeout")
            }
            response.code() == 500 -> {
                ApiResult.Error(response.message())
            }
            response.isSuccessful -> {
                ApiResult.NullDataSuccess()
            }
            else -> {
                ApiResult.Error(response.message())
            }
        }
    }

    fun <T> handleFCMSingleApiResponse(response: Response<SingleFCMResponse>): ApiResult<T> {
        return when {
            response.message().toString().contains("timeout") -> {
                ApiResult.Error("Timeout")
            }
            response.code() == 500 -> {
                ApiResult.Error(response.message())
            }
            response.isSuccessful -> {
                ApiResult.NullDataSuccess()
            }
            else -> {
                ApiResult.Error(response.message())
            }
        }
    }

}