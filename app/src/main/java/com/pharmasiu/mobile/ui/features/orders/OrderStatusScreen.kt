package com.pharmasiu.mobile.ui.features.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pharmasiu.mobile.data.model.MockRequest
import com.pharmasiu.mobile.ui.components.PharmaTopBar
import com.pharmasiu.mobile.ui.components.formatVnd
import com.pharmasiu.mobile.ui.theme.PharmaPalette

/**
 * Successful CUSTOMER checkout in the offline demo.
 * Success applies to saving the local order only; it never represents a live
 * pharmacy acknowledgment, Rx approval, payment or stock reservation.
 */
@Composable
fun OrderStatusScreen(
    request: MockRequest,
    onViewOrder: () -> Unit,
    onHome: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        PharmaTopBar("Hoàn tất đặt hàng", onBack = onHome)
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(PharmaPalette.Emerald)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = "Đã lưu đơn hàng thành công",
                modifier = Modifier.size(72.dp),
                tint = PharmaPalette.Lime,
            )
            Text(
                "Đặt hàng thành công!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Đơn hàng demo của bạn đã được lưu thành công.",
                color = PharmaPalette.Mint,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                "Chế độ trải nghiệm • chưa gửi đến nhà thuốc",
                color = PharmaPalette.Lime,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            Surface(color = PharmaPalette.Paper, shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("MÃ ĐƠN HÀNG DEMO", color = PharmaPalette.Muted, style = MaterialTheme.typography.labelLarge)
                    Text(request.reference, color = PharmaPalette.Emerald, style = MaterialTheme.typography.headlineMedium)
                    HorizontalDivider(color = PharmaPalette.Line)
                    Text("Điểm nhận dự kiến", style = MaterialTheme.typography.bodySmall, color = PharmaPalette.Muted)
                    Text(request.pickupPoint, color = PharmaPalette.Ink, fontWeight = FontWeight.SemiBold)
                    Text("${request.lines.sumOf { it.quantity }} sản phẩm • Tạm tính ${formatVnd(request.illustrativeTotal)}",
                        style = MaterialTheme.typography.bodyMedium, color = PharmaPalette.Ink)
                }
            }
            Surface(color = PharmaPalette.Mint, shape = RoundedCornerShape(16.dp)) {
                Text(
                    "Đây là đơn mô phỏng trên thiết bị. Nhà thuốc chưa xác nhận tồn hàng, duyệt toa hoặc thời gian nhận. Chưa phát sinh thanh toán.",
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    color = PharmaPalette.Forest,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(
                onClick = onViewOrder,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Outlined.ReceiptLong, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(9.dp))
                Text("Xem đơn hàng của tôi", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onHome,
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Outlined.ShoppingBag, contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(9.dp))
                Text("Tiếp tục mua sắm")
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
