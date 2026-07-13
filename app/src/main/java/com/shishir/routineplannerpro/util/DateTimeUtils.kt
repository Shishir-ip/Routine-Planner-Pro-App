package com.shishir.routineplannerpro.util

import com.shishir.routineplannerpro.model.RoutineItemEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateTimeUtils {
    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun parseTime(input: String): LocalTime? {
        return runCatching { LocalTime.parse(input.trim(), timeFormatter) }
            .recoverCatching { LocalTime.parse(input.trim()) }
            .getOrNull()
    }

    fun parseDate(input: String?): LocalDate? {
        if (input.isNullOrBlank()) return null
        return try {
            LocalDate.parse(input, dateFormatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    fun isActiveOnDate(item: RoutineItemEntity, date: LocalDate): Boolean {
        val startDate = parseDate(item.startDate)
        val endDate = parseDate(item.endDate)
        if (startDate != null && date.isBefore(startDate)) return false
        if (endDate != null && date.isAfter(endDate)) return false

        val day = date.dayOfWeek.name
        val allowed = item.daysCsv.split(',').map { it.trim().uppercase() }
        return allowed.contains("EVERYDAY") || allowed.contains(day)
    }

    fun nextOccurrenceStart(item: RoutineItemEntity, from: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
        val startTime = parseTime(item.startTime) ?: return null
        val startDateLimit = parseDate(item.startDate)
        val endDateLimit = parseDate(item.endDate)

        val allowedDays = item.daysCsv.split(',').map { it.trim().uppercase() }
        var date = from.toLocalDate()
        repeat(370) {
            if (startDateLimit != null && date.isBefore(startDateLimit)) {
                date = startDateLimit
            }
            if (endDateLimit != null && date.isAfter(endDateLimit)) return null

            val dayAllowed = allowedDays.contains("EVERYDAY") || allowedDays.contains(date.dayOfWeek.name)
            if (dayAllowed) {
                val candidate = LocalDateTime.of(date, startTime)
                if (candidate.isAfter(from)) return candidate
            }
            date = date.plusDays(1)
        }
        return null
    }

    fun dayAbbreviation(day: DayOfWeek): String = day.name.lowercase().replaceFirstChar { it.uppercase() }
}
