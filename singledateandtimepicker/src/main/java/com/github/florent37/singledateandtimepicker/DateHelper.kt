package com.github.florent37.singledateandtimepicker

import java.util.*

object DateHelper {
    @JvmStatic
    var timeZone: TimeZone? = null

    fun getCalendarOfDate(date: Date?): Calendar {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeZone = timeZone
        calendar.time = date
        calendar[Calendar.MILLISECOND] = 0
        calendar[Calendar.SECOND] = 0
        return calendar
    }

    fun getHour(date: Date?): Int = getCalendarOfDate(date)[Calendar.HOUR]

    fun getHourOfDay(date: Date?): Int = getCalendarOfDate(date)[Calendar.HOUR]

    fun getHour(date: Date?, isAmPm: Boolean): Int =
            if (isAmPm) {
                getHourOfDay(date)
            } else {
                getHour(date)
            }

    fun getMinuteOf(date: Date?): Int = getCalendarOfDate(date)[Calendar.MINUTE]

    fun today(): Date {
        val now = Calendar.getInstance(Locale.getDefault())
        now.timeZone = timeZone
        return now.time
    }

    fun getMonth(date: Date?): Int = getCalendarOfDate(date)[Calendar.MONTH]

    fun getDay(date: Date?): Int = getCalendarOfDate(date)[Calendar.DAY_OF_MONTH]
}