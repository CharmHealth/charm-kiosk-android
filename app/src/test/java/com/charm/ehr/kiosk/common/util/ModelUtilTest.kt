package com.charm.ehr.kiosk.common.util

import org.junit.Assert.*
import org.junit.Test

class ModelUtilTest {

    @Test
    fun testFormatSecondsToMmSs_zeroAndLarge() {
        assertEquals("00:00", ModelUtil.formatSecondsToMmSs(0))
        assertEquals("01:05", ModelUtil.formatSecondsToMmSs(65))
        assertEquals("60:00", ModelUtil.formatSecondsToMmSs(3600))
    }

    @Test
    fun testIsValidEmail_validAndInvalid() {
        assertTrue(ModelUtil.isValidEmail("user@example.com"))
        assertTrue(ModelUtil.isValidEmail("first.last+tag@example.co.uk"))
        assertFalse(ModelUtil.isValidEmail("not-an-email"))
        assertFalse(ModelUtil.isValidEmail("user@invalid_domain"))
    }

    @Test
    fun testIsValidMobileNumber_in() {
        assertTrue(ModelUtil.isValidMobileNumber("9876543210", "in"))
        assertFalse(ModelUtil.isValidMobileNumber("0876543210", "in"))
        assertFalse(ModelUtil.isValidMobileNumber("987654321", "in"))
    }

    @Test
    fun testIsValidMobileNumber_us_uk_and_nonDigits() {
        assertTrue(ModelUtil.isValidMobileNumber("1234567890", "us"))
        assertFalse(ModelUtil.isValidMobileNumber("12345", "us"))
        assertTrue(ModelUtil.isValidMobileNumber("07123456789", "uk"))
        assertTrue(ModelUtil.isValidMobileNumber("7123456789", "uk"))
        assertFalse(ModelUtil.isValidMobileNumber("abc1234567", "in"))
    }

}
