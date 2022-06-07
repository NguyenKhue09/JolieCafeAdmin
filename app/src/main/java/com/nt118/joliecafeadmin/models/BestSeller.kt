package com.nt118.joliecafeadmin.models

import kotlinx.serialization.Serializable

@Serializable
data class BestSeller(
    val product: Product,
    val sold: Int
)
