package com.pharmasiu.mobile.ui.features.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharmasiu.mobile.data.model.*
import com.pharmasiu.mobile.ui.features.home.FeatureButton
import com.pharmasiu.mobile.ui.features.home.FeatureColors
import com.pharmasiu.mobile.ui.features.home.FeatureHeader
import com.pharmasiu.mobile.ui.features.home.FeatureTag
import com.pharmasiu.mobile.ui.features.home.MedicineArt
import com.pharmasiu.mobile.ui.features.home.asFeatureView
import com.pharmasiu.mobile.ui.features.home.toVnd

@Composable
fun ProductDetailScreen(
    medicine: Medicine,
    onAddToCart: (Medicine) -> Unit,
    onUploadPrescription: () -> Unit,
    onBack: () -> Unit
) {
    val product = medicine.asFeatureView()
    Column(Modifier.fillMaxSize().background(FeatureColors.canvas)) {
        FeatureHeader("Chi tiết sản phẩm", onBack)
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(276.dp)) {
                MedicineArt(Modifier.fillMaxSize(), product.id)
                Box(
                    Modifier.align(Alignment.TopStart).padding(17.dp).clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = .95f)).padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(if (product.requiresPrescription) "THUỐC KÊ ĐƠN · Rx" else "THÔNG TIN SẢN PHẨM", color = FeatureColors.emerald, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = .6.sp)
                }
            }
            Spacer(Modifier.height(22.dp))
            Column(Modifier.padding(horizontal = 22.dp)) {
                Text(product.category.uppercase(), color = FeatureColors.emerald, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Spacer(Modifier.height(7.dp))
                Text(product.name, color = FeatureColors.deep, fontSize = 27.sp, lineHeight = 33.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(product.subtitle, color = FeatureColors.muted, fontSize = 13.sp)
                Spacer(Modifier.height(17.dp))
                Text(product.price?.toVnd() ?: "Giá được xác nhận khi đặt", color = FeatureColors.emerald, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                if (product.price != null) {
                    Spacer(Modifier.height(3.dp))
                    Text("Giá tham khảo / ${product.unit}; nhà thuốc xác nhận khi xử lý yêu cầu", color = FeatureColors.muted, fontSize = 11.sp, lineHeight = 16.sp)
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeatureTag("Kiểm tra khi đặt trước")
                }
                if (product.requiresPrescription) {
                    Spacer(Modifier.height(9.dp))
                    FeatureTag("Cần dược sĩ duyệt toa", prescription = true)
                }
                Spacer(Modifier.height(23.dp))
                HorizontalDivider(color = FeatureColors.line)
                Spacer(Modifier.height(22.dp))
                Text("Thông tin sản phẩm", color = FeatureColors.deep, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(9.dp))
                Text(product.description, color = FeatureColors.muted, fontSize = 13.sp, lineHeight = 21.sp)
                Spacer(Modifier.height(17.dp))
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                        .background(Color.White).border(1.dp, FeatureColors.line, RoundedCornerShape(20.dp))
                        .padding(17.dp)
                ) {
                    Text("THÔNG TIN THAM KHẢO", color = FeatureColors.muted, fontSize = 10.sp, letterSpacing = 1.1.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    DetailRow("Thành phần", product.ingredient)
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = FeatureColors.line)
                    DetailRow("Quy cách", product.subtitle)
                }
                Spacer(Modifier.height(18.dp))
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                        .background(if (product.requiresPrescription) FeatureColors.rxSurface else FeatureColors.mint)
                        .padding(18.dp)
                ) {
                    Text(if (product.requiresPrescription) "LƯU Ý VỀ TOA THUỐC" else "QUY TRÌNH ĐẶT TRƯỚC", color = if (product.requiresPrescription) FeatureColors.rx else FeatureColors.emerald, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(9.dp))
                    Text(
                        if (product.requiresPrescription) "Gửi toa để dược sĩ kiểm tra và phê duyệt trước khi nhà thuốc xác nhận yêu cầu."
                        else "Nhà thuốc sẽ kiểm tra giá, tồn kho và khả năng đáp ứng trước khi xác nhận đặt trước.",
                        color = FeatureColors.text,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
                Spacer(Modifier.height(26.dp))
            }
        }
        Column(
            Modifier.fillMaxWidth().background(Color.White)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            FeatureButton(
                text = if (product.requiresPrescription) "Tải toa để được duyệt  ↗" else "Thêm vào giỏ  +",
                onClick = { if (product.requiresPrescription) onUploadPrescription() else onAddToCart(medicine) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Yêu cầu chưa được xác nhận và chưa giữ hàng.",
                modifier = Modifier.fillMaxWidth(),
                color = FeatureColors.muted,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(label, modifier = Modifier.width(101.dp), color = FeatureColors.muted, fontSize = 12.sp)
        Text(value, modifier = Modifier.weight(1f), color = FeatureColors.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, lineHeight = 17.sp)
    }
}
