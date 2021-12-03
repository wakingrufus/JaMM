package com.github.wakingrufus.jamm.common

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

sealed class DateParseResult
data class DateParseSuccess(val date: LocalDate) : DateParseResult()
data class DateParseFail(val originalValue: String) : DateParseResult()
object DateParseSkip : DateParseResult()

fun String?.parseDate(): DateParseResult {
    if (this == null || this.isBlank()) {
        return DateParseSkip
    }
    return try {
        when {
            this.toIntOrNull() != null -> DateParseSuccess(LocalDate.of(this.toInt(), Month.JANUARY, 1))
            this.contains("T") -> DateParseSuccess(LocalDateTime.parse(this).toLocalDate())
            this.count { it == '-' } == 2 -> DateParseSuccess(LocalDate.parse(this))
            this.count { it == '-' } == 1 -> DateParseSuccess(LocalDate.parse("$this-01"))
            else -> DateParseFail(this)
        }
    } catch (ex: RuntimeException) {
        DateParseFail(this)
    }
}