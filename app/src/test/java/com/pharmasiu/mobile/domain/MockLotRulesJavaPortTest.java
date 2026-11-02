package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.legacy.LegacyDomainRules;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exact lot-date fixtures from MockLotRulesTest; FEFO and all-or-nothing allocation. */
public class MockLotRulesJavaPortTest {
    private final LocalDate today=LocalDate.of(2026,10,29);
    private LegacyDomainRules.DemoLot lot(String id,int qty,LocalDate expires) {
        return new LegacyDomainRules.DemoLot(id,"p",id,qty,expires);
    }
    private static void allocation(List<LegacyDomainRules.LotAllocation> result,
                                   String[] ids,int[] qty) {
        assertNotNull(result);
        assertEquals(ids.length,result.size());
        for(int i=0;i<ids.length;i++) {
            assertEquals(ids[i],result.get(i).lotId);
            assertEquals(qty[i],result.get(i).quantity);
        }
    }
    @Test public void expiredAndQuarantinedLotsAreNeverAllocated() {
        List<LegacyDomainRules.DemoLot> lots=Arrays.asList(
            lot("expired",20,today.minusDays(1)),lot("today",3,today),
            new LegacyDomainRules.DemoLot("quarantine","p","Q",50,today.plusDays(8),
                LegacyDomainRules.LotCondition.QUARANTINED_DEMO),
            lot("healthy",2,today.plusMonths(1)));
        assertEquals(2,LegacyDomainRules.eligibleQuantity(lots,"p",today));
        assertNull(LegacyDomainRules.allocateFefo(lots,"p",3,today));
        allocation(LegacyDomainRules.allocateFefo(lots,"p",2,today),
            new String[]{"healthy"},new int[]{2});
    }
    @Test public void fefoPrioritizesSoonerExpiryAndUsesSeedOnlyAsFallback() {
        List<LegacyDomainRules.DemoLot> lots=Arrays.asList(
            lot("undated",9,null),lot("later",5,today.plusMonths(3)),
            lot("early",4,today.plusDays(5)));
        allocation(LegacyDomainRules.allocateFefo(lots,"p",6,today),
            new String[]{"early","later"},new int[]{4,2});
        allocation(LegacyDomainRules.allocateFefo(lots,"p",10,today),
            new String[]{"early","later","undated"},new int[]{4,5,1});
        assertNull(LegacyDomainRules.allocateFefo(lots,"p",19,today));
        assertNull(LegacyDomainRules.allocateFefo(lots,"p",0,today));
    }
}
