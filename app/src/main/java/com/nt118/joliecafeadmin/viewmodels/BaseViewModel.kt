package com.nt118.joliecafeadmin.viewmodels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nt118.joliecafeadmin.data.DataStoreRepository
import com.nt118.joliecafeadmin.models.ApiResponseSingleData
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

    var adminToken = ""
    var networkStatus = false
    var backOnline = false

    init {
        viewModelScope.launch {
            readAdminToken.collectLatest { token ->
                println(token)
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
            Toast.makeText(getApplication(), "No Internet Connection", Toast.LENGTH_SHORT).show()
            saveBackOnline(true)
        } else if (networkStatus) {
            if (backOnline) {
                Toast.makeText(getApplication(), "We're back online", Toast.LENGTH_SHORT).show()
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
}