package com.pharmasiu.mobile.ui.features.prescription

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
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pharmasiu.mobile.ui.components.DemoDisclosure
import com.pharmasiu.mobile.ui.components.PharmaTopBar
import com.pharmasiu.mobile.ui.theme.PharmaPalette

@Composable
fun PrescriptionScreen(
    onBack: () -> Unit,
    onChooseFile: () -> Unit,
    selectedFileName: String?,
    onContinue: () -> Unit,
    onRemoveFile: () -> Unit = {},
) {
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        PharmaTopBar("Thông tin toa thuốc", onBack = onBack)
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(Icons.Outlined.HealthAndSafety, null, tint = PharmaPalette.Emerald, modifier = Modifier.size(48.dp))
            Text("Dược sĩ cần kiểm tra toa trước khi xác nhận.", style = MaterialTheme.typography.headlineMedium, color = PharmaPalette.Emerald)
            Text("Hãy chọn ảnh hoặc PDF toa rõ nét. Việc chọn tệp không đồng nghĩa toa đã được chấp thuận hoặc sản phẩm đã được giữ.", color = PharmaPalette.Muted, style = MaterialTheme.typography.bodyLarge)
            Column(
                Modifier.fillMaxWidth().background(PharmaPalette.Mint, RoundedCornerShape(26.dp)).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Outlined.CloudUpload, null, tint = PharmaPalette.Emerald, modifier = Modifier.size(54.dp))
                Text(if (selectedFileName == null) "Chưa có tệp toa được chọn" else "Đã chọn tệp trong phiên này", style = MaterialTheme.typography.titleMedium)
                Text("Tệp cần rõ thông tin, không bị mờ hoặc thiếu trang.", style = MaterialTheme.typography.bodyMedium, color = PharmaPalette.Muted)
                OutlinedButton(onClick = onChooseFile) { Text(if (selectedFileName == null) "Chọn ảnh/PDF toa" else "Chọn tệp khác") }
                if (selectedFileName != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.CheckCircleOutline, null, tint = PharmaPalette.Emerald)
                        Text(selectedFileName, color = PharmaPalette.Emerald, style = MaterialTheme.typography.bodyMedium)
                    }
                    TextButton(onClick = onRemoveFile) { Text("Bỏ tệp đã chọn") }
                }
            }
            DemoDisclosure()
            Text("Quy trình dự kiến: gửi yêu cầu → dược sĩ xem xét toa → kiểm tra tồn thực tế → xác nhận giữ hàng → chuẩn bị → thông báo đến nhận.", style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = onContinue,
                enabled = selectedFileName != null,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(17.dp),
            ) { Text("Tiếp tục", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(20.dp))
        }
    }
}
