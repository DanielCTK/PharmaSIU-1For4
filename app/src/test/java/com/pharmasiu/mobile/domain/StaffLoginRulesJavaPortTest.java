package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.legacy.LegacyDomainRules;
import org.junit.Test;
import static org.junit.Assert.*;

/** Four source StaffLoginRulesTest cases using the staff-only Java resolver. */
public class StaffLoginRulesJavaPortTest {
    @Test public void cashierEmailAndPasswordRouteCorrectly() {
        assertEquals(LegacyDomainRules.StaffRole.CASHIER,
            LegacyDomainRules.authenticateStaff("nhanvien@pharmasiu.demo","1111"));
        assertEquals(LegacyDomainRules.StaffRole.CASHIER,
            LegacyDomainRules.authenticateStaff("  NHANVIEN@PharmaSIU.demo  ","1111"));
    }
    @Test public void managerEmailAndPasswordRouteCorrectly() {
        assertEquals(LegacyDomainRules.StaffRole.ADMIN,
            LegacyDomainRules.authenticateStaff("quanly@pharmasiu.demo","2222"));
        assertEquals(2,LegacyDomainRules.StaffRole.values().length);
    }
    @Test public void rejectWrongCredentialsInsteadOfAllowingRoleSelection() {
        assertNull(LegacyDomainRules.authenticateStaff("quanly@pharmasiu.demo","1111"));
        assertNull(LegacyDomainRules.authenticateStaff("nhanvien@pharmasiu.demo","2222"));
        assertNull(LegacyDomainRules.authenticateStaff("unknown@pharmasiu.demo","2222"));
        assertNull(LegacyDomainRules.authenticateStaff("",""));
        assertNull(LegacyDomainRules.authenticateStaff("quanly@pharmasiu.demo"," 2222 "));
    }
    @Test public void demoPasswordNotNeededAfterRoleHasBeenDetermined() {
        assertEquals(LegacyDomainRules.StaffRole.ADMIN,
            LegacyDomainRules.authenticateStaff("quanly@pharmasiu.demo","2222"));
        assertEquals(LegacyDomainRules.StaffRole.CASHIER,
            LegacyDomainRules.authenticateStaff("nhanvien@pharmasiu.demo","1111"));
        DemoState state=new DemoState();
        assertTrue(state.login("quanly@pharmasiu.demo","2222"));
        assertTrue(state.canAdmin());
        assertFalse(state.canApproveRx());
    }
}
