package com.pharmasiu.mobile.core;

import com.pharmasiu.mobile.domain.JavaRules;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Migration contracts against the real DemoState and JavaRules classes.
 * No Android framework, network, database, real inventory or payment.
 * Each test constructs an independent RAM-only state; never adds catalog seeds.
 */
public final class DemoStateMigrationContractTest {
    private static final String CUSTOMER_A = "0900000000";
    private static final String CUSTOMER_B = "0911111111";
    private static final String PICKUP_A = "Điểm nhận mẫu A";
    private static final String PICKUP_B = "Điểm nhận mẫu B";

    private static DemoState customer() {
        DemoState state = new DemoState();
        assertTrue(state.login(CUSTOMER_A, "demo1234"));
        return state;
    }

    private static DemoState admin() {
        DemoState state = new DemoState();
        assertTrue(state.login("quanly@pharmasiu.demo", "2222"));
        return state;
    }

    private static DemoState cashier() {
        DemoState state = new DemoState();
        assertTrue(state.login("nhanvien@pharmasiu.demo", "1111"));
        return state;
    }

    private static DemoState.Order request(DemoState state, String contact, String pickup) {
        assertTrue("Customer must be allowed to add demo OTC to cart", state.addToCart("mask-01"));
        DemoState.Order order = state.saveRequest("Nguyễn Văn An", contact, pickup);
        assertNotNull("A fully valid local request must be saved", order);
        return order;
    }

    @Test public void originalCatalogHasExactlyThirtyOrderedUniqueFixtureIds() {
        DemoState state = new DemoState();
        List<String> expected = Arrays.asList(
            "mask-01", "saline-01", "cotton-01", "thermometer-01", "bandage-01",
            "rx-demo-01", "otc-01", "otc-02", "otc-03", "rx-demo-02",
            "beauty-01", "beauty-02", "beauty-03", "beauty-04", "beauty-05",
            "beauty-06", "food-01", "food-02", "food-03", "food-04",
            "food-05", "food-06", "supp-01", "supp-02", "care-01",
            "care-02", "care-03", "device-02", "device-03", "supplies-02");
        List<String> actual = state.products.stream().map(product -> product.id)
            .collect(Collectors.toList());
        assertEquals("Keep exact 30 fixture IDs, order and single seed per item", expected, actual);
        assertEquals(30, new HashSet<>(actual).size());
        assertEquals(30, state.stock.size());
        for (String id : expected) {
            assertNotNull("Catalog lookup must work for " + id, state.product(id));
            assertEquals("Every offline fixture starts with 10 demo units", Integer.valueOf(10),
                state.stock.get(id));
        }
        assertTrue(state.product("rx-demo-01").requiresPrescription);
        assertTrue(state.product("rx-demo-02").requiresPrescription);
        assertEquals(0L, state.product("rx-demo-01").priceVnd);
        assertEquals(0L, state.product("rx-demo-02").priceVnd);
    }

