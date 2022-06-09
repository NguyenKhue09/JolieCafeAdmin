package com.nt118.joliecafeadmin.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nt118.joliecafeadmin.data.DataStoreRepository
import com.nt118.joliecafeadmin.data.Repository
import com.nt118.joliecafeadmin.models.Voucher
import com.nt118.joliecafeadmin.models.VoucherFormState
import com.nt118.joliecafeadmin.use_cases.VoucherFormValidationUseCase
import com.nt118.joliecafeadmin.util.ApiResult
import com.nt118.joliecafeadmin.util.Constants
import com.nt118.joliecafeadmin.util.VoucherFormStateEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VouchersViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val voucherFormValidationUseCase: VoucherFormValidationUseCase,
    dataStoreRepository: DataStoreRepository
) : BaseViewModel(application, dataStoreRepository) {

    companion object {
        const val DISCOUNT_TAB = 0
        const val SHIPPING_TAB = 1
    }

    val getAllVouchersResponse: MutableLiveData<ApiResult<List<Voucher>>> = MutableLiveData()
    val deleteVoucherResponse: MutableLiveData<ApiResult<Unit>> = MutableLiveData()
    val selectedTab: MutableLiveData<Int> = MutableLiveData(0)
    val voucherList: MutableLiveData<List<Voucher>> = MutableLiveData(listOf())

    fun getAllVouchers() = viewModelScope.launch {
        getAllVouchersResponse.value = ApiResult.Loading()
        try {
            val response = repository.remote.getAllVouchers(adminToken)
            getAllVouchersResponse.value = handleApiMultiResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
            getAllVouchersResponse.value = ApiResult.Error(e.message)
        }
    }

    fun deleteVoucher(voucherId: String) = viewModelScope.launch {
        deleteVoucherResponse.value = ApiResult.Loading()
        try {
            val response = repository.remote.deleteVoucher(adminToken, voucherId)
            if (response.isSuccessful) {
                deleteVoucherResponse.value = ApiResult.NullDataSuccess()
            } else {
                deleteVoucherResponse.value = ApiResult.Error(response.message())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            deleteVoucherResponse.value = ApiResult.Error(e.message)
        }
    }

}