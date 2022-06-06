package com.nt118.joliecafeadmin.models

import kotlinx.serialization.Serializable

@Serializable
data class NotificationData(
    val title: String,
    val message: String,
)