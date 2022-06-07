package com.nt118.joliecafeadmin.use_cases.validation_form

import com.nt118.joliecafeadmin.models.ValidationResult
import com.nt118.joliecafeadmin.util.Constants
import com.nt118.joliecafeadmin.util.extenstions.toDate
import java.text.ParseException
import java.util.*

class ValidateVoucherEndDateUseCase {

    fun execute(endDate: String): ValidationResult {

        try {
            endDate.toDate(
                dateFormat = Constants.LOCAL_TIME_FORMAT,
                timeZone = TimeZone.getDefault()
            )?: return ValidationResult(
                successful = false,
                errorMessage = "Invalid date!"
            )

            return ValidationResult(
                successful = true
            )
        }
        catch (e: ParseException) {
            return ValidationResult(
                successful = false,
                errorMessage = "End date is blank!"
            )
        }
        catch (e: Exception) {
            return ValidationResult(
                successful = false,
                errorMessage = "Some thing wrong with end date"
            )
        }
    }
}