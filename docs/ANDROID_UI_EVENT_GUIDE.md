# PharmaSIU Android — quy ước UI, sự kiện và điều hướng

## Mục tiêu

Áp dụng các nguyên tắc về cấu trúc ứng dụng Android, View/Layout, bắt sự kiện và thanh điều hướng **vào source Kotlin + Jetpack Compose hiện tại**, không chuyển đổi framework hoặc làm lại UI. Các luồng Trang chủ, Danh mục, Giỏ hàng, Checkout, Đơn hàng demo, Tài khoản được giữ nguyên.

## Cách ánh xạ vào dự án hiện tại

| Khái niệm | Hiện thực trong PharmaSIU |
|---|---|
| Android Studio, Activity và lifecycle | \`MainActivity\` làm entry point; \`PharmaSIUApp\` điều phối màn hình. \`rememberSaveable\` giữ các trường nhập liệu và lựa chọn UI phù hợp. |
| LinearLayout, FrameLayout, các thuộc tính layout | Dùng \`Column\`, \`Row\`, \`Box\`, \`Modifier.padding / weight / fillMaxWidth\`. Danh sách nhiều sản phẩm dùng \`LazyVerticalGrid\` hoặc \`LazyVerticalStaggeredGrid\`. Không thay bằng XML View truyền thống. |
| TextView, EditText, Button | Dùng \`Text\`, \`OutlinedTextField\`, \`Button\` của Compose/Material 3. Nhãn tái dùng đặt trong \`res/values/strings.xml\`. |
| onClick và các Listener | Dùng callback \`onClick\`, \`onValueChange\`, \`KeyboardActions\`; không nhét quy tắc nghiệp vụ trực tiếp vào sự kiện nút bấm. |
| Action Bar, menu và điều hướng | Thanh trên \`PharmaTopBar\` có tên màn hình/nút Back, thanh dưới \`NavigationBar\` điều hướng giữa 5 phần chính. Không thêm Navigation Drawer vì trùng chức năng và gây thay đổi trải nghiệm không cần thiết. |
| Debug và kiểm thử | Chạy \`:app:assembleDebug\`, \`:app:testDebugUnitTest\`; kiểm tra các thao tác trên emulator và làm rõ các trường hợp chưa đo được. |

## Quy tắc cho UI events

1. **Sự kiện thuộc màn hình:** các Composable chỉ cập nhật trạng thái UI, gửi callback khi cần. Ví dụ nhấn “Đặt hàng” gọi \`onPlaceRequest\`; quy tắc đặt hàng demo vẫn được kiểm tra thêm ở \`PharmaSIUApp\`.
2. **Quy tắc thuần được đưa vào domain:** \`CheckoutInputRules\` quản lý chuẩn hóa/kiểm tra họ tên, số điện thoại; không phụ thuộc Android hay Compose nên có unit tests riêng.
3. **Hành vi bàn phím rõ ràng:** Checkout cho phép Next từ tên sang số điện thoại, Done để đóng bàn phím; Search trong Danh mục đóng bàn phím khi người dùng bấm Search. Kết quả tìm kiếm vẫn cập nhật theo văn bản như trước.
4. **Hỗ trợ accessibility:** tiêu đề chung trong \`PharmaTopBar\` được đánh dấu \`heading\`; nút Back có nhãn đọc màn hình. Không bỏ nhãn các nút bấm.
5. **Không thay đổi nghiệp vụ:** dữ liệu mock, giá, nhóm hàng, FEFO, toa thuốc, điều kiện checkout và trạng thái đơn vẫn như trước; không suy diễn đơn demo là đơn đã được nhà thuốc xác nhận.

## Phạm vi chưa triển khai

**SQLite/SQL để sau.** Không tạo \`SQLiteOpenHelper\`, Room, bảng, schema, migration, \`DAO\` hay thay \`DemoCatalogRepository\`. Khi thiết kế dữ liệu, cần chốt entity và vòng đời thông tin trước, rồi lựa chọn persistence phù hợp; không chèn SQL trực tiếp vào Composable.

## Checklist hồi quy

- Mở Trang chủ, lướt lưới sản phẩm, mở Danh mục, tìm kiếm và sắp xếp như trước.
- Trong Checkout nhập tên và bấm Next: con trỏ đến điện thoại; Done đóng bàn phím, không tự gửi đơn.
- Tên/số điện thoại không hợp lệ vẫn vô hiệu hóa nút Đặt hàng; dữ liệu hợp lệ với giỏ không rỗng vẫn cho phép thao tác.
- Đặt hàng demo → màn thành công → xem lịch sử vẫn hoạt động, không ghi dữ liệu vào SQLite.
- Các nút Back và 5 tab chính giữ đường dẫn cũ, không xuất hiện Drawer trùng.
- Bản build, unit tests đạt; xác nhận bổ sung trên emulator và thiết bị thật trước khi đưa vào sử dụng thực tế.
