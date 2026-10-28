package com.pharmasiu.mobile.domain

import java.net.URI

/** Accepts only HTTPS URLs from a trusted catalog. All local demo artwork is offline. */
object CatalogMediaRules {
    fun supportedImageUrl(value: String?): String? {
        val address = value?.trim()?.takeIf { it.isNotBlank() && it.length <= 2048 } ?: return null
        val uri = runCatching { URI(address) }.getOrNull() ?: return null
        return if (
            uri.scheme.equals("https", ignoreCase = true) &&
            !uri.host.isNullOrBlank() &&
            uri.userInfo == null &&
            uri.fragment == null
        ) address else null
    }
}
