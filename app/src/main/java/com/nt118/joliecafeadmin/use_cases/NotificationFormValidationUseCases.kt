package com.nt118.joliecafeadmin.use_cases

import com.nt118.joliecafeadmin.use_cases.validation_form.ValidateNotificationImageUseCase
import com.nt118.joliecafeadmin.use_cases.validation_form.ValidateNotificationMessageUseCase
import com.nt118.joliecafeadmin.use_cases.validation_form.ValidateNotificationTitleUseCase

data class NotificationFormValidationUseCases(
    val validateNotificationImageUseCase: ValidateNotificationImageUseCase,
    val validateNotificationMessageUseCase: ValidateNotificationMessageUseCase,
    val validateNotificationTitleUseCase: ValidateNotificationTitleUseCase
)
