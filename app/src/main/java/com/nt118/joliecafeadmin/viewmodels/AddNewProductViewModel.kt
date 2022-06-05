package com.nt118.joliecafeadmin.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.nt118.joliecafeadmin.data.DataStoreRepository
import com.nt118.joliecafeadmin.data.Repository
import com.nt118.joliecafeadmin.models.Product
import com.nt118.joliecafeadmin.models.ProductFormState
import com.nt118.joliecafeadmin.models.ValidationResult
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
class AddNewProductViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val productFormValidationUseCases: ProductFormValidationUseCases,
    dataStoreRepository: DataStoreRepository
) : BaseViewModel(application, dataStoreRepository) {

    private val _addNewProductResponse =
        MutableStateFlow<ApiResult<Product>>(ApiResult.Idle())
    val addNewProductResponse: StateFlow<ApiResult<Product>> = _addNewProductResponse

    private val validationEventChannel = Channel<ValidationEvent>()
    val validationEvents = validationEventChannel.receiveAsFlow()

    val productFormState = MutableStateFlow(ProductFormState())

    fun addNewProduct(productData: Map<String, String>) {
        viewModelScope.launch {
            _addNewProductResponse.value = ApiResult.Loading()
            try {
                val response =
                    repository.remote.addNewProduct(productData = productData, token = adminToken)
                _addNewProductResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                _addNewProductResponse.value = ApiResult.Error(e.message)
            }
        }
    }

    fun onProductFormEvent(event: ProductFormStateEvent) {
        when(event) {
            is ProductFormStateEvent.OnProductImageChanged -> {
                productFormState.value = productFormState.value.copy(productImage = event.productImage)
            }
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
        val imageResult = productFormValidationUseCases.validateProductImageUseCase.execute(productImage = productFormState.value.productImage)
        val nameResult = productFormValidationUseCases.validateProductNameUseCase.execute(productFormState.value.productName)
        val priceResult = productFormValidationUseCases.validateProductPriceUseCase.execute(productFormState.value.productPrice)
        val descriptionResult = productFormValidationUseCases.validateProductDescriptionUseCase.execute(productFormState.value.productDescription)
        val endDateResult = productFormValidationUseCases.validateProductEndDateDiscountUseCase.execute(
            startDate = productFormState.value.productStartDateDiscount,
            endDate = productFormState.value.productEndDateDiscount
        )
        val startDateResult = productFormValidationUseCases.validateProductStartDateDiscountUseCase.execute(
            startDate = productFormState.value.productStartDateDiscount,
        )

        val discountResult = productFormValidationUseCases.validateProductDiscountPercentUseCase.execute(productFormState.value.productDiscountPercent)

        val listResult = if(productFormState.value.isDiscount) {
            listOf(
                imageResult,
                nameResult,
                priceResult,
                descriptionResult,
                startDateResult,
                endDateResult,
                discountResult
            )
        } else {
            listOf(
                imageResult,
                nameResult,
                priceResult,
                descriptionResult
            )
        }

        val hasError = listResult.any { !it.successful }

        if(hasError) {
            productFormState.value = productFormState.value.copy(
                productImageError = imageResult.errorMessage,
                productNameError = nameResult.errorMessage,
                productPriceError = priceResult.errorMessage,
                productDescriptionError = descriptionResult.errorMessage,
                productStartDateDiscountError = if(productFormState.value.isDiscount) startDateResult.errorMessage else null,
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

    private fun cleanProductFormError() {
        productFormState.value = productFormState.value.copy(
            productImageError = null,
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