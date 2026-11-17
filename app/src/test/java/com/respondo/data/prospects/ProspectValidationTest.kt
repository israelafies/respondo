package com.respondo.data.prospects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProspectValidationTest {
    @Test fun requiresNameAndBusiness() {
        assertEquals("Enter a full name and business name.", ProspectValidation.error(" ", "Acme", "5551234567"))
        assertEquals("Enter a full name and business name.", ProspectValidation.error("Jordan Lee", "", "5551234567"))
    }

    @Test fun requiresAtLeastSevenPhoneDigits() {
        assertEquals("Enter a valid phone number with at least 7 digits.", ProspectValidation.error("Jordan Lee", "Acme", "+1 555-12"))
        assertNull(ProspectValidation.error("Jordan Lee", "Acme", "+1 (555) 123-4567"))
    }
}
