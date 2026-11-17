package com.respondo.data.prospects

object ProspectValidation {
    fun error(fullName: String, businessName: String, phoneNumber: String): String? = when {
        fullName.isBlank() || businessName.isBlank() -> "Enter a full name and business name."
        phoneNumber.filter(Char::isDigit).length < 7 -> "Enter a valid phone number with at least 7 digits."
        else -> null
    }
}
