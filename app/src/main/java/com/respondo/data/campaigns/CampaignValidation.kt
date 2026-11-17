package com.respondo.data.campaigns

object CampaignValidation {
    fun error(name: String): String? = if (name.isBlank()) "Enter a campaign name." else null
}
