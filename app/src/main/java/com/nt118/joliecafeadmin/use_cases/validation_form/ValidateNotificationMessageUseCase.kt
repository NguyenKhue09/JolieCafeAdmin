package com.nt118.joliecafeadmin.use_cases.validation_form

import com.nt118.joliecafeadmin.models.ValidationResult


class ValidateNotificationMessageUseCase {

    fun execute(notificationTitle: String): ValidationResult {
        if(notificationTitle.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "The notification message can't be blank"
            )
        }

        return ValidationResult(
            successful = true
        )
    }
}