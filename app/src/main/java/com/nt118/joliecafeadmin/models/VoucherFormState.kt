package com.nt118.joliecafeadmin.models

data class VoucherFormState(
    val voucherCode: String = "",
    val voucherCodeError: String? = null,
    val voucherDescription: String = "",
    val voucherDescriptionError: String? = null,
    val voucherStartDate: String = "",
    val voucherStartDateError: String? = null,
    val voucherEndDate: String = "",
    val voucherEndDateError: String? = null,
    val voucherType: String = "",
    val voucherCondition: String = "",
    val voucherConditionError: String? = null,
    val voucherDiscountPercent: String = "",
    val voucherDiscountPercentError: String? = null,
    val voucherQuantity: String = "",
    val voucherQuantityError: String? = null,
)
