package com.pharmasiu.mobile.ui.features.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pharmasiu.mobile.ui.components.PharmaTopBar
import com.pharmasiu.mobile.ui.theme.PharmaPalette

@Composable
fun OrderStatusScreen(reference: String, onHome: () -> Unit) {
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        PharmaTopBar("Bản xem trước", onBack = onHome)
        Column(Modifier.padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Icon(Icons.Outlined.Info, null, tint = PharmaPalette.Emerald, modifier = Modifier.size(58.dp))
            Text("Đã tạo bản xem trước", style = MaterialTheme.typography.headlineMedium, color = PharmaPalette.Emerald)
            Text("Chưa gửi yêu cầu đến nhà thuốc. Chưa có thuốc nào được giữ hoặc toa nào được duyệt.", style = MaterialTheme.typography.bodyLarge, color = PharmaPalette.Ink)
            Column(
                Modifier.fillMaxWidth().background(PharmaPalette.Mint, RoundedCornerShape(22.dp)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text("Mã tham chiếu trên thiết bị", style = MaterialTheme.typography.bodyMedium, color = PharmaPalette.Muted)
                Text(reference, style = MaterialTheme.typography.headlineMedium, color = PharmaPalette.Emerald)
                Text("Mã DEMO không thể dùng để nhận hàng tại quầy.", style = MaterialTheme.typography.bodyMedium, color = PharmaPalette.Warning)
            }
            Text("Luồng xử lý khi kết nối hệ thống thật", style = MaterialTheme.typography.titleLarge)
            listOf(
                "Nhà thuốc tiếp nhận yêu cầu",
                "Dược sĩ xem xét toa (nếu có)",
                "Kiểm tra và xác nhận tồn thực tế",
                "Giữ hàng trong thời hạn được xác nhận",
                "Soạn hàng và thông báo sẵn sàng",
                "Khách đến quầy xác minh và thanh toán",
            ).forEachIndexed { index, label ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, null, tint = PharmaPalette.Muted, modifier = Modifier.size(20.dp))
                    Text("${index + 1}. $label", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Button(onClick = onHome, modifier = Modifier.fillMaxWidth().height(55.dp), shape = RoundedCornerShape(17.dp)) {
                Text("Xem các yêu cầu mẫu", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
