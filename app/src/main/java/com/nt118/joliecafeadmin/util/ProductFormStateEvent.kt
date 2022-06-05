package com.nt118.joliecafeadmin.util

import android.net.Uri

sealed class ProductFormStateEvent {
    data class OnProductImageChanged(val productImage: Uri): ProductFormStateEvent()
    data class OnProductNameChanged(val productName: String): ProductFormStateEvent()
    data class OnProductTypeChanged(val productType: String): ProductFormStateEvent()
    data class OnProductPriceChanged(val productPrice: String): ProductFormStateEvent()
    data class OnProductStatusChanged(val productStatus: String): ProductFormStateEvent()
    data class OnProductDescriptionChanged(val productDescription: String): ProductFormStateEvent()
    data class OnProductStartDateDiscountChanged(val productStartDateDiscount: String): ProductFormStateEvent()
    data class OnProductEndDateDiscountChanged(val productEndDateDiscount: String): ProductFormStateEvent()
    data class OnProductDiscountPercentChanged(val productDiscountPercent: String): ProductFormStateEvent()
    data class IsDiscountChanged(val isDiscount: Boolean) : ProductFormStateEvent()
    object Submit: ProductFormStateEvent()
}
