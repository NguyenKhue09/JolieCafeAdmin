package com.nt118.joliecafeadmin.util

import android.content.Context
import android.util.TypedValue

object ScreenUtils {
    fun spToPx(sp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        ).toInt()
    }
}