    @Test public void customerProfilesFavoritesAddressesAndOrdersAreIsolatedAcrossLogins() {
        DemoState state = new DemoState();
        assertTrue(state.registerCustomer("Khách hàng B", CUSTOMER_B,
            "customer.b@example.test", "secretB1"));
        assertTrue(state.login(CUSTOMER_A, "demo1234"));
        assertTrue(state.updateCustomer("Khách hàng A", "customer.a@example.test", "01/01/1999"));
        state.toggleFavorite("mask-01");
        assertTrue(state.saveAddress(null, "Nhà A", "Người A", CUSTOMER_A,
            "Địa chỉ liên hệ của A", true));
        DemoState.Order orderA = request(state, CUSTOMER_A, PICKUP_A);
        assertEquals(CUSTOMER_A, orderA.customerPhone);
        assertEquals(1, state.customerOrders().size());
        state.logout();

        assertTrue(state.login(CUSTOMER_B, "secretB1"));
        assertEquals("Khách hàng B", state.currentCustomer().displayName);
        assertTrue("A's favorite must not leak to B", state.favorites.isEmpty());
        assertTrue("A's address must not leak to B", state.myAddresses().isEmpty());
        assertTrue("A's orders must not leak to B", state.customerOrders().isEmpty());
        assertFalse(state.cancelRequest(orderA.reference, "Không có quyền"));
        assertFalse(state.reorderRequest(orderA.reference));
        state.toggleFavorite("cotton-01");
        assertTrue(state.saveAddress(null, "Nhà B", "Người B", CUSTOMER_B,
            "Địa chỉ liên hệ của B", true));
        DemoState.Order orderB = request(state, CUSTOMER_B, PICKUP_B);
        assertEquals(CUSTOMER_B, orderB.customerPhone);
        state.logout();

        assertTrue(state.login(CUSTOMER_A, "demo1234"));
        assertEquals("Khách hàng A", state.currentCustomer().displayName);
        assertEquals("customer.a@example.test", state.currentCustomer().email);
        assertEquals(Arrays.asList("mask-01"), state.favorites);
        assertEquals(1, state.myAddresses().size());
        assertEquals("Nhà A", state.myAddresses().get(0).label);
        assertEquals(1, state.customerOrders().size());
        assertEquals(orderA.reference, state.customerOrders().get(0).reference);
        assertFalse(state.cancelRequest(orderB.reference, "Không sở hữu"));
        state.logout();

        assertTrue(state.login(CUSTOMER_B, "secretB1"));
        assertEquals(Arrays.asList("cotton-01"), state.favorites);
        assertEquals("Nhà B", state.myAddresses().get(0).label);
        assertEquals(1, state.customerOrders().size());
        assertEquals(orderB.reference, state.customerOrders().get(0).reference);
    }

    @Test public void phoneMigrationMovesOwnedOrderFavoritesAndAddressesWithoutLosingLogin() {
        DemoState state = customer();
        String nextPhone = "0922222222";
        state.toggleFavorite("mask-01");
        assertTrue(state.saveAddress(null, "Nhà", "Khách A", CUSTOMER_A,
            "Địa chỉ liên hệ trước chuyển số", true));
        DemoState.Order beforeMigration = request(state, CUSTOMER_A, PICKUP_A);
        assertFalse(state.changeCustomerPhone("invalid"));
        assertTrue(state.changeCustomerPhone(nextPhone));
        assertEquals(nextPhone, state.customerPhone);
        assertEquals(nextPhone, state.currentCustomer().phone);
        assertFalse(state.customerAccounts.containsKey(CUSTOMER_A));
        assertTrue(state.customerAccounts.containsKey(nextPhone));
        assertEquals(nextPhone, beforeMigration.customerPhone);
        assertEquals(1, state.customerOrders().size());
        assertEquals(1, state.myAddresses().size());
        assertEquals(Arrays.asList("mask-01"), state.favorites);
        assertEquals("Khách hàng mẫu", state.currentCustomer().displayName);
        state.logout();

        assertFalse(state.login(CUSTOMER_A, "demo1234"));
        assertTrue(state.login(nextPhone, "demo1234"));
        assertEquals(1, state.customerOrders().size());
        assertEquals(beforeMigration.reference, state.customerOrders().get(0).reference);
        assertEquals("Nhà", state.myAddresses().get(0).label);
        assertEquals(Arrays.asList("mask-01"), state.favorites);
    }

    @Test public void changingPhoneToExistingAccountIsRejectedWithoutMovingData() {
        DemoState state = customer();
        assertTrue(state.registerCustomer("Khách B", CUSTOMER_B, "b@example.test", "secretB1"));
        DemoState.Order order = request(state, CUSTOMER_A, PICKUP_A);
        assertFalse(state.changeCustomerPhone(CUSTOMER_B));
        assertEquals(CUSTOMER_A, state.customerPhone);
        assertEquals(CUSTOMER_A, order.customerPhone);
        assertNotNull(state.customerAccounts.get(CUSTOMER_B));
        assertEquals(1, state.customerOrders().size());
    }

