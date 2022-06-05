package com.nt118.joliecafeadmin.use_cases.validation_form

import com.nt118.joliecafeadmin.models.ValidationResult


class ValidateProductNameUseCase {

    fun execute(productName: String): ValidationResult {
        if(productName.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "The product name can't be blank"
            )
        }

        return ValidationResult(
            successful = true
        )
    }
}