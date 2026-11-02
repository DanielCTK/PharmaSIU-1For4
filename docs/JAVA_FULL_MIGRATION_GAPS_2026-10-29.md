# PharmaSIU — kiểm toán khoảng trống chuyển đổi Kotlin → Java Views/XML

**Ngày kiểm toán:** 29/10/2026. **Chế độ:** chỉ đọc source; tài liệu này được tạo trong staging `/ui-ux-brain/java_migration/audit/docs/`. Không chỉnh repository, Gradle, Android sources, Kotlin, SQL, database hay backend.

## 1. Kết luận và phạm vi kiểm kê

**CHƯA ĐẠT 100% PARITY.** Java là ứng dụng Android Views/XML **demo độc lập**, chưa tương đương đầy đủ về hành vi, thiết kế, trạng thái phiên và chứng cứ kiểm thử với bản Kotlin/Jetpack Compose. Các chức năng đòi hỏi máy chủ/dược sĩ/xác minh giao dịch còn **CHỜ TÍCH HỢP** theo SRS; không được tính là đã thực hiện nghiệp vụ thật.

Đã so khớp danh sách **toàn bộ 50 file `.kt` trong `app/src/main`** và **16 file `.kt` trong `app/src/test`** theo workspace `C:\PharmaSIU\PharmaSIU-1For4`. Bộ Kotlin có **58 hàm `@Test`**. Snapshot sau khi prime đang tích hợp các worker: **`app-java/src/main/java` có 15 file Java**, **`app-java/src/test` có 7 file Java** với **50 hàm `@Test`** (6 JavaRules + 4 account + 8 orders + 6 admin + 4 POS + 5 catalog + 17 legacy domain). Đây chỉ là **kiểm kê source**, không phải 50/58 cases có kiểm thử tương đương một-một.

**Chứng cứ test có sẵn:** XML test trong `app/build/test-results/testDebugUnitTest`: 16 suites/58 tests, 0 fail/error/skip, dấu thời gian 29/10/2026 15:47. Khi đọc lần đầu, XML `app-java/build/test-results/testDebugUnitTest` còn 5 suites/28 tests, 0 fail/error/skip, thời gian 19:52; kết quả đó **không đủ chứng minh hai bộ catalog/domain mới tích hợp đã PASS**. Audit này **không chạy Gradle, không render UI và không chạy emulator** vì phạm vi read-only. Cần làm lại verification sau lần tích hợp cuối.

**Phạm vi Java đã có trong active module:** `MainActivity.java`, `core/{DemoState,UiHost}.java`, `domain/JavaRules.java`, `legacy/LegacyDomainRules.java`, `ui/{AuthScreen,CustomerScreens,CashierScreens,AdminScreens,UiKit}.java`, `ui/customer/{CustomerAccountViews,CustomerOrderViews,CustomerCatalogViews}.java`, `ui/staff/{StaffPosViews,StaffAdminViews}.java`. Các file `CustomerScreens`/`CashierScreens`/`AdminScreens` đời đầu vẫn nằm trong source; một số chỉ còn fallback/không được dispatch, cần xác minh trước khi dọn. Java Views/XML không phụ thuộc Kotlin/Compose code ở module riêng; project cấp trên vẫn có `:app` Kotlin và `.gradle.kts`.

**Worker staging được so sánh:** `/ui-ux-brain/java_migration/catalog/app-java/src` chứa `CustomerCatalogViews`, 4 layout, 3 drawable và test 5 cases; `/ui-ux-brain/java_migration/domain/app-java/src` chứa `LegacyDomainRules` và test 17 cases. Trong quá trình audit, prime đã đưa **cả hai Java class, XML/drawable catalog và hai test** vào `app-java`; `MainActivity.render()` đã dùng `CustomerCatalogViews` cho `home/catalog/product`, nhưng vẫn giữ `CustomerScreens` làm fallback. Các bản `catalog_build`, `staffpos_build` trong staging là **bản build/copy trung gian**, không cộng vào số tính năng hoặc test được tích hợp. Phải xác nhận source hiện tại một lần nữa trước merge vì nhiều worker đang chỉnh đồng thời.

**Nguồn contract:** `docs/ALIGNMENT_SOURCE_OF_TRUTH.md` (BFD 5 nhánh/17 chức năng, DFD, SRS, State + 2 Activity, ERD 6 entity chỉ để đối chiếu); `docs/ALIGNMENT_DOMAIN_CONTRACT.md`, `docs/ALIGNMENT_TRACEABILITY.md`, `docs/JAVA_MIGRATION_PLAN_2026-10-29.md`. Không xem BFD/SRS về kho/toa/thanh toán là đã được triển khai server.

## 2. Khoảng trống P0/P1 có thể chứng minh từ source

