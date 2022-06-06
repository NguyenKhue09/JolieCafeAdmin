package com.nt118.joliecafeadmin.models

import kotlinx.serialization.Serializable

@Serializable
data class WeeklyRevenue(
    val totalCostOfWeek: Int,
    val week: Int
)
