package com.titin.firebasecrud.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtil {
    private const val DATE_FORMAT = "dd/MM/yyyy"

    fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return sdf.format(date)
    }

    fun formatDate(calendar: Calendar): String {
        return formatDate(calendar.time)
    }

    fun parseDate(dateString: String): Calendar {
        val calendar = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val date = sdf.parse(dateString)
            date?.let {
                calendar.time = it
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return calendar
    }
}