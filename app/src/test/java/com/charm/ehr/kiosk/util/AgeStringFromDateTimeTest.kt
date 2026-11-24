package com.charm.ehr.kiosk.util

import com.charm.ehr.kiosk.common.util.DateUtil
import com.charm.ehr.kiosk.common.util.UIUtil
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class AgeStringFromDateTimeTest {


    @Test
    fun testGetAgeFromDob_withNull_returnsEmptyString() {
        val result = DateUtil.getAgeFromDob(null)
        assertEquals("", result)
    }

    @Test
    fun testGetAgeFromDob_withOneYearOld_returnsYear() {
        val dob = DateTime.now().minusYears(1)
        val result = UIUtil.getAgeFromDob(dob)
        assertEquals("1 year", result)
    }

    @Test
    fun testGetAgeFromDob_withMultipleYears_returnsYearsPlural() {
        val dob = DateTime.now().minusYears(5)
        val result = DateUtil.getAgeFromDob(dob)
        assertEquals("5 years", result)
    }

    @Test
    fun testGetAgeFromDob_withOneMonth_returnsMonth() {
        val dob = DateTime.now().minusMonths(1)
        val result = DateUtil.getAgeFromDob(dob)
        assertEquals("1 month", result)
    }

    @Test
    fun testGetAgeFromDob_withMultipleMonths_returnsMonthsPlural() {
        val dob = DateTime.now().minusMonths(6)
        val result = DateUtil.getAgeFromDob(dob)
        assertEquals("6 months", result)
    }

    @Test
    fun testGetAgeFromDob_withOneDay_returnsDay() {
        val dob = DateTime.now().minusDays(1)
        val result = DateUtil.getAgeFromDob(dob)
        assertEquals("1 day", result)
    }

    @Test
    fun testGetAgeFromDob_withMultipleDays_returnsDaysPlural() {
        val dob = DateTime.now().minusDays(15)
        val result = DateUtil.getAgeFromDob(dob)
        assertEquals("15 days", result)
    }

    @Test
    fun testGetAgeFromDob_withZeroDays_returnsDaysPlural() {
        val dob = DateTime.now()
        val result = DateUtil.getAgeFromDob(dob)
        assertEquals("0 days", result)
    }


}