| Ưu tiên | Nguồn bằng chứng | Thực trạng Java | Việc còn phải làm / điều kiện nghiệm thu |
|---|---|---|---|
| **P0 — lưu lại giá POS đang lập** | Kotlin `StaffDemoStore.updateDemoProduct` từ chối chỉnh sản phẩm có ID trong `cartState`. Java `DemoState.renameProduct` sửa `p.priceVnd` mà không kiểm `posCart`; `posTotal()` tính từ giá hiện tại. | Quản lý có thể đổi giá sau khi cashier đã bỏ mặt hàng vào giỏ demo, làm thay đổi tạm tính và phiếu. | Chặn sửa hàng đang trong POS draft, hoặc dùng immutable price snapshot cho từng dòng; kiểm bằng test đổi giá khi draft có hàng. |
| **P0 — đối soát ca một lần, sau đóng ca** | Kotlin `StaffDemoStore.reconcileDemoShift` yêu cầu `!shiftOpen`, `shiftNumber > 0`, chưa có reconciliation cùng ca và giới hạn tiền. Java `DemoState.reconcileDemoShift` hiện chỉ kiểm role, số không âm, note; giao diện `StaffAdminViews.audit` kiểm có ca, nhưng không chặn ca đang mở/lặp đối soát ở store. | Có thể lưu nhiều reconciliation hoặc lưu khi ca đang mở, làm sai semantics 5.3. | Enforce trong `DemoState` trước cả UI, chốt snapshot doanh số **theo ca**, phân biệt CASH/OTHER_DEMO, test open/closed/repeated/shift #2. |
| **P0/P1 — lot/store invariants** | Kotlin `StaffDemoStore.receiveDemoStock` giới hạn mỗi lần 1..999, tổng theo mặt hàng ≤999, chặn trùng mã trong sản phẩm, ngăn quarantine làm thiếu draft; `adjustDemoStock` dựa seed lot + quantity draft. Java `DemoState.receiveLot` chỉ kiểm qty>0/hạn tương lai, cộng thẳng aggregate; `quarantineLot` trừ tổng stock, không xét POS draft. | UI admin giới hạn nhập mỗi lần nhưng lời gọi trực tiếp vào store có thể vượt giới hạn; quarantine giữa draft và pay làm draft mất hợp lệ. | Đưa giới hạn tổng, xử lý seed fallback và kiểm draft vào store; thử lot expired/quarantine, FEFO all-or-nothing, POS không trừ âm/không ghi phiếu khi đủ một phần. |
| **P1 — hoàn thiện 5.1 / quên mật khẩu** | Kotlin `UnifiedLoginScreen` có login/register/forgot/reset mode trên cổng chung. Java `AuthScreen.create` hiện chỉ có `register` và login; không có route hoặc UI quên/khôi phục mật khẩu demo. `CustomerAccountViews` có đổi mật khẩu *khi đã đăng nhập*, không thay thế reset từ cổng login. | Luồng quên mật khẩu DEMO chưa chuyển. | Thêm reset DEMO với kiểm phone/identity trong RAM và UI cảnh báo **không có OTP/email thật**; route tests. Không yêu cầu đưa mật khẩu thật vào ứng dụng. |
| **P1 — first-run onboarding / lifecycle** | Kotlin `PharmaSIUApp` dùng cờ `onboarding_complete` trong Preferences, có stack màn khách và `rememberSaveableStateHolder`. Java `CustomerAccountViews` có trang `onboarding`/progress trong RAM, nhưng `AuthScreen` login mặc định điều hướng thẳng `home`; `MainActivity` không tự áp first-run flag. | Onboarding tương tác có nhưng không tự hiện lần đầu/không giữ lựa chọn sau relaunch như Kotlin; Java dựng `DemoState` mới khi Activity mới, mất toàn bộ sample data (đúng phần RAM), chưa có state recovery từng màn. | Định nghĩa contract first-run tương ứng; giữ cờ onboarding được phép lưu trong Preferences, mọi dữ liệu demo còn RAM; kiểm fresh install, login, skip, launch lần 2, Back, rotation/process recreation. |
| **P1 — Android Back/navigation** | Kotlin dùng `navigation` stack, `back()`, `go()`, và `BackHandler`. Java `MainActivity.onBackPressed()`: khi customer ở bất kỳ route khác `home` thì quay về `home`; admin quay thẳng `admin`. | Back từ product về catalog, từ order-detail về lịch sử, từ checkout về giỏ không tương đương Kotlin; không có backstack Java đầy đủ. | Ánh xạ Back theo ngữ cảnh, entry/logout isolation và receipt; thử hệ thống Back/IME và direct route. |
| **P1 — media/ảnh sản phẩm** | Kotlin `MedicineMedia` và `StaffProductMedia` cho HTTPS ảnh có fallback, dialog phóng ảnh, chọn/đổi/bỏ ảnh RAM `content://`. Java catalog mới dựng `ProductArtwork(Canvas)` minh họa offline; `DemoState.Product` không có `imageUrl`/photo URI. Admin Java không có picker ảnh nội bộ. | Chưa tương đương media và 4.1 ảnh hàng mẫu; không hiển thị ảnh catalogue HTTPS có kiểm URI. | Java media adapter có HTTPS allowlist, default drawable/fallback, picker UI, content-URI lifecycle; không gọi ảnh minh họa là ảnh thuốc thật; test ảnh lỗi và quyền Android picker. |
| **P1 — thông tin đơn lịch sử không bất biến** | Kotlin `MockRequest` giữ snapshot `CartLine(Medicine)`, tên/liên hệ, displayTime; `MockRequestDetailScreen` hiển thị snapshot. Java `DemoState.Order` giữ lines theo ID + `pricedSubtotalVnd`, tên/phone nhưng **không có timestamp**; `CustomerOrderViews.showLines(historical=true)` tra `state.product(id)` với **tên/giá hiện tại**. | Thay giá/tên catalogue sau đặt demo có thể khiến chi tiết hiện tên/đơn giá khác tổng đã lưu; không có thời gian lịch sử như Kotlin. | Snapshot immutable tên, giá, unit, qty, timestamp tại lưu; tạm tính tính từ snapshot, có nhãn rõ chưa báo giá, test đổi catalogue sau lưu. |
| **P1 — xác thực file toa cục bộ** | Kotlin picker chấp nhận ảnh/PDF, giữ fingerprint các dòng Rx; Java `MainActivity.ACTION_OPEN_DOCUMENT` thêm MIME types và `DemoState.attachLocalPrescription(name)` chỉ lưu display name + fingerprint, không dùng MIME do ContentResolver trả về để kiểm sau chọn. | Không upload/duyệt toa là **đúng**; song kiểm loại file thực trả về/picker canceled/revoked URI chưa đạt parity. | Validate MIME, xử lý cancel, URI inaccessible, current fingerprint; không lưu dữ liệu/toa thật; chỉ nhận local selection, không nói “đã gửi”. |
| **P1 — visual/accessibility** | Compose có typography/token, illustration, staggered grids, responsive Lazy layouts, `MedicineMedia`, 5 gallery `@Preview`. Java có 7 màu XML/Views và một số Canvas artwork nhưng nhiều trang là ScrollView/LinearLayout cards, chưa có screenshot benchmark tương đương. | Đã có native Views, **chưa chứng minh pixel/interaction parity**. | Đo 320/390/412dp, 1.0/1.5× font, TalkBack, order of focus, error semantics, 48dp touch targets, picker, keyboard, large catalog performance. |
| **P2 — hệ thống test/CI và tài liệu** | Kotlin 16 files/58 tests, Java source 7 files/50 tests. `docs/JAVA_PARITY_CHECKLIST.md` và `JAVA_MIGRATION_QA_2026-10-29.md` vẫn nói Java 9 file/6 tests; số này chỉ đúng ở mốc lịch sử. | Mốc cũ được trình bày như hiện tại; không có E2E parity fixtures/visual regression. | Chuyển baseline mới thành tài liệu hiện hành, giữ QA log cũ gắn nhãn HISTORY; test và chụp trên emulator thật trước khi kết luận. |

