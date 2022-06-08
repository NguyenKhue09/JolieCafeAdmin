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

        if (voucherCode.length > 20) {
            return ValidationResult(
                successful = false,
                errorMessage = "The voucher code can't be longer than 20 characters"
            )
        }

        return ValidationResult(
            successful = true
        )
    }
}