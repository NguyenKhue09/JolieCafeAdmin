package com.nt118.joliecafeadmin.use_cases.validation_form

import com.nt118.joliecafeadmin.models.ValidationResult
import com.nt118.joliecafeadmin.util.Constants.Companion.LOCAL_TIME_FORMAT
import com.nt118.joliecafeadmin.util.extenstions.toDate
import java.util.*


class ValidateProductEndDateDiscountUseCase {

    fun execute(startDate: String, endDate: String): ValidationResult {

        val resultCompare = startDate.toDate(
            dateFormat = LOCAL_TIME_FORMAT,
            timeZone = TimeZone.getDefault()
        )?.compareTo(
            endDate.toDate(
                dateFormat = LOCAL_TIME_FORMAT,
                timeZone = TimeZone.getDefault()
            )
        )
            ?: return ValidationResult(
                successful = false,
                errorMessage = "Valid date"
            )

        when {
            resultCompare > 0 -> {
                return ValidationResult(
                    successful = false,
                    errorMessage = "End date discount must after start date!"
                )
            }
        }

        return ValidationResult(
            successful = true
        )
    }
}