package com.nt118.joliecafeadmin.models

import kotlinx.serialization.Serializable

@Serializable
data class NotificationData(
    val title: String,
    val message: String,
    val image: String? = null,
    val type: String,
    val productId: String? = null,
    val productName: String? = null,
    val voucherId: String? = null,
    val voucherCode: String? = null,
    val billId: String? = null,
    val userId: String? = null,
)