package com.nt118.joliecafeadmin.models

import android.net.Uri

data class ProductFormState(
    val isDiscount: Boolean = false,
    val productImage: Uri = Uri.EMPTY,
    val productImageError: String? = null,
    val productName: String = "",
    val productNameError: String? = null,
    val productType: String = "",
    val productPrice: String = "",
    val productPriceError: String? = null,
    val productStatus: String = "",
    val productDescription: String = "",
    val productDescriptionError: String? = null,
    val productStartDateDiscount: String = "",
    val productStartDateDiscountError: String? = null,
    val productEndDateDiscount: String = "",
    val productEndDateDiscountError: String? = null,
    val productDiscountPercent: String = "",
    val productDiscountPercentError: String? = null,
)
