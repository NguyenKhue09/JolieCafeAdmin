package com.nt118.joliecafeadmin.util

import android.net.Uri

sealed class NotificationFormStateEvent {
    data class onTitleChanged(val title: String) : NotificationFormStateEvent()
    data class onMessageChanged(val message: String) : NotificationFormStateEvent()
    data class onImageChanged(val imageUri: Uri) : NotificationFormStateEvent()
    data class onTypeChanged(val type: String) : NotificationFormStateEvent()
    data class onProductIdChanged(val productId: String) : NotificationFormStateEvent()
    data class onVoucherIdChanged(val voucherId: String) : NotificationFormStateEvent()
    data class onBillIdChanged(val billId: String) : NotificationFormStateEvent()
    data class onUserIdChanged(val userId: String) : NotificationFormStateEvent()
    object Submit: NotificationFormStateEvent()
}
