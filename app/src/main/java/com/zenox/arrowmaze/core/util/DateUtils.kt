package com.zenox.arrowmaze.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    init {
        dateFormat.timeZone = TimeZone.getDefault()
        dateTimeFormat.timeZone = TimeZone.getDefault()
    }

    fun todayString(): String = dateFormat.format(Date())

    fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

    fun daysBetween(dateStr1: String, dateStr2: String): Long {
        return try {
            val d1 = dateFormat.parse(dateStr1) ?: return 0
            val d2 = dateFormat.parse(dateStr2) ?: return 0
            kotlin.math.abs((d2.time - d1.time) / (24 * 60 * 60 * 1000))
        } catch (e: Exception) {
            0
        }
    }

    fun isToday(dateStr: String): Boolean = dateStr == todayString()

    fun isYesterday(dateStr: String): Boolean {
        val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
        return dateStr == dateFormat.format(yesterday)
    }

    fun currentTimeMillis(): Long = System.currentTimeMillis()

    fun isSameDay(ts1: Long, ts2: Long): Boolean {
        return formatDate(ts1) == formatDate(ts2)
    }
}