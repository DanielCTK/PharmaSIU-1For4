package com.pharmasiu.mobile.domain

import org.junit.Assert.*
import org.junit.Test

class StaffLoginRulesTest {
    @Test fun cashierEmailAndPasswordRouteCorrectly() {
        assertEquals(StaffRole.CASHIER,
            StaffDemoAccess.authenticate("nhanvien@pharmasiu.demo", "1111"))
        assertEquals(StaffRole.CASHIER,
            StaffDemoAccess.authenticate("  NHANVIEN@PharmaSIU.demo  ", "1111"))
    }

    @Test fun managerEmailAndPasswordRouteCorrectly() {
        assertEquals(StaffRole.ADMIN,
            StaffDemoAccess.authenticate("quanly@pharmasiu.demo", "2222"))
        assertEquals(2, StaffDemoAccess.demoAccounts.size)
    }

    @Test fun rejectWrongCredentialsInsteadOfAllowingRoleSelection() {
        assertNull(StaffDemoAccess.authenticate("quanly@pharmasiu.demo", "1111"))
        assertNull(StaffDemoAccess.authenticate("nhanvien@pharmasiu.demo", "2222"))
        assertNull(StaffDemoAccess.authenticate("unknown@pharmasiu.demo", "2222"))
        assertNull(StaffDemoAccess.authenticate("", ""))
        assertNull(StaffDemoAccess.authenticate("quanly@pharmasiu.demo", " 2222 "))
    }

    @Test fun demoPasswordNotNeededAfterRoleHasBeenDetermined() {
        assertEquals(StaffRole.ADMIN, StaffDemoAccess.authenticate(
            StaffDemoAccess.ADMIN_EMAIL, "2222"))
        assertEquals(StaffRole.CASHIER, StaffDemoAccess.authenticate(
            StaffDemoAccess.CASHIER_EMAIL, "1111"))
    }
}
