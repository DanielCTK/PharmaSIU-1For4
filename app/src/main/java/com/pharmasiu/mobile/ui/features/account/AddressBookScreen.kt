package com.pharmasiu.mobile.ui.features.account

import androidx.activity.compose.BackHandler

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.pharmasiu.mobile.data.model.MockProfile
import com.pharmasiu.mobile.data.model.MockAddress
import com.pharmasiu.mobile.ui.components.PharmaTopBar
import com.pharmasiu.mobile.ui.theme.PharmaPalette

@Composable
fun AddressBookScreen(
    addresses: List<MockAddress>,
    onBack: () -> Unit,
    onSave: (MockAddress) -> Unit,
    onRemove: (String) -> Unit,
    onSetDefault: (String) -> Unit,
) {
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var label by rememberSaveable { mutableStateOf("Nhà") }
    var receiver by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var detail by rememberSaveable { mutableStateOf("") }
    val valid = receiver.trim().length >= 2 && phone.length == 10 && phone.startsWith("0") &&
        phone.all(Char::isDigit) && detail.trim().length >= 8
    fun clearForm() {
        editing = null; creating = false; label = "Nhà"; receiver = ""; phone = ""; detail = ""
    }
    Column(Modifier.fillMaxSize()) {
        PharmaTopBar(if (creating) "Sửa địa chỉ mẫu" else "Địa chỉ liên hệ", onBack = { if (creating) clearForm() else onBack() })
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Text("Địa chỉ chỉ dùng để thử quản lý hồ sơ. Đơn Click & Collect luôn nhận tại điểm nhận được chọn riêng.", color = PharmaPalette.Muted)
            if (creating) {
                OutlinedTextField(label, { label = it.take(24) }, Modifier.fillMaxWidth(), label = { Text("Nhãn địa chỉ (Nhà / Công ty)") })
                OutlinedTextField(receiver, { receiver = it.take(70) }, Modifier.fillMaxWidth(), label = { Text("Người liên hệ") })
                OutlinedTextField(phone, { phone = it.filter(Char::isDigit).take(10) }, Modifier.fillMaxWidth(), label = { Text("Số điện thoại") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(detail, { detail = it.take(190) }, Modifier.fillMaxWidth(), minLines = 3, label = { Text("Địa chỉ chi tiết, phường/xã, tỉnh/thành") })
                if (!valid) Text("Nhập đủ người liên hệ, điện thoại và địa chỉ chi tiết (tối thiểu 8 ký tự).", color = PharmaPalette.Warning)
                Button(
                    enabled = valid,
                    onClick = {
                        onSave(MockAddress(editing ?: "addr-" + System.nanoTime(), label.trim(), receiver.trim(), phone, detail.trim(), addresses.isEmpty()))
                        clearForm()
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                ) { Text("Lưu địa chỉ demo") }
            } else {
                if (addresses.isEmpty()) {
                    Text("Chưa có địa chỉ liên hệ nào trong phiên demo.", style = MaterialTheme.typography.titleMedium)
                }
                addresses.forEach { item ->
                    Surface(shape = RoundedCornerShape(22.dp), color = PharmaPalette.Paper) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                if (item.isDefault) Text("Mặc định", color = PharmaPalette.Emerald, fontWeight = FontWeight.Bold)
                            }
                            Text("${item.receiver} • ${item.phone}")
                            Text(item.detail, color = PharmaPalette.Muted)
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                TextButton(onClick = {
                                    editing = item.id; creating = true; label = item.label; receiver = item.receiver
                                    phone = item.phone; detail = item.detail
                                }) { Text("Sửa") }
                                if (!item.isDefault) TextButton(onClick = { onSetDefault(item.id) }) { Text("Mặc định") }
                                TextButton(onClick = { onRemove(item.id) }) { Text("Xóa") }
                            }
                        }
                    }
                }
                Button(onClick = { creating = true }, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("Thêm địa chỉ mới") }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}
