package com.nt118.joliecafeadmin.use_cases.validation_form

import com.nt118.joliecafeadmin.models.ValidationResult

class ValidateVoucherDiscountPercentUseCase {
    fun execute(percent: String): ValidationResult {

        if (percent.isEmpty()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Voucher discount percent cannot be blank"
            )
        }

        try {
            if (percent.toDouble() < 0 || percent.toDouble() > 100) {
                return ValidationResult(
                    successful = false,
                    errorMessage = "Voucher discount percent must be between 0 and 100"
                )
            }
        } catch (e: NumberFormatException) {
            return ValidationResult(
                successful = false,
                errorMessage = "Voucher discount percent must be a number"
            )
        } catch (e: Exception) {
            return ValidationResult(
                successful = false,
                errorMessage = "Something went wrong with discount percent"
            )
        }

        return ValidationResult(successful = true)

    }
}