package com.nt118.joliecafeadmin.models

import kotlinx.serialization.Serializable

@Serializable
data class YearlyRevenue(
    val totalCostOfYear: Int,
    val year: Int,
)
