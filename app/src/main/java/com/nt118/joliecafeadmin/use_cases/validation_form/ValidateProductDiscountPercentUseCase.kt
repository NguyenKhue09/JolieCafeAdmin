package com.nt118.joliecafeadmin.use_cases.validation_form

import com.nt118.joliecafeadmin.models.ValidationResult


class ValidateProductDiscountPercentUseCase {

    fun execute(discountPercent: Int): ValidationResult {
        if(discountPercent == 0) {
            return ValidationResult(
                successful = false,
                errorMessage = "You can't set discount percent equal 0%"
            )
        }

        if(discountPercent > 100) {
            return ValidationResult(
                successful = false,
                errorMessage = "You can't set discount percent bigger than 100%"
            )
        }

        return ValidationResult(
            successful = true
        )
    }
}