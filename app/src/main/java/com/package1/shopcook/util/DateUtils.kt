@file:Suppress("NewApi")

package com.package1.shopcook.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object DateUtils {

    fun getCurrentMonday(referenceDate: LocalDate = LocalDate.now()): LocalDate {
        return referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    fun formatWeekRange(monday: LocalDate): String {
        val sunday = monday.plusDays(6)
        val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.US)
        val startMonth = monday.format(monthFormatter)
        val startDay = monday.dayOfMonth

        return if (monday.month == sunday.month) {
            "$startMonth $startDay–${sunday.dayOfMonth}"
        } else {
            val endMonth = sunday.format(monthFormatter)
            "$startMonth $startDay–$endMonth ${sunday.dayOfMonth}"
        }
    }

    fun getUpcomingWeekRanges(referenceDate: LocalDate = LocalDate.now()): List<String> {
        val currentMonday = getCurrentMonday(referenceDate)
        return (0..3).map { weekIndex ->
            formatWeekRange(currentMonday.plusWeeks(weekIndex.toLong()))
        }
    }

    fun getWeekOptions(referenceDate: LocalDate = LocalDate.now()): List<String> {
        val ranges = getUpcomingWeekRanges(referenceDate)
        return listOf("This week") + ranges.drop(1)
    }

    fun getMondayForWeek(selectedWeek: String, referenceDate: LocalDate = LocalDate.now()): LocalDate {
        val currentMonday = getCurrentMonday(referenceDate)
        val ranges = getUpcomingWeekRanges(referenceDate)

        if ((selectedWeek.equals("This week", ignoreCase = true)) || (selectedWeek == ranges.getOrNull(0))) {
            return currentMonday
        }

        ranges.forEachIndexed { index, range ->
            if (range.equals(selectedWeek, ignoreCase = true)) {
                return currentMonday.plusWeeks(index.toLong())
            }
        }

        return currentMonday
    }

    fun getDateBadge(
        dayOfWeek: String,
        selectedWeek: String,
        referenceDate: LocalDate = LocalDate.now(),
        fallbackLabel: String = "",
    ): String {
        val cleanDay = dayOfWeek.trim().uppercase(Locale.US)
        val dayOffset = when {
            cleanDay.startsWith("MON") -> 0
            cleanDay.startsWith("TUE") -> 1
            cleanDay.startsWith("WED") -> 2
            cleanDay.startsWith("THU") -> 3
            cleanDay.startsWith("FRI") -> 4
            cleanDay.startsWith("SAT") -> 5
            cleanDay.startsWith("SUN") -> 6
            else -> -1
        }

        if (dayOffset == -1) {
            return fallbackLabel.ifEmpty { dayOfWeek }
        }

        val monday = getMondayForWeek(selectedWeek, referenceDate)
        val targetDate = monday.plusDays(dayOffset.toLong())
        val shortDay = when (dayOffset) {
            0 -> "MON"
            1 -> "TUE"
            2 -> "WED"
            3 -> "THU"
            4 -> "FRI"
            5 -> "SAT"
            6 -> "SUN"
            else -> cleanDay
        }
        return "$shortDay ${targetDate.dayOfMonth}"
    }
}
