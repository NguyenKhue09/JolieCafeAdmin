package com.nt118.joliecafeadmin.use_cases.validation_form

import com.nt118.joliecafeadmin.models.ValidationResult
import com.nt118.joliecafeadmin.util.Constants.Companion.LOCAL_TIME_FORMAT
import com.nt118.joliecafeadmin.util.extenstions.toDate
import java.lang.reflect.InvocationTargetException
import java.text.ParseException
import java.util.*


class ValidateProductEndDateDiscountUseCase {

    fun execute(startDate: String, endDate: String): ValidationResult {

        try {
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



            if (resultCompare > 0) {
                println("compate date $resultCompare")
                return ValidationResult(
                    successful = false,
                    errorMessage = "End date discount must after start date!"
                )
            }


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