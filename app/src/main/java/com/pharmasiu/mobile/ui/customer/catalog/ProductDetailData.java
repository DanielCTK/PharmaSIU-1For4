package com.pharmasiu.mobile.ui.customer.catalog;

import java.util.HashMap;
import java.util.Map;

/**
 * Clinical and usage information for PharmaSIU products.
 * Provides rich descriptions, active ingredients, and safety notices.
 */
public final class ProductDetailData {
    private ProductDetailData() {}

    private static final Map<String, String> USAGES = new HashMap<>();
    private static final Map<String, String> INGREDIENTS = new HashMap<>();

    static {
        USAGES.put("RX-001", "Thuốc điều trị đái tháo đường típ 2 và các chỉ định bảo vệ tim mạch – thận theo chỉ định của bác sĩ.");
        USAGES.put("RX-002", "Thuốc nhóm statin, dùng kiểm soát rối loạn lipid máu và giảm nguy cơ biến cố tim mạch ở đối tượng phù hợp.");
        USAGES.put("RX-003", "Thuốc chẹn beta chọn lọc, dùng trong điều trị suy tim mạn ổn định theo chỉ định của bác sĩ.");
        USAGES.put("RX-004", "Thuốc chẹn beta chọn lọc, sử dụng trong điều trị tăng huyết áp và bệnh mạch vành (đau thắt ngực) theo chỉ định.");
        USAGES.put("RX-005", "Thuốc ức chế men chuyển, điều trị tăng huyết áp và các bệnh lý suy tim, tim mạch theo chỉ định.");
        USAGES.put("RX-006", "Thuốc hỗ trợ điều trị triệu chứng đau thắt ngực ổn định ở bệnh nhân tim mạch phù hợp.");
        USAGES.put("RX-007", "Thuốc ức chế bơm proton (PPI), giảm tiết acid trong bệnh lý viêm loét dạ dày - tá tràng và trào ngược dạ dày thực quản (GERD).");
        USAGES.put("RX-008", "Kháng sinh beta-lactam phối hợp chất ức chế men beta-lactamase; điều trị các nhiễm khuẩn đường hô hấp, tai mũi họng, tiết niệu nhạy cảm.");
        USAGES.put("RX-009", "Kháng sinh phối hợp phổ rộng, điều trị các nhiễm khuẩn tai mũi họng, phế quản, phổi, da và tiết niệu theo đơn.");
        USAGES.put("RX-010", "Kháng sinh phối hợp điều trị nhiễm khuẩn nhạy cảm hô hấp, da, mô mềm và đường tiết niệu theo chỉ định.");
        USAGES.put("RX-013", "Kháng sinh nhóm macrolid/azalid, điều trị nhiễm khuẩn đường hô hấp trên và dưới, nhiễm trùng da và mô mềm.");
        USAGES.put("RX-014", "Kháng sinh cephalosporin thế hệ 1, điều trị nhiễm khuẩn đường hô hấp, tai mũi họng và tiết niệu nhạy cảm.");
        USAGES.put("OTC-001", "Giảm đau nhẹ đến vừa, hạ sốt cho trẻ em và người lớn (đau đầu, đau răng, sốt do cảm cúm).");
        USAGES.put("OTC-002", "Giảm đau, hạ sốt hiệu quả phù hợp với thể trọng người Việt Nam (đau đầu, đau cơ xương khớp, sốt).");
        USAGES.put("OTC-003", "Giảm đau nhanh, hạ sốt mạnh mẽ, tăng cường tỉnh táo, giảm đau đầu nặng và đau nửa đầu.");
        USAGES.put("OTC-004", "Giảm đau, hạ sốt tác dụng nhanh dạng hòa tan sủi bọt, dễ uống khi sốt cao hoặc đau nhức.");
        USAGES.put("OTC-005", "Giảm đau nhanh các chứng đau đầu, đau nửa đầu, đau răng, đau cơ và hạ sốt không gây buồn ngủ.");
        USAGES.put("OTC-006", "Giảm đau nhẹ đến vừa và hạ sốt thông thường, an toàn cho dạ dày khi dùng đúng liều lượng.");
        USAGES.put("OTC-007", "Giảm đau, hạ sốt tác dụng nhanh dạng viên sủi, vị dễ uống, hấp thu nhanh qua đường tiêu hóa.");
        USAGES.put("OTC-008", "Giảm triệu chứng cảm cúm, sổ mũi, nghẹt mũi, hạ sốt và giảm đau không gây buồn ngủ (công thức ban ngày).");
        USAGES.put("OTC-009", "Giảm nhanh các triệu chứng cảm sốt, nghẹt mũi, chảy nước mũi, hắt hơi và viêm mũi dị ứng.");
        USAGES.put("OTC-010", "Viên nang thảo dược giúp làm ấm đường hô hấp, giảm các triệu chứng ho do lạnh, đau rát cổ họng, khản tiếng, sổ mũi.");
        USAGES.put("OTC-011", "Giảm ho đờm đặc, sát trùng đường hô hấp, làm loãng niêm dịch và dịu cơn đau họng rát cổ.");
        USAGES.put("OTC-012", "Thuốc tiêu chất nhầy, làm lỏng chất tiết niêm mạc phế quản trong các bệnh lý hô hấp có đờm đặc.");
        USAGES.put("OTC-013", "Giảm đau, hạ sốt tác dụng kéo dài theo thể trọng, trị cảm sốt, nhức đầu, đau răng.");
        USAGES.put("OTC-014", "Phối hợp hiệp đồng kép Paracetamol và Ibuprofen giảm đau và kháng viêm mạnh (đau răng, đau cơ xương khớp, đau sau phẫu thuật).");
        USAGES.put("OTC-015", "Giảm đau, hạ sốt kết hợp bổ sung Vitamin C tăng cường sức đề kháng cơ thể khi cảm cúm.");
        USAGES.put("OTC-016", "Thuốc chống viêm không steroid (NSAID), giảm đau từ vừa đến nặng và hạ sốt (viêm khớp, đau răng, đau bụng kinh).");
        USAGES.put("OTC-017", "Trị các triệu chứng cảm cúm: sốt, nhức đầu, sổ mũi, nghẹt mũi, ớn lạnh, đau nhức cơ bắp.");
        USAGES.put("OTC-018", "Kháng histamin H1 thế hệ 2, điều trị viêm mũi dị ứng và mày đay tự phát mạn tính, không gây buồn ngủ.");
        USAGES.put("OTC-019", "Kháng histamin tác dụng kéo dài 24h, điều trị các triệu chứng mề đay, mẩn ngứa và viêm mũi dị ứng theo mùa ở người lớn.");
        USAGES.put("OTC-020", "Điều trị các triệu chứng do viêm mũi dị ứng theo mùa (hắt hơi, chảy nước mũi, ngứa mũi, mắt đỏ ngứa) ở người lớn và trẻ trên 12 tuổi.");
        USAGES.put("OTC-021", "Bù nước và bổ sung các chất điện giải khi mất nước do tiêu chảy cấp, nôn mửa, sốt cao hoặc tập thể thao ra nhiều mồ hôi.");
        USAGES.put("VTYT-001", "Che phủ mũi và miệng, hạn chế tiếp xúc với giọt bắn, khói bụi mịn và vi khuẩn đường hô hấp; thoáng khí, thanh nẹp mũi ôm khít mặt.");
        USAGES.put("VTYT-003", "Khẩu trang y tế xuất khẩu 4 lớp kháng khuẩn, ngăn ngừa vi khuẩn và giọt bắn lây nhiễm; quai thun mềm mại không gây đau tai.");
        USAGES.put("VTYT-006", "Bông tẩy trang 100% sợi bông tự nhiên nguyên chất, hỗ trợ tẩy trang và thoa dưỡng chất, không để lại xơ bông trên da.");
        USAGES.put("VTYT-008", "Bông y tế cuộn sạch hỗ trợ vệ sinh, thấm hút dịch vết thương và sát trùng ngoài da an toàn.");
        USAGES.put("VTYT-009", "Băng vệ sinh siêu mỏng có cánh Kotex, khóa ẩm và chống tràn vượt trội cho ngày chu kỳ.");
        USAGES.put("VTYT-010", "Băng vệ sinh Diana mặt vi sợi mềm mại công nghệ Nano kháng khuẩn, thấm hút tức thì giữ bề mặt khô thoáng.");
        USAGES.put("VTYT-012", "Cảm biến hồng ngoại đo trán không tiếp xúc Model MC-720, đo nhiệt độ nhanh trong 1 giây với độ chính xác y khoa cao.");
        USAGES.put("DA-001", "Kem rửa mặt làm sạch sâu bã nhờn, kháng khuẩn gây mụn và giữ ẩm màng tế bào da dịu nhẹ.");
        USAGES.put("DA-002", "Bọt rửa mặt tạo bọt sẵn làm sạch nhẹ dịu, giảm viêm mụn và ngăn ngừa mụn mới xuất hiện.");
        USAGES.put("DA-003", "Sữa rửa mặt phục hồi hàng rào bảo vệ da, cấp ẩm sâu với 3 Ceramide thiết yếu, phù hợp da nhạy cảm.");
        USAGES.put("DA-004", "Nước tẩy trang 3 trong 1 làm sạch sâu lớp trang điểm, thông thoáng lỗ chân lông mà không làm khô căng da.");
        USAGES.put("DA-005", "Nước tẩy trang Micellar làm sạch mọi cặn trang điểm và bụi bẩn chỉ trong một bước, an toàn cho da nhạy cảm.");
        USAGES.put("DA-007", "Serum dưỡng sáng đều màu da, ức chế melanin hình thành nám sạm và chống oxy hóa mạnh mẽ.");
        USAGES.put("DA-008", "Dầu dưỡng ẩm ban đêm, phục hồi màng ẩm sinh học, giảm nếp nhăn và cải thiện độ đàn hồi săn chắc cho da.");
        USAGES.put("DA-009", "Sữa chống nắng bảo vệ cực mạnh SPF81 PA++++, kháng nước và mồ hôi tối đa cho các hoạt động ngoài trời.");
        USAGES.put("DA-010", "Gel xử lý mụn viêm, làm mờ thâm sẹo sau mụn và tái tạo tế bào da tổn thương với Nano Curcumin.");
        USAGES.put("DA-011", "Kem bôi da chiết xuất nghệ tươi và rau má, làm mát dịu da, mờ vết thâm sẹo và tái tạo mô da non.");
        USAGES.put("DA-014", "Serum dưỡng sáng mịn màng vùng da dưới cánh tay và giảm tiết mồ hôi suốt 48 giờ.");
        USAGES.put("CN-001", "Nước súc miệng vị trà xanh tự nhiên không cay, chứa Flouride ngừa sâu răng và diệt 99.9% vi khuẩn mảng bám.");
        USAGES.put("CN-002", "Nước súc miệng trẻ em hương dâu ngọt ngào, không cồn, bảo vệ men răng non nớt của bé.");
        USAGES.put("CN-003", "Nước súc miệng công thức 6 tác động: sạch khuẩn, ngừa sâu răng, thơm miệng, chắc nướu, giảm mảng bám, trắng răng.");
        USAGES.put("CN-004", "Nước súc miệng diệt khuẩn cho hơi thở bạc hà the mát sảng khoái và bảo vệ khoang miệng suốt 24h.");
        USAGES.put("CN-005", "Nước súc miệng khoáng kẽm Zinc bảo vệ răng miệng toàn diện, ngừa sâu răng và chống viêm lợi.");
        USAGES.put("CN-006", "Kem đánh răng dạng gel giảm nhanh cảm giác ê buốt răng nhạy cảm khi ăn uống đồ nóng lạnh.");
        USAGES.put("CN-007", "Kem đánh răng chống ê buốt vị bạc hà tươi mát, tái khoáng hóa và bảo vệ ngà răng 24/7.");
        USAGES.put("CN-011", "Sữa dưỡng thể công nghệ GlutaGlow và Niacinamide dưỡng da trắng sáng rạng rỡ và cấp nước tức thì.");
        USAGES.put("CN-013", "Sữa tắm dưỡng thể Deep Moisture chứa tinh chất NutriumMoisture giúp da ẩm mịn sau khi tắm.");
        USAGES.put("CN-016", "Dầu gội xả 2 trong 1 hương nước hoa nam tính quý phái, làm sạch gàu và nuôi dưỡng da đầu.");
        USAGES.put("CN-018", "Dầu gội thảo dược phủ bạc đen tóc tự nhiên, nhanh chóng và không gây kích ứng da đầu.");
        USAGES.put("TIEUDUNG-001", "Bình giữ nhiệt inox 304 cao cấp giữ nhiệt nóng lạnh 12h, có quai xách tiện lợi cho sinh hoạt.");
        USAGES.put("TIEUDUNG-002", "Bình inox 2 lớp chân không giữ nhiệt vượt trội, thiết kế thanh lịch bỏ túi mang theo.");
        USAGES.put("TIEUDUNG-005", "Nước xả vải hương hoa thơm dịu, chống xơ cứng sợi vải và giữ hương thơm ngát bền lâu.");
        USAGES.put("TIEUDUNG-007", "Cọ rửa bình sữa, bình giữ nhiệt bằng sợi silicone mềm, vệ sinh sạch tận đáy bình.");
        USAGES.put("TIEUDUNG-008", "Dụng cụ hút mũi trẻ em đầu silicone mềm mại, làm sạch chất nhầy mũi an toàn cho bé sơ sinh.");
        USAGES.put("DOUONG-001", "Nước giải khát có ga hương cola nguyên bản mát lạnh sảng khoái, giải tỏa cơn khát tức thì.");
        USAGES.put("DOUONG-002", "Nước ngọt giải nhiệt không calo không đường, vị chanh thanh mát phù hợp cho người ăn kiêng.");
        USAGES.put("DOUONG-003", "Nước tăng lực hương dâu tây bổ sung Taurine và Vitamin B phục hồi năng lượng tỉnh táo.");
        USAGES.put("DOUONG-004", "Nước tăng lực hương nhân sâm tiếp sức năng lượng bền bỉ cho ngày dài hoạt động.");
        USAGES.put("DOUONG-005", "Nước tăng lực Red Bull nắp vàng nạp năng lượng mạnh mẽ cho ngày làm việc học tập căng thẳng.");
        USAGES.put("DOUONG-008", "Sữa lúa mạch Nestlé Milo cung cấp Canxi, Sắt và Vitamin giúp phát triển thể chất bền bỉ.");
        USAGES.put("DOUONG-009", "Sữa tươi tiệt trùng 100% sữa bò tươi nguyên chất từ trang trại TH giàu dinh dưỡng tự nhiên.");
        USAGES.put("DOUONG-013", "Nước uống vị sữa chua thanh nhẹ bổ sung nước và mang lại cảm giác sảng khoái tinh thần.");
        USAGES.put("DOUONG-014", "Trà Ô Long nguyên chất Tea+ chứa OTPP giúp hạn chế hấp thu chất béo sau bữa ăn.");
        USAGES.put("DOUONG-016", "Nước giải khát có ga vị chanh tự nhiên tươi mát hỗ trợ tiêu hóa thức ăn tốt hơn.");
        USAGES.put("DOUONG-017", "Nước tinh khiết qua hệ thống lọc thẩm thấu ngược Hydro-7 đạt chuẩn vệ sinh an toàn quốc tế.");
        USAGES.put("SNACK-001", "Khoai tây chiên giòn rụm từ 100% khoai tây tươi tự nhiên chọn lọc.");
        USAGES.put("SNACK-003", "Snack khoai tây lát lượn sóng dày dặn đậm đà vị bít tết New York thơm lừng.");
        USAGES.put("SNACK-004", "Bắp ngọt sấy giòn vị phô mai Oishi bùi ngậy quen thuộc cho bữa ăn nhẹ.");
        USAGES.put("SNACK-005", "Snack khoai tây cay giòn bùng nổ vị ớt cay truyền thống Nhật Bản.");
        USAGES.put("SNACK-009", "Bánh snack bắp nướng bơ tỏi thơm lừng giòn tan hấp dẫn.");
        USAGES.put("SNACK-011", "Bánh bông lan xốp mềm kẹp nhân marshmallow phủ socola đậm đà giàu năng lượng.");
        USAGES.put("SNACK-012", "Bánh que nhúng socola hạnh nhân giòn rụm thơm ngon tiện lợi mang theo.");
        USAGES.put("SNACK-014", "Kẹo cao su không đường chứa Xylitol ngăn ngừa sâu răng và mang lại hơi thở thơm mát.");
        USAGES.put("SNACK-017", "Kẹo ngậm nhai mềm hương bạc hà và trái cây tươi mát sảng khoái.");
        USAGES.put("TPCN-001", "Thực phẩm bảo vệ sức khỏe chiết xuất cao bạch quả giúp tăng tuần hoàn máu não, giảm đau đầu chóng mặt.");
        USAGES.put("TPCN-003", "Sụn vi cá mập thiên nhiên bổ sung Chondroitin tái tạo sụn khớp và giảm thoái hóa khớp.");
        USAGES.put("TPCN-005", "Bổ sung Canxi, Magie và Vitamin D3 hỗ trợ xương răng chắc khỏe và ngăn ngừa loãng xương.");
        USAGES.put("TPCN-006", "Dầu cá biển sâu giàu EPA và DHA hỗ trợ sức khỏe tim mạch, phát triển trí não và thị lực.");
        USAGES.put("TPCN-008", "Hỗ trợ cải thiện giấc ngủ tự nhiên, giúp ngủ ngon và sâu giấc hơn, giảm mệt mỏi do lệch múi giờ.");
        USAGES.put("TPCN-009", "Viên bổ mắt Lutein và tinh chất quả Berry giảm mỏi mắt, khô mắt khi làm việc máy tính nhiều.");
        USAGES.put("TPCN-010", "Nhân sâm G115 kết hợp đa vitamin và khoáng chất phục hồi thể lực và giảm mệt mỏi suy nhược.");
        USAGES.put("TPCN-011", "Viên uống bổ sung 9 loại vitamin và khoáng chất thiết yếu tăng cường sức đề kháng cho cơ thể.");
        USAGES.put("TPCN-012", "Trà túi lọc thảo mộc Atiso Đà Lạt giúp thanh nhiệt giải độc, mát gan và lợi mật.");
        USAGES.put("TPCN-017", "Viên uống thảo dược sâm nhung bổ thận tráng dương, tăng cường sinh lực phái mạnh.");
        USAGES.put("TPCN-020", "Men vi sinh hỗ trợ cân bằng hệ vi sinh đường ruột, giảm rối loạn tiêu hóa và đầy hơi khó tiêu.");
        USAGES.put("TPCN-022", "Bổ sung phức hợp Vitamin B1, B6, B12 hỗ trợ chức năng hệ thần kinh và cải thiện mệt mỏi, đau nhức cơ.");

        INGREDIENTS.put("CN-001", "Tinh dầu tự nhiên, Trà xanh Chai 250 ml");
        INGREDIENTS.put("CN-002", "Fluoride không cồn vị Berry Chai 250 ml");
        INGREDIENTS.put("CN-003", "Công thức 6 tác động Total Care Chai 250 ml");
        INGREDIENTS.put("CN-004", "Bạc hà the mát Cool Mint Chai 250 ml");
        INGREDIENTS.put("CN-005", "Zinc khoáng chất ngừa sâu răng Chai 500 ml");
        INGREDIENTS.put("CN-006", "Potassium Nitrate giảm ê buốt Gel Tuýp 100 g");
        INGREDIENTS.put("CN-007", "Bạc hà tươi mát Fresh Mint Tuýp 100 g");
        INGREDIENTS.put("CN-011", "GlutaGlow + Niacinamide Tone-Up Tuýp 300 ml");
        INGREDIENTS.put("CN-013", "Sữa dưỡng ẩm dịu mát Deep Moisture Chai 500 g");
        INGREDIENTS.put("CN-016", "Hương nước hoa Classic 2 trong 1 Chai 650 g");
        INGREDIENTS.put("CN-018", "Chiết xuất thảo dược phủ bạc đen Hộp 2 gói x 30 ml");
        INGREDIENTS.put("DA-001", "Isopropylmethylphenol, Vitamin C, E Tuýp 100 g");
        INGREDIENTS.put("DA-002", "Bọt rửa mặt kháng khuẩn Acnes Chai vòi bơm 150 ml");
        INGREDIENTS.put("DA-003", "Ceramide, Hyaluronic Acid Chai 236 ml");
        INGREDIENTS.put("DA-004", "Nước tẩy trang Micellar Water Chai 400 ml");
        INGREDIENTS.put("DA-005", "Nước làm sạch Micellar Cleansing Chai 400 ml");
        INGREDIENTS.put("DA-007", "Serum dưỡng trắng Vitamin C NNO Vite Hộp 30 viên nang");
        INGREDIENTS.put("DA-008", "Dầu dưỡng ban đêm NNO Vitamin E Hộp 30 viên nang");
        INGREDIENTS.put("DA-009", "Chống nắng Sunplay Super Block SPF81 Chai 30 g");
        INGREDIENTS.put("DA-010", "Nano Curcumin, Tinh chất hành tây đỏ Tuýp 20 g");
        INGREDIENTS.put("DA-011", "Chiết xuất nghệ, Rau má Tuýp 25 g");
        INGREDIENTS.put("DA-014", "Serum dưỡng trắng ngọc trai Nivea Chai 200 ml");
        INGREDIENTS.put("DOUONG-001", "Nước giải khát có ga Pepsi Cola Lon 320 ml");
        INGREDIENTS.put("DOUONG-002", "Pepsi không calo vị chanh Zero Lon 320 ml");
        INGREDIENTS.put("DOUONG-003", "Nước tăng lực hương dâu tây Chai 330 ml");
        INGREDIENTS.put("DOUONG-004", "Nước tăng lực hương nhân sâm Chai 330 ml");
        INGREDIENTS.put("DOUONG-005", "Taurine, Vitamin nhóm B Lon 250 ml");
        INGREDIENTS.put("DOUONG-008", "Sữa lúa mạch giàu Canxi Hộp 180 ml");
        INGREDIENTS.put("DOUONG-009", "Sữa tươi tiệt trùng nguyên chất Hộp 180 ml");
        INGREDIENTS.put("DOUONG-013", "Nước uống hương sữa chua thanh nhẹ Chai 455 ml");
        INGREDIENTS.put("DOUONG-014", "Trà Ô Long Tea Plus nguyên bản Chai 455 ml");
        INGREDIENTS.put("DOUONG-016", "Nước ngọt có ga vị chanh tự nhiên Lon 320 ml");
        INGREDIENTS.put("DOUONG-017", "Nước tinh khiết đóng chai Chai 500 ml");
        INGREDIENTS.put("OTC-001", "Paracetamol 325 mg");
        INGREDIENTS.put("OTC-002", "Paracetamol 650 mg");
        INGREDIENTS.put("OTC-003", "Paracetamol 650 mg + Cafein 65 mg");
        INGREDIENTS.put("OTC-004", "Paracetamol 500 mg (viên sủi bọt hòa tan)");
        INGREDIENTS.put("OTC-005", "Paracetamol 500 mg + Cafein 65 mg");
        INGREDIENTS.put("OTC-006", "Paracetamol 500 mg");
        INGREDIENTS.put("OTC-007", "Paracetamol 500 mg dạng viên sủi");
        INGREDIENTS.put("OTC-008", "Paracetamol 500 mg + Phenylephrine HCl 10 mg");
        INGREDIENTS.put("OTC-009", "Paracetamol 500 mg + Phenylephrine HCl 10 mg + Chlorpheniramine maleate 2 mg");
        INGREDIENTS.put("OTC-010", "Eucalyptol, Tinh dầu tràm, Tinh dầu gừng, Tinh dầu tần");
        INGREDIENTS.put("OTC-011", "Eucalyptol 100 mg + Tinh dầu tràm 50 mg + Menthol 0,5 mg + Tinh dầu gừng 0,75 mg + Tinh dầu tần 0,36 mg");
        INGREDIENTS.put("OTC-012", "Acetylcystein 200 mg");
        INGREDIENTS.put("OTC-013", "Paracetamol 650 mg");
        INGREDIENTS.put("OTC-014", "Paracetamol 325 mg + Ibuprofen 200 mg");
        INGREDIENTS.put("OTC-015", "Paracetamol 500 mg + Vitamin C");
        INGREDIENTS.put("OTC-016", "Ibuprofen 400 mg");
        INGREDIENTS.put("OTC-017", "Paracetamol 500 mg + Chlorpheniramine maleate 2 mg");
        INGREDIENTS.put("OTC-018", "Fexofenadine HCl 60 mg");
        INGREDIENTS.put("OTC-019", "Fexofenadine HCl 180 mg");
        INGREDIENTS.put("OTC-020", "Fexofenadine HCl 120 mg");
        INGREDIENTS.put("OTC-021", "Natri clorid 520 mg + Natri citrat 580 mg + Kali clorid 300 mg + Glucose khan 2,7 g / gói");
        INGREDIENTS.put("RX-001", "Empagliflozin 25 mg");
        INGREDIENTS.put("RX-002", "Atorvastatin 10 mg");
        INGREDIENTS.put("RX-003", "Bisoprolol 2,5 mg");
        INGREDIENTS.put("RX-004", "Bisoprolol 5 mg");
        INGREDIENTS.put("RX-005", "Perindopril arginine 5 mg (tương đương 3,395 mg perindopril)");
        INGREDIENTS.put("RX-006", "Trimetazidine 35 mg dạng giải phóng biến đổi");
        INGREDIENTS.put("RX-007", "Omeprazol 20 mg");
        INGREDIENTS.put("RX-008", "Amoxicillin 250 mg + Acid Clavulanic 31,25 mg / gói");
        INGREDIENTS.put("RX-009", "Amoxicillin 500 mg + Acid Clavulanic 125 mg");
        INGREDIENTS.put("RX-010", "Amoxicillin 500 mg + Acid Clavulanic 125 mg");
        INGREDIENTS.put("RX-013", "Azithromycin 250 mg");
        INGREDIENTS.put("RX-014", "Cefadroxil 500 mg");
        INGREDIENTS.put("SNACK-001", "Khoai tây tự nhiên chiên giòn Gói 56 g");
        INGREDIENTS.put("SNACK-003", "Khoai tây lượn sóng vị bít tết Gói 48 g");
        INGREDIENTS.put("SNACK-004", "Bắp ngọt sấy giòn Oishi Gói 40 g");
        INGREDIENTS.put("SNACK-005", "Snack khoai tây cay bùng nổ Gói 55 g");
        INGREDIENTS.put("SNACK-009", "Bắp nướng bơ tỏi giòn rụm Gói 60 g");
        INGREDIENTS.put("SNACK-011", "Bánh xốp phủ sô-cô-la Chocopie Hộp 6 cái x 33 g");
        INGREDIENTS.put("SNACK-012", "Bánh que nhúng socola hạnh nhân Hộp 32 g");
        INGREDIENTS.put("SNACK-014", "Kẹo cao su không đường hương bạc hà Hũ 58 g");
        INGREDIENTS.put("SNACK-017", "Kẹo ngậm hương bạc hà cầu vồng Thỏi 37.5 g");
        INGREDIENTS.put("TIEUDUNG-001", "Thép không gỉ 304 giữ nhiệt 12h Dung tích 550 ml");
        INGREDIENTS.put("TIEUDUNG-002", "Inox giữ nhiệt cao cấp 2 lớp Dung tích 450 ml");
        INGREDIENTS.put("TIEUDUNG-005", "Nước xả vải thơm dịu Comfort Túi 800 ml");
        INGREDIENTS.put("TIEUDUNG-007", "Cọ rửa bình sữa bình nước silicone Chiều dài 30 cm");
        INGREDIENTS.put("TIEUDUNG-008", "Dụng cụ hút mũi trẻ em đầu mềm Chất liệu silicone");
        INGREDIENTS.put("TPCN-001", "Cao lá bạch quả (Ginkgo Biloba) 120 mg");
        INGREDIENTS.put("TPCN-003", "Sụn vi cá mập (Shark Cartilage) 750 mg");
        INGREDIENTS.put("TPCN-005", "Magie, Canxi, Vitamin D3 Hộp 30 viên");
        INGREDIENTS.put("TPCN-006", "Dầu cá Omega-3 EPA DHA Hộp 30 viên");
        INGREDIENTS.put("TPCN-008", "Melatonin hỗ trợ giấc ngủ 3 mg");
        INGREDIENTS.put("TPCN-009", "Lutein, Berry, Vitamin A Hộp 60 viên");
        INGREDIENTS.put("TPCN-010", "Ginseng G115 + Vitamin tổng hợp Lọ 30 viên");
        INGREDIENTS.put("TPCN-011", "9 loại Vitamin và Khoáng chất Hộp 60 viên");
        INGREDIENTS.put("TPCN-012", "Cao Atiso Đà Lạt Hộp 20 túi lọc");
        INGREDIENTS.put("TPCN-017", "Sâm nhung bổ thận Kara Gold Hộp 30 viên");
        INGREDIENTS.put("TPCN-020", "Men vi sinh Lactomin Plus Hộp 30 gói");
        INGREDIENTS.put("TPCN-022", "Vitamin B1, B6, B12 Hộp 100 viên");
        INGREDIENTS.put("VTYT-001", "Vải không dệt y tế kháng khuẩn 4 lớp (Hộp 50 chiếc)");
        INGREDIENTS.put("VTYT-003", "Vải không dệt 4 lớp đạt chuẩn xuất khẩu (Hộp 50 chiếc)");
        INGREDIENTS.put("VTYT-006", "100% sợi bông tự nhiên nguyên chất mềm mịn (Bịch 100 miếng)");
        INGREDIENTS.put("VTYT-008", "Bông y tế sạch tự nhiên thấm hút tốt (Gói 50 g)");
        INGREDIENTS.put("VTYT-009", "Băng vệ sinh siêu mỏng có cánh, màng đáy thoáng khí (Gói 8 miếng)");
        INGREDIENTS.put("VTYT-010", "Băng vệ sinh mặt vi sợi, công nghệ Nano kháng khuẩn (Gói 8 miếng)");
        INGREDIENTS.put("VTYT-012", "Cảm biến hồng ngoại đo trán không tiếp xúc Model MC-720");
    }

