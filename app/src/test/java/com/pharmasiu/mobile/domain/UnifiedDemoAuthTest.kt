package com.pharmasiu.mobile.domain

import org.junit.Assert.*
import org.junit.Test

class UnifiedDemoAuthTest {
    private val customers = listOf(
        DemoCustomerIdentity("0900000000", "demo@pharmasiu.example", "demo1234"),
        DemoCustomerIdentity("0911111111", null, "newpass1"),
    )

    @Test fun employeesAndManagerUseOneResolver() {
        assertEquals(DemoLoginIdentity.Staff(StaffRole.CASHIER),
            UnifiedDemoAuth.authenticate("NHANVIEN@PHARMASIU.DEMO", "1111", customers))
        assertEquals(DemoLoginIdentity.Staff(StaffRole.ADMIN),
            UnifiedDemoAuth.authenticate("quanly@pharmasiu.demo", "2222", customers))
    }

    @Test fun customerUsesPhoneOrEmailInSameField() {
        val expected = DemoLoginIdentity.Customer("0900000000")
        assertEquals(expected, UnifiedDemoAuth.authenticate("0900000000", "demo1234", customers))
        assertEquals(expected, UnifiedDemoAuth.authenticate(" DEMO@PHARMASIU.EXAMPLE ", "demo1234", customers))
        assertEquals(DemoLoginIdentity.Customer("0911111111"),
            UnifiedDemoAuth.authenticate("0911111111", "newpass1", customers))
    }

    @Test fun wrongCredentialsAreRejectedForAllRoles() {
        assertNull(UnifiedDemoAuth.authenticate("quanly@pharmasiu.demo", "1111", customers))
        assertNull(UnifiedDemoAuth.authenticate("0900000000", "2222", customers))
        assertNull(UnifiedDemoAuth.authenticate("", "demo1234", customers))
        assertNull(UnifiedDemoAuth.authenticate("unknown@example.com", "123456", customers))
    }
}
