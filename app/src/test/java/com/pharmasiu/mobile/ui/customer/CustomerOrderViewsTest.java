package com.pharmasiu.mobile.ui.customer;

import com.pharmasiu.mobile.core.DemoState;
import org.junit.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.Assert.*;

/** JVM checks of guards and actual DemoState transitions used by Java Views. */
public final class CustomerOrderViewsTest {
    private DemoState customer() {
        DemoState state = new DemoState();
        assertTrue(state.login("0900000000", "demo1234"));
        return state;
    }

    @Test public void cartCannotAdvanceEmptyAndOnlyLoggedInCustomerCanProceed() {
        DemoState state = customer();
        assertFalse(CustomerOrderViews.canAdvance(state));
        assertTrue(state.addToCart("mask-01"));
        assertTrue(CustomerOrderViews.canAdvance(state));
        state.logout();
        assertFalse(CustomerOrderViews.canAdvance(state));
    }

    @Test public void rxRequiresLocalSelectionAndInvalidatesAfterCartChange() {
        DemoState state = customer();
        assertTrue(state.addToCart("rx-demo-01"));
        assertFalse(CustomerOrderViews.canAdvance(state));
        state.attachLocalPrescription("test-prescription.pdf");
        assertTrue(CustomerOrderViews.canAdvance(state));
        state.changeCartQty("rx-demo-01", 2);
        assertFalse(state.localPrescriptionSelected());
        assertFalse(CustomerOrderViews.canAdvance(state));
        state.attachLocalPrescription("test-again.png");
        assertTrue(CustomerOrderViews.canAdvance(state));
        state.removeLocalPrescription();
        assertFalse(CustomerOrderViews.canAdvance(state));
    }

    @Test public void checkoutPickupMustBeAorBAndBadFormDoesNotCreateOrder() {
        DemoState state = customer();
        assertTrue(state.addToCart("mask-01"));
        assertTrue(CustomerOrderViews.validPickup(CustomerOrderViews.PICKUP_A));
        assertTrue(CustomerOrderViews.validPickup(CustomerOrderViews.PICKUP_B));
        assertFalse(CustomerOrderViews.validPickup("Nhà riêng"));
        assertNull(CustomerOrderViews.submit(state, "Nguyễn An", "0900000000",
            "Nhà riêng", new AtomicBoolean()));
        assertNull(CustomerOrderViews.submit(state, "A", "0900000000",
            CustomerOrderViews.PICKUP_A, new AtomicBoolean()));
        assertNull(CustomerOrderViews.submit(state, "Nguyễn An", "1234",
            CustomerOrderViews.PICKUP_A, new AtomicBoolean()));
        assertTrue(state.orders.isEmpty());
        assertEquals(1, (int) state.cart.get("mask-01"));
    }

    @Test public void requestIsSavedOnceAndOnlyLocally() {
        DemoState state = customer();
        assertTrue(state.addToCart("mask-01"));
        AtomicBoolean inFlight = new AtomicBoolean();
        DemoState.Order order = CustomerOrderViews.submit(state, "Nguyễn An", "0900000000",
            CustomerOrderViews.PICKUP_B, inFlight);
        assertNotNull(order);
        assertEquals("DEMO-0001", order.reference);
        assertEquals(CustomerOrderViews.PICKUP_B, order.pickup);
        assertEquals("Đã lưu yêu cầu DEMO", order.status);
        assertEquals(1, state.customerOrders().size());
        assertTrue(state.cart.isEmpty());
        assertSame(order, CustomerOrderViews.selectedCustomerOrder(state));
        assertNull(CustomerOrderViews.submit(state, "Nguyễn An", "0900000000",
            CustomerOrderViews.PICKUP_B, inFlight));
        assertEquals(1, state.customerOrders().size());
    }

    @Test public void rxRequestNeverBecomesAnApprovedPrescription() {
        DemoState state = customer();
        assertTrue(state.addToCart("rx-demo-01"));
        assertNull(CustomerOrderViews.submit(state, "Nguyễn An", "0900000000",
            CustomerOrderViews.PICKUP_A, new AtomicBoolean()));
        state.attachLocalPrescription("demo-rx.pdf");
        DemoState.Order order = CustomerOrderViews.submit(state, "Nguyễn An", "0900000000",
            CustomerOrderViews.PICKUP_A, new AtomicBoolean());
        assertNotNull(order);
        assertTrue(order.containsRx);
        assertFalse(state.canApproveRx());
        assertFalse(state.localPrescriptionSelected());
        assertEquals("Đã lưu yêu cầu DEMO", order.status);
    }

    @Test public void cancelReasonRequiredAndCancellationIsFinalForLocalRecord() {
        DemoState state = customer();
        assertTrue(state.addToCart("mask-01"));
        DemoState.Order order = CustomerOrderViews.submit(state, "Nguyễn An", "0900000000",
            CustomerOrderViews.PICKUP_A, new AtomicBoolean());
        assertNotNull(order);
        assertFalse(CustomerOrderViews.canCancel(state, order, "ab"));
        assertFalse(CustomerOrderViews.canCancel(state, order, "a".repeat(161)));
        assertTrue(CustomerOrderViews.canCancel(state, order, "Không còn nhu cầu"));
        assertTrue(state.cancelRequest(order.reference, "Không còn nhu cầu"));
        assertEquals("Đã hủy DEMO", order.status);
        assertEquals("Không còn nhu cầu", order.cancelReason);
        assertFalse(CustomerOrderViews.canCancel(state, order, "Hủy lần nữa"));
        assertFalse(state.cancelRequest(order.reference, "Hủy lần nữa"));
    }

    @Test public void reorderRestoresCartButRequiresRxFileAgain() {
        DemoState state = customer();
        assertTrue(state.addToCart("rx-demo-01"));
        assertTrue(state.addToCart("mask-01"));
        state.attachLocalPrescription("demo-rx.pdf");
        DemoState.Order order = CustomerOrderViews.submit(state, "Nguyễn An", "0900000000",
            CustomerOrderViews.PICKUP_A, new AtomicBoolean());
        assertNotNull(order);
        assertTrue(state.reorderRequest(order.reference));
        assertEquals(1, (int) state.cart.get("rx-demo-01"));
        assertEquals(1, (int) state.cart.get("mask-01"));
        assertFalse(state.localPrescriptionSelected());
        assertFalse(CustomerOrderViews.canAdvance(state));
    }

    @Test public void aDifferentRoleCannotSeeOrdersOrReorder() {
        DemoState state = customer();
        assertTrue(state.addToCart("mask-01"));
        DemoState.Order order = CustomerOrderViews.submit(state, "Nguyễn An", "0900000000",
            CustomerOrderViews.PICKUP_A, new AtomicBoolean());
        assertNotNull(order);
        assertTrue(state.login("nhanvien@pharmasiu.demo", "1111"));
        assertNull(CustomerOrderViews.selectedCustomerOrder(state));
        assertFalse(CustomerOrderViews.canCancel(state, order, "Hủy bản demo"));
        assertFalse(state.reorderRequest(order.reference));
        assertFalse(CustomerOrderViews.canAdvance(state));
    }
}