    @Test public void passwordRecoveryOnlyWhenLoggedOutAndUsesNewPasswordInRam() {
        DemoState state = customer();
        assertFalse("Active customer cannot bypass old-password change flow",
            state.resetCustomerPasswordDemo(CUSTOMER_A, "newSecret9"));
        assertFalse(state.changePassword("wrong", "newSecret9"));
        assertEquals("demo1234", state.currentCustomer().password);
        state.logout();
        assertFalse(state.resetCustomerPasswordDemo("0999999999", "newSecret9"));
        assertFalse(state.resetCustomerPasswordDemo(CUSTOMER_A, "123"));
        assertTrue(state.resetCustomerPasswordDemo(" " + CUSTOMER_A + " ", "newSecret9"));
        assertFalse(state.login(CUSTOMER_A, "demo1234"));
        assertTrue(state.login(CUSTOMER_A, "newSecret9"));
        assertFalse(state.resetCustomerPasswordDemo(CUSTOMER_A, "anotherSecret9"));
        state.logout();
        assertTrue(state.login("nhanvien@pharmasiu.demo", "1111"));
        assertFalse("Staff session cannot reset a customer account", state.resetCustomerPasswordDemo(CUSTOMER_A, "anotherSecret9"));
        state.logout();
        assertTrue(state.login(CUSTOMER_A, "newSecret9"));
        assertTrue(state.changePassword("newSecret9", "anotherSecret9"));
        state.logout();
        assertFalse(state.login(CUSTOMER_A, "newSecret9"));
        assertTrue(state.login(CUSTOMER_A, "anotherSecret9"));
    }

    @Test public void adminCashierAndCustomerNeverReceivePharmacistApprovalRights() {
        DemoState state = new DemoState();
        assertFalse(state.canApproveRx());
        assertFalse(JavaRules.canApproveRx(DemoState.ADMIN));
        assertFalse(JavaRules.canApproveRx(DemoState.CASHIER));
        assertFalse(JavaRules.canApproveRx(DemoState.CUSTOMER));
        assertTrue(state.login(CUSTOMER_A, "demo1234"));
        assertFalse(state.canApproveRx());
        assertFalse(state.canAdmin());
        assertFalse(state.canSell());
        state.logout();
        assertTrue(state.login("nhanvien@pharmasiu.demo", "1111"));
        assertFalse(state.canApproveRx());
        assertTrue(state.canSell());
        assertFalse(state.canAdmin());
        state.logout();
        assertTrue(state.login("quanly@pharmasiu.demo", "2222"));
        assertFalse(state.canApproveRx());
        assertTrue(state.canSell());
        assertTrue(state.canAdmin());
        assertFalse(JavaRules.canTransitionForOrder(JavaRules.OrderStatus.RX_REVIEW_PENDING,
            JavaRules.OrderStatus.STOCK_CONFIRMATION_PENDING, true, false));
        assertFalse(JavaRules.canTransitionForOrder(JavaRules.OrderStatus.REQUEST_RECEIVED,
            JavaRules.OrderStatus.STOCK_CONFIRMATION_PENDING, true, true));
    }

    @Test public void pickerSelectionIsBoundOnlyToExactCurrentRxFingerprint() {
        DemoState state = customer();
        assertFalse(state.localPrescriptionSelected());
        assertTrue(state.addToCart("rx-demo-02"));
        assertTrue(state.addToCart("rx-demo-01"));
        String fingerprint = "rx-demo-01:1|rx-demo-02:1";
        assertEquals(fingerprint, state.rxFingerprint());
        assertFalse(state.canSaveRequest("Nguyễn An", CUSTOMER_A, PICKUP_A));
        state.attachLocalPrescription("toa-local.pdf");
        assertTrue(state.localPrescriptionSelected());
        assertEquals(fingerprint, state.chosenPrescriptionFingerprint);
        assertTrue(state.addToCart("mask-01"));
        assertTrue("OTC does not invalidate selected Rx file", state.localPrescriptionSelected());
        state.changeCartQty("mask-01", 3);
        assertTrue(state.localPrescriptionSelected());
        assertTrue(state.canSaveRequest("Nguyễn An", CUSTOMER_A, PICKUP_A));
        state.changeCartQty("rx-demo-01", 2);
        assertEquals("rx-demo-01:2|rx-demo-02:1", state.rxFingerprint());
        assertFalse(state.localPrescriptionSelected());
        assertNull(state.chosenPrescriptionName);
        assertNull(state.chosenPrescriptionFingerprint);
        assertFalse(state.canSaveRequest("Nguyễn An", CUSTOMER_A, PICKUP_A));
        state.attachLocalPrescription("toa-moi.png");
        assertTrue(state.localPrescriptionSelected());
        state.removeLocalPrescription();
        assertFalse(state.localPrescriptionSelected());
        assertNull(state.chosenPrescriptionName);
    }

