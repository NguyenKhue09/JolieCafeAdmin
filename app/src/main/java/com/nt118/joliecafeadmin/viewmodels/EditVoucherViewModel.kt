package com.nt118.joliecafeadmin.viewmodels

import android.app.Application
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
class EditVoucherViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val voucherFormValidationUseCase: VoucherFormValidationUseCase,
    dataStoreRepository: DataStoreRepository
): BaseViewModel(application, dataStoreRepository) {

    val voucherFormState = MutableStateFlow(VoucherFormState())
    val updateVoucherResponse = MutableStateFlow<ApiResult<Voucher>>(ApiResult.Idle())
    lateinit var voucherId: String

    fun onVoucherFormEvent(event: VoucherFormStateEvent) {
        when (event) {
            is VoucherFormStateEvent.OnCodeChanged -> {
                voucherFormState.value = voucherFormState.value.copy(voucherCode = event.code)
            }
            is VoucherFormStateEvent.OnDescriptionChanged -> {
                voucherFormState.value = voucherFormState.value.copy(voucherDescription = event.description)
            }
            is VoucherFormStateEvent.OnDiscountPercentChanged -> {
                voucherFormState.value = voucherFormState.value.copy(voucherDiscountPercent = event.discountPercent)
            }
            is VoucherFormStateEvent.OnStartDateChanged -> {
                voucherFormState.value = voucherFormState.value.copy(voucherStartDate = event.startDate)
            }
            is VoucherFormStateEvent.OnEndDateChanged -> {
                voucherFormState.value = voucherFormState.value.copy(voucherEndDate = event.endDate)
            }
            is VoucherFormStateEvent.OnConditionChanged -> {
                voucherFormState.value = voucherFormState.value.copy(voucherCondition = event.condition)
            }
            is VoucherFormStateEvent.OnTypeChanged -> {
                voucherFormState.value = voucherFormState.value.copy(voucherType = event.type)
            }
            is VoucherFormStateEvent.OnQuantityChanged -> {
                voucherFormState.value = voucherFormState.value.copy(voucherQuantity = event.quantity)
            }
            is VoucherFormStateEvent.Submit -> {
                submitVoucherFormData()
            }
        }
    }

    sealed class ValidationEvent {
        object Success: ValidationEvent()
    }

    private fun submitVoucherFormData() {
        val codeResult = voucherFormValidationUseCase.validateVoucherCodeUseCase.execute(voucherFormState.value.voucherCode)
        val descriptionResult = voucherFormValidationUseCase.validateVoucherDescriptionUseCase.execute(voucherFormState.value.voucherDescription)
        val startDateResult = voucherFormValidationUseCase.validateVoucherStartDateUseCase.execute(voucherFormState.value.voucherStartDate)
        val endDateResult = voucherFormValidationUseCase.validateVoucherEndDateUseCase.execute(voucherFormState.value.voucherEndDate)
        val conditionResult = voucherFormValidationUseCase.validateVoucherConditionUseCase.execute(voucherFormState.value.voucherCondition)
        val discountPercentResult = voucherFormValidationUseCase.validateVoucherDiscountPercentUseCase.execute(voucherFormState.value.voucherDiscountPercent)
        val quantityResult = voucherFormValidationUseCase.validateVoucherQuantityUseCase.execute(voucherFormState.value.voucherQuantity)

        val listResult = listOf(
            codeResult,
            descriptionResult,
            startDateResult,
            endDateResult,
            conditionResult,
            discountPercentResult,
            quantityResult
        )

        val hasError = listResult.any { !it.successful }

        if (hasError) {
            voucherFormState.value = voucherFormState.value.copy(
                voucherCodeError = codeResult.errorMessage,
                voucherDescriptionError = descriptionResult.errorMessage,
                voucherStartDateError = startDateResult.errorMessage,
                voucherEndDateError = endDateResult.errorMessage,
                voucherConditionError = conditionResult.errorMessage,
                voucherDiscountPercentError = discountPercentResult.errorMessage,
                voucherQuantityError = quantityResult.errorMessage
            )
            return
        } else {
            clearVoucherFormError()
            val newVoucher = if (voucherFormState.value.voucherType == Constants.listVoucherTypes[0]) {
                mapOf(
                    "code" to voucherFormState.value.voucherCode,
                    "description" to voucherFormState.value.voucherDescription,
                    "startDate" to voucherFormState.value.voucherStartDate,
                    "endDate" to voucherFormState.value.voucherEndDate,
                    "condition" to voucherFormState.value.voucherCondition,
                    "discountPercent" to voucherFormState.value.voucherDiscountPercent,
                    "quantity" to voucherFormState.value.voucherQuantity,
                    "type" to voucherFormState.value.voucherType
                )
            } else {
                mapOf(
                    "code" to voucherFormState.value.voucherCode,
                    "description" to voucherFormState.value.voucherDescription,
                    "startDate" to voucherFormState.value.voucherStartDate,
                    "endDate" to voucherFormState.value.voucherEndDate,
                    "condition" to voucherFormState.value.voucherCondition,
                    "quantity" to voucherFormState.value.voucherQuantity,
                    "type" to voucherFormState.value.voucherType
                )
            }

            updateVoucher(newVoucher)
        }
    }

    private fun updateVoucher(voucherData: Map<String, String>) {
        viewModelScope.launch {
            updateVoucherResponse.value = ApiResult.Loading()
            try {
                val response = repository.remote.updateVoucher(voucherData = voucherData, token = adminToken, voucherId = voucherId)
                updateVoucherResponse.value = handleApiResponse(response)
            } catch (e: Exception) {
                e.printStackTrace()
                updateVoucherResponse.value = ApiResult.Error(e.message)
            }
        }
    }

    private fun clearVoucherFormError() {
        voucherFormState.value = voucherFormState.value.copy(
            voucherCodeError = null,
            voucherDescriptionError = null,
            voucherStartDateError = null,
            voucherEndDateError = null,
            voucherConditionError = null,
            voucherDiscountPercentError = null,
            voucherQuantityError = null
        )
    }
}