**Điểm đúng cần giữ:** app-java dùng tài khoản demo RAM và có role gate cơ bản; `canApproveRx()` trả false; CustomerOrderViews bắt buộc file Rx cục bộ khớp fingerprint; pickup chỉ A/B; local cancel cần lý do; lịch sử lọc theo customerPhone; JavaRules có 11 status và guard Rx; POS có bước kiểm FEFO demo theo lô; giao diện công khai cảnh báo CHỜ TÍCH HỢP. Đây là **safe demo behaviors**, không phải xác nhận lâm sàng hoặc backend.

## 3. Đối chiếu 17 chức năng BFD/SRS (5 nhánh)

| BFD | Java đang tích hợp | Tình trạng và gap còn lại |
|---|---|---|
| 1.1 Tìm kiếm sản phẩm | `CustomerCatalogViews.catalog`; 30 mẫu từ `DemoState`; filter/sort, không dấu | **DEMO gần đủ logic**; test thực tế IME/empty/filter/danh sách lớn, giữ filter khi Back. |
| 1.2 Xem chi tiết, giá bán | `CustomerCatalogViews.product` + custom Canvas artwork | **DEMO một phần visual**; chưa có HTTPS/photo media và đầy đủ thông tin sản phẩm xác thực. |
| 1.3 Xem khả dụng dự kiến | Thông báo tồn chưa xác minh | **CHỜ TÍCH HỢP** nguồn tồn thật; không dùng stock POS RAM để nói còn hàng trên catalog khách. |
| 2.1 Tạo đơn đặt trước | `CustomerOrderViews.cart/checkout/receipt`, `DemoState.saveRequest` | **LOCAL DEMO**; không gửi; snapshot timestamp/giá chưa parity, kiểm double-tap và checkout lifecycle. |
| 2.2 Gửi toa khi cần | `rx-v2` + `choosePrescriptionFile` | **CHỌN FILE LOCAL**, không phải gửi; phải kiểm MIME thực và tình huống picker cancel, không tự duyệt. |
| 2.3 Theo dõi, hủy đơn | `orders-v2/order-detail-v2`, `cancelRequest`, `reorderRequest` | **LOCAL DEMO**; lý do/hủy cục bộ; không có theo dõi server, snapshot chi tiết cần giữ bất biến. |
| 3.1 Bán hàng tại quầy | `StaffPosViews` + `DemoState` | **POS DEMO**; mở/đóng ca, OTC, tiền mặt, phiếu mẫu; store guards/draft pricing cần sửa. |
| 3.2 Xử lý đơn đặt trước | `StaffAdminViews.requests` chỉ xem `state.orders` | **CHỜ TÍCH HỢP** queue/nhận yêu cầu từ nhà thuốc; cùng thiết bị không phải giao tiếp khách–quầy thật. |
| 3.3 Thẩm định toa theo quyền | `JavaRules.canApproveRx(false)`, `DemoState.canApproveRx(false)` | **CHỜ NGƯỜI CÓ QUYỀN DƯỢC SĨ + BACKEND**; admin/cashier không có quyền. |
| 3.4 Thu tiền và giao thuốc | `StaffPosViews` lưu `POS-DEMO`, CASH/OTHER_DEMO | **DEMO số tiền/hóa đơn**; không có xác minh gateway/tiền thật/bàn giao; không gán COMPLETED. |
| 4.1 Cập nhật tên, giá, hàng | `StaffAdminViews.catalog` + `DemoState.renameProduct` | **DEMO**, cần guard đổi giá khi draft và ảnh sản phẩm; hiện tên/giá có thể sửa trực tiếp khi cart POS có hàng. |
| 4.2 Nhập theo lô | `StaffAdminViews.lots` + `DemoState.receiveLot` | **RAM DEMO**; số lượng/hạn/mã lô, cần bound tổng quantity và duplicate identity consistency. |
| 4.3 Hạn và chất lượng | `JavaRules.allocateFefo` + cách ly lô | **DEMO có guard**, chưa tương đương seed lot Kotlin/kiểm draft và kiểm phẩm thực; quá hạn/cách ly không được bán. |
| 4.4 Kiểm kê chênh lệch | `StaffAdminViews.audit` + `DemoState.countDemoStock` | **RAM DEMO** lưu log, không tự chỉnh kho; giới hạn counted/ghi chú/store test cần đối chiếu Kotlin. |
| 5.1 Tài khoản, phân quyền | `AuthScreen`, `CustomerAccountViews`, `DemoState` | **DEMO** login/register/profile/address/change-password + role gate; thiếu forgot/reset login và admin user/RBAC thật. |
| 5.2 Báo cáo | `StaffAdminViews.reports` | **DEMO KPI** tổng POS/lot/cash; chưa có báo cáo kế toán/server. |
| 5.3 Đối soát ca | `StaffAdminViews.audit` + `DemoState.reconcileDemoShift` | **RAM DEMO nhưng guard chưa parity**: cần đóng ca, duy nhất một bản/ca, phân nhóm CASH/OTHER_DEMO. |