    @Test public void pickerWithoutRxCannotBeReportedAsCurrentPrescription() {
        DemoState state = customer();
        assertTrue(state.addToCart("mask-01"));
        assertEquals("", state.rxFingerprint());
        state.attachLocalPrescription("file.pdf");
        assertFalse(state.localPrescriptionSelected());
        assertTrue("OTC does not require Rx picker", state.canSaveRequest("Nguyễn An", CUSTOMER_A, PICKUP_A));
    }

    @Test public void pickupPointMustMatchStrictAOrBWithValidContact() {
        DemoState state = customer();
        assertTrue(state.addToCart("mask-01"));
        assertTrue(state.canSaveRequest("Nguyễn An", CUSTOMER_A, PICKUP_A));
        assertTrue(state.canSaveRequest("Nguyễn An", CUSTOMER_A, PICKUP_B));
        for (String invalid : Arrays.asList("Điểm nhận mẫu C", "Điểm nhận mẫu A ",
                "Điểm nhận mẫu", "quầy thật", "")) {
            assertFalse("Reject unknown pickup: " + invalid, state.canSaveRequest(
                "Nguyễn An", CUSTOMER_A, invalid));
        }
        assertFalse(state.canSaveRequest("Nguyễn An", CUSTOMER_A, null));
        assertFalse(state.canSaveRequest("A", CUSTOMER_A, PICKUP_A));
        assertFalse(state.canSaveRequest("Nguyễn An", "12345", PICKUP_A));
        assertNull(state.saveRequest("Nguyễn An", CUSTOMER_A, "Điểm nhận mẫu C"));
        assertTrue(state.orders.isEmpty());
        DemoState.Order order = state.saveRequest("Nguyễn An", CUSTOMER_A, PICKUP_B);
        assertNotNull(order);
        assertEquals(PICKUP_B, order.pickup);
        assertEquals(CUSTOMER_A, order.customerPhone);
        assertEquals(CUSTOMER_A, order.contactPhone);
        assertEquals("Nguyễn An", order.customerName);
    }

    @Test public void fefoAllocationRejectsExpiredQuarantinedAndInsufficientEligibleLots() {
        LocalDate today = LocalDate.now();
        List<DemoState.Lot> lots = new ArrayList<>();
        DemoState.Lot expired = new DemoState.Lot("EXPIRED", "mask-01", 7, today.minusDays(1));
        DemoState.Lot onExpiryDate = new DemoState.Lot("TODAY", "mask-01", 2, today);
        DemoState.Lot quarantined = new DemoState.Lot("QUAR", "mask-01", 40, today.plusDays(1));
        quarantined.quarantined = true;
        DemoState.Lot early = new DemoState.Lot("EARLY", "mask-01", 2, today.plusDays(2));
        DemoState.Lot late = new DemoState.Lot("LATE", "mask-01", 3, today.plusDays(9));
        DemoState.Lot otherProduct = new DemoState.Lot("OTHER", "saline-01", 99, today.plusDays(1));
        lots.addAll(Arrays.asList(expired, onExpiryDate, quarantined, early, late, otherProduct));
        assertFalse(JavaRules.eligibleLot(expired, today));
        assertFalse(JavaRules.eligibleLot(onExpiryDate, today));
        assertFalse(JavaRules.eligibleLot(quarantined, today));
        assertTrue(JavaRules.eligibleLot(early, today));
        List<JavaRules.Allocation> allocation = JavaRules.allocateFefo(lots, "mask-01", 4, today);
        assertNotNull(allocation);
        assertEquals(2, allocation.size());
        assertEquals("EARLY", allocation.get(0).code);
        assertEquals(2, allocation.get(0).quantity);
        assertEquals("LATE", allocation.get(1).code);
        assertEquals(2, allocation.get(1).quantity);
        assertNull(JavaRules.allocateFefo(lots, "mask-01", 6, today));
        assertNull(JavaRules.allocateFefo(lots, "mask-01", 0, today));
        assertEquals(2, early.quantity);
        assertEquals(3, late.quantity);
    }

