package com.pharmasiu.mobile.legacy;

import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.domain.JavaRules;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Cross-language fixtures mirroring original app/src/test/.../domain Kotlin tests.
 * Catalog always comes from DemoState, which Prime already seeded with 30 fixtures.
 */
public class LegacyDomainRulesTest {
    private final DemoState demo = new DemoState();
    private final LocalDate today = LocalDate.of(2026, 10, 29);

    private DemoState.Product product(String id) {
        DemoState.Product result = demo.product(id);
        assertNotNull("Demo catalog must expose product " + id, result);
        return result;
    }

    private LegacyDomainRules.CartLine line(String id, int qty) {
        return new LegacyDomainRules.CartLine(product(id), qty);
    }

    private List<String> ids(List<DemoState.Product> items) {
        return items.stream().map(p -> p.id).collect(Collectors.toList());
    }

    @Test public void normalizationMatchesKotlinVietnameseAccentsAndCase() {
        assertEquals("sua rua mat", LegacyDomainRules.normalizeSearch("  SỮA RỬA MẶT  "));
        assertEquals("dien thoai", LegacyDomainRules.normalizeSearch("ĐiỆN THOẠI"));
        // Kotlin normalized() trims outer whitespace but keeps internal spacing.
        assertEquals("a   b", LegacyDomainRules.normalizeSearch("  À   B  "));
        assertEquals("", LegacyDomainRules.normalizeSearch("  "));
    }

    @Test public void catalogSearchIsOrderIndependentAcrossWordsAndAccentFormats() {
        assertEquals(30, demo.products.size());
        List<String> accented = ids(LegacyDomainRules.filterCatalog(
            demo.products, "SỮA RỬA MẶT", "Tất cả", "Đề xuất"));
        List<String> unaccented = ids(LegacyDomainRules.filterCatalog(
            demo.products, "  sua   rua  mat  ", "Tất cả", "Đề xuất"));
        List<String> reordered = ids(LegacyDomainRules.filterCatalog(
            demo.products, "mat rua sua", "Tất cả", "Đề xuất"));
        assertFalse(accented.isEmpty());
        assertEquals(accented, unaccented);
        assertEquals(accented, reordered);
        assertTrue(LegacyDomainRules.filterCatalog(demo.products, "not-in-catalog",
            "Tất cả", "Đề xuất").isEmpty());
    }

    @Test public void catalogFoodIncludesSupplementsButExcludesRxAndCosmetics() {
        List<DemoState.Product> food = LegacyDomainRules.filterCatalog(
            demo.products, "", "Thực phẩm", "Đề xuất");
        assertFalse(food.isEmpty());
        assertTrue(food.stream().anyMatch(p -> "Thực phẩm bổ sung".equals(p.category)));
        assertTrue(food.stream().allMatch(p -> "Thực phẩm".equals(p.category)
            || "Thực phẩm bổ sung".equals(p.category)));
        List<DemoState.Product> rx = LegacyDomainRules.filterCatalog(
            demo.products, "", "Kê đơn", "Đề xuất");
        assertFalse(rx.isEmpty());
        assertTrue(rx.stream().allMatch(p -> p.requiresPrescription));
    }

    @Test public void catalogPriceSortKeepsZeroPriceLastAndAlphabeticalIsDeterministic() {
        List<DemoState.Product> asc = LegacyDomainRules.filterCatalog(
            demo.products, "", "Tất cả", "Giá thấp");
        List<DemoState.Product> desc = LegacyDomainRules.filterCatalog(
            demo.products, "", "Tất cả", "Giá cao");
        assertEquals(demo.products.size(), asc.size());
        assertTrue(asc.get(asc.size() - 1).priceVnd <= 0L);
        assertTrue(desc.get(desc.size() - 1).priceVnd <= 0L);
        assertEquals("rx-demo-01", desc.get(desc.size() - 2).id);
        long previousAsc = Long.MIN_VALUE;
        for (DemoState.Product p : asc) {
            if (p.priceVnd <= 0L) break;
            assertTrue(p.priceVnd >= previousAsc);
            previousAsc = p.priceVnd;
        }
        long previousDesc = Long.MAX_VALUE;
        for (DemoState.Product p : desc) {
            if (p.priceVnd <= 0L) break;
            assertTrue(p.priceVnd <= previousDesc);
            previousDesc = p.priceVnd;
        }
        List<DemoState.Product> alphabetical = LegacyDomainRules.filterCatalog(
            demo.products, "", "Tất cả", "Tên A–Z");
        for (int i = 1; i < alphabetical.size(); i++) {
            assertTrue(String.CASE_INSENSITIVE_ORDER.compare(
                alphabetical.get(i - 1).name, alphabetical.get(i).name) <= 0);
        }
    }

