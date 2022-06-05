package com.nt118.joliecafeadmin.use_cases.validation_form

import com.nt118.joliecafeadmin.models.ValidationResult


class ValidateProductDescriptionUseCase {

    fun execute(description: String): ValidationResult {
        if(description.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "The product description can't be blank"
            )
        }

        return ValidationResult(
            successful = true
        )
    }
}