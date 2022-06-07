package com.nt118.joliecafeadmin.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommonFCMResponse(
    @SerialName("message_id")
    val messageId: Long,
)