    @Test public void catalogMediaAcceptsOnlySafeStructuredHttpsUri() {
        assertEquals("https://cdn.example.org/medicine.png",
            LegacyDomainRules.supportedImageUrl(" https://cdn.example.org/medicine.png "));
        assertEquals("HTTPS://cdn.example.org/p.png?size=2",
            LegacyDomainRules.supportedImageUrl("HTTPS://cdn.example.org/p.png?size=2"));
        for (String rejected : Arrays.asList("", " ", "http://cdn.example.org/a.png",
                "file:///sdcard/a.png", "content://media/photo", "https:///a.png",
                "https://user:secret@example.org/a.png", "https://example.org/a.png#fragment",
                "https://example.org/space here.png", "//example.org/a.png")) {
            assertNull(rejected, LegacyDomainRules.supportedImageUrl(rejected));
        }
        assertNull(LegacyDomainRules.supportedImageUrl(null));
        assertNull(LegacyDomainRules.supportedImageUrl("https://cdn.example.org/" +
            String.join("", Collections.nCopies(2049, "a"))));
    }

    @Test public void checkoutNameAndPhoneMatchKotlinFormRules() {
        assertEquals("Nguyễn Văn An", LegacyDomainRules.normalizeName("  Nguyễn Văn An  "));
        assertEquals("0900000000", LegacyDomainRules.normalizePhoneInput("09 00-000000"));
        assertEquals(11, LegacyDomainRules.normalizePhoneInput("0900-000-000-999").length());
        assertTrue(LegacyDomainRules.isValidName("  Nguyễn An  "));
        assertFalse(LegacyDomainRules.isValidName(" A "));
        assertTrue(LegacyDomainRules.isValidPhone("0900000000"));
        assertTrue(LegacyDomainRules.isValidPhone("09000000000"));
        for (String invalid : Arrays.asList("+84900000000", "09000abc00", "090000000000", "12345")) {
            assertFalse(LegacyDomainRules.isValidPhone(invalid));
        }
        assertTrue(LegacyDomainRules.canSubmitCheckout(" Nguyễn An ", "0900000000", true));
        assertFalse(LegacyDomainRules.canSubmitCheckout(" Nguyễn An ", "0900000000", false));
        assertFalse(LegacyDomainRules.canSubmitCheckout(" A ", "0900000000", true));
    }

