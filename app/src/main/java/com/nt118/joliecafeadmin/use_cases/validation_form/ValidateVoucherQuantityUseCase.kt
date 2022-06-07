package com.nt118.joliecafeadmin.use_cases.validation_form

import com.nt118.joliecafeadmin.models.ValidationResult

class ValidateVoucherQuantityUseCase {
    fun execute(quantity: String): ValidationResult {

        if (quantity.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "The quantity can't be blank"
            )
        }

        try {
            val quantityInt = quantity.toInt()
            if (quantityInt <= 0) {
                return ValidationResult(
                    successful = false,
                    errorMessage = "The quantity must be greater than 0"
                )
            }
        } catch (e: NumberFormatException) {
            return ValidationResult(
                successful = false,
                errorMessage = "The quantity must be an integer"
            )
        } catch (e: Exception) {
            return ValidationResult(
                successful = false,
                errorMessage = "An error occurred"
            )
        }

        return ValidationResult(
            successful = true
        )
    }
}