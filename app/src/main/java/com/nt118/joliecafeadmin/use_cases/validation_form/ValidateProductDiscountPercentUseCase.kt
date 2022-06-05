package com.nt118.joliecafeadmin.use_cases.validation_form

import com.nt118.joliecafeadmin.models.ValidationResult


class ValidateProductDiscountPercentUseCase {

    fun execute(discountPercent: String): ValidationResult {
        try {
            val parseIntDiscountPercent = discountPercent.toInt()
            if(parseIntDiscountPercent == 0) {
                return ValidationResult(
                    successful = false,
                    errorMessage = "You can't set discount percent equal 0%"
                )
            }

            if(parseIntDiscountPercent > 100) {
                return ValidationResult(
                    successful = false,
                    errorMessage = "You can't set discount percent bigger than 100%"
                )
            }

            return ValidationResult(
                successful = true
            )
        } catch (e: Exception) {
            return ValidationResult(
                successful = false,
                errorMessage = "Discount percent is blank!"
            )
        }

    }
}