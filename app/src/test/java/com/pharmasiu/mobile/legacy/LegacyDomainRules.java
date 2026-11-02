package com.pharmasiu.mobile.legacy;

import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.domain.JavaRules;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Pure-Java equivalents of the legacy Kotlin domain contracts.
 * This class does not own or seed catalog data, authenticate a production user,
 * approve prescriptions, reserve inventory, settle a payment, or persist orders.
 * Catalog entries are the existing, RAM-only DemoState.Product values.
 */
public final class LegacyDomainRules {
    public static final int MAX_CUSTOMER_QUANTITY = 20;
    public static final int MAX_COUNTER_QUANTITY = 30;
    private static final Locale VIETNAMESE = new Locale("vi", "VN");

    private LegacyDomainRules() { }

    /** Kotlin CatalogSearchRules.normalized: lower-case, strip accents, đ→d, trim. */
    public static String normalizeSearch(String text) {
        String lower = Objects.requireNonNull(text, "text").toLowerCase(VIETNAMESE);
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .replace('đ', 'd').trim();
    }

    /**
     * Kotlin CatalogSearchRules.filter: every search token must be present;
     * "Thực phẩm" includes supplements, Rx matches the product Rx flag,
     * and missing prices stay last in either price direction.
     */
    public static List<DemoState.Product> filterCatalog(List<DemoState.Product> products,
                                                        String query, String category, String sort) {
        Objects.requireNonNull(products, "products");
        String normalized = normalizeSearch(Objects.requireNonNull(query, "query"));
        List<String> words = new ArrayList<>();
        for (String word : normalized.split("\\s+")) {
            if (!word.isEmpty()) words.add(word);
        }
        List<DemoState.Product> filtered = new ArrayList<>();
        for (DemoState.Product item : products) {
            if (!categoryMatches(item, category)) continue;
            String searchable = normalizeSearch(item.name + " " + item.subtitle + " " + item.category);
            boolean allTermsMatch = true;
            for (String word : words) {
                if (!searchable.contains(word)) { allTermsMatch = false; break; }
            }
            if (allTermsMatch) filtered.add(item);
        }
        Comparator<DemoState.Product> comparator = null;
        if ("Tên A–Z".equals(sort)) {
            comparator = Comparator.comparing((DemoState.Product p) -> p.name,
                String.CASE_INSENSITIVE_ORDER).thenComparing(p -> p.id);
        } else if ("Giá thấp".equals(sort)) {
            comparator = Comparator.comparing((DemoState.Product p) -> p.priceVnd <= 0L)
                .thenComparingLong(p -> p.priceVnd).thenComparing(p -> p.id);
        } else if ("Giá cao".equals(sort)) {
            comparator = Comparator.comparing((DemoState.Product p) -> p.priceVnd <= 0L)
                .thenComparing(Comparator.comparingLong((DemoState.Product p) -> p.priceVnd).reversed())
                .thenComparing(p -> p.id);
        }
        if (comparator != null) filtered.sort(comparator);
        return filtered;
    }

    private static boolean categoryMatches(DemoState.Product product, String category) {
        if ("Tất cả".equals(category)) return true;
        if ("Kê đơn".equals(category)) return product.requiresPrescription;
        if ("Thực phẩm".equals(category)) {
            return "Thực phẩm".equals(product.category)
                || "Thực phẩm bổ sung".equals(product.category);
        }
        return Objects.equals(category, product.category);
    }

    /** Kotlin CatalogMediaRules.supportedImageUrl: HTTPS host, no credentials/fragment. */
    public static String supportedImageUrl(String value) {
        if (value == null) return null;
        String candidate = value.trim();
        if (candidate.isEmpty() || candidate.length() > 2048) return null;
        try {
            URI uri = new URI(candidate);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getHost().isEmpty() || uri.getRawUserInfo() != null
                || uri.getRawFragment() != null) return null;
            return candidate;
        } catch (URISyntaxException | IllegalArgumentException ex) {
            return null;
        }
    }

    /** Preserve the Kotlin form's length and leading-zero policy (no country-code conversion). */
    public static String normalizeName(String input) {
        return Objects.requireNonNull(input, "input").trim();
    }

    public static String normalizePhoneInput(String input) {
        StringBuilder digits = new StringBuilder(11);
        for (int i = 0; i < Objects.requireNonNull(input, "input").length() && digits.length() < 11; i++) {
            char c = input.charAt(i);
            if (Character.isDigit(c)) digits.append(c);
        }
        return digits.toString();
    }

    public static boolean isValidName(String input) {
        return normalizeName(input).length() >= 2;
    }

    public static boolean isValidPhone(String input) {
        if (input == null || input.length() < 10 || input.length() > 11 || !input.startsWith("0")) return false;
        for (int i = 0; i < input.length(); i++) {
            if (!Character.isDigit(input.charAt(i))) return false;
        }
        return true;
    }

    public static boolean canSubmitCheckout(String name, String phone, boolean hasProducts) {
        return hasProducts && name != null && isValidName(name) && isValidPhone(phone);
    }

    /** Immutable cart line fixture; no catalog duplication. */
    public static final class CartLine {
        public final DemoState.Product product;
        public final int quantity;

        public CartLine(DemoState.Product product, int quantity) {
            this.product = Objects.requireNonNull(product, "product");
            if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
            this.quantity = quantity;
        }
    }

    public static boolean requiresPrescription(List<CartLine> lines) {
        return lines.stream().anyMatch(line -> line.product.requiresPrescription);
    }

    public static boolean canProceedToRequest(List<CartLine> lines, boolean prescriptionChosen) {
        return !lines.isEmpty()
            && lines.stream().allMatch(line -> line.quantity <= MAX_CUSTOMER_QUANTITY)
            && (!requiresPrescription(lines) || prescriptionChosen);
    }

    public static final class PriceSummary {
        public final long pricedSubtotalVnd;
        public final int unpricedLineCount;

        public PriceSummary(long pricedSubtotalVnd, int unpricedLineCount) {
            this.pricedSubtotalVnd = pricedSubtotalVnd;
            this.unpricedLineCount = unpricedLineCount;
        }

        public boolean includesUnpricedItems() { return unpricedLineCount > 0; }
    }

    /** Kotlin MockPriceCalculator: exclude unpriced lines rather than treating them as free. */
    public static PriceSummary summarizePrices(List<CartLine> lines) {
        long priced = 0L;
        int unpriced = 0;
        for (CartLine line : lines) {
            if (line.product.priceVnd > 0L) priced += line.product.priceVnd * line.quantity;
            else unpriced++;
        }
        return new PriceSummary(priced, unpriced);
    }

    /** Rx-only fingerprint; ordering and non-Rx quantities do not affect its value. */
    public static String rxFingerprint(List<CartLine> lines) {
        return lines.stream().filter(line -> line.product.requiresPrescription)
            .map(line -> line.product.id + ":" + line.quantity).sorted()
            .collect(Collectors.joining("|"));
    }

    public static boolean isCurrentLocalPrescription(boolean fileWasChosen,
                                                      String chosenFingerprint,
                                                      List<CartLine> lines) {
        String current = rxFingerprint(lines);
        return fileWasChosen && !current.isEmpty() && current.equals(chosenFingerprint);
    }

    /** Exact seven customer-visible groups from the supplied state diagram. */
    public enum CanonicalOrderStage {
        PENDING("CHỜ XỬ LÝ"),
        RX_PENDING("CHỜ DUYỆT TOA"),
        REJECTED("TỪ CHỐI"),
        HELD("ĐÃ GIỮ HÀNG"),
        READY("SẴN SÀNG NHẬN"),
        COMPLETED("HOÀN TẤT"),
        CANCELLED("ĐÃ HỦY");

        public final String label;
        CanonicalOrderStage(String label) { this.label = label; }
    }

    /** Delegate transition constraints to JavaRules, already ported from OrderStateMachine. */
    public static boolean canTransition(JavaRules.OrderStatus from, JavaRules.OrderStatus to) {
        return JavaRules.canTransition(from, to);
    }

    public static boolean canTransitionForOrder(JavaRules.OrderStatus from, JavaRules.OrderStatus to,
                                                boolean requiresRx, boolean pharmacistApproved) {
        return JavaRules.canTransitionForOrder(from, to, requiresRx, pharmacistApproved);
    }

    public static CanonicalOrderStage canonicalStage(JavaRules.OrderStatus status) {
        Objects.requireNonNull(status, "status");
        switch (status) {
            case REQUEST_RECEIVED:
            case STOCK_CONFIRMATION_PENDING: return CanonicalOrderStage.PENDING;
            case RX_REVIEW_PENDING:
            case RX_NEEDS_UPDATE: return CanonicalOrderStage.RX_PENDING;
            case RX_REJECTED: return CanonicalOrderStage.REJECTED;
            case HELD:
            case PREPARING: return CanonicalOrderStage.HELD;
            case READY_FOR_PICKUP: return CanonicalOrderStage.READY;
            case COMPLETED: return CanonicalOrderStage.COMPLETED;
            case EXPIRED:
            case CANCELLED: return CanonicalOrderStage.CANCELLED;
            default: throw new IllegalStateException("Unmapped order status: " + status);
        }
    }

    public static String terminationReasonLabel(JavaRules.OrderStatus status) {
        if (status == null) return null;
        switch (status) {
            case EXPIRED: return "Quá hạn giữ hàng";
            case CANCELLED: return "Đã hủy";
            case RX_REJECTED: return "Toa bị từ chối";
            default: return null;
        }
    }

    /** Demo lot snapshot matches MockLotRules.kt; null expiry is the undated seed fallback. */
    public enum LotCondition { AVAILABLE_DEMO, QUARANTINED_DEMO }

    public static final class DemoLot {
        public final String id;
        public final String productId;
        public final String code;
        public final int quantity;
        public final LocalDate expiresOn;
        public final LotCondition condition;

        public DemoLot(String id, String productId, String code, int quantity,
                       LocalDate expiresOn, LotCondition condition) {
            this.id = Objects.requireNonNull(id, "id");
            this.productId = Objects.requireNonNull(productId, "productId");
            this.code = Objects.requireNonNull(code, "code");
            this.quantity = quantity;
            this.expiresOn = expiresOn;
            this.condition = Objects.requireNonNull(condition, "condition");
        }

        public DemoLot(String id, String productId, String code, int quantity, LocalDate expiresOn) {
            this(id, productId, code, quantity, expiresOn, LotCondition.AVAILABLE_DEMO);
        }
    }

    public static final class LotAllocation {
        public final String lotId;
        public final int quantity;

        public LotAllocation(String lotId, int quantity) {
            this.lotId = lotId;
            this.quantity = quantity;
        }
    }

    public static boolean eligibleLot(DemoLot lot, LocalDate today) {
        return lot != null && lot.quantity > 0 && lot.condition == LotCondition.AVAILABLE_DEMO
            && (lot.expiresOn == null || lot.expiresOn.isAfter(today));
    }

    public static int eligibleQuantity(List<DemoLot> lots, String productId, LocalDate today) {
        return lots.stream().filter(lot -> lot.productId.equals(productId) && eligibleLot(lot, today))
            .mapToInt(lot -> lot.quantity).sum();
    }

    /**
     * Kotlin FEFO equivalence: eligible dated lots first by expiry then ID;
     * undated demo seed last. Returns null on shortage without mutating any lot.
     */
    public static List<LotAllocation> allocateFefo(List<DemoLot> lots, String productId,
                                                   int requested, LocalDate today) {
        if (requested <= 0) return null;
        List<DemoLot> candidates = lots.stream()
            .filter(lot -> lot.productId.equals(productId) && eligibleLot(lot, today))
            .sorted(Comparator.comparing((DemoLot lot) -> lot.expiresOn == null)
                .thenComparing(lot -> lot.expiresOn, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(lot -> lot.id))
            .collect(Collectors.toList());
        int remaining = requested;
        List<LotAllocation> result = new ArrayList<>();
        for (DemoLot lot : candidates) {
            if (remaining == 0) break;
            int taken = Math.min(remaining, lot.quantity);
            result.add(new LotAllocation(lot.id, taken));
            remaining -= taken;
        }
        return remaining == 0 ? result : null;
    }

    public enum StaffRole { CASHIER, ADMIN }

    public static boolean maySell(StaffRole role) { return role != null; }
    public static boolean mayEditCatalog(StaffRole role) { return role == StaffRole.ADMIN; }
    public static boolean mayReconcileShift(StaffRole role) { return role == StaffRole.ADMIN; }
    public static boolean mayApprovePrescription(StaffRole role) { return false; }

    public static StaffRole authenticateStaff(String email, String password) {
        if (email == null || password == null) return null;
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if ("nhanvien@pharmasiu.demo".equals(normalized) && "1111".equals(password)) {
            return StaffRole.CASHIER;
        }
        if ("quanly@pharmasiu.demo".equals(normalized) && "2222".equals(password)) {
            return StaffRole.ADMIN;
        }
        return null;
    }

    public static boolean acceptsPin(StaffRole role, String pin) {
        return role == StaffRole.CASHIER && "1111".equals(pin)
            || role == StaffRole.ADMIN && "2222".equals(pin);
    }

    public static boolean counterSellable(DemoState.Product product) {
        return product != null && !product.requiresPrescription && product.priceVnd > 0L;
    }

    public static boolean availableToAddAtCounter(DemoState.Product product, int alreadyInCart,
                                                   int demoAvailable) {
        return counterSellable(product) && alreadyInCart >= 0
            && demoAvailable > alreadyInCart && alreadyInCart < MAX_COUNTER_QUANTITY;
    }

    public static final class CounterLine {
        public final String productId;
        public final long unitPriceVnd;
        public final int quantity;
        public final boolean requiresPrescription;

        public CounterLine(String productId, long unitPriceVnd, int quantity, boolean requiresPrescription) {
            this.productId = Objects.requireNonNull(productId, "productId");
            this.unitPriceVnd = unitPriceVnd;
            this.quantity = quantity;
            this.requiresPrescription = requiresPrescription;
        }
    }

    public enum CounterPayment { CASH, OTHER_DEMO }

    public static long counterTotal(List<CounterLine> lines) {
        return lines.stream().mapToLong(line -> line.unitPriceVnd * line.quantity).sum();
    }

    /** RAM demo POS validity only; no real transaction or allocation authority. */
    public static boolean canConfirmCounter(List<CounterLine> lines, Map<String, Integer> demoStock,
                                            boolean shiftOpen, CounterPayment payment, long tendered) {
        if (!shiftOpen || lines.isEmpty() || payment == null) return false;
        for (CounterLine line : lines) {
            if (line.quantity < 1 || line.quantity > MAX_COUNTER_QUANTITY
                || line.unitPriceVnd <= 0L || line.requiresPrescription
                || demoStock.getOrDefault(line.productId, 0) < line.quantity) return false;
        }
        long total = counterTotal(lines);
        return total > 0L && (payment != CounterPayment.CASH || tendered >= total);
    }

    public static long changeDue(long total, long tendered) {
        return Math.max(0L, tendered - total);
    }
}
