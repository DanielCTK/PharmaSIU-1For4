package com.pharmasiu.mobile.data.repository

import com.pharmasiu.mobile.data.model.Medicine

/**
 * Demo catalog used strictly for UI testing.
 * No price, batch, stock level or medical guidance below is authoritative.
 */
object DemoCatalogRepository {
    private val initialMedicines = listOf(
        Medicine(
            id = "mask-01",
            name = "Khẩu trang y tế",
            subtitle = "Hộp 50 chiếc · Vật tư y tế",
            priceVnd = 36000,
            category = "Vật tư y tế",
            requiresPrescription = false,
            unit = "Hộp",
            description = "Thông tin sản phẩm minh họa. Vui lòng kiểm tra quy cách, giá và tình trạng hàng tại nhà thuốc.",
        ),
        Medicine(
            id = "saline-01",
            name = "Dung dịch NaCl 0,9%",
            subtitle = "Chai 500 ml · Chăm sóc cá nhân",
            priceVnd = 18000,
            category = "Chăm sóc",
            requiresPrescription = false,
            unit = "Chai",
            description = "Sản phẩm được đưa vào dữ liệu mẫu chỉ để trình bày giao diện. Không thay thế hướng dẫn chuyên môn.",
        ),
        Medicine(
            id = "cotton-01",
            name = "Bông y tế",
            subtitle = "Gói 100 g · Vật tư y tế",
            priceVnd = 24000,
            category = "Vật tư y tế",
            requiresPrescription = false,
            unit = "Gói",
            description = "Dữ liệu minh họa cho luồng đặt trước và nhận tại quầy.",
        ),
        Medicine(
            id = "thermometer-01",
            name = "Nhiệt kế điện tử",
            subtitle = "Thiết bị chăm sóc gia đình",
            priceVnd = 129000,
            category = "Thiết bị",
            requiresPrescription = false,
            unit = "Cái",
            description = "Thông số, giá và điều kiện cung ứng sẽ được xác nhận tại quầy.",
        ),
        Medicine(
            id = "bandage-01",
            name = "Băng gạc vô trùng",
            subtitle = "Gói 10 miếng · Vật tư y tế",
            priceVnd = 29000,
            category = "Vật tư y tế",
            requiresPrescription = false,
            unit = "Gói",
            description = "Thông tin giả lập nhằm kiểm tra giao diện xem sản phẩm.",
        ),
        Medicine(
            id = "rx-demo-01",
            name = "Thuốc kê đơn · Mẫu",
            subtitle = "Sản phẩm minh họa quy trình Rx",
            priceVnd = 0,
            category = "Thuốc kê đơn",
            requiresPrescription = true,
            unit = "Hộp",
            description = "Không phải thuốc được chào bán. Quy trình này yêu cầu gửi toa hợp lệ và được dược sĩ xem xét trước khi xác nhận.",
        ),
    )

    val medicines: List<Medicine> = initialMedicines + ExpandedMockCatalog.products

    fun byId(id: String): Medicine? = medicines.firstOrNull { it.id == id }
}
