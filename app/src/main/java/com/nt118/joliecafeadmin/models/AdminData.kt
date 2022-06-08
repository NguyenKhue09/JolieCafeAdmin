package com.nt118.joliecafeadmin.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminData (
    @SerialName("_id")
    val id: String,
    val username: String,
    val token: String
)