package com.nt118.joliecafeadmin.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BillProduct(
    @SerialName("productId")
    val product: Product,
    val size: String,
    val quantity: Int,
    val price: Double
)
