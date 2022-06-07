package com.nt118.joliecafeadmin.use_cases.validation_form

import android.net.Uri
import com.nt118.joliecafeadmin.models.ValidationResult
import com.nt118.joliecafeadmin.util.extenstions.isValidUrl
import java.io.File


class ValidateNotificationImageUseCase {

    fun execute(notificationImage: Uri): ValidationResult {
        try {
            if (notificationImage.toString().isValidUrl()) {
                return ValidationResult(
                    successful = true
                )
            }

            val imageFile = File(notificationImage.path!!)

            if(imageFile.exists()) {
                return ValidationResult(
                    successful = false,
                    errorMessage = "Notification photo not exists in your device!"
                )
            }

            return ValidationResult(
                successful = true
            )
        } catch (e: Exception) {
            return ValidationResult(
                successful = false,
                errorMessage = "Some things wrong with notification photo!"
            )
        }
    }
}