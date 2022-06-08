package com.nt118.joliecafeadmin.util

import android.os.Build
import android.text.Editable

import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class DateTimeUtil {
    companion object {
        fun getTextChangeListener(date: TextInputEditText, callback: (() -> Unit)?) = object : TextWatcher {
            private var current = ""
            private val dateFormat = "yyyyMMdd"
            private val cal: Calendar = Calendar.getInstance()
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString() != current) {
                    var clean = s.toString().replace("[^\\d.]".toRegex(), "")
                    val cleanC = current.replace("[^\\d.]".toRegex(), "")
                    val cl = clean.length
                    var sel = cl
                    var i = 4
                    while (i <= cl && i < 8) {
                        sel++
                        i += 2
                    }
                    //Fix for pressing delete next to a forward slash
                    if (clean == cleanC) sel--
                    if (clean.length < 8) {
                        clean += dateFormat.substring(clean.length)
                    } else {
                        //This part makes sure that when we finish entering numbers
                        //the date is correct, fixing it otherwise
                        var day = clean.substring(6, 8).toInt()
                        var mon = clean.substring(4, 6).toInt()
                        var year = clean.substring(0, 4).toInt()
                        if (mon > 12) mon = 12
                        cal.set(Calendar.MONTH, mon - 1)
                        year = if (year < 1900) 1900 else if (year > 2100) 2100 else year
                        cal.set(Calendar.YEAR, year)
                        // ^ first set year for the line below to work correctly
                        //with leap years - otherwise, date e.g. 29/02/2012
                        //would be automatically corrected to 28/02/2012
                        day = if (day > cal.getActualMaximum(Calendar.DATE)) cal.getActualMaximum(
                            Calendar.DATE
                        ) else day
                        clean = String.format("%02d%02d%02d", year, mon, day)
                    }
                    clean = String.format(
                        "%s/%s/%s", clean.substring(0, 4),
                        clean.substring(4, 6),
                        clean.substring(6, 8)
                    )
                    sel = if (sel < 0) 0 else sel
                    current = clean
                    date.setText(current)
                    date.setSelection(if (sel < current.length) sel else current.length)
                }
                callback?.invoke()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        }
        fun dateFormatter(dateString: String): String = dateString.substring(0,10).split("-").asReversed().joinToString(separator = "/")
    }
}