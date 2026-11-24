package com.charm.ehr.kiosk.common.util

import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.DateTimeZone

class DateUtilTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupTimezone() {
            DateTimeZone.setDefault(DateTimeZone.UTC)
        }
    }

    @Test
    fun testIsDateAfterToday() {
        val formatter = ModelUtil.commonDateFormatWithSlash()
        val tomorrowStr = LocalDate.now().plusDays(1).toString(formatter)
        val todayStr = LocalDate.now().toString(formatter)

        assertTrue("Tomorrow should be after today", DateUtil.isDateAfterToday(tomorrowStr, formatter))
        assertFalse("Today should not be after today", DateUtil.isDateAfterToday(todayStr, formatter))
    }

    @Test
    fun testIsDateBefore() {
        val formatter = ModelUtil.commonDateFormatWithSlash()
        val fromDate = "01/01/2020"
        val toDate = "02/01/2020"
        assertTrue(DateUtil.isDateBefore(fromDate, toDate, formatter))
        assertFalse(DateUtil.isDateBefore(toDate, fromDate, formatter))
    }

    @Test
    fun testGetAgeFromDob_years_months_days() {
        // Years
        val threeYearsAgo = DateTime.now().minusYears(3)
        assertEquals("3 years", DateUtil.getAgeFromDob(threeYearsAgo))

        val oneYearAgo = DateTime.now().minusYears(1)
        assertEquals("1 year", DateUtil.getAgeFromDob(oneYearAgo))

        // Months (less than a year)
        val twoMonthsAgo = DateTime.now().minusMonths(2)
        val ageMonths = DateUtil.getAgeFromDob(twoMonthsAgo)
        assertTrue("Expected months string", ageMonths.contains("month"))

        // Days (less than a month)
        val fiveDaysAgo = DateTime.now().minusDays(5)
        val ageDays = DateUtil.getAgeFromDob(fiveDaysAgo)
        assertTrue("Expected days string", ageDays.contains("day"))
    }

    @Test
    fun testValidateAndFormatDateInput_empty_and_full_and_invalid() {
        // Empty input
        val emptyRes = DateUtil.validateAndFormatDateInput("", null, null, isAdding = true)
        assertNotNull(emptyRes)
        assertEquals("", emptyRes?.formatted)
        assertFalse(emptyRes?.isInputError == true)

        // Full valid input (month-first MMDDYYYY)
        val fullRes = DateUtil.validateAndFormatDateInput("12312020", null, null, isAdding = true)
        assertNotNull(fullRes)
        assertEquals("12/31/2020", fullRes?.formatted)
        assertFalse(fullRes?.isInputError == true)

        // Invalid month -> should return null
        val invalidRes = DateUtil.validateAndFormatDateInput("99122020", null, null, isAdding = true)
        assertNull(invalidRes)
    }

    @Test
    fun testGetDate_parsing() {
        val fmt = ModelUtil.charmApiDateFormat()
        val dt = DateTime.now()
        val s = dt.toString(fmt)
        val parsed = DateUtil.getDate(s, fmt)
        assertNotNull(parsed)
        // Allow same day comparison
        assertEquals(dt.toString(fmt), parsed?.toString(fmt))

        // invalid string
        assertNull(DateUtil.getDate("not-a-date", fmt))
    }
}
