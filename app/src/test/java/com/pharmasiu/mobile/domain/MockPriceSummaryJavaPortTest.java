package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.legacy.LegacyDomainRules;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

/** Original unpriced-Rx examples from MockPriceSummaryTest. */
public class MockPriceSummaryJavaPortTest {
    private DemoState.Product item(String id,long price,boolean rx) {
        return new DemoState.Product(id,id,"",price,"Mẫu",rx,"Hộp","");
    }
    @Test public void unpricedRxIsExcludedAndFlagged() {
        LegacyDomainRules.PriceSummary summary=LegacyDomainRules.summarizePrices(Arrays.asList(
            new LegacyDomainRules.CartLine(item("otc",12000L,false),2),
            new LegacyDomainRules.CartLine(item("rx",0L,true),3)));
        assertEquals(24000L,summary.pricedSubtotalVnd);
        assertEquals(1,summary.unpricedLineCount);
        assertTrue(summary.includesUnpricedItems());
    }
    @Test public void allUnpricedDoesNotMeanFree() {
        LegacyDomainRules.PriceSummary summary=LegacyDomainRules.summarizePrices(
            Collections.singletonList(new LegacyDomainRules.CartLine(item("rx",0L,true),1)));
        assertEquals(0L,summary.pricedSubtotalVnd);
        assertEquals(1,summary.unpricedLineCount);
        assertTrue(summary.includesUnpricedItems());
    }
}