**State & Activity:** Kotlin `OrderStateMachine.kt`, Java `JavaRules.java` và staged/integrated `LegacyDomainRules.java` có 11 nội bộ + ánh xạ 7 công khai. Không được chuyển yêu cầu DEMO sang `HELD/READY_FOR_PICKUP/COMPLETED`; các giai đoạn server chỉ là contract và tests. POS và Click & Collect có sơ đồ Activity nhưng thiếu backend nên không thể claim Rx approved, TTL hold, FEFO kho thật, settlement, xuất một lần, proof-of-pickup. ERD 6 entity chỉ được dùng đối chiếu dữ liệu, không tạo DB.

## 4. Kiểm kê **đủ 50** Kotlin source; đường chuyển đổi và gap

Quy ước: **T** = Java tích hợp và có thao tác tương ứng nhưng còn phải test parity; **P** = một phần/chức năng thiếu; **D** = code domain port hoặc staged, phải kiểm hành vi; **N** = chưa có đối ứng/Preview chưa chuyển. Tất cả đường dẫn Kotlin dưới đây có tiền tố `app/src/main/java/com/pharmasiu/mobile/`. Đường dẫn Java tương ứng có tiền tố `app-java/src/main/java/com/pharmasiu/javaapp/`.

### 4.1 App, data, domain (17 files)

