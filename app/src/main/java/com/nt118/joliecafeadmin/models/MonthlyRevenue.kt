package com.nt118.joliecafeadmin.models

import kotlinx.serialization.Serializable

@Serializable
data class MonthlyRevenue(
    val totalCostOfMonth: Int,
    val month: Int
)