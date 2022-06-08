package com.nt118.joliecafeadmin.models


import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val userId: String,
    val userName: String,
    val phone: String,
    val address: String,
)
