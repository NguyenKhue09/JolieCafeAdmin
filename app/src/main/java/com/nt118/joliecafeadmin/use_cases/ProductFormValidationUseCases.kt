package com.nt118.joliecafeadmin.use_cases

import com.nt118.joliecafeadmin.use_cases.validation_form.*

data class ProductFormValidationUseCases(
    val validateProductNameUseCase: ValidateProductNameUseCase,
    val validateProductPriceUseCase: ValidateProductPriceUseCase,
    val validateProductDescriptionUseCase: ValidateProductDescriptionUseCase,
    val validateProductEndDateDiscountUseCase: ValidateProductEndDateDiscountUseCase,
    val validateProductDiscountPercentUseCase: ValidateProductDiscountPercentUseCase
)