    public static String getUsage(String productId, String fallback) {
        if (productId != null && USAGES.containsKey(productId)) {
            return USAGES.get(productId);
        }
        return (fallback != null && !fallback.trim().isEmpty()) ? fallback :
            "Sản phẩm chính hãng tại PharmaSIU. Đọc kỹ hướng dẫn sử dụng trước khi dùng.";
    }

    public static String getIngredients(String productId, String fallback) {
        if (productId != null && INGREDIENTS.containsKey(productId)) {
            return INGREDIENTS.get(productId);
        }
        return (fallback != null && !fallback.trim().isEmpty()) ? fallback :
            "Chi tiết thành phần được in đầy đủ trên bao bì sản phẩm.";
    }

    public static String getSafetyNotice(String productId, String category, boolean requiresPrescription) {
        if (requiresPrescription || (productId != null && productId.startsWith("RX-"))) {
            return "LƯU Ý VỀ THUỐC KÊ ĐƠN\n• Thuốc kê đơn chỉ sử dụng theo đúng chỉ định và liều dùng của bác sĩ.\n• Dược sĩ PharmaSIU sẽ kiểm tra đơn thuốc trước khi hoàn tất chuẩn bị thuốc.";
        }
        if (productId != null && (productId.startsWith("OTC-001") || productId.startsWith("OTC-002") ||
                productId.startsWith("OTC-003") || productId.startsWith("OTC-004") ||
                productId.startsWith("OTC-005") || productId.startsWith("OTC-006") ||
                productId.startsWith("OTC-007") || productId.startsWith("OTC-013") ||
                productId.startsWith("OTC-014") || productId.startsWith("OTC-015") ||
                productId.startsWith("OTC-017"))) {
            return "HƯỚNG DẪN AN TOÀN SỬ DỤNG\n• Không sử dụng chung với các thuốc khác có chứa Paracetamol để tránh quá liều.\n• Không dùng quá liều khuyến cáo. Tham khảo ý kiến dược sĩ nếu cơn đau kéo dài quá 5 ngày hoặc sốt quá 3 ngày.";
        }
        if (productId != null && (productId.startsWith("OTC-018") || productId.startsWith("OTC-019") || productId.startsWith("OTC-020"))) {
            return "HƯỚNG DẪN SỬ DỤNG KHÁNG HISTAMIN\n• Uống trước bữa ăn, không uống cùng nước hoa quả có vị chua (bưởi, cam, táo).\n• Đọc kỹ tờ hướng dẫn sử dụng đi kèm trong hộp thuốc.";
        }
        if ("Thực phẩm".equals(category) || (productId != null && productId.startsWith("TPCN-"))) {
            return "LƯU Ý QUAN TRỌNG\n• Thực phẩm bảo vệ sức khỏe: Sản phẩm này không phải là thuốc và không có tác dụng thay thế thuốc chữa bệnh.\n• Bảo quản nơi khô ráo thoáng mát, tránh ánh sáng trực tiếp.";
        }
        if (productId != null && productId.startsWith("DA-")) {
            return "LƯU Ý CHĂM SÓC DA\n• Ngưng sử dụng và hỏi ý kiến bác sĩ/dược sĩ nếu xuất hiện dấu hiệu dị ứng, mẩn đỏ.\n• Tránh để sản phẩm tiếp xúc trực tiếp vào mắt.";
        }
        return "LƯU Ý\n• Đọc kỹ hướng dẫn sử dụng trước khi dùng.\n• Để xa tầm tay trẻ em. Bảo quản nơi khô mát dưới 30°C.";
    }
}
