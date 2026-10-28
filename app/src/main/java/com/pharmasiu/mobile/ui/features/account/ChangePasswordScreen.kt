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
fun ChangePasswordScreen(
    onBack: () -> Unit,
    onChange: (String, String) -> String?,
) {
    var current by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize()) {
        PharmaTopBar("Đổi mật khẩu demo", onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Text("Không nhập mật khẩu thật. Mật khẩu demo chỉ tồn tại trong RAM của phiên này.", color = PharmaPalette.Muted)
            OutlinedTextField(current, { current = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Mật khẩu hiện tại") },
                visualTransformation = PasswordVisualTransformation())
            OutlinedTextField(next, { next = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Mật khẩu mới (tối thiểu 6 ký tự)") },
                visualTransformation = PasswordVisualTransformation())
            OutlinedTextField(repeat, { repeat = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Nhập lại mật khẩu mới") },
                visualTransformation = PasswordVisualTransformation())
            if (error != null) Text(error ?: "", color = PharmaPalette.Warning)
            Button(
                onClick = { error = if (next.length < 6 || next != repeat) "Mật khẩu mới chưa hợp lệ hoặc không khớp." else onChange(current, next) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) { Text("Cập nhật mật khẩu mẫu") }
        }
    }
}
