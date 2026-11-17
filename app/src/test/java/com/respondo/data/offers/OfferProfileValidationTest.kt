package com.respondo.data.offers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OfferProfileValidationTest {
    @Test fun offerAndProviderNamesAreRequired() {
        assertEquals("Enter an offer name and business or provider name.", OfferProfileValidation.error("", "Studio"))
        assertEquals("Enter an offer name and business or provider name.", OfferProfileValidation.error("Consulting", " "))
    }

    @Test fun optionalFieldsDoNotAffectValidation() {
        assertNull(OfferProfileValidation.error("Consulting", "Northstar Studio"))
    }

    @Test fun namesHaveReasonableMaximumLength() {
        assertEquals("Offer name must be 120 characters or fewer.", OfferProfileValidation.error("x".repeat(121), "Studio"))
    }
}
