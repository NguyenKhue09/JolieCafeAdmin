package com.nt118.joliecafeadmin.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nt118.joliecafeadmin.data.DataStoreRepository
import com.nt118.joliecafeadmin.data.Repository
import com.nt118.joliecafeadmin.models.AdminData
import com.nt118.joliecafeadmin.models.ApiResponseSingleData
import com.nt118.joliecafeadmin.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    dataStoreRepository: DataStoreRepository
) : BaseViewModel(application, dataStoreRepository) {
    var adminLogin: MutableLiveData<ApiResult<AdminData>> = MutableLiveData()


    fun loginAdmin(loginData: Map<String, String>) =
        viewModelScope.launch {
            adminLogin.value = ApiResult.Loading()
            try {
                val response = repository.remote.loginAdmin(loginData)
                adminLogin.value = handleLoginApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                adminLogin.value = ApiResult.Error(e.message)
            }

        }

    private fun handleLoginApiResponse(response: Response<ApiResponseSingleData<AdminData>>): ApiResult<AdminData> {
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