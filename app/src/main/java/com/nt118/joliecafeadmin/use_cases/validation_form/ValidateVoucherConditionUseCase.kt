package com.nt118.joliecafeadmin.use_cases.validation_form

import com.nt118.joliecafeadmin.models.ValidationResult

class ValidateVoucherConditionUseCase {

    fun execute(condition: String): ValidationResult {
        if (condition.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "The condition can't be blank"
            )
        }
        try {
            if (condition.toInt() < 0) {
                return ValidationResult(
                    successful = false,
                    errorMessage = "The condition must be greater than zero"
                )
            }
        } catch (e: NumberFormatException) {
            return ValidationResult(
                successful = false,
                errorMessage = "The condition must be an integer"
            )
        } catch (e: Exception) {
            return ValidationResult(
                successful = false,
                errorMessage = "Something went wrong with the condition"
            )
        }

        return ValidationResult(
            successful = true
        )
    }
}