package com.pharmasiu.mobile.ui.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pharmasiu.mobile.ui.theme.PharmaPalette

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf("Chăm sóc mỗi ngày.", "Khám phá theo nhu cầu.", "Đặt trước • nhận tại quầy.")
    val details = listOf(
        "Khám phá PharmaSIU với giao diện Emerald hiện đại, thân thiện và rõ ràng.",
        "Sản phẩm chăm sóc sức khỏe, mỹ phẩm, thực phẩm và thiết bị — phân loại minh bạch.",
        "Tạo yêu cầu mô phỏng và quản lý bản xem trước. Chưa gửi đến nhà thuốc, chưa giữ hàng.",
    )
    val symbols = listOf(Icons.Outlined.HealthAndSafety, Icons.Outlined.Spa, Icons.Outlined.Storefront)
    Column(Modifier.fillMaxSize().background(PharmaPalette.Cream).padding(24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onFinish) { Text("Bỏ qua") }
        }
        Spacer(Modifier.weight(0.5f))
        Box(
            Modifier.fillMaxWidth().height(258.dp)
                .background(PharmaPalette.Emerald, RoundedCornerShape(40.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(symbols[page], null, tint = PharmaPalette.Lime, modifier = Modifier.size(118.dp))
        }
        Spacer(Modifier.height(32.dp))
        Text(titles[page], style = MaterialTheme.typography.headlineLarge, color = PharmaPalette.Emerald)
        Spacer(Modifier.height(12.dp))
        Text(details[page], style = MaterialTheme.typography.bodyLarge, color = PharmaPalette.Muted)
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { index ->
                Box(Modifier.size(if (index == page) 24.dp else 8.dp, 8.dp)
                    .background(if (index == page) PharmaPalette.Emerald else PharmaPalette.Line, CircleShape))
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = { if (page < 2) page += 1 else onFinish() }, modifier = Modifier.height(54.dp)) {
                Text(if (page == 2) "Khám phá ngay" else "Tiếp tục", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(22.dp))
        Text("Tất cả dữ liệu giao dịch trong phiên hiện tại là bản mô phỏng.", color = PharmaPalette.Muted,
            style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}
