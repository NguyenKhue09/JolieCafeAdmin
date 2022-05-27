package com.nt118.joliecafeadmin.util

class Constants {

    companion object {
        const val BASE_SEND_NOTICE_URL = "https://fcm.googleapis.com"
        const val SERVER_KEY = "AAAAsTUp0FM:APA91bHv_RMOReZTpvGleS8A_DrjejSziUGEFv9he4s0pFKzy4G_PYU5Snn6ABhdR_ihS9E5rDOorPpfNtzGAuOTiw1v-MlUuTbGrPH0bdDZQq1y5drjXh6rS2gRpI-0UH8DeWO_4Kdq"
        const val BASE_URL = "https://joliecafe.herokuapp.com"
        val listProductTypes = listOf("All", "Coffee", "Tea", "Juice", "Pasty", "Milk shake", "Milk tea")
        const val API_GATEWAY = "/api/v1/jolie-cafe"
        const val PAGE_SIZE = 20

        const val PREFERENCES_NAME = "jolie_admin_preferences"
        const val PREFERENCES_BACK_ONLINE = "backOnline"
        const val PREFERENCES_ADMIN_TOKEN = "adminToken"
    }
}