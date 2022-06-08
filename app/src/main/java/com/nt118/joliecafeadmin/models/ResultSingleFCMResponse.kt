package com.nt118.joliecafeadmin.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResultSingleFCMResponse(
    @SerialName("message_id")
    val messageId: String,
)