# PharmaSIU `app-java` — Visual / Interaction QA (2026-10-29)

**Mode:** READ-ONLY code audit + **real emulator smoke**. **Scope:** `C:\PharmaSIU\PharmaSIU-1For4\app-java` Java Views/XML and shell router only. **No application source changes, commits, pushes or backend calls.** This is **not** a claim of complete UAT, accessibility certification, or 100% Kotlin UI parity.

## 1. Test basis, limits, and version mismatch

| Field | Evidence / qualification |
|---|---|
| Device | `adb devices -l`: `emulator-5554`, model `AOSP_on_IA_Emulator`, `Android 9` (API 28) |
| Requested viewport | Emulator `wm size 390x844` and `wm density 160` overridden; UIAutomator bounds show **390×844 logical pixels ≈ dp**. The emulator's captured raw PNG remains **1080×2400** owing to its display pipeline; logical UI coordinates and testing used 390×844. |
| Package | `com.pharmasiu.mobile.java`, `versionName=0.1.0-java-preview` |
| APK exercised | Installed from root `app-java/build/outputs/apk/debug/app-java-debug.apk` with `adb install -r`, update observed **2026-10-29 20:02:54** device-local time. |
| **Build freshness caveat** | File `app-java-debug.apk` on disk was subsequently modified **20:08:44**; root `app-java/src/main/java/com/pharmasiu/javaapp/MainActivity.java` was modified **20:17:01**. Therefore **the emulator-tested installed binary is older than current working sources and later APK**. Screens and routing seen on device must not be reported as a current-root final-build sign-off. Current Java/XML files were separately reviewed statically. |
| Source state | `git status --short -- app-java` showed `?? app-java/` (new, untracked module). No clean-commit hash is asserted. Current source is being actively integrated by other agents. |
| Runtime crash check | After fresh `adb logcat -c`, repeatedly inspected `adb logcat -d -s AndroidRuntime:E '*:S'` across navigations: **no `FATAL EXCEPTION` / AndroidRuntime:E observed** in tested sequences. This does **not** rule out all crash paths or non-fatal errors. |
| Accessibility evidence | Read XML and Java, inspected UIAutomator hierarchy and touch flows. **TalkBack, Switch Access, large-font, contrast analyzer and accessibility scanner were not run**. |

### Suggested reproducible test setup

PowerShell on host with Android SDK/ADB installed (perform **against a fresh, built APK** for final release QA):

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
& $adb shell wm size 390x844
& $adb shell wm density 160
& $adb install -r 'C:\PharmaSIU\PharmaSIU-1For4\app-java\build\outputs\apk\debug\app-java-debug.apk'
& $adb shell am force-stop com.pharmasiu.mobile.java
& $adb shell am start -n com.pharmasiu.mobile.java/com.pharmasiu.javaapp.MainActivity
& $adb logcat -c
# Screenshot/element inspection: adb shell screencap -p, adb shell uiautomator dump
```

> The QA runner changed only emulator size/density, installed APK and **demo RAM state**. The emulator is a test environment; no customer/medical/production records were used. Restore preferred emulator defaults with `adb shell wm size reset` and `adb shell wm density reset` if needed.

## 2. Actual emulator coverage — 390×844

**TESTED** means visually opened and/or tapped on the installed APK; row details distinguish a viewed screen from a completed interaction. No activity-level full automated UI test suite was executed.

| Screen / flow | Real emulator steps and outcome | Limits |
|---|---|---|
| Login | Launch, scroll to preset demo accounts, tap customer preset, tap **Đăng nhập**. Sign-in succeeded. Hero/card/buttons fit width; account presets require scroll to see cashier/admin. | Wrong password, registration, reset password and keyboard at 200% font **not tested**. |
| Customer home | Opened after customer login; Emerald hero, search entry, shortcuts, featured two-column cards and 5-item nav rendered. | Scroll to last featured product and all home shelves **not exhaustively checked**. |
| Category/catalog | Tap bottom **Danh mục**; hero showed **30 sản phẩm / 8 nhóm**. Enter ASCII `sua rua mat`, hide IME, wait for layout settle: **1 result** (`Sữa rửa mặt dịu nhẹ`). Scroll to card; full price/detail readable and tappable. | Changing spinner sort and tapping every category was not executed; deterministic filter/sort logic cross-checked statically. |
| Product detail | Tap matched product card; artwork, title, subtitle, reference price, stock caveat and fixed **Thêm vào giỏ +** CTA rendered. Tap CTA → cart. | Rx product detail and local-file picker **not tested**. |
| Customer cart | Item added at qty 1, reference price 118,000 VND. Scroll twice to subtotal and CTA. Buttons to change qty visible. | Minus/plus quantity changes and 20-item limit not exercised dynamically; only path to checkout used. |
| Checkout | Tap **Tiếp tục tạo yêu cầu DEMO**; contact and pickup fields, sample pickup `A`, line summary and disclaimers displayed. Scroll, tap **Lưu yêu cầu DEMO trên thiết bị**. | Invalid phone/name, pickup `B`, Rx gating, focus/IME at form bottom not executed. |
| Save request / receipt | `DEMO-0001` displayed; text clearly distinguishes RAM-saved request from pharmacy receipt/hold/payment. No crash. | Order cancel/details/empty state untested. |
| Customer account | Tap **Tài khoản**; overview showed 1 demo request, profile/address entry points. Tap **Mở chỉnh sửa hồ sơ**, fields rendered, back to account, scroll to bottom, logout. | Did not mutate identity, addresses, password or inspect their error states. |
| Cashier POS | Log out; tap cashier preset, login. Open shift #1 with sample opening cash 0; search `mask` → mask-01, tap add, cart qty 1, choose **Phương thức khác (DEMO)**, tap **Lưu phiếu POS-DEMO**. Receipt `POS-DEMO-0001` and history correctly showed 36,000 VND and no real payment/delivery claims. | Cash tender / change validation, Rx search entry, FEFO lot mutation, close-shift prompt and data restoration untested. |
| Admin overview | Log out from cashier; login admin preset. Dashboard rendered 30 products, 2 blocked Rx examples, 1 POS-demo sale, 1 local request, open demo shift. | Real authorization/security/backend assertions out of scope. |
| Admin products | Tap **Hàng hóa** bottom tab; existing item selector, name and reference-price form, save CTA, create OTC section rendered. | Did not save edits/create product; no validation paths exercised. |
| Admin lots | Tap **Lô hàng**; selector, lot code/qty/ISO expiry form and FEFO disclosure rendered. | Did not create/quarantine a lot or verify actual stock transfer in UI. |
| Admin audit | Tap **Đối soát**; stock count form and shift reconciliation sections rendered. | No sample reconciliation or variance submitted. |

The customer and staff flows displayed no hard horizontal text clipping at the tested **default font scale** in the captured viewports. Large cards and tall XML scroll forms require scrolling, which worked for the exercised flows. **The bottom navigation remained present and interactive** for the routes tapped.

## 3. Findings, severity and exact reproduction

**Summary:** **P0 confirmed = 0; P1 application/runtime defect confirmed = 0.** **One P1 *release evidence* gap** and **five P2 design/accessibility/performance issues or risks**. Clearly separate directly observed behavior from static-only forecasts.

### QA-GATE-01 — P1 verification gap: installed APK behind current sources (**confirmed metadata, not app bug**)

- **Evidence:** app update on emulator 20:02:54; newer root APK 20:08:44; `MainActivity.java` source timestamp 20:17:01. Other agents were editing app-java in parallel. No final install/test of an APK built after these source changes was performed in this read-only pass.
- **Reproduction:** compare `adb shell dumpsys package com.pharmasiu.mobile.java` `lastUpdateTime` to file modification dates for `app-java-debug.apk` and `app-java/src/main/java/com/pharmasiu/javaapp/MainActivity.java`.
- **Impact:** directly observed successful UI/interaction cannot prove current Java/XML integration has the same routing/crash behavior. **Do not certify latest root APK** from this pass.
- **Required action (prime):** freeze integrated sources, build a uniquely timestamped `app-java` APK, `adb install -r`, cold start, rerun all critical role/routes and inspect logcat; record APK hash/build timestamp before release decision.

### UX-01 — P2: Catalog overview occupies most initial viewport; actual priced cards start below fold (**observed emulator**)

- **Repro:** customer sign-in → bottom **Danh mục** → do not scroll. At logical 390×844, the stacked `Danh mục` header, 178dp Emerald intro, search, helper line, horizontal filters, toolbar occupy most space; only the top of first two product image cards is visible above the fixed bottom navigation. Names/prices and card action become visible after scrolling.
- **Evidence:** running catalog screen with `30 kết quả tìm thấy`; matched single search result only became fully readable after swipe.
- **Risk:** catalog discovery/product scan requires extra swipe even when user has made a narrow search; UX friction, not a tap failure.
- **Suggestion:** compact or collapse intro/toolbar after a search/filter; avoid fixed large hero when query non-empty; preserve touch targets ≥48dp.
- **Likely locations:** `ui/customer/CustomerCatalogViews.java`, `res/layout/catalog_v2_catalog.xml`, `catalog_v2_card.xml`.

### UX-02 — P2: Cart and checkout are vertically dense before primary action (**observed emulator**)

- **Repro:** customer → catalog → product `Sữa rửa mặt dịu nhẹ` → add to cart. Cart initially shows hero, separate intro/section panels and item panel; **Tiếp tục tạo yêu cầu DEMO** required two upward swipes to reveal. Checkout similarly places hero, contact panel, pickup panel and item review before **Lưu yêu cầu DEMO**.
- **Impact:** action discovery, especially with bigger font or keyboard, could be slow; **tested scroll works and CTA can be tapped**. Not a broken interaction.
- **Suggestion:** remove repeated explanatory panels or add a compact sticky subtotal/CTA with demo disclosure; retain error messages near their fields.
- **Likely locations:** `ui/customer/CustomerOrderViews.java`, `res/layout/order_v2_cart.xml`, `order_v2_checkout.xml`.

### NAV-01 — P2: Android Back collapses customer navigation to Home rather than previous page (**STATIC inspection; runtime repro pending**)

- **Source evidence:** `MainActivity.java` overrides `onBackPressed()` with `if (DemoState.CUSTOMER.equals(store.role) && !"home".equals(route)) navigate("home")`, not a page-aware back stack. Admin non-overview routes similarly jump to `admin`.
- **Predicted repro:** customer Home → Catalog → Product; press **system Back** (not the in-screen arrow). Expected conventional behavior: return to catalog with search/category context; code always calls `navigate("home")`. Customer Cart/Checkout/Account also lose breadcrumb history on hardware Back.
- **Impact:** confusing in-app back behavior, possible loss of search/scroll context. **Not tested on emulator**; distinguish from in-screen product back arrow, which was used successfully.
- **Suggestion:** explicit per-flow back destinations or an actual route stack, with root Home handling system Back separately. Prime owns routing; don't patch in this read-only task.

### A11Y-01 — P2: Semantic labels and scale/responsiveness incomplete (**STATIC risk only; no TalkBack run**)

- **Source evidence:** many dynamic form fields use `EditText.setHint` from `UiKit.java` / `CustomerOrderViews.java` instead of persistent programmatically associated labels; many view trees use `LinearLayout` + `TextView`/`Button` rather than grouped semantics. Product artwork is correctly marked as illustration rather than verified photo, and several critical CTA elements do have accessible labels.
- **Predicted repro:** enable TalkBack / text scale 1.3–2.0, step through login, checkout, profile and admin lot form. Determine whether fields remain identifiable after typing and whether errors are spoken near their source. Also inspect 5 equal-width bottom nav buttons, 11sp labels, at large font.
- **Impact:** label may be lost once hint is replaced with entered text; long localized labels may wrap/clip at accessibility font sizes. **Neither failure was observed with a screen reader**.
- **Suggestion:** permanent `TextInputLayout`-style label equivalents / `labelFor`, descriptive `contentDescription` on icon-only clickables, accessibility traversal tests and 200%-font screenshots; keep target dimensions at least 48×48dp.
- **Likely locations:** `ui/UiKit.java`, `ui/AuthScreen.java`, `ui/customer/CustomerOrderViews.java`, `MainActivity.java` nav construction, `res/layout/order_v2_*.xml`.

### PERF-01 — P2: Eager 30-card XML grid re-inflation on catalog edits (**STATIC risk, not profiled**)

- **Source evidence:** `CustomerCatalogViews.java` uses `buildGrid` with child `LinearLayout`/card inflation for all matches, inside a parent `ScrollView`; text search/filter reruns the full rendering. The exact fixture set contains 30 products, and search test `sua rua mat` worked.
- **Predicted repro:** lower-end Android device → type/erase query rapidly in catalog; monitor input latency, `Choreographer` dropped frames and memory. Return to 30-card result, repeatedly sort/switch categories.
- **Impact:** possible avoidable frame drops/GC as fixture count grows, not a confirmed performance regression at 30 entries. **No benchmark executed**.
- **Suggestion:** `RecyclerView` grid + diffed adapter, or debounce + minimal card changes; no need to block demo unless measured jank appears.

## 4. Specific pass / fail / not-tested matrix

| Area | Screen visible | Core tap flow | Empty/error/edge | Accessibility | Crash conclusion |
|---|---|---|---|---|---|
| Login | **PASS tested** | **PASS tested** (3 role presets across session) | NOT TESTED (invalid/reset/register) | Static hints only | No AndroidRuntime fatal observed |
| Home | **PASS tested** | **PASS tested** (to catalog) | NOT TESTED (all shelves/last card) | Static-only | No fatal observed |
| Catalog | **PASS tested** | **PASS tested** (search no accent, card) | PARTIAL: 1 result; empty/sort all NOT TESTED | Static-only | No fatal observed |
| Product | **PASS tested** | **PASS tested** (add OTC) | NOT TESTED (Rx/file picker) | Static-only | No fatal observed |
| Cart | **PASS tested** | **PASS tested** (go checkout) | NOT TESTED (qty edge/remove/empty) | Static-only | No fatal observed |
| Checkout | **PASS tested** | **PASS tested** (RAM request) | NOT TESTED (invalid input/Rx) | Static-only | No fatal observed |
| Account | **PASS tested** | **PASS tested** (edit, back, logout) | NOT TESTED (save/address/password) | Static-only | No fatal observed |
| Staff POS | **PASS tested** | **PASS tested** (open/add/other method/save receipt) | NOT TESTED (cash/close/Rx FEFO) | Static-only | No fatal observed |
| Admin dashboard | **PASS tested** | **PASS tested** (tabs) | NOT TESTED (long metrics) | Static-only | No fatal observed |
| Admin product/lot/audit | **PASS tested** | **PARTIAL** (open tabs only) | NOT TESTED (submit/validate/quarantine/reconcile) | Static-only | No fatal observed |

## 5. Concrete next acceptance gate (not performed here)

1. **New APK, same source snapshot:** rebuild Java-only `app-java`, capture SHA-256 + build timestamp, install and verify `MainActivity` route dispatch to `CustomerCatalogViews`, `CustomerOrderViews`, `CustomerAccountViews`, `StaffPosViews`, and `StaffAdminViews`; repeat current smoke and zero-fatal logcat check.
2. **Complete role/edge paths:** customer Rx product → local prescription file selection → modify Rx qty → ensure fingerprint reset; invalid checkout and cancel request; cashier cash insufficient/equal/change, block sale of Rx, lot expired/quarantined; close shift, admin inventory count/lot quarantine and reconciliation. All are **untested here**.
3. **Accessibility:** TalkBack/Accessibility Scanner, ≥1.3× and 2× font, touch-target size, correct spoken form names, keyboard occlusion/next focus, live error/Toast announcements and navigation semantics.
4. **Devices:** Android 13/14+, 320dp small phone, 390dp target, large phone/tablet, landscape, gesture navigation and system dark mode (theme deliberately light), cold-start process recreation.
5. **Performance:** baseline launch/scroll jank in 30-product grid, rapid text search/filter, memory growth after repeated route renders; report measured frame time instead of inferring.

## 6. Final disposition

**Visual smoke verdict for *installed* test APK:** **PASS with P2 observations** for navigated happy-path screens; **no confirmed P0/P1 runtime defect** in exercised paths. **Release qualification for *current root sources*:** **NOT YET VERIFIED**, because code and APK timestamps moved during testing and large swaths of role/error/a11y behavior remain untested. The Java frontend is a **RAM-only simulation**, not a backend pharmacy or Rx/payment-compliant production app.

**Report destination:** `/ui-ux-brain/java_migration/visualqa/docs/JAVA_VISUAL_QA_2026-10-29.md` — only final staged artifact; screenshots and hierarchy XML were temporary local inspection files and are not part of deliverables.
