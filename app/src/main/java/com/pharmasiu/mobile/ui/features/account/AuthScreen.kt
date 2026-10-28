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

private val fieldShape = RoundedCornerShape(18.dp)

@Composable
fun AuthScreen(
    onBack: () -> Unit,
    onLogin: (String, String) -> String?,
    onRegister: (String, String, String) -> String?,
    onReset: (String, String) -> String?,
) {
    var mode by rememberSaveable { mutableStateOf("login") }
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordAgain by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    fun switch(next: String) { mode = next; error = null; password = ""; passwordAgain = "" }
    val phoneValid = phone.length == 10 && phone.startsWith("0") && phone.all { it.isDigit() }
    Column(Modifier.fillMaxSize()) {
        PharmaTopBar("Tài khoản PharmaSIU", onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            Spacer(Modifier.height(10.dp))
            Text(
                when (mode) {
                    "register" -> "Tạo tài khoản demo"
                    "reset" -> "Đặt lại mật khẩu demo"
                    else -> "Chào mừng trở lại."
                },
                color = PharmaPalette.Emerald,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text("Tất cả thao tác xác thực chỉ diễn ra trong bộ nhớ ứng dụng. Không dùng mật khẩu thật.", color = PharmaPalette.Muted)
            if (mode != "reset") {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(selected = mode == "login", onClick = { switch("login") }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text("Đăng nhập") }
                    SegmentedButton(selected = mode == "register", onClick = { switch("register") }, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text("Đăng ký") }
                }
            }
            if (mode == "register") {
                OutlinedTextField(name, { name = it.take(70); error = null }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Họ và tên") }, singleLine = true, shape = fieldShape)
            }
            OutlinedTextField(phone, { phone = it.filter(Char::isDigit).take(10); error = null },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("Số điện thoại demo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phone.isNotEmpty() && !phoneValid, shape = fieldShape)
            OutlinedTextField(password, { password = it.take(60); error = null },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text(if (mode == "reset") "Mật khẩu mới" else "Mật khẩu demo") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, "Ẩn hoặc hiện mật khẩu")
                } }, shape = fieldShape)
            if (mode != "login") {
                OutlinedTextField(passwordAgain, { passwordAgain = it.take(60); error = null }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nhập lại mật khẩu") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(), shape = fieldShape)
            }
            if (error != null) {
                Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = {
                    error = when {
                        !phoneValid -> "Số điện thoại cần đủ 10 chữ số và bắt đầu bằng 0."
                        password.length < 6 -> "Mật khẩu demo cần ít nhất 6 ký tự."
                        mode == "register" && name.trim().length < 2 -> "Vui lòng nhập họ tên."
                        mode != "login" && passwordAgain != password -> "Mật khẩu nhập lại chưa trùng khớp."
                        mode == "register" -> onRegister(name.trim(), phone, password)
                        mode == "reset" -> onReset(phone, password)
                        else -> onLogin(phone, password)
                    }
                    if (mode == "reset" && error == null) switch("login")
                },
                modifier = Modifier.fillMaxWidth().height(55.dp), shape = fieldShape,
            ) {
                Text(when (mode) { "register" -> "Tạo tài khoản mẫu"; "reset" -> "Đặt lại trên thiết bị"; else -> "Đăng nhập mô phỏng" })
            }
            if (mode == "login") {
                TextButton(onClick = { switch("reset") }) { Text("Quên mật khẩu mẫu?") }
                Surface(shape = RoundedCornerShape(18.dp), color = PharmaPalette.Mint) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text("Tài khoản thử ngay", fontWeight = FontWeight.Bold, color = PharmaPalette.Emerald)
                        Text("Điện thoại: 0900000000 • Mật khẩu: demo1234")
                        OutlinedButton(onClick = { phone = "0900000000"; password = "demo1234"; error = null }) {
                            Text("Điền tài khoản mẫu")
                        }
                    }
                }
            }
            if (mode == "reset") {
                TextButton(onClick = { switch("login") }) { Text("Quay lại đăng nhập") }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}
