package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.legacy.LegacyDomainRules;
import org.junit.Test;
import static org.junit.Assert.*;

/** Port of CatalogMediaRulesTest with the original eight unsafe URL examples. */
public class CatalogMediaRulesJavaPortTest {
    @Test public void httpsImageIsSupported() {
        assertEquals("https://cdn.example.org/medicine.png",
            LegacyDomainRules.supportedImageUrl(" https://cdn.example.org/medicine.png "));
    }
    @Test public void insecureLocalAndMalformedImagesAreRejected() {
        String[] invalid={null,"","http://example.org/a.png","file:///sdcard/photo.png",
            "content://images/a","https://user:secret@example.org/a.png",
            "https:///a.png","https://example.org/a.png#tracking"};
        for(String value:invalid)assertNull("Unexpected trusted media: "+value,
            LegacyDomainRules.supportedImageUrl(value));
    }
}
