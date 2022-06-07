package com.nt118.joliecafeadmin.use_cases.validation_form

import com.nt118.joliecafeadmin.models.ValidationResult

class ValidateVoucherCodeUseCase {

    fun execute(voucherCode: String): ValidationResult {
        if (voucherCode.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "The voucher code can't be blank"
            )
        }

        return ValidationResult(
            successful = true
        )
    }
}