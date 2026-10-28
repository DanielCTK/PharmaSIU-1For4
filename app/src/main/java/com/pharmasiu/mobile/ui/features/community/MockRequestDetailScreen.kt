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
fun MockRequestDetailScreen(
    request: MockRequest,
    onBack: () -> Unit,
    onReorder: (List<CartLine>) -> Unit,
    onCancel: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        PharmaTopBar("Chi tiết đơn hàng", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Surface(color = PharmaPalette.Mint, shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(19.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ĐƠN HÀNG DEMO • LƯU TRÊN THIẾT BỊ", color = PharmaPalette.Emerald, fontWeight = FontWeight.Bold)
                    Text(request.reference, style = MaterialTheme.typography.headlineMedium, color = PharmaPalette.Emerald)
                    Text(if (request.status == MockRequestStatus.CANCELLED_LOCALLY) "Đã hủy đơn demo" else "Đặt hàng thành công!", fontWeight = FontWeight.SemiBold)
                    Text("Chưa gửi nhà thuốc, chưa giữ hàng, chưa thanh toán hoặc duyệt toa.", color = PharmaPalette.Muted)
                }
            }
            Text("Sản phẩm đã đặt (demo)", style = MaterialTheme.typography.titleLarge)
            request.lines.forEach { line ->
                Surface(shape = RoundedCornerShape(17.dp), color = PharmaPalette.Paper) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MedicineMedia(line.medicine, Modifier.size(65.dp))
                        Column(Modifier.weight(1f)) {
                            Text(line.medicine.name, fontWeight = FontWeight.SemiBold)
                            Text("Số lượng: ${line.quantity}", color = PharmaPalette.Muted)
                            if (line.medicine.requiresPrescription) Text("Cần dược sĩ kiểm tra toa", color = PharmaPalette.Warning)
                        }
                    }
                }
            }
            Text("Thông tin nhận tại quầy", style = MaterialTheme.typography.titleLarge)
            Text("${request.customerName} • ${request.contactPhone}", style = MaterialTheme.typography.bodyLarge)
            Text(request.pickupPoint, style = MaterialTheme.typography.bodyLarge)
            Text("Tạm tính minh họa: ${formatVnd(request.illustrativeTotal)}", fontWeight = FontWeight.Bold, color = PharmaPalette.Emerald)
            if (request.hasPrescription) Text("Ảnh toa chỉ được chọn trong phiên trước; toa chưa được tải lên hoặc phê duyệt.", color = PharmaPalette.Warning)
            Button(onClick = { onReorder(request.lines) }, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("Đặt lại sản phẩm") }
            if (request.status == MockRequestStatus.PLACED_DEMO) {
                OutlinedButton(onClick = { onCancel(request.reference) }, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                    Text("Hủy đơn demo")
                }
            }
            Spacer(Modifier.height(26.dp))
        }
    }
}
