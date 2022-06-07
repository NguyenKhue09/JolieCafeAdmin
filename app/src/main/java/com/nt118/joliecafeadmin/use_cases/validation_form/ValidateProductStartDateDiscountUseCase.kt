package com.nt118.joliecafeadmin.use_cases.validation_form

import com.nt118.joliecafeadmin.models.ValidationResult
import com.nt118.joliecafeadmin.util.Constants.Companion.LOCAL_TIME_FORMAT
import com.nt118.joliecafeadmin.util.extenstions.toDate
import java.lang.reflect.InvocationTargetException
import java.text.ParseException
import java.util.*


class ValidateProductStartDateDiscountUseCase {

    fun execute(startDate: String): ValidationResult {

        try {
            startDate.toDate(
                dateFormat = LOCAL_TIME_FORMAT,
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
                errorMessage = "Start date is blank!"
            )
        }
        catch (e: Exception) {
            return ValidationResult(
                successful = false,
                errorMessage = "Some thing wrong with start date"
            )
        }
    }
}