package com.nt118.joliecafeadmin.util.extenstions

import com.nt118.joliecafeadmin.util.Constants.Companion.UTC_TIME_FORMAT
import java.text.SimpleDateFormat
import java.util.*

fun String.toDate(dateFormat: String = UTC_TIME_FORMAT, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Date? {
    val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
    parser.timeZone = timeZone
    return parser.parse(this)
}