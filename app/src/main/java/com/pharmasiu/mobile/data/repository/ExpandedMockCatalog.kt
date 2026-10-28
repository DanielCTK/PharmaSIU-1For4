package com.pharmasiu.mobile.data.repository

import com.pharmasiu.mobile.data.model.Medicine

/** Fictional products for offline UI only. No therapeutic or inventory claims. */
object ExpandedMockCatalog {
    private fun mock(
        id: String, name: String, subtitle: String, price: Long, category: String,
        unit: String = "Sản phẩm", rx: Boolean = false,
    ) = Medicine(
        id = id, name = name, subtitle = subtitle, priceVnd = price,
        category = category, requiresPrescription = rx, unit = unit,
        description = "Dữ liệu mẫu dành cho giao diện PharmaSIU. Quy cách, nguồn gốc, thành phần, giá và khả năng cung ứng phải được xác minh tại nơi bán. Sản phẩm không thay thế tư vấn của nhân viên chuyên môn.",
    )
    val products: List<Medicine> = listOf(
        mock("otc-01", "Sản phẩm không kê đơn · Mẫu A", "Chỉ là mẫu UI, chưa xác minh phân loại", 42000, "Không kê đơn", "Hộp"),
        mock("otc-02", "Sản phẩm không kê đơn · Mẫu B", "Chỉ là mẫu UI, chưa xác minh phân loại", 55000, "Không kê đơn", "Chai"),
        mock("otc-03", "Sản phẩm không kê đơn · Mẫu C", "Chỉ là mẫu UI, chưa xác minh phân loại", 32000, "Không kê đơn", "Gói"),
        mock("rx-demo-02", "Thuốc kê đơn · Mẫu B", "Chỉ mô phỏng luồng toa Rx", 0, "Thuốc kê đơn", "Hộp", true),
        mock("beauty-01", "Sữa rửa mặt dịu nhẹ", "Mỹ phẩm chăm sóc da · 150 ml", 118000, "Mỹ phẩm", "Chai"),
        mock("beauty-02", "Kem dưỡng ẩm hằng ngày", "Mỹ phẩm chăm sóc da · 50 g", 169000, "Mỹ phẩm", "Hũ"),
        mock("beauty-03", "Son dưỡng môi", "Mỹ phẩm chăm sóc môi · 4 g", 59000, "Mỹ phẩm", "Thỏi"),
        mock("beauty-04", "Sữa tắm dịu nhẹ", "Mỹ phẩm chăm sóc cơ thể · 400 ml", 129000, "Mỹ phẩm", "Chai"),
        mock("beauty-05", "Dầu gội chăm sóc tóc", "Mỹ phẩm chăm sóc tóc · 300 ml", 145000, "Mỹ phẩm", "Chai"),
        mock("beauty-06", "Kem chống nắng mẫu", "Mỹ phẩm chăm sóc da · 50 ml", 189000, "Mỹ phẩm", "Tuýp"),
        mock("food-01", "Yến mạch cán mỏng", "Thực phẩm đóng gói · 500 g", 79000, "Thực phẩm", "Gói"),
        mock("food-02", "Ngũ cốc ăn sáng", "Thực phẩm đóng gói · 350 g", 68000, "Thực phẩm", "Hộp"),
        mock("food-03", "Hạt điều rang", "Thực phẩm đóng gói · 250 g", 109000, "Thực phẩm", "Hũ"),
        mock("food-04", "Bánh quy yến mạch", "Thực phẩm đóng gói · 180 g", 45000, "Thực phẩm", "Hộp"),
        mock("food-05", "Trà thảo mộc dạng túi", "Thực phẩm, đồ uống · 20 túi", 66000, "Thực phẩm", "Hộp"),
        mock("food-06", "Nước trái cây đóng chai", "Thực phẩm, đồ uống · 300 ml", 29000, "Thực phẩm", "Chai"),
        mock("supp-01", "Thực phẩm bổ sung · Mẫu A", "Mẫu thiết kế · không phải thuốc", 179000, "Thực phẩm bổ sung", "Hộp"),
        mock("supp-02", "Thực phẩm bổ sung · Mẫu B", "Mẫu thiết kế · không thay thế thuốc", 239000, "Thực phẩm bổ sung", "Hộp"),
        mock("care-01", "Nước súc miệng", "Chăm sóc cá nhân · 250 ml", 64000, "Chăm sóc", "Chai"),
        mock("care-02", "Bàn chải đánh răng mềm", "Chăm sóc răng miệng · 1 chiếc", 32000, "Chăm sóc", "Cái"),
        mock("care-03", "Khăn giấy bỏ túi", "Chăm sóc cá nhân · 10 gói", 35000, "Chăm sóc", "Bịch"),
        mock("device-02", "Máy đo huyết áp mẫu", "Thiết bị chăm sóc gia đình", 599000, "Thiết bị", "Cái"),
        mock("device-03", "Hộp chia đồ dùng", "Dụng cụ gia đình · 7 ngăn", 79000, "Thiết bị", "Cái"),
        mock("supplies-02", "Băng cá nhân", "Vật tư y tế · hộp 20 miếng", 27000, "Vật tư y tế", "Hộp"),
    )
}
