package com.nt118.joliecafeadmin.models

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null
)
