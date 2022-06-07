package com.nt118.joliecafeadmin.use_cases

import com.nt118.joliecafeadmin.use_cases.validation_form.*

data class VoucherFormValidationUseCase(
    val validateVoucherCodeUseCase: ValidateVoucherCodeUseCase,
    val validateVoucherDescriptionUseCase: ValidateVoucherDescriptionUseCase,
    val validateVoucherConditionUseCase: ValidateVoucherConditionUseCase,
    val validateVoucherStartDateUseCase: ValidateVoucherStartDateUseCase,
    val validateVoucherEndDateUseCase: ValidateVoucherEndDateUseCase,
    val validateVoucherDiscountPercentUseCase: ValidateVoucherDiscountPercentUseCase,
    val validateVoucherQuantityUseCase: ValidateVoucherQuantityUseCase
)