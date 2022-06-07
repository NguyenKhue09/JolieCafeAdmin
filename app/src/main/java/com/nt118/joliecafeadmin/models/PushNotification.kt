package com.nt118.joliecafeadmin.models

import kotlinx.serialization.Serializable

@Serializable
data class PushNotification(
    val data: NotificationData,
    val to: String?
)