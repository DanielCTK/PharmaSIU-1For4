package com.pharmasiu.mobile.domain

/**
 * Pure UI-form rules, intentionally independent of Activity/View/Compose.
 * Keep this mock checkout validation separate from real pharmacy order policy.
 */
object CheckoutInputRules {
    fun normalizeName(input: String): String = input.trim()

    fun normalizePhoneInput(input: String): String = input.filter(Char::isDigit).take(11)

    fun isValidName(input: String): Boolean = normalizeName(input).length >= 2

    fun isValidPhone(input: String): Boolean =
        input.length in 10..11 && input.startsWith("0") && input.all(Char::isDigit)

    fun canSubmit(name: String, phone: String, hasProducts: Boolean): Boolean =
        hasProducts && isValidName(name) && isValidPhone(phone)
}
