package com.nt118.joliecafeadmin.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponseMultiData<T>(
    val success: Boolean,
    val message: String,
    val prevPage: Int? = null,
    val nextPage: Int? = null,
    val data: List<T>? = null
)
