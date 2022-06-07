package com.nt118.joliecafeadmin.di

import com.nt118.joliecafeadmin.use_cases.NotificationFormValidationUseCases
import com.nt118.joliecafeadmin.use_cases.ProductFormValidationUseCases
import com.nt118.joliecafeadmin.use_cases.VoucherFormValidationUseCase
import com.nt118.joliecafeadmin.use_cases.validation_form.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProductFormValidationUseCases(): ProductFormValidationUseCases {
        return ProductFormValidationUseCases(
            validateProductImageUseCase = ValidateProductImageUseCase(),
            validateProductNameUseCase = ValidateProductNameUseCase(),
            validateProductPriceUseCase = ValidateProductPriceUseCase(),
            validateProductDescriptionUseCase = ValidateProductDescriptionUseCase(),
            validateProductDiscountPercentUseCase = ValidateProductDiscountPercentUseCase(),
            validateProductStartDateDiscountUseCase = ValidateProductStartDateDiscountUseCase(),
            validateProductEndDateDiscountUseCase = ValidateProductEndDateDiscountUseCase(),
        )
    }

    @Provides
    @Singleton
    fun provideNotificationFormValidationUseCases(): NotificationFormValidationUseCases {
        return NotificationFormValidationUseCases(
            validateNotificationImageUseCase = ValidateNotificationImageUseCase(),
            validateNotificationMessageUseCase = ValidateNotificationMessageUseCase(),
            validateNotificationTitleUseCase = ValidateNotificationTitleUseCase(),
        )
    }

    @Provides
    @Singleton
    fun provideVoucherFormValidationUseCase(): VoucherFormValidationUseCase {
        return VoucherFormValidationUseCase(
            validateVoucherCodeUseCase = ValidateVoucherCodeUseCase(),
            validateVoucherDescriptionUseCase = ValidateVoucherDescriptionUseCase(),
            validateVoucherStartDateUseCase = ValidateVoucherStartDateUseCase(),
            validateVoucherEndDateUseCase = ValidateVoucherEndDateUseCase(),
            validateVoucherDiscountPercentUseCase = ValidateVoucherDiscountPercentUseCase(),
            validateVoucherConditionUseCase = ValidateVoucherConditionUseCase(),
            validateVoucherQuantityUseCase = ValidateVoucherQuantityUseCase()
        )
    }

}