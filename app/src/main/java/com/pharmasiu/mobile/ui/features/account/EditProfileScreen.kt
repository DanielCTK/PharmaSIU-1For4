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
fun EditProfileScreen(profile: MockProfile, onSave: (MockProfile) -> String?, onBack: () -> Unit) {
    var name by rememberSaveable(profile.phone) { mutableStateOf(profile.displayName) }
    var phone by rememberSaveable(profile.phone) { mutableStateOf(profile.phone) }
    var email by rememberSaveable(profile.phone) { mutableStateOf(profile.email) }
    var birthday by rememberSaveable(profile.phone) { mutableStateOf(profile.birthday) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var discardDialog by remember { mutableStateOf(false) }
    val dirty = name != profile.displayName || phone != profile.phone || email != profile.email || birthday != profile.birthday
    BackHandler(enabled = dirty) { discardDialog = true }
    val valid = name.trim().length >= 2 && phone.length == 10 && phone.startsWith("0") && phone.all(Char::isDigit) &&
        (email.isBlank() || (email.contains("@") && email.substringAfter("@").contains(".")))
    Column(Modifier.fillMaxSize()) {
        PharmaTopBar("Chỉnh sửa hồ sơ demo", onBack = { if (dirty) discardDialog = true else onBack() })
        Column(Modifier.verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Text("Thông tin chỉ được cập nhật trong phiên chạy trên thiết bị này.", color = PharmaPalette.Muted)
            OutlinedTextField(name, { name = it.take(70) }, Modifier.fillMaxWidth(), label = { Text("Họ tên") }, singleLine = true)
            OutlinedTextField(phone, { phone = it.filter(Char::isDigit).take(10) }, Modifier.fillMaxWidth(),
                label = { Text("Số điện thoại") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            OutlinedTextField(email, { email = it.take(100) }, Modifier.fillMaxWidth(),
                label = { Text("Email (không bắt buộc)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            OutlinedTextField(birthday, { birthday = it.take(20) }, Modifier.fillMaxWidth(),
                label = { Text("Ngày sinh (chỉ là ghi chú mẫu)") }, singleLine = true)
            if (!valid) Text("Nhập tên, điện thoại hợp lệ và kiểm tra định dạng email.", color = PharmaPalette.Warning)
            if (saveError != null) Text(saveError ?: "", color = PharmaPalette.Warning)
            Button(onClick = { saveError = onSave(MockProfile(name.trim(), phone, email.trim(), birthday.trim())) },
                enabled = valid, modifier = Modifier.fillMaxWidth().height(55.dp)) { Text("Lưu hồ sơ mẫu") }
        }
    }
    if (discardDialog) AlertDialog(
        onDismissRequest = { discardDialog = false },
        title = { Text("Bỏ thay đổi hồ sơ?") },
        text = { Text("Các thay đổi chưa lưu sẽ bị bỏ. Hồ sơ mẫu hiện tại vẫn giữ nguyên.") },
        confirmButton = { TextButton(onClick = { discardDialog = false; onBack() }) { Text("Bỏ thay đổi") } },
        dismissButton = { TextButton(onClick = { discardDialog = false }) { Text("Tiếp tục chỉnh sửa") } },
    )
}
