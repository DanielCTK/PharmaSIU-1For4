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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.pharmasiu.mobile.R
import com.pharmasiu.mobile.data.model.CartLine
import com.pharmasiu.mobile.domain.CartRules
import com.pharmasiu.mobile.domain.CheckoutInputRules
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
    val validName = CheckoutInputRules.isValidName(fullName)
    val validPhone = CheckoutInputRules.isValidPhone(phone)
    val canSubmit = CheckoutInputRules.canSubmit(fullName, phone, lines.isNotEmpty())
    val phoneFocus = androidx.compose.runtime.remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(17.dp)) {
        PharmaTopBar(stringResource(R.string.checkout_title), onBack = onBack)
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Thông tin liên hệ", style = MaterialTheme.typography.headlineMedium, color = PharmaPalette.Emerald)
            Text("Bạn đang trải nghiệm đặt hàng demo. Đơn hoàn tất sẽ được lưu trong phiên ứng dụng.", style = MaterialTheme.typography.bodyMedium, color = PharmaPalette.Muted)
            OutlinedTextField(
                value = fullName, onValueChange = { fullName = it.take(80) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text(stringResource(R.string.checkout_customer_name)) }, shape = RoundedCornerShape(16.dp),
                isError = fullName.isNotBlank() && !validName,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(onNext = { phoneFocus.requestFocus() }),
            )
            OutlinedTextField(
                value = phone, onValueChange = { phone = CheckoutInputRules.normalizePhoneInput(it) },
                modifier = Modifier.fillMaxWidth().focusRequester(phoneFocus), singleLine = true,
                label = { Text(stringResource(R.string.checkout_contact_phone)) }, shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboard?.hide()
                    focusManager.clearFocus()
                }),
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
                    Text("Tóm tắt đơn hàng", style = MaterialTheme.typography.titleMedium)
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
            Button(onClick = {
                keyboard?.hide()
                focusManager.clearFocus()
                onPlaceRequest(CheckoutInputRules.normalizeName(fullName), phone, pickup)
            }, enabled = canSubmit, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(17.dp)) {
                Text(stringResource(R.string.checkout_place_order), fontWeight = FontWeight.Bold)
            }
            Text("Đặt hàng thành công trong bản demo; chưa gửi đến nhà thuốc và chưa thanh toán.", style = MaterialTheme.typography.bodyMedium, color = PharmaPalette.Muted)
            Spacer(Modifier.height(20.dp))
        }
    }
}
