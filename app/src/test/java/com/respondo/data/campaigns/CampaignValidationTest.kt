package com.respondo.data.campaigns

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CampaignValidationTest {
    @Test fun requiresCampaignName() {
        assertEquals("Enter a campaign name.", CampaignValidation.error("  "))
        assertNull(CampaignValidation.error("Spring launch"))
    }
}
