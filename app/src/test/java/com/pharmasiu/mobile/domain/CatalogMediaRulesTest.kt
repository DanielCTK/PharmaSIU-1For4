package com.pharmasiu.mobile.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CatalogMediaRulesTest {
    @Test fun httpsImageIsSupported() {
        assertEquals(
            "https://cdn.example.org/medicine.png",
            CatalogMediaRules.supportedImageUrl(" https://cdn.example.org/medicine.png "),
        )
    }

    @Test fun insecureLocalAndMalformedImagesAreRejected() {
        listOf(null, "", "http://example.org/a.png", "file:///sdcard/photo.png",
            "content://images/a", "https://user:secret@example.org/a.png",
            "https:///a.png", "https://example.org/a.png#tracking")
            .forEach { assertNull(CatalogMediaRules.supportedImageUrl(it)) }
    }
}
