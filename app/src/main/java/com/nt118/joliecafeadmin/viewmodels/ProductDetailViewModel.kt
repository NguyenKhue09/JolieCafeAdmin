package com.nt118.joliecafeadmin.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.nt118.joliecafeadmin.data.DataStoreRepository
import com.nt118.joliecafeadmin.data.Repository
import com.nt118.joliecafeadmin.models.Product
import com.nt118.joliecafeadmin.models.ProductFormState
import com.nt118.joliecafeadmin.use_cases.ProductFormValidationUseCases
import com.nt118.joliecafeadmin.util.ApiResult
import com.nt118.joliecafeadmin.util.ProductFormStateEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val productFormValidationUseCases: ProductFormValidationUseCases,
    dataStoreRepository: DataStoreRepository
) : BaseViewModel(application, dataStoreRepository) {

    private val _getProductDetailResponse =
        MutableStateFlow<ApiResult<Product>>(ApiResult.Loading())
    val getProductDetailResponse: StateFlow<ApiResult<Product>> = _getProductDetailResponse

    private val _productUpdateResponse =
        MutableStateFlow<ApiResult<Product>>(ApiResult.Loading())
    val productUpdateResponse: StateFlow<ApiResult<Product>> = _productUpdateResponse

    private val validationEventChannel = Channel<ValidationEvent>()
    val validationEvents = validationEventChannel.receiveAsFlow()

    val productFormState = MutableStateFlow(ProductFormState())

    fun getProductDetail(productId: String) {
        viewModelScope.launch {
            _getProductDetailResponse.value = ApiResult.Loading()
            try {
                val response =
                    repository.remote.getProductDetail(token = adminToken, productId = productId)
                _getProductDetailResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                _getProductDetailResponse.value = ApiResult.Error(e.message)
            }
        }
    }

    fun updateProduct(newData: Map<String, String>) {
        viewModelScope.launch {
            _productUpdateResponse.value = ApiResult.Loading()
            try {
                val response =
                    repository.remote.updateProduct(token = adminToken, newData = newData)
                _productUpdateResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                _productUpdateResponse.value = ApiResult.Error(e.message)
            }
        }
    }

    fun onProductFormEvent(event: ProductFormStateEvent) {
        when(event) {
            is ProductFormStateEvent.OnProductNameChanged -> {
                productFormState.value = productFormState.value.copy(productName = event.productName)
            }
            is ProductFormStateEvent.OnProductTypeChanged -> {
                productFormState.value = productFormState.value.copy(productType = event.productType)
            }
            is ProductFormStateEvent.OnProductPriceChanged -> {
                productFormState.value = productFormState.value.copy(productPrice = event.productPrice)
            }
            is ProductFormStateEvent.OnProductStatusChanged -> {
                productFormState.value = productFormState.value.copy(productStatus = event.productStatus)
            }
            is ProductFormStateEvent.OnProductDescriptionChanged -> {
                productFormState.value = productFormState.value.copy(productDescription = event.productDescription)
            }
            is ProductFormStateEvent.OnProductStartDateDiscountChanged -> {
                productFormState.value = productFormState.value.copy(productStartDateDiscount = event.productStartDateDiscount)
            }
            is ProductFormStateEvent.OnProductEndDateDiscountChanged -> {
                productFormState.value = productFormState.value.copy(productEndDateDiscount = event.productEndDateDiscount)
            }
            is ProductFormStateEvent.OnProductDiscountPercentChanged -> {
                productFormState.value = productFormState.value.copy(productDiscountPercent = event.productDiscountPercent)
            }
            is ProductFormStateEvent.IsDiscountChanged -> {
                productFormState.value = productFormState.value.copy(isDiscount = event.isDiscount)
            }
            is ProductFormStateEvent.Submit -> {
                submitProductFormData()
            }
        }
    }

    private fun submitProductFormData() {
        val nameResult = productFormValidationUseCases.validateProductNameUseCase.execute(productFormState.value.productName)
        val priceResult = productFormValidationUseCases.validateProductPriceUseCase.execute(productFormState.value.productPrice)
        val descriptionResult = productFormValidationUseCases.validateProductDescriptionUseCase.execute(productFormState.value.productDescription)
        val endDateResult = productFormValidationUseCases.validateProductEndDateDiscountUseCase.execute(
            startDate = productFormState.value.productStartDateDiscount,
            endDate = productFormState.value.productEndDateDiscount
        )
        val discountResult = productFormValidationUseCases.validateProductDiscountPercentUseCase.execute(productFormState.value.productDiscountPercent.toInt())

        val listResult = if(productFormState.value.isDiscount) {
            listOf(
                nameResult,
                priceResult,
                descriptionResult
            )
        } else {
            listOf(
                nameResult,
                priceResult,
                descriptionResult,
                endDateResult,
                discountResult
            )
        }

        val hasError = listResult.any { !it.successful }

        if(hasError) {
            productFormState.value = productFormState.value.copy(
                productNameError = nameResult.errorMessage,
                productPriceError = priceResult.errorMessage,
                productDescriptionError = descriptionResult.errorMessage,
                productEndDateDiscountError = if(productFormState.value.isDiscount) endDateResult.errorMessage else null,
                productDiscountPercentError = if(productFormState.value.isDiscount) discountResult.errorMessage else null,
            )
            return
        } else {
            cleanProductFormError()
        }
        viewModelScope.launch {
            validationEventChannel.send(ValidationEvent.Success)
        }
    }

    fun cleanProductFormError() {
        productFormState.value = productFormState.value.copy(
            productNameError = null,
            productPriceError = null,
            productDescriptionError = null,
            productEndDateDiscountError = null,
            productDiscountPercentError = null,
        )
    }

    sealed class ValidationEvent {
        object Success: ValidationEvent()
    }
}