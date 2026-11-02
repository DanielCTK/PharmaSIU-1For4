package com.pharmasiu.mobile.core;

import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

public class LotGuardParityTest {
    @Test public void receiveLotIsBoundedAndDisallowsAmbiguousCodes(){
        DemoState s=new DemoState();
        assertTrue(s.login("quanly@pharmasiu.demo","2222"));
        LocalDate future=LocalDate.now().plusDays(90);
        assertFalse(s.receiveLot("mask-01","A",20,future));
        assertFalse(s.receiveLot("mask-01","LOT-1",1000,future));
        assertTrue(s.receiveLot("mask-01","LOT-1",900,future));
        assertFalse(s.receiveLot("mask-01","LOT-2",100,future));
        assertFalse(s.receiveLot("cotton-01","lot-1",2,future));
        assertTrue(s.receiveLot("mask-01","LOT-2",99,future));
    }
    @Test public void quarantineCannotInvalidateExistingPosDraft(){
        DemoState s=new DemoState();
        assertTrue(s.login("quanly@pharmasiu.demo","2222"));
        assertTrue(s.receiveLot("mask-01","LOT-1",2,LocalDate.now().plusDays(14)));
        assertTrue(s.openShift(0L));
        assertTrue(s.addPos("mask-01"));
        assertFalse(s.quarantineLot("LOT-1"));
        s.changePosQty("mask-01",0);
        assertTrue(s.quarantineLot("LOT-1"));
        assertFalse(s.quarantineLot("LOT-1"));
    }
    @Test public void cashierCannotEditOrQuarantineLot(){
        DemoState s=new DemoState();
        assertTrue(s.login("nhanvien@pharmasiu.demo","1111"));
        assertFalse(s.receiveLot("mask-01","LOT-1",1,LocalDate.now().plusDays(30)));
        assertFalse(s.quarantineLot("LOT-1"));
    }
}
