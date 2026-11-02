package com.pharmasiu.mobile.ui.customer;

import com.pharmasiu.mobile.core.DemoState;
import org.junit.Test;

import static org.junit.Assert.*;

/** Contract tests for the RAM APIs consumed by CustomerAccountViews. */
public class CustomerAccountViewsContractTest {
    @Test public void customerEditsAreRamScopedAndRequireCustomerRole() {
        DemoState state = new DemoState();
        assertFalse(state.updateCustomer("Nguyễn An", "an@example.test", ""));
        assertFalse(state.changePassword("demo1234", "newdemo123"));
        assertTrue(state.login("0900000000", "demo1234"));
        assertTrue(state.updateCustomer("Nguyễn An", "an@example.test", "01/01/2000"));
        assertEquals("Nguyễn An", state.currentCustomer().displayName);
        assertEquals("an@example.test", state.currentCustomer().email);
        assertEquals("01/01/2000", state.currentCustomer().birthday);
        assertFalse(state.updateCustomer("A", "bad-email", ""));
        assertTrue(state.changePassword("demo1234", "newdemo123"));
        state.logout();
        assertFalse(state.login("0900000000", "demo1234"));
        assertTrue(state.login("0900000000", "newdemo123"));
        assertEquals("Nguyễn An", state.currentCustomer().displayName);
    }

    @Test public void addressEditDefaultAndDeletionRespectDemoAccount() {
        DemoState state = new DemoState();
        assertFalse(state.saveAddress(null, "Nhà", "Nguyễn An", "0900000000", "Số 1 Hà Nội", true));
        assertTrue(state.login("0900000000", "demo1234"));
        assertTrue(state.saveAddress(null, "Nhà", "Nguyễn An", "0900000000", "Số 1 Hà Nội", true));
        assertTrue(state.saveAddress(null, "Công ty", "Nguyễn An", "0900000000", "Số 2 Hà Nội", false));
        assertEquals(2, state.myAddresses().size());
        String firstId = state.myAddresses().get(0).id;
        DemoState.Address second = state.myAddresses().get(1);
        assertTrue(state.saveAddress(second.id, second.label, second.receiver,
            second.phone, second.detail, true));
        assertFalse(state.myAddresses().get(0).isDefault);
        assertTrue(state.myAddresses().get(1).isDefault);
        assertTrue(state.deleteAddress(second.id));
        assertEquals(1, state.myAddresses().size());
        assertEquals(firstId, state.myAddresses().get(0).id);
        assertTrue(state.myAddresses().get(0).isDefault);
        state.logout();
        assertTrue(state.myAddresses().isEmpty());
        assertFalse(state.deleteAddress(firstId));
    }

    @Test public void favoritesFollowCustomerSessionAndStaffCannotToggle() {
        DemoState state = new DemoState();
        state.toggleFavorite("mask-01");
        assertTrue(state.favorites.isEmpty());
        assertTrue(state.login("0900000000", "demo1234"));
        state.toggleFavorite("mask-01");
        assertEquals(1, state.favorites.size());
        state.logout();
        assertTrue(state.login("nhanvien@pharmasiu.demo", "1111"));
        state.toggleFavorite("cotton-01");
        assertTrue(state.favorites.isEmpty());
        state.logout();
        assertTrue(state.login("0900000000", "demo1234"));
        assertEquals("mask-01", state.favorites.get(0));
        state.toggleFavorite("mask-01");
        assertTrue(state.favorites.isEmpty());
    }

    @Test public void phoneChangeKeepsAddressFavoriteAndExistingRequestsOwned() {
        DemoState state = new DemoState();
        assertTrue(state.login("0900000000", "demo1234"));
        assertTrue(state.saveAddress(null, "Nhà", "Nguyễn An", "0900000000", "Số 1 Hà Nội", true));
        state.toggleFavorite("mask-01");
        assertTrue(state.addToCart("mask-01"));
        DemoState.Order order = state.saveRequest("Nguyễn An", "0900000000", "Điểm nhận mẫu A");
        assertNotNull(order);
        assertFalse(state.changeCustomerPhone("invalid"));
        assertTrue(state.changeCustomerPhone("0911111111"));
        assertEquals("0911111111", state.customerPhone);
        assertEquals("0911111111", state.currentCustomer().phone);
        assertEquals("0911111111", state.customerOrders().get(0).customerPhone);
        assertEquals(1, state.myAddresses().size());
        assertEquals(1, state.favorites.size());
        state.logout();
        assertFalse(state.login("0900000000", "demo1234"));
        assertTrue(state.login("0911111111", "demo1234"));
        assertEquals(1, state.customerOrders().size());
        assertEquals(1, state.myAddresses().size());
        assertEquals(1, state.favorites.size());
    }
}
