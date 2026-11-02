package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.legacy.LegacyDomainRules;
import org.junit.Test;
import static org.junit.Assert.*;

/** Port of UnifiedDemoAuthTest: Java role/identity resolver is DemoState.login. */
public class UnifiedDemoAuthJavaPortTest {
    private DemoState customers() {
        DemoState state=new DemoState();
        assertTrue(state.registerCustomer("Khách mới","0911111111",null,"newpass1"));
        return state;
    }
    @Test public void employeesAndManagerUseOneResolver() {
        DemoState staff=customers();
        assertTrue(staff.login("NHANVIEN@PHARMASIU.DEMO","1111"));
        assertEquals(DemoState.CASHIER,staff.role);
        staff.logout();
        assertTrue(staff.login("quanly@pharmasiu.demo","2222"));
        assertEquals(DemoState.ADMIN,staff.role);
    }
    @Test public void customerUsesPhoneOrEmailInSameField() {
        DemoState state=customers();
        assertTrue(state.login("0900000000","demo1234"));
        assertEquals(DemoState.CUSTOMER,state.role);
        assertEquals("0900000000",state.customerPhone);
        state.logout();
        assertTrue(state.login(" DEMO@PHARMASIU.EXAMPLE ","demo1234"));
        assertEquals("0900000000",state.customerPhone);
        state.logout();
        assertTrue(state.login("0911111111","newpass1"));
        assertEquals("0911111111",state.customerPhone);
    }
    @Test public void wrongCredentialsAreRejectedForAllRoles() {
        DemoState state=customers();
        assertFalse(state.login("quanly@pharmasiu.demo","1111"));
        assertFalse(state.login("0900000000","2222"));
        assertFalse(state.login("","demo1234"));
        assertFalse(state.login("unknown@example.com","123456"));
        assertEquals(DemoState.NONE,state.role);
    }
}
