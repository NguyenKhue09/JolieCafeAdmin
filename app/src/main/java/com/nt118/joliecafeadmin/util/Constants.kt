package com.nt118.joliecafeadmin.util

class Constants {

    companion object {

        const val SERVER_KEY = "AAAAsTUp0FM:APA91bHv_RMOReZTpvGleS8A_DrjejSziUGEFv9he4s0pFKzy4G_PYU5Snn6ABhdR_ihS9E5rDOorPpfNtzGAuOTiw1v-MlUuTbGrPH0bdDZQq1y5drjXh6rS2gRpI-0UH8DeWO_4Kdq"
        const val BASE_URL = "https://joliecafe.herokuapp.com"
        const val FCM_BASE_URL = "https://fcm.googleapis.com"

        val listProductTypes = listOf("All", "Coffee", "Tea", "Juice", "Pasty", "Milk shake", "Milk tea")
        val listProductStatus = listOf("Avaiable", "Coming soon", "Not available")
        val listVoucherTypes = listOf("Discount", "Ship")
        const val API_GATEWAY = "/api/v1/jolie-cafe"
        const val PAGE_SIZE = 20

        const val PREFERENCES_NAME = "jolie_admin_preferences"
        const val PREFERENCES_BACK_ONLINE = "backOnline"
        const val PREFERENCES_ADMIN_TOKEN = "adminToken"

        const val START_DATE_DISCOUNT_TAG = "StartDateDiscount"
        const val END_DATE_DISCOUNT_TAG = "EndDateDiscount"

        const val UTC_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        const val LOCAL_TIME_FORMAT = "yyyy/MM/dd"
        const val SNACK_BAR_STATUS_SUCCESS = 1
        const val SNACK_BAR_STATUS_DISABLE = 0
        const val SNACK_BAR_STATUS_ERROR = -1

        const val ACTION_TYPE_VIEW = 1
        const val ACTION_TYPE_EDIT = 0
        const val ACTION_TYPE_ADD = -1
        const val ACTION_TYPE = "actionTypeKey"
        const val NOTIFICATION_TYPE = "notificationType"
        const val NOTIFICATION_ID = "notificationId"
        const val NOTIFICATION_IMAGE = "notificationImage"
        const val PRODUCT_ID = "productId"
        const val PRODUCT_NAME = "productName"
        const val VOUCHER_ID = "voucherId"
        const val VOUCHER_CODE = "voucherCode"
        const val USER_ID = "userId"
        const val BILL_ID = "billId"
        const val USER_NOTICE_TOKEN = "userNoticeToken"
        const val COMMON_NOTIFICATION_TOPIC = "JolieCafeNotificationMainTopic"
        const val VOUCHER_DATA = "voucherData"

        val listNotificationType = listOf("COMMON", "PRODUCT", "VOUCHER", "BILL")
        val listTabNotificationType = listOf("All", "Common", "Product", "Voucher", "Bill")
        val listTabBillStatus = listOf("Pending", "Delivering", "Received", "Cancelled")
    }
}