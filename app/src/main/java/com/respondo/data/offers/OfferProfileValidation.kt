package com.respondo.data.offers

object OfferProfileValidation {
    fun error(offerName: String, providerName: String): String? = when {
        offerName.isBlank() || providerName.isBlank() -> "Enter an offer name and business or provider name."
        offerName.length > 120 -> "Offer name must be 120 characters or fewer."
        providerName.length > 120 -> "Business or provider name must be 120 characters or fewer."
        else -> null
    }
}