    @Test public void posSaleRequiresEligibleLotsEvenIfLegacyAggregateSeedIsPositive() {
        DemoState state = admin();
        LocalDate today = LocalDate.now();
        assertTrue(state.receiveLot("mask-01", "LOT-ONE", 1, today.plusDays(8)));
        state.lots.add(new DemoState.Lot("EXPIRED", "mask-01", 50, today.minusDays(1)));
        assertEquals(1, state.eligibleLotQty("mask-01", today));
        assertTrue(state.openShift(0));
        assertTrue(state.addPos("mask-01"));
        assertTrue(state.addPos("mask-01"));
        assertNull("Two units cannot sell from only one eligible dated lot",
            state.savePosSale("CASH", 72000));
        assertEquals(0, state.sales.size());
        state.changePosQty("mask-01", 1);
        assertNotNull(state.savePosSale("CASH", 36000));
        assertEquals(0, state.lots.get(0).quantity);
        assertTrue(state.addPos("mask-01"));
        assertNull("An exhausted dated lot cannot be bypassed by seeded aggregate stock",
            state.savePosSale("CASH", 36000));
    }

    @Test public void quarantiningLotReducesDemoStockAndBlocksItFromFefo() {
        DemoState state = admin();
        LocalDate today = LocalDate.now();
        assertTrue(state.receiveLot("saline-01", "Q-01", 3, today.plusDays(7)));
        assertEquals(3, state.eligibleLotQty("saline-01", today));
        int before = state.stock.get("saline-01");
        assertTrue(state.quarantineLot("Q-01"));
        assertFalse("Repeated quarantine should not change inventory again",
            state.quarantineLot("Q-01"));
        assertEquals(before - 3, (int) state.stock.get("saline-01"));
        assertEquals(0, state.eligibleLotQty("saline-01", today));
        assertNull(JavaRules.allocateFefo(state.lots, "saline-01", 1, today));
    }

    @Test public void adminCannotRenameProductWhileItIsPresentInPosDraft() {
        DemoState state = admin();
        DemoState.Product item = state.product("mask-01");
        String originalName = item.name;
        long originalPrice = item.priceVnd;
        assertTrue(state.openShift(10000));
        assertTrue(state.addPos("mask-01"));
        assertFalse(state.renameProduct("mask-01", "Tên bị chặn", 123000));
        assertEquals(originalName, item.name);
        assertEquals(originalPrice, item.priceVnd);
        assertEquals(Integer.valueOf(1), state.posCart.get("mask-01"));
        assertFalse(state.renameProduct("rx-demo-01", "Thuốc Rx đổi giá", 123000));
        state.changePosQty("mask-01", 0);
        assertTrue("Once draft cleared, admin may edit permitted OTC demo product",
            state.renameProduct("mask-01", "Khẩu trang mẫu mới", 38000));
        assertEquals("Khẩu trang mẫu mới", item.name);
        assertEquals(38000L, item.priceVnd);
        assertTrue(state.closeShift());
    }

    @Test public void cashierCannotRenameAnyCatalogItemEvenWhenShiftOpen() {
        DemoState state = cashier();
        assertTrue(state.openShift(0));
        assertFalse(state.renameProduct("mask-01", "Không đủ quyền", 40000));
        assertFalse(state.renameProduct("rx-demo-01", "Không đủ quyền", 40000));
    }

