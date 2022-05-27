package com.nt118.joliecafeadmin.viewmodels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nt118.joliecafeadmin.data.DataStoreRepository
import com.nt118.joliecafeadmin.models.ApiResponseSingleData
import com.nt118.joliecafeadmin.util.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

open class BaseViewModel(
    application: Application,
    var dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application){

    val readBackOnline by lazy { dataStoreRepository.readBackOnline }
    val readUserToken by lazy { dataStoreRepository.readAdminToken }


    var adminToken = ""

    var networkStatus = false
    var backOnline = false

    private fun saveUserToken(userToken: String) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveAdminToken(userToken = userToken)
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
}