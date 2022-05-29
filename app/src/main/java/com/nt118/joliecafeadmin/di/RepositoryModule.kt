package com.nt118.joliecafeadmin.di

import com.nt118.joliecafeadmin.use_cases.ProductFormValidationUseCases
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
            validateProductNameUseCase = ValidateProductNameUseCase(),
            validateProductPriceUseCase = ValidateProductPriceUseCase(),
            validateProductDescriptionUseCase = ValidateProductDescriptionUseCase(),
            validateProductDiscountPercentUseCase = ValidateProductDiscountPercentUseCase(),
            validateProductEndDateDiscountUseCase = ValidateProductEndDateDiscountUseCase()
        )
    }

}