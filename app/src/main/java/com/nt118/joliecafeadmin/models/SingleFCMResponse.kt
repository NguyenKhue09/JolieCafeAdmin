package com.nt118.joliecafeadmin.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SingleFCMResponse(
    @SerialName("multicast_id")
    val multicastId: Long,
    val success: Long,
    val failure: Long,
    @SerialName("canonical_ids")
    val canonicalIds: Long,
    val results: List<ResultSingleFCMResponse>
)
