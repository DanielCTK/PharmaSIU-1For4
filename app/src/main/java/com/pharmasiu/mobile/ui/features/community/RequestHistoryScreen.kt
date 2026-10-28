package com.pharmasiu.mobile.ui.features.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pharmasiu.mobile.data.model.*
import com.pharmasiu.mobile.ui.components.*
import com.pharmasiu.mobile.ui.theme.PharmaPalette

@Composable
fun RequestHistoryScreen(
    requests: List<MockRequest>,
    onOpenRequest: (String) -> Unit,
    onExplore: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        PharmaTopBar("Yêu cầu của tôi")
        if (requests.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.ReceiptLong, null, modifier = Modifier.size(64.dp), tint = PharmaPalette.Emerald)
                Spacer(Modifier.height(14.dp))
                Text("Bạn chưa tạo yêu cầu mẫu", style = MaterialTheme.typography.titleLarge)
                Text("Sau khi hoàn tất checkout, bản xem trước sẽ xuất hiện tại đây. Không có đơn thật.", color = PharmaPalette.Muted)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onExplore) { Text("Khám phá sản phẩm") }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                item { DemoDisclosure() }
                items(requests.asReversed(), key = { it.reference }) { request ->
                    Surface(shape = RoundedCornerShape(24.dp), color = PharmaPalette.Paper) {
                        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(request.reference, fontWeight = FontWeight.Bold, color = PharmaPalette.Emerald)
                                StatusPill(if (request.status == MockRequestStatus.CANCELLED_LOCALLY) "Đã hủy tại máy" else "Lưu trên máy", isWarning = true)
                            }
                            Text("${request.lines.sumOf { it.quantity }} sản phẩm • ${request.displayTime}", color = PharmaPalette.Muted)
                            Text("Tạm tính: ${formatVnd(request.illustrativeTotal)}", style = MaterialTheme.typography.titleMedium)
                            Text("Nhà thuốc chưa tiếp nhận hoặc xác nhận yêu cầu.", color = PharmaPalette.Warning, style = MaterialTheme.typography.bodyMedium)
                            OutlinedButton(onClick = { onOpenRequest(request.reference) }) { Text("Xem chi tiết bản mẫu") }
                        }
                    }
                }
            }
        }
    }
}
