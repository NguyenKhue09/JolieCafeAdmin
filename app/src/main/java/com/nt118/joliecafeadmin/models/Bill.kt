package com.nt118.joliecafeadmin.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Bill(
    @SerialName("_id")
    val id: String,
    val userInfo: String,
    val products: List<BillProduct>,
    val address: Address,
    val totalCost: Double,
    val discountCost: Double,
    val shippingFee: Double,
    val voucherApply: List<Voucher>,
    val scoreApply: Int,
    val paid: Boolean,
    val paymentMethod: String,
    val orderDate: String,
    val status: String,
    val orderId: String,
    val token: String? = null
)
