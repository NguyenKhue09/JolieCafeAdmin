package com.nt118.joliecafeadmin.use_cases

import com.nt118.joliecafeadmin.use_cases.validation_form.*

data class ProductFormValidationUseCases(
    val validateProductImageUseCase: ValidateProductImageUseCase,
    val validateProductNameUseCase: ValidateProductNameUseCase,
    val validateProductPriceUseCase: ValidateProductPriceUseCase,
    val validateProductDescriptionUseCase: ValidateProductDescriptionUseCase,
    val validateProductStartDateDiscountUseCase: ValidateProductStartDateDiscountUseCase,
    val validateProductEndDateDiscountUseCase: ValidateProductEndDateDiscountUseCase,
    val validateProductDiscountPercentUseCase: ValidateProductDiscountPercentUseCase
)