    @Test public void cartRequiresLocalRxSelectionAndEnforcesOneToTwenty() {
        List<LegacyDomainRules.CartLine> rx = Collections.singletonList(line("rx-demo-01", 1));
        assertTrue(LegacyDomainRules.requiresPrescription(rx));
        assertFalse(LegacyDomainRules.canProceedToRequest(rx, false));
        assertTrue(LegacyDomainRules.canProceedToRequest(rx, true));
        assertTrue(LegacyDomainRules.canProceedToRequest(
            Collections.singletonList(line("mask-01", 20)), false));
        assertFalse(LegacyDomainRules.canProceedToRequest(
            Collections.singletonList(line("mask-01", 21)), false));
        assertFalse(LegacyDomainRules.canProceedToRequest(Collections.emptyList(), true));
        try {
            line("mask-01", 0);
            fail("Kotlin CartLine rejects quantity <= 0");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("quantity"));
        }
    }

    @Test public void mockPricingExcludesUnpricedRxButNeverCallsItFree() {
        List<LegacyDomainRules.CartLine> lines = Arrays.asList(
            line("mask-01", 2), line("rx-demo-01", 3));
        LegacyDomainRules.PriceSummary partial = LegacyDomainRules.summarizePrices(lines);
        assertEquals(72000L, partial.pricedSubtotalVnd);
        assertEquals(1, partial.unpricedLineCount);
        assertTrue(partial.includesUnpricedItems());
        LegacyDomainRules.PriceSummary allUnknown = LegacyDomainRules.summarizePrices(
            Collections.singletonList(line("rx-demo-01", 1)));
        assertEquals(0L, allUnknown.pricedSubtotalVnd);
        assertEquals(1, allUnknown.unpricedLineCount);
        assertTrue(allUnknown.includesUnpricedItems());
        assertFalse(LegacyDomainRules.summarizePrices(
            Collections.singletonList(line("mask-01", 1))).includesUnpricedItems());
    }

    @Test public void fingerprintTracksOnlyRxIdsAndQuantitiesWithoutFalseApprovals() {
        List<LegacyDomainRules.CartLine> initial = Arrays.asList(
            line("rx-demo-01", 1), line("rx-demo-02", 2));
        String fingerprint = LegacyDomainRules.rxFingerprint(initial);
        assertEquals("rx-demo-01:1|rx-demo-02:2", fingerprint);
        List<LegacyDomainRules.CartLine> reordered = Arrays.asList(
            line("mask-01", 3), line("rx-demo-02", 2), line("rx-demo-01", 1));
        assertEquals(fingerprint, LegacyDomainRules.rxFingerprint(reordered));
        assertTrue(LegacyDomainRules.isCurrentLocalPrescription(true, fingerprint, reordered));
        assertFalse(LegacyDomainRules.isCurrentLocalPrescription(false, fingerprint, reordered));
        assertFalse(LegacyDomainRules.isCurrentLocalPrescription(true, fingerprint,
            Collections.singletonList(line("rx-demo-01", 2))));
        assertFalse(LegacyDomainRules.isCurrentLocalPrescription(true, "",
            Collections.singletonList(line("mask-01", 1))));
    }

    @Test public void canonicalSevenStagesAndReasonsMirrorAllElevenKotlinStatuses() {
        assertEquals(7, LegacyDomainRules.CanonicalOrderStage.values().length);
        assertEquals(JavaRules.OrderStatus.values().length, 11);
        for (JavaRules.OrderStatus internal : JavaRules.OrderStatus.values()) {
            LegacyDomainRules.CanonicalOrderStage stage = LegacyDomainRules.canonicalStage(internal);
            assertNotNull(stage);
            assertEquals(JavaRules.publicStage(internal), stage.label);
        }
        assertEquals(LegacyDomainRules.CanonicalOrderStage.PENDING,
            LegacyDomainRules.canonicalStage(JavaRules.OrderStatus.STOCK_CONFIRMATION_PENDING));
        assertEquals(LegacyDomainRules.CanonicalOrderStage.RX_PENDING,
            LegacyDomainRules.canonicalStage(JavaRules.OrderStatus.RX_NEEDS_UPDATE));
        assertEquals(LegacyDomainRules.CanonicalOrderStage.HELD,
            LegacyDomainRules.canonicalStage(JavaRules.OrderStatus.PREPARING));
        assertEquals(LegacyDomainRules.CanonicalOrderStage.READY,
            LegacyDomainRules.canonicalStage(JavaRules.OrderStatus.READY_FOR_PICKUP));
        assertEquals(LegacyDomainRules.CanonicalOrderStage.CANCELLED,
            LegacyDomainRules.canonicalStage(JavaRules.OrderStatus.EXPIRED));
        assertEquals("Quá hạn giữ hàng",
            LegacyDomainRules.terminationReasonLabel(JavaRules.OrderStatus.EXPIRED));
        assertEquals("Đã hủy",
            LegacyDomainRules.terminationReasonLabel(JavaRules.OrderStatus.CANCELLED));
        assertEquals("Toa bị từ chối",
            LegacyDomainRules.terminationReasonLabel(JavaRules.OrderStatus.RX_REJECTED));
        assertNull(LegacyDomainRules.terminationReasonLabel(JavaRules.OrderStatus.HELD));
    }

    @Test public void stateTransitionGraphMatchesJavaRulesIncludingTerminalCases() {
        for (JavaRules.OrderStatus from : JavaRules.OrderStatus.values()) {
            for (JavaRules.OrderStatus to : JavaRules.OrderStatus.values()) {
                assertEquals(from + " -> " + to, JavaRules.canTransition(from, to),
                    LegacyDomainRules.canTransition(from, to));
            }
        }
        EnumSet<JavaRules.OrderStatus> terminal = EnumSet.of(
            JavaRules.OrderStatus.COMPLETED, JavaRules.OrderStatus.EXPIRED,
            JavaRules.OrderStatus.CANCELLED, JavaRules.OrderStatus.RX_REJECTED);
        for (JavaRules.OrderStatus from : terminal) {
            for (JavaRules.OrderStatus to : JavaRules.OrderStatus.values()) {
                assertFalse(LegacyDomainRules.canTransition(from, to));
            }
        }
        assertFalse(LegacyDomainRules.canTransition(
            JavaRules.OrderStatus.REQUEST_RECEIVED, JavaRules.OrderStatus.COMPLETED));
        assertTrue(LegacyDomainRules.canTransition(
            JavaRules.OrderStatus.READY_FOR_PICKUP, JavaRules.OrderStatus.COMPLETED));
    }

    @Test public void rxCannotSkipPharmacistReviewEvenIfApprovedFlagIsPassed() {
        assertFalse(LegacyDomainRules.canTransitionForOrder(
            JavaRules.OrderStatus.REQUEST_RECEIVED,
            JavaRules.OrderStatus.STOCK_CONFIRMATION_PENDING, true, true));
        assertFalse(LegacyDomainRules.canTransitionForOrder(
            JavaRules.OrderStatus.RX_REVIEW_PENDING,
            JavaRules.OrderStatus.STOCK_CONFIRMATION_PENDING, true, false));
        assertTrue(LegacyDomainRules.canTransitionForOrder(
            JavaRules.OrderStatus.RX_REVIEW_PENDING,
            JavaRules.OrderStatus.STOCK_CONFIRMATION_PENDING, true, true));
        assertTrue(LegacyDomainRules.canTransitionForOrder(
            JavaRules.OrderStatus.REQUEST_RECEIVED,
            JavaRules.OrderStatus.STOCK_CONFIRMATION_PENDING, false, false));
    }

    @Test public void fefoRejectsExpiredQuarantinedEmptyAndShortageWithoutMutation() {
        List<LegacyDomainRules.DemoLot> lots = Arrays.asList(
            new LegacyDomainRules.DemoLot("expired", "p", "EX", 20, today.minusDays(1)),
            new LegacyDomainRules.DemoLot("today", "p", "TODAY", 3, today),
            new LegacyDomainRules.DemoLot("quarantine", "p", "Q", 50,
                today.plusDays(8), LegacyDomainRules.LotCondition.QUARANTINED_DEMO),
            new LegacyDomainRules.DemoLot("empty", "p", "EMPTY", 0, today.plusDays(3)),
            new LegacyDomainRules.DemoLot("healthy", "p", "OK", 2, today.plusMonths(1)));
        assertEquals(2, LegacyDomainRules.eligibleQuantity(lots, "p", today));
        assertNull(LegacyDomainRules.allocateFefo(lots, "p", 3, today));
        List<LegacyDomainRules.LotAllocation> picked =
            LegacyDomainRules.allocateFefo(lots, "p", 2, today);
        assertNotNull(picked);
        assertEquals(1, picked.size());
        assertEquals("healthy", picked.get(0).lotId);
        assertEquals(2, picked.get(0).quantity);
        assertNull(LegacyDomainRules.allocateFefo(lots, "p", 0, today));
        assertNull(LegacyDomainRules.allocateFefo(lots, "p", -1, today));
        assertEquals(2, lots.get(4).quantity);
    }

    @Test public void fefoUsesSoonestDatedThenUndatedSeedAsLastFallback() {
        List<LegacyDomainRules.DemoLot> lots = Arrays.asList(
            new LegacyDomainRules.DemoLot("undated", "p", "SAMPLE", 9, null),
            new LegacyDomainRules.DemoLot("later", "p", "L", 5, today.plusMonths(3)),
            new LegacyDomainRules.DemoLot("early", "p", "E", 4, today.plusDays(5)));
        List<LegacyDomainRules.LotAllocation> first =
            LegacyDomainRules.allocateFefo(lots, "p", 6, today);
        assertEquals(Arrays.asList("early", "later"),
            first.stream().map(l -> l.lotId).collect(Collectors.toList()));
        assertEquals(4, first.get(0).quantity);
        assertEquals(2, first.get(1).quantity);
        List<LegacyDomainRules.LotAllocation> withFallback =
            LegacyDomainRules.allocateFefo(lots, "p", 10, today);
        assertEquals(Arrays.asList("early", "later", "undated"),
            withFallback.stream().map(l -> l.lotId).collect(Collectors.toList()));
        assertEquals(1, withFallback.get(2).quantity);
        assertNull(LegacyDomainRules.allocateFefo(lots, "p", 19, today));
        assertEquals(9, lots.get(0).quantity);
    }

    @Test public void datedFefoMatchesExistingJavaRulesForSharedSubset() {
        List<LegacyDomainRules.DemoLot> legacyLots = Arrays.asList(
            new LegacyDomainRules.DemoLot("FIRST", "p", "FIRST", 4, today.plusDays(3)),
            new LegacyDomainRules.DemoLot("SECOND", "p", "SECOND", 3, today.plusDays(9)));
        List<DemoState.Lot> javaLots = Arrays.asList(
            new DemoState.Lot("FIRST", "p", 4, today.plusDays(3)),
            new DemoState.Lot("SECOND", "p", 3, today.plusDays(9)));
        List<LegacyDomainRules.LotAllocation> legacy =
            LegacyDomainRules.allocateFefo(legacyLots, "p", 6, today);
        List<JavaRules.Allocation> existing = JavaRules.allocateFefo(javaLots, "p", 6, today);
        assertNotNull(legacy);
        assertNotNull(existing);
        assertEquals(existing.size(), legacy.size());
        for (int i = 0; i < legacy.size(); i++) {
            assertEquals(existing.get(i).code, legacy.get(i).lotId);
            assertEquals(existing.get(i).quantity, legacy.get(i).quantity);
        }
    }

    @Test public void demoStaffRoleAuthenticationIsExactAndDoesNotGrantPharmacist() {
        assertEquals(LegacyDomainRules.StaffRole.CASHIER,
            LegacyDomainRules.authenticateStaff(" NHANVIEN@PharmaSIU.demo ", "1111"));
        assertEquals(LegacyDomainRules.StaffRole.ADMIN,
            LegacyDomainRules.authenticateStaff("quanly@pharmasiu.demo", "2222"));
        assertNull(LegacyDomainRules.authenticateStaff("quanly@pharmasiu.demo", "1111"));
        assertNull(LegacyDomainRules.authenticateStaff("nhanvien@pharmasiu.demo", "2222"));
        assertNull(LegacyDomainRules.authenticateStaff("nhanvien@pharmasiu.demo", " 1111 "));
        assertNull(LegacyDomainRules.authenticateStaff("", ""));
        assertTrue(LegacyDomainRules.acceptsPin(LegacyDomainRules.StaffRole.CASHIER, "1111"));
        assertTrue(LegacyDomainRules.acceptsPin(LegacyDomainRules.StaffRole.ADMIN, "2222"));
        assertFalse(LegacyDomainRules.acceptsPin(LegacyDomainRules.StaffRole.ADMIN, "1111"));
        assertTrue(LegacyDomainRules.maySell(LegacyDomainRules.StaffRole.CASHIER));
        assertTrue(LegacyDomainRules.maySell(LegacyDomainRules.StaffRole.ADMIN));
        assertFalse(LegacyDomainRules.maySell(null));
        assertFalse(LegacyDomainRules.mayEditCatalog(LegacyDomainRules.StaffRole.CASHIER));
        assertTrue(LegacyDomainRules.mayEditCatalog(LegacyDomainRules.StaffRole.ADMIN));
        assertFalse(LegacyDomainRules.mayReconcileShift(LegacyDomainRules.StaffRole.CASHIER));
        assertTrue(LegacyDomainRules.mayReconcileShift(LegacyDomainRules.StaffRole.ADMIN));
        assertFalse(LegacyDomainRules.mayApprovePrescription(LegacyDomainRules.StaffRole.ADMIN));
        assertFalse(LegacyDomainRules.mayApprovePrescription(LegacyDomainRules.StaffRole.CASHIER));
    }

    @Test public void counterRulesRejectRxOverStockAndInsufficientCash() {
        DemoState.Product otc = product("mask-01"), rx = product("rx-demo-01");
        assertTrue(LegacyDomainRules.counterSellable(otc));
        assertFalse(LegacyDomainRules.counterSellable(rx));
        assertTrue(LegacyDomainRules.availableToAddAtCounter(otc, 1, 2));
        assertFalse(LegacyDomainRules.availableToAddAtCounter(otc, 2, 2));
        assertFalse(LegacyDomainRules.availableToAddAtCounter(rx, 0, 20));
        LegacyDomainRules.CounterLine regular = new LegacyDomainRules.CounterLine(
            otc.id, otc.priceVnd, 2, false);
        List<LegacyDomainRules.CounterLine> lines = Collections.singletonList(regular);
        long total = LegacyDomainRules.counterTotal(lines);
        assertEquals(72000L, total);
        Map<String, Integer> stock = Collections.singletonMap(otc.id, 5);
        assertFalse(LegacyDomainRules.canConfirmCounter(lines, stock, false,
            LegacyDomainRules.CounterPayment.CASH, total));
        assertFalse(LegacyDomainRules.canConfirmCounter(lines, stock, true,
            LegacyDomainRules.CounterPayment.CASH, total - 1));
        assertTrue(LegacyDomainRules.canConfirmCounter(lines, stock, true,
            LegacyDomainRules.CounterPayment.CASH, total));
        assertTrue(LegacyDomainRules.canConfirmCounter(lines, stock, true,
            LegacyDomainRules.CounterPayment.OTHER_DEMO, 0));
        assertFalse(LegacyDomainRules.canConfirmCounter(lines, Collections.singletonMap(otc.id, 1),
            true, LegacyDomainRules.CounterPayment.OTHER_DEMO, 0));
        assertFalse(LegacyDomainRules.canConfirmCounter(Collections.singletonList(
            new LegacyDomainRules.CounterLine(rx.id, 0, 1, true)), stock,
            true, LegacyDomainRules.CounterPayment.CASH, 100000));
        assertFalse(LegacyDomainRules.canConfirmCounter(Collections.singletonList(
            new LegacyDomainRules.CounterLine(otc.id, otc.priceVnd, 31, false)),
            Collections.singletonMap(otc.id, 50), true,
            LegacyDomainRules.CounterPayment.OTHER_DEMO, 0));
        assertEquals(5000L, LegacyDomainRules.changeDue(total, total + 5000));
        assertEquals(0L, LegacyDomainRules.changeDue(total, total - 1));
    }
}
