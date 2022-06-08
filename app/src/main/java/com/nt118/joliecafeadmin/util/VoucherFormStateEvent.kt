package com.nt118.joliecafeadmin.util

sealed class VoucherFormStateEvent {
    data class OnCodeChanged(val code: String): VoucherFormStateEvent()
    data class OnDescriptionChanged(val description: String): VoucherFormStateEvent()
    data class OnStartDateChanged(val startDate: String): VoucherFormStateEvent()
    data class OnEndDateChanged(val endDate: String): VoucherFormStateEvent()
    data class OnTypeChanged(val type: String): VoucherFormStateEvent()
    data class OnConditionChanged(val condition: String): VoucherFormStateEvent()
    data class OnDiscountPercentChanged(val discountPercent: String): VoucherFormStateEvent()
    data class OnQuantityChanged(val quantity: String): VoucherFormStateEvent()
    object Submit: VoucherFormStateEvent()
}