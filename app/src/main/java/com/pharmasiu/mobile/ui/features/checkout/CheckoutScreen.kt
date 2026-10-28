package com.pharmasiu.mobile.ui.features.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.pharmasiu.mobile.data.model.CartLine
import com.pharmasiu.mobile.domain.CartRules
import com.pharmasiu.mobile.ui.components.DemoDisclosure
import com.pharmasiu.mobile.ui.components.PharmaTopBar
import com.pharmasiu.mobile.ui.components.formatVnd
import com.pharmasiu.mobile.ui.theme.PharmaPalette

@Composable
fun CheckoutScreen(
    lines: List<CartLine>,
    onBack: () -> Unit,
    onPlaceRequest: (String, String, String) -> Unit,
    initialName: String = "",
    initialPhone: String = "",
) {
    var fullName by rememberSaveable { mutableStateOf(initialName) }
    var phone by rememberSaveable { mutableStateOf(initialPhone) }
    var pickup by rememberSaveable { mutableStateOf("Điểm nhận mẫu A") }
    val validName = fullName.trim().length >= 2
    val validPhone = phone.length in 10..11 && phone.startsWith("0") && phone.all(Char::isDigit)
    val canSubmit = validName && validPhone && lines.isNotEmpty()

    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(17.dp)) {
        PharmaTopBar("Gửi yêu cầu đặt trước", onBack = onBack)
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Thông tin liên hệ", style = MaterialTheme.typography.headlineMedium, color = PharmaPalette.Emerald)
            Text("Thông tin chỉ tồn tại trong phiên minh họa này, chưa được gửi đến hệ thống nhà thuốc.", style = MaterialTheme.typography.bodyMedium, color = PharmaPalette.Muted)
            OutlinedTextField(
                value = fullName, onValueChange = { fullName = it.take(80) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("Họ và tên") }, shape = RoundedCornerShape(16.dp),
                isError = fullName.isNotBlank() && !validName,
            )
            OutlinedTextField(
                value = phone, onValueChange = { phone = it.filter(Char::isDigit).take(11) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("Số điện thoại") }, shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phone.isNotBlank() && !validPhone,
                supportingText = { Text("Ví dụ: 09xxxxxxxx") },
            )
            Surface(color = PharmaPalette.Paper, shape = RoundedCornerShape(21.dp)) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, tint = PharmaPalette.Emerald)
                    Column {
                        Text("Nhận hàng tại quầy", fontWeight = FontWeight.Bold)
                        Text("Thời gian đến nhận sẽ được thống nhất sau khi nhà thuốc xác nhận.", color = PharmaPalette.Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Text("Chọn điểm nhận minh họa", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Điểm nhận mẫu A", "Điểm nhận mẫu B").forEach { location ->
                    FilterChip(
                        selected = pickup == location,
                        onClick = { pickup = location },
                        label = { Text(location, style = MaterialTheme.typography.bodySmall) },
                    )
                }
            }
            Surface(color = PharmaPalette.Mint, shape = RoundedCornerShape(21.dp)) {
                Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Tóm tắt yêu cầu", style = MaterialTheme.typography.titleMedium)
                    lines.forEach { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.quantity} × ${item.medicine.name}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text(if (item.medicine.priceVnd > 0) formatVnd(item.medicine.priceVnd * item.quantity) else "Chờ báo giá", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Text("Tạm tính minh họa: ${formatVnd(CartRules.totalIllustrativeVnd(lines))}", color = PharmaPalette.Emerald, fontWeight = FontWeight.Bold)
                }
            }
            if (CartRules.requiresPrescription(lines)) {
                Text("Đơn có thuốc kê đơn. Dược sĩ phải duyệt toa; thao tác này không thay thế thẩm định chuyên môn.", color = PharmaPalette.Warning, style = MaterialTheme.typography.bodyMedium)
            }
            DemoDisclosure()
            Button(onClick = { onPlaceRequest(fullName.trim(), phone, pickup) }, enabled = canSubmit, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(17.dp)) {
                Text("Lưu yêu cầu mô phỏng", fontWeight = FontWeight.Bold)
            }
            Text("Chức năng gửi API/đặt hàng thực chưa được kết nối.", style = MaterialTheme.typography.bodyMedium, color = PharmaPalette.Muted)
            Spacer(Modifier.height(20.dp))
        }
    }
}
