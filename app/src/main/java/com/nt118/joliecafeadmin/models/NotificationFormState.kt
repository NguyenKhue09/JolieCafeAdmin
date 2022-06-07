package com.nt118.joliecafeadmin.models

import android.net.Uri

data class NotificationFormState(
    val title: String = "",
    val titleError: String? = null,
    val message: String = "",
    val messageError : String? = null,
    val image: Uri = Uri.EMPTY,
    val imageError: String? = null,
    val type: String = "Common",
    val typeError: String? = null,
    val productId: String = "",
    val voucherId: String = "",
    val billId: String = "",
    val userId: String = "",
    val notificationId: String = ""
)
