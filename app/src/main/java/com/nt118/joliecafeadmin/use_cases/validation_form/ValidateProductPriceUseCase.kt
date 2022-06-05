package com.nt118.joliecafeadmin.use_cases.validation_form

import com.nt118.joliecafeadmin.models.ValidationResult


class ValidateProductPriceUseCase {

    fun execute(price: String): ValidationResult {
        if(price.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "The product price can't be blank"
            )
        }

        return ValidationResult(
            successful = true
        )
    }
}