| # | Kotlin path | Java tương ứng | Mức / lưu ý |
|---:|---|---|---|
| 01 | `MainActivity.kt` | `MainActivity.java` | **P** Activity Java có nhưng lifecycle/Back stack khác. |
| 02 | `data/model/Medicine.kt` | `core/DemoState.Product`, cart ID→qty | **P** thiếu media URL, immutable line model, snapshot product metadata. |
| 03 | `data/model/MockCommerce.kt` | `core/DemoState.{CustomerProfile,Address,Order}` | **P** nhiều field đã chuyển, request timestamp và immutable product snapshot thiếu. |
| 04 | `data/repository/DemoCatalogRepository.kt` | `core/DemoState.seed/product` | **T** 6 sản phẩm gốc và ID được giữ, cần fixture diff exact. |
| 05 | `data/repository/ExpandedMockCatalog.kt` | `DemoState.seed`, `CustomerCatalogViews` | **T** 24 mẫu bổ sung → 30; không có datasource chung sinh từ một nguồn. |
| 06 | `domain/AppEntryState.kt` | `DemoState.role`, `MainActivity` | **P** role routing có, thiếu state/navigation/relaunch test tương đương. |
| 07 | `domain/CartRules.kt` | `DemoState`, `CustomerOrderViews`, `legacy/LegacyDomainRules` | **D/T** 20 per line và Rx guard demo; kiểm qty/invariants trực tiếp. |
| 08 | `domain/CatalogMediaRules.kt` | `legacy/LegacyDomainRules.supportedImageUrl` | **D/P** logic kiểm HTTPS đã port; UI media adapter chưa dùng. |
| 09 | `domain/CatalogSearchRules.kt` | `CustomerCatalogViews.filterProducts`, `LegacyDomainRules.filterCatalog` | **T/D** filter, accent-insensitive, sort; cần so fixture hai phía tránh drift. |
| 10 | `domain/CheckoutInputRules.kt` | `CustomerOrderViews.submit`, `DemoState.canSaveRequest`, `LegacyDomainRules` | **P** chuẩn hóa số điện thoại có ở domain port nhưng UI Java chưa áp hệt Kotlin lúc nhập. |
| 11 | `domain/CounterRules.kt` | `JavaRules`, `StaffPosViews`, `LegacyDomainRules` | **P** Rx/stock/cash guard demo, nhưng store vs draft snapshot và lot guard còn khác. |
| 12 | `domain/MockLotRules.kt` | `JavaRules.allocateFefo`, `LegacyDomainRules.allocateFefo` | **D/P** Kotlin seed lot có expiry=null fallback; Java model POS dùng aggregate+danh sách lot khác cấu trúc. |
| 13 | `domain/MockPriceSummary.kt` | `DemoState.knownSubtotal/hasUnpriced`, `LegacyDomainRules.summarizePrices` | **T/D** loại khỏi subtotal hàng chưa giá; kiểm historical snapshot. |
| 14 | `domain/OrderStateMachine.kt` | `JavaRules.OrderStatus/publicStage`, `LegacyDomainRules` | **D** 11→7 groups/transition guard ở domain, không dùng cho local order. |
| 15 | `domain/PrescriptionSelectionRules.kt` | `DemoState.rxFingerprint/localPrescriptionSelected`, `LegacyDomainRules` | **T/D** logic fingerprint Rx; picker MIME/URI checks thiếu. |
| 16 | `domain/UnifiedDemoAuth.kt` | `DemoState.login`, `AuthScreen`, `LegacyDomainRules.authenticateStaff` | **P** 3 role mock hoạt động, thiếu forgot/reset và test session boundaries sâu. |
| 17 | `ui/PharmaSIUApp.kt` | `MainActivity.java` + `core/DemoState.java` | **P** chia nhỏ navigation/screen state, nhưng chưa đủ stack, owner lifecycle, first-run/restore; cần cross-flow E2E. |

### 4.2 UI components, theme, customer flows (27 files)

| # | Kotlin path | Java tương ứng | Mức / lưu ý |
|---:|---|---|---|
| 18 | `ui/components/FeaturedProductCard.kt` | `ui/customer/CustomerCatalogViews.java` + Canvas | **P** hai cột và illustration đã mô phỏng, chưa parity Compose stagger/motion/semantics. |
| 19 | `ui/components/FeatureVisuals.kt` | `ui/UiKit.java` + XML + artwork | **P** UI utilities chỉ chuyển phần lớn layout/typography, chưa đầy đủ visual. |
| 20 | `ui/components/MedicineMedia.kt` | `CustomerCatalogViews.ProductArtwork` | **P** offline art có, thiếu Coil HTTPS fallback và ảnh bao bì có URL. |
| 21 | `ui/components/PharmaComponents.kt` | `UiKit.java`, `res/values/*` | **P** header, chip, demo disclosure và status có tương ứng khác hình/semantics. |
| 22 | `ui/features/account/AccountScreen.kt` | `ui/customer/CustomerAccountViews.java` | **T/P** account/profile, stats/navigation demo, cần visual/back/accessibility parity. |
| 23 | `ui/features/account/AddressBookScreen.kt` | `CustomerAccountViews` + `DemoState.addressBooks` | **T** create/edit/delete/default local; UI keyboard/validation cần device QA. |
| 24 | `ui/features/account/ChangePasswordScreen.kt` | `CustomerAccountViews` + `DemoState.changePassword` | **T** change password logged-in; thiếu login forgot/reset (nguồn #44). |
| 25 | `ui/features/account/EditProfileScreen.kt` | `CustomerAccountViews` + `DemoState.updateCustomer` | **T** update phone/name/email/birthday, migrate owned data cần regression. |
| 26 | `ui/features/cart/CartScreen.kt` | `ui/customer/CustomerOrderViews.java` (`cart-v2`) | **T/P** +/−, Rx, subtotal demo; chưa hình thumbnails/scroll retention giống Compose. |
| 27 | `ui/features/catalog/CatalogChrome.kt` | `CustomerCatalogViews`, `catalog_v2_*.xml` | **P** hero/chips/results mới nhưng cần visual parity. |
| 28 | `ui/features/catalog/CatalogScreen.kt` | `CustomerCatalogViews.catalog` | **T/P** search/filter/sort demo, Java dynamic grid không LazyGrid. |
| 29 | `ui/features/checkout/CheckoutScreen.kt` | `CustomerOrderViews.checkout` | **T/P** pickup A/B, validation, Rx gate; IME/SaveableState khác. |
| 30 | `ui/features/community/FavoritesScreen.kt` | `CustomerAccountViews.favorites` | **T** owner-scoped favorites và add/remove; layout khác. |
| 31 | `ui/features/community/MockRequestDetailScreen.kt` | `CustomerOrderViews.detail` | **P** hủy có lý do/reorder đã có; snapshot product/giá/created time chưa parity. |
| 32 | `ui/features/community/RequestHistoryScreen.kt` | `CustomerOrderViews.orders` | **P** owner filter có; thiếu timestamp và đầy đủ copy/status visual. |
| 33 | `ui/features/home/HomeScreen.kt` | `CustomerCatalogViews.home` | **P** home featured/shelves có; ảnh/staggered/motion/scroll chưa full parity. |
| 34 | `ui/features/onboarding/OnboardingScreen.kt` | `CustomerAccountViews.onboarding` | **P** 3 bước/skip có nhưng chưa hook first-run + Preferences. |
| 35 | `ui/features/orders/OrderStatusScreen.kt` | `CustomerOrderViews.receipt` | **T/P** receipt DEMO 3 bước/CTA, không có xác nhận nhà thuốc; visual khác. |
| 36 | `ui/features/prescription/PrescriptionScreen.kt` | `CustomerOrderViews.rx`, `MainActivity.choosePrescriptionFile` | **T/P** chọn/bỏ local file; validation MIME/recovery chưa full. |
| 37 | `ui/features/product/ProductDetailScreen.kt` | `CustomerCatalogViews.product` | **P** category/price/Rx/CTA; media/ingredient verified info khác. |
| 38 | `ui/features/staff/CashierScreen.kt` | `ui/staff/StaffPosViews.java` | **T/P** POS demo, ca, giỏ/phiếu, thiếu ảnh preview/guard parity và visual. |
| 39 | `ui/features/staff/OperationsHub.kt` | `StaffAdminViews` + `StaffPosViews` | **T/P** overview/goods/history + requests CHỜ TÍCH HỢP, khác UI 4 tabs/navigation. |
| 40 | `ui/features/staff/StaffCatalogEditor.kt` | `StaffAdminViews.catalog/lots/audit` | **P** tên/giá/tạo/nhập/kiểm kê; thiếu picker ảnh, quarantine cùng lúc nhập, guard forms. |
| 41 | `ui/features/staff/StaffDemoStore.kt` | `core/DemoState.java` | **P** RAM mock data nhưng nghiệp vụ role/stock/reconcile chưa parity; **P0**. |
| 42 | `ui/features/staff/StaffProductMedia.kt` | `CustomerCatalogViews.ProductArtwork`, `staff_pos_*.xml` | **P** chưa full ảnh staff, zoom/pick/replace/remove photo. |
| 43 | `ui/features/staff/StaffWorkspace.kt` | `UiKit`/notice panels | **P nhỏ** composable StaffNotice tương ứng cảnh báo đơn giản, không cần port 1:1 file. |
| 44 | `ui/features/staff/UnifiedLoginScreen.kt` | `ui/AuthScreen.java` | **P** login/register + role picker filling; thiếu forgot/reset UI, reveal-password/IME/visual states. |

### 4.3 Các file preview và theme (6 files, đều còn nghiệm thu)

| # | Kotlin path | Java đối ứng | Mức / lưu ý |
|---:|---|---|---|
| 45 | `ui/previews/CatalogDesignPreviews.kt` | `catalog_v2_*.xml` + Android Studio Layout Preview | **N/P** chưa có bộ ảnh benchmark 320/390/font 1.5 tương đương. |
| 46 | `ui/previews/FeaturedShowcasePreviews.kt` | `CustomerCatalogViews` artwork XML | **N/P** thiếu showcase preview/ảnh render đối chiếu. |
| 47 | `ui/previews/PharmaScreenPreviews.kt` | XML Layout Preview từng trang | **N/P** chưa có fixture states cho Home/Catalog/Cart/Orders... |
| 48 | `ui/previews/ShopFlowPreviews.kt` | Java XML layouts | **N/P** chưa có screenshots account/login/Rx/checkout/receipt theo state. |
| 49 | `ui/previews/StaffPreviews.kt` | `staff_pos_*.xml`, `staff_admin_*.xml` | **N/P** chưa có preview staff/customer/admin ở 320/390/focus/large text. |
| 50 | `ui/theme/Theme.kt` | `res/values/{colors,styles}.xml` | **P** Emerald/Lime/Forest/Cream đã chuyển; typography, dynamic behavior, elevations/shape variants cần đối chiếu. |

**Tổng đúng 50:** nhóm 4.1 = 17, nhóm 4.2 = 27, nhóm 4.3 = 6. “Chưa migrate 1:1” không có nghĩa tất cả 50 Kotlin phải chuyển từng dòng; phần UI preview/component có thể thay bằng Java Views, XML và ảnh kiểm chứng **tương đương hành vi**.

## 5. Kiểm kê đủ 16 Kotlin test files và coverage Java

Tất cả Kotlin dưới `app/src/test/java/com/pharmasiu/mobile/`, Java tests dưới `app-java/src/test/java/com/pharmasiu/javaapp/`.

| # | Kotlin test (số cases) | Java nơi đối chiếu | Coverage gap |
|---:|---|---|---|
| 01 | `domain/AppEntryStateTest.kt` (3) | `CustomerAccountViewsContractTest`, `MainActivity` | Thiếu direct tests launch→login, fresh Activity, logout+Back UI. |
| 02 | `domain/CartRulesTest.kt` (3) | `CustomerOrderViewsTest`, `LegacyDomainRulesTest` | Mức domain cơ bản có; cần invalid qty/value mutation và click listeners. |
| 03 | `domain/CatalogMediaRulesTest.kt` (2) | `LegacyDomainRulesTest` | URI allowlist đã test trong stage/integrated domain; UI HTTPS fallback chưa test. |
| 04 | `domain/CheckoutInputRulesTest.kt` (3) | `CustomerOrderViewsTest`, `LegacyDomainRulesTest` | Form invalid test, chuẩn hóa phone UI khác Kotlin. |
| 05 | `domain/CounterRulesTest.kt` (5) | `JavaRulesTest`, `StaffPosViewsParityTest`, `LegacyDomainRulesTest` | Rx/cash/stock có, frozen draft price và idempotent sale chưa đầy đủ. |
| 06 | `domain/MockFlowRulesTest.kt` (7) | `CustomerCatalogViewsTest`, `CustomerOrderViewsTest`, `LegacyDomainRulesTest` | 30 fixtures/search/DEMO status có; khác timestamp/proof UI, add-on categories. |
| 07 | `domain/MockLotRulesTest.kt` (2) | `JavaRulesTest`, `LegacyDomainRulesTest` | FEFO/quarantine test nhưng runtime Java seed lot khác Kotlin. |
| 08 | `domain/MockPriceSummaryTest.kt` (2) | `LegacyDomainRulesTest`, `CustomerOrderViewsTest` | subtotal/chưa giá có; immutable order line price thiếu. |
| 09 | `domain/OrderStateContractTest.kt` (3) | `LegacyDomainRulesTest`, `JavaRulesTest` | 7 nhãn/11 trạng thái có, còn test full allowed/forbidden graph + context approvals. |
| 10 | `domain/OrderStateMachineTest.kt` (3) | `JavaRulesTest`, `LegacyDomainRulesTest` | Có canTransition, thiếu exhaustiveness và actor/payment/TTL (server only). |
| 11 | `domain/PrescriptionSelectionRulesTest.kt` (4) | `CustomerOrderViewsTest`, `LegacyDomainRulesTest` | Rx fingerprint có; test picker cancellation/mime/URI/recreation còn thiếu. |
| 12 | `domain/StaffLoginRulesTest.kt` (4) | `JavaRulesTest`, DemoState tests gián tiếp | Role login mock có; thiếu unauth reset/back/login history test độc lập. |
| 13 | `domain/UnifiedDemoAuthTest.kt` (3) | `DemoState.login`, `CustomerAccountViewsContractTest` | Login nhân viên/quản trị/khách có; reset/registered customer across account ownership chưa đủ. |
| 14 | `ui/features/staff/OperationsHubFlowTest.kt` (5) | `StaffPosViewsParityTest`, `StaffAdminViewsTest` | Ca/giỏ/quyền demo có; thiếu UI 4 tabs, admin/catalog operation interference. |
| 15 | `ui/features/staff/StaffCatalogEditorTest.kt` (4) | `StaffAdminViewsTest` | Tạo/nhập/rx có; photo URI + replace/remove chưa migrate. |
| 16 | `ui/features/staff/StaffDemoContractTest.kt` (5) | `StaffAdminViewsTest`, `StaffPosViewsParityTest` | **Còn thiếu P0** guard draft price, kiểm kê store/once-per-closed-shift, aggregate limit và role trực tiếp. |

**Tổng Kotlin: 58 tests / 16 files.** **Tổng Java source: 50 tests / 7 files**, trong đó 22 là 5 catalog + 17 legacy-domain đã mới tích hợp từ staging; file tồn tại ≠ test đã chạy. Không so sánh tỉ lệ 50/58 để tuyên bố độ bao phủ, vì cases và execution paths không tương đương. Không tìm thấy tracked instrumented `androidTest` của Java lúc kiểm kê; chưa có coverage output, screenshot baseline hay E2E role flows.

## 6. Design parity: Compose Preview ≠ Android XML Preview

- Kotlin có **34 chú giải `@Preview(` được tìm bằng search source** trong 5 gallery file; con số 31 trong README/QA là mốc cũ hoặc cách đếm khác, cần xác minh live gallery count. Java chỉ có XML layout resources, custom `Canvas`, `Activity` runtime và tài nguyên style; **không có Compose Preview tương ứng tự động**.
- XML Layout Preview chỉ cho thấy phần XML tĩnh; nhiều card, chips, nội dung đơn, biểu đồ KPI, adapter items được dựng bằng Java runtime nên preview XML **không thể tự chứng minh** layout khi có data. Phải chạy screenshot UI bằng Activity + dữ liệu mẫu/test harness.
- Thiết kế cần review **cùng bề rộng thực 320/390/412dp** và 1.5× font: header, nav bar nhiều nút, grid/cell, search/keyboard, modal hủy, Rx picker, account/address form, POS giỏ–thu tiền, admin form/quarantine.
- Kiểm TalkBack labels và focus order cho chip/card favorite, input hint/error, dialog lý do hủy; minimum 48dp touch; dark-mode/rotation/insets/IME; visual empty/loading/invalid states. Kotlin card hero/media/staggered/animation không thể mặc định coi là đã sao chép chỉ vì Java có màu Emerald và cùng nhãn.
- Không có baseline hiệu năng native mới; QA Kotlin từng ghi phép đo emulator tải nặng 95/95 janky frames. Phải đo release/physical device với môi trường ghi rõ; không suy ra Java nhanh hơn hoặc đạt 60fps vì dùng Views.

## 7. Quy trình hoàn thiện và tiêu chí nghiệm thu (không xóa Kotlin sớm)

1. **Đóng băng bản snapshot Java + mapping**: xác nhận 15 main/7 test files mới nhất, không còn bản staged chưa copy, route `MainActivity` xác định rõ owner `CustomerCatalogViews`/`CustomerOrderViews`/`CustomerAccountViews`/`StaffPosViews`/`StaffAdminViews`. Dọn `CustomerScreens`/`CashierScreens`/`AdminScreens` chỉ khi tìm được không còn callers, bằng thay đổi sở hữu rõ ràng.
2. **Sửa P0 ở `DemoState`/staff store**: giá khi draft, lot total/FEFO/quarantine, role checks ở mutation method, reconciliation sau đóng ca duy nhất một lần, snapshot ca. Bổ sung tests phủ các đường thất bại; không sửa backend/DB.
3. **Hoàn thành customer parity**: quên/khôi phục mật khẩu demo, first-run onboarding + cờ hoàn tất, Back stack/rotation/owner isolation, ảnh sản phẩm catalog và staff, order snapshot timestamp/name/price, MIME validation local Rx, cancellation/reorder. Giữ các nhãn CHỜ TÍCH HỢP.
4. **Đồng nhất contract domain**: ưu tiên một nguồn Java rule được UI/store dùng (`JavaRules` vs `LegacyDomainRules` vs logic trùng trong `DemoState`/`CustomerCatalogViews`), duy trì 7 nhóm state + 11 internal state. Tránh hai bộ FEFO/filter/checkout rules lệch nhau; kiểm fixtures Kotlin–Java với input/output chung.
5. **Hoàn thiện test migration**: table-driven 58 test case intent từ Kotlin sang Java cùng expected results; bổ sung test UI event/callback, navigation, denied roles, double submit, expired/quarantined lot, no payment claims, onboarding lifecycle. Chạy lại `gradlew.bat :app-java:assembleDebug :app-java:testDebugUnitTest`; CI Android lint + instrumentation nếu runner được cấu hình.
6. **Native UI acceptance**: screenshot và kiểm thực tế emulator API 28/36 hoặc device, 3 role, 17 BFD map, 320/390/412dp, 1.5× font, TalkBack/IME/Back, modal/picker/logout, product artwork và scroll. Với mục 1.3/3.2/3.3/3.4 thật chỉ xác minh UI đúng chữ **CHỜ TÍCH HỢP**, không claim server.
7. **Báo cáo hai mức rõ ràng**: (a) **Java demo feature parity** nếu tất cả hành vi demo và visual/test đạt; (b) **SRS production parity** vẫn NOT DONE đến khi hệ thống thật có backend, dược sĩ có quyền, FEFO/tồn thực, TTL/idempotency/payment/handover. Chỉ cân nhắc đổi default launcher/retire `:app` sau sign-off từ prime và so rollback. Không xóa Kotlin/gradle user changes trong audit này.

## 8. Các thành phần cần giữ là historical, không coi là current sign-off

- `docs/JAVA_PARITY_CHECKLIST.md` và `docs/JAVA_MIGRATION_QA_2026-10-29.md` mô tả số file/test/build lúc module Java còn bản nhỏ. Hãy thêm nhãn **HISTORY — initial Java v0** nếu giữ nguyên log; tạo current validation table khác khi build bản tích hợp.
- `docs/QA_REPORT.md` ghi lịch sử QA Compose nhiều vòng (14→23→28→32→36→42…), không chứng nhận Java 50 tests/7 suites hiện tại. Cờ device/TalkBack/visual NOT RUN vẫn NOT PASS khi không có bằng chứng mới.
- Giữ `docs/ALIGNMENT_*.md` là contract nghiệp vụ của đợt này, không xem UI mock là chứng nhận dược sĩ/thanh toán/kho.
- `gradle.properties` và thay đổi unrelated trong working tree đã tồn tại trước audit; không chỉnh, không reset, không commit.

**Kết quả audit:** tìm được đối ứng **hầu hết các tuyến UI demo** trong Java, nhưng khoảng cách về nghiệp vụ POS/store, Rx file validation, forgot password, onboarding, Back stack, media, snapshot order, thiết kế, test E2E và chứng cứ build mới còn thực chất. **Tuyệt đối không tuyên bố “migrated 100%”.**
