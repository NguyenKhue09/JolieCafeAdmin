package com.nt118.joliecafeadmin.use_cases.validation_form

import com.nt118.joliecafeadmin.models.ValidationResult

class ValidateVoucherDescriptionUseCase {

    fun execute(voucherDescription: String): ValidationResult {
        if (voucherDescription.isEmpty()) {
            return ValidationResult(
                successful = false,
                errorMessage = "The voucher description can't be blank"
            )
        }

        return ValidationResult(successful = true)
    }
}