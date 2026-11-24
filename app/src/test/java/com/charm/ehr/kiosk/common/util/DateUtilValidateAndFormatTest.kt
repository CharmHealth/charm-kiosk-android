package com.charm.ehr.kiosk.common.util

import org.junit.Assert.*
import org.junit.Test
import org.joda.time.LocalDate

class DateUtilValidateAndFormatTest {

    @Test
    fun testPartialTwoDigits_isAddingTrue_appendsSlash_and_isInputErrorTrue() {
        val res = DateUtil.validateAndFormatDateInput("12", null, null, isAdding = true)
        assertNotNull(res)
        assertTrue(res?.isInputError == true)
        assertEquals("12/", res?.formatted)
    }

    @Test
    fun testPartialTwoDigits_isAddingFalse_noSlash_and_isInputErrorTrue() {
        val res = DateUtil.validateAndFormatDateInput("12", null, null, isAdding = false)
        assertNotNull(res)
        assertTrue(res?.isInputError == true)
        assertEquals("12", res?.formatted)
    }

    @Test
    fun testFullDate_beforeMinDate_setsIsMinDateErrorTrue() {
        val minDate = LocalDate(2020, 1, 2) // Jan 2, 2020
        val res = DateUtil.validateAndFormatDateInput("01012020", minDate, null, isAdding = true)
        assertNotNull(res)
        assertTrue(res?.isMinDateError == true)
        assertTrue(res?.isInputError == true)
        assertEquals("01/01/2020", res?.formatted)
    }

    @Test
    fun testInvalidDay_returnsNull() {
        val res = DateUtil.validateAndFormatDateInput("02322020", null, null, isAdding = true) // Feb 32 invalid
        assertNull(res)
    }

}
