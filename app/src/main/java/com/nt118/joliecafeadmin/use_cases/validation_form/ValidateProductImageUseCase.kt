package com.nt118.joliecafeadmin.use_cases.validation_form

import android.net.Uri
import com.nt118.joliecafeadmin.models.ValidationResult
import java.io.File


class ValidateProductImageUseCase {

    fun execute(productImage: Uri): ValidationResult {
        try {
            if(productImage == Uri.EMPTY) {
                return ValidationResult(
                    successful = false,
                    errorMessage = "You have not selected a product photo!"
                )
            }

            val imageFile = File(productImage.path!!)

            if(imageFile.exists()) {
                return ValidationResult(
                    successful = false,
                    errorMessage = "Product photo not exists in your device!"
                )
            }

            return ValidationResult(
                successful = true
            )
        } catch (e: Exception) {
            return ValidationResult(
                successful = false,
                errorMessage = "Some things wrong with product photo!"
            )
        }
    }
}