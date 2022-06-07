package com.nt118.joliecafeadmin.util

import android.net.Uri

sealed class NotificationFormStateEvent {
    data class OnTitleChanged(val title: String) : NotificationFormStateEvent()
    data class OnMessageChanged(val message: String) : NotificationFormStateEvent()
    data class OnImageChanged(val imageUri: Uri) : NotificationFormStateEvent()
    data class OnTypeChanged(val type: String) : NotificationFormStateEvent()
    data class OnProductIdChanged(val productId: String) : NotificationFormStateEvent()
    data class OnVoucherIdChanged(val voucherId: String) : NotificationFormStateEvent()
    data class OnBillIdChanged(val billId: String) : NotificationFormStateEvent()
    data class OnUserIdChanged(val userId: String) : NotificationFormStateEvent()
    data class OnNotificationIdChanged(val notificationId: String) : NotificationFormStateEvent()
    object Submit: NotificationFormStateEvent()
}
