package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.legacy.LegacyDomainRules;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/** Preserve all four source Rx fingerprint/current-selection scenarios. */
public class PrescriptionSelectionRulesJavaPortTest {
    private DemoState.Product rxA=product("rxA",0,true);
    private DemoState.Product rxB=product("rxB",0,true);
    private DemoState.Product otc=product("otc",10000,false);
    private static DemoState.Product product(String id,long price,boolean rx) {
        return new DemoState.Product(id,id,"",price,rx?"Thuốc kê đơn":"OTC",rx,"Hộp","");
    }
    private LegacyDomainRules.CartLine line(DemoState.Product p,int qty) {
        return new LegacyDomainRules.CartLine(p,qty);
    }
    @Test public void noSelectedFileNeverPasses() {
        List<LegacyDomainRules.CartLine> lines=Collections.singletonList(line(rxA,1));
        String signature=LegacyDomainRules.rxFingerprint(lines);
        assertFalse(LegacyDomainRules.isCurrentLocalPrescription(false,signature,lines));
    }
    @Test public void prescriptionChangeInvalidatesLocalPickerSelection() {
        List<LegacyDomainRules.CartLine> initial=Collections.singletonList(line(rxA,1));
        String chosen=LegacyDomainRules.rxFingerprint(initial);
        assertTrue(LegacyDomainRules.isCurrentLocalPrescription(true,chosen,initial));
        assertFalse(LegacyDomainRules.isCurrentLocalPrescription(true,chosen,
            Collections.singletonList(line(rxB,1))));
        assertFalse(LegacyDomainRules.isCurrentLocalPrescription(true,chosen,
            Collections.singletonList(line(rxA,2))));
    }
    @Test public void otcOnlyCartCannotClaimPrescription() {
        List<LegacyDomainRules.CartLine> lines=Collections.singletonList(line(otc,1));
        assertFalse(LegacyDomainRules.isCurrentLocalPrescription(true,"",lines));
    }
    @Test public void otcChangeDoesNotInvalidateSameRxAndOrderIsStable() {
        List<LegacyDomainRules.CartLine> selected=Arrays.asList(line(rxA,1),line(rxB,2));
        List<LegacyDomainRules.CartLine> changed=Arrays.asList(line(otc,3),line(rxB,2),line(rxA,1));
        String signature=LegacyDomainRules.rxFingerprint(selected);
        assertEquals(signature,LegacyDomainRules.rxFingerprint(changed));
        assertTrue(LegacyDomainRules.isCurrentLocalPrescription(true,signature,changed));
    }
}