    @Test public void reconciliationRequiresClosedShiftAndAdminPrivileges() {
        DemoState state = admin();
        assertFalse("No shift has been opened", state.reconcileDemoShift(0, "Tổng kết mẫu"));
        assertTrue(state.openShift(50000));
        assertFalse("Cannot reconcile an active shift", state.reconcileDemoShift(50000, "Tổng kết mẫu"));
        assertTrue(state.shiftReconciliations.isEmpty());
        assertTrue(state.closeShift());
        assertFalse(state.reconcileDemoShift(-1, "Tổng kết mẫu"));
        assertFalse(state.reconcileDemoShift(50000, "x"));
        assertTrue(state.reconcileDemoShift(50000, "Tổng kết mẫu"));
        assertEquals(1, state.shiftReconciliations.size());
        assertEquals(50000L, state.cashCountedVnd);
        state.logout();
        assertTrue(state.login("nhanvien@pharmasiu.demo", "1111"));
        assertFalse("Cashier cannot reconcile an admin-only shift",
            state.reconcileDemoShift(50000, "Đếm thử lại"));
    }

    @Test public void eachShiftCanBeReconciledExactlyOnceAndNextShiftOnce() {
        DemoState state = admin();
        assertTrue(state.openShift(120000));
        assertEquals(1, state.shiftNumber);
        assertTrue(state.closeShift());
        assertTrue(state.reconcileDemoShift(120000, "Đối soát ca một"));
        assertFalse("P1: same shift must not reconcile twice",
            state.reconcileDemoShift(120000, "Đối soát ca một lần nữa"));
        assertEquals(1, state.shiftReconciliations.size());
        assertTrue(state.openShift(10000));
        assertEquals(2, state.shiftNumber);
        assertFalse(state.reconcileDemoShift(10000, "Ca đang mở"));
        assertTrue(state.closeShift());
        assertTrue(state.reconcileDemoShift(10000, "Đối soát ca hai"));
        assertFalse(state.reconcileDemoShift(10000, "Ca hai lặp lại"));
        assertEquals(2, state.shiftReconciliations.size());
    }

    @Test public void cashAndOtherDemoSalesStayDistinctAndDoNotPretendToSettle() {
        DemoState state = cashier();
        assertFalse(state.openShift(-1));
        assertTrue(state.openShift(20000));
        assertTrue(state.addPos("mask-01"));
        assertTrue(state.addPos("mask-01"));
        assertEquals(72000L, state.posTotal());
        assertNull(state.savePosSale("CASH", 71999));
        assertTrue(state.sales.isEmpty());
        DemoState.Sale cash = state.savePosSale("CASH", 80000);
        assertNotNull(cash);
        assertEquals("CASH", cash.method);
        assertEquals(72000L, cash.amountVnd);
        assertEquals(72000L, state.cashCollectedVnd);
        assertEquals(1, state.sales.size());
        assertEquals(8, (int) state.stock.get("mask-01"));
        assertTrue(state.addPos("saline-01"));
        DemoState.Sale other = state.savePosSale("OTHER_DEMO", 0);
        assertNotNull(other);
        assertEquals("OTHER_DEMO", other.method);
        assertEquals(18000L, other.amountVnd);
        assertEquals("Non-cash demo sale must not inflate cash drawer",
            72000L, state.cashCollectedVnd);
        assertEquals(2, state.sales.size());
        assertTrue(state.closeShift());
        assertFalse(state.shiftOpen);
        assertNull("No sale when the shift is closed", state.savePosSale("CASH", 100000));
    }

    @Test public void onlyCashOrOtherDemoPaymentNamesAreAllowed() {
        DemoState state = cashier();
        assertTrue(state.openShift(0));
        assertTrue(state.addPos("mask-01"));
        assertNull("Unknown payment method must not create a sale",
            state.savePosSale("TRANSFER_PRODUCTION", 0));
        assertNull("Null payment method must not create a sale",
            state.savePosSale(null, 0));
        assertEquals("Rejected modes must preserve the unsold draft", 1, state.posCart.size());
        assertTrue(state.sales.isEmpty());
        assertNotNull(state.savePosSale("OTHER_DEMO", 0));
        assertEquals(1, state.sales.size());
    }
}
