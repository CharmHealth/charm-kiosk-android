package com.charm.ehr.kiosk.common.util

import android.util.Log
import com.charm.ehr.kiosk.common.util.UIUtil.TAG
import com.charm.ehr.kiosk.domain.DateInputResult
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.Months
import org.joda.time.Years
import org.joda.time.format.DateTimeFormatter

object DateUtil {
    fun isDateAfterToday(
        dateStr: String,
        formatter: org.joda.time.format.DateTimeFormatter
    ): Boolean {
        return try {
            val inputDate = org.joda.time.LocalDate.parse(dateStr, formatter)
            val today = org.joda.time.LocalDate.now()
            inputDate.isAfter(today)
        } catch (e: Exception) {
            false
        }
    }

    fun isDateBefore(
        fromDate: String,
        toDate: String,
        formatter: org.joda.time.format.DateTimeFormatter
    ): Boolean {
        return try {
            val dateObjA = org.joda.time.LocalDate.parse(fromDate, formatter)
            val dateObjB = org.joda.time.LocalDate.parse(toDate, formatter)
            dateObjA.isBefore(dateObjB)
        } catch (e: Exception) {
            false
        }
    }

    fun getAgeFromDob(dob: DateTime?): String {
        if (dob == null) return ""
        val now = DateTime.now()

        val dobStart = dob.withTimeAtStartOfDay()
        val nowStart = now.withTimeAtStartOfDay()

        // Guard: a DOB in the future is a data-entry error — return safe default
        if (dobStart.isAfter(nowStart)) return "0 days"

        val years = Years.yearsBetween(dobStart, nowStart).years

        return when {
            years >= 1 -> "$years year${if (years > 1) "s" else ""}"
            else -> {
                val months = Months.monthsBetween(dobStart, nowStart).months
                if (months >= 1) {
                    "$months month${if (months > 1) "s" else ""}"
                } else {
                    val days = Days.daysBetween(dobStart, nowStart).days
                    "$days day${if (days != 1) "s" else ""}"
                }
            }
        }
    }

    fun validateAndFormatDateInput(
        input: String,
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
        isAdding: Boolean
    ): DateInputResult? {
        if (input.isEmpty()) {
            return DateInputResult(
                formatted = "",
                isInputError = false,
                isMinDateError = false,
                isMaxDateError = false
            )
        }
        val maxDigits = 8 // MMddyyyy
        val digits = input.filter { it.isDigit() }.take(maxDigits)
        var correctedDigits = digits
        var isInputError = false
        var isMinDateError = false
        var isMaxDateError = false

        if (digits.length == 1) {
            val first = digits[0]
            // If first digit is not 0 or 1, prepend 0
            correctedDigits = if (first !in listOf('0', '1')) {
                "0$first"
            } else {
                digits
            }
        } else if (digits.length >= 2) {
            val first = digits[0]
            val second = digits[1]
            val valid = when (first) {
                '0' -> second in '1'..'9'      // 01–09
                '1' -> second in '0'..'2'      // 10–12
                else -> false
            }
            if (!valid) {
                return null
            }
        }

        if (digits.length >= 3) {
            val dayFirst = digits[2]
            correctedDigits = if (dayFirst !in listOf('0', '1', '2', '3')) {
                digits.substring(0, 2) + "0" + dayFirst
            } else {
                digits
            }
        }

        if (digits.length >= 4) {
            val dayFirst = digits[2]
            val daySecond = digits[3]
            val validDay = when (dayFirst) {
                '0' -> daySecond in '1'..'9'   // 01–09
                '1', '2' -> daySecond in '0'..'9' // 10–29
                '3' -> daySecond in '0'..'1'   // 30–31
                else -> false
            }
            if (!validDay) return null
        }

        if (digits.length >= 5) {
            val yearDigits = digits.drop(4).take(4)
            if (yearDigits.isNotEmpty() && yearDigits[0] !in listOf('1', '2')) {
                return null
            }
            // Partial year input
            if (yearDigits.length in 1..3) {
                isInputError = true
            }
            if (yearDigits.length == 4) {
                if (minDate != null || maxDate != null) {
                    val formatter = ModelUtil.mothFirstDateFormatWithSlash()
                    val inputDateStr = digits.take(2) + "/" + digits.substring(
                        2,
                        4
                    ) + "/" + digits.substring(4, 8)
                    try {
                        val inputDate = LocalDate.parse(inputDateStr, formatter)
                        if (minDate != null) {
                            isMinDateError = inputDate.isBefore(minDate)
                        }
                        if (maxDate != null) {
                            isMaxDateError = inputDate.isAfter(maxDate)
                        }
                        isInputError = isMinDateError || isMaxDateError
                    } catch (_: Exception) {
                        isInputError = true
                    }
                } else {
                    isInputError = false
                }
            }
        } else {
            // Partial input check: less than 8 digits
            isInputError = true

        }

        val formatted = buildString {
            correctedDigits.forEachIndexed { i, c ->
                append(c)
                if (i == 1 || i == 3) {
                    if (isAdding) append('/')
                    else if (i < correctedDigits.lastIndex) append('/')
                }
            }
        }
        return DateInputResult(
            formatted = formatted,
            isInputError = isInputError,
            isMinDateError = isMinDateError,
            isMaxDateError = isMaxDateError
        )

    }

    fun getDate(dateString: String?, dateTimeFormatter: DateTimeFormatter): DateTime? {

        try {
            dateString ?: return null
            return DateTime.parse(dateString, dateTimeFormatter)
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
        return null
    }

    fun getDate(dateString: String?): DateTime? {

        try {
            dateString ?: return null
            return DateTime.parse(dateString)
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
        return null
    }
}