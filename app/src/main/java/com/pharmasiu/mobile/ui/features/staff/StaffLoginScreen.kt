package com.pharmasiu.mobile.ui.features.staff

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharmasiu.mobile.domain.StaffDemoAccess
import com.pharmasiu.mobile.domain.StaffRole
import com.pharmasiu.mobile.ui.theme.PharmaPalette

/**
 * Professional-looking mock sign-in, not a security boundary.
 * No passwords are persisted or sent to a server.
 */
@Composable
fun StaffLoginScreen(onExit: () -> Unit, onAuthenticated: (StaffRole) -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showDemoAccounts by rememberSaveable { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val passwordFocus = remember { FocusRequester() }

    fun submit() {
        attempted = true
        val result = StaffDemoAccess.authenticate(email, password)
        if (result == null) {
            error = "Email hoặc mật khẩu demo chưa đúng. Hãy kiểm tra lại thông tin."
            return
        }
        keyboard?.hide()
        focusManager.clearFocus()
        password = ""
        error = null
        onAuthenticated(result)
    }

    Column(
        Modifier.fillMaxSize().background(PharmaPalette.Cream)
            .imePadding().verticalScroll(rememberScrollState()),
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 62.dp)
                .padding(start = 12.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            IconButton(onClick = onExit, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Trở về tài khoản",
                    tint = PharmaPalette.Emerald)
            }
            Column(Modifier.weight(1f)) {
                Text("PharmaSIU", style = MaterialTheme.typography.titleLarge,
                    color = PharmaPalette.Emerald, fontWeight = FontWeight.ExtraBold)
                Text("KHU VỰC VẬN HÀNH", color = PharmaPalette.Muted,
                    fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.35.sp)
            }
            Surface(color = PharmaPalette.Mint, shape = RoundedCornerShape(8.dp)) {
                Text("BẢN DEMO", color = PharmaPalette.Emerald,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth().height(229.dp)
                .clipToBounds()
                .background(PharmaPalette.Emerald),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width * 1.04f, size.height * 0.40f)
                drawCircle(PharmaPalette.Lime.copy(alpha = .13f),
                    radius = size.width * .31f, center = center)
                drawCircle(PharmaPalette.Mint.copy(alpha = .27f),
                    radius = size.width * .40f, center = center,
                    style = Stroke(1.2.dp.toPx()))
                drawCircle(PharmaPalette.Mint.copy(alpha = .15f),
                    radius = size.width * .27f, center = center,
                    style = Stroke(1.dp.toPx()))
                drawCircle(PharmaPalette.Lime, radius = 6.dp.toPx(),
                    center = Offset(size.width * .79f, size.height * .76f))
            }
            Column(
                Modifier.fillMaxSize().padding(horizontal = 23.dp, vertical = 21.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Storefront, contentDescription = null,
                        tint = PharmaPalette.Lime, modifier = Modifier.size(25.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("NHÂN SỰ  /  PHARMASIU", color = PharmaPalette.Lime,
                        letterSpacing = 1.4.sp, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text("Chào mừng\ntrở lại.", color = Color.White,
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp),
                    lineHeight = 38.sp, fontWeight = FontWeight.ExtraBold)
                Text("Tiếp tục công việc tại quầy trong một không gian thống nhất.",
                    color = PharmaPalette.Mint, style = MaterialTheme.typography.bodySmall)
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Text("Đăng nhập nội bộ", style = MaterialTheme.typography.titleLarge,
                color = PharmaPalette.Ink, fontWeight = FontWeight.Bold)
            Text("Nhập tài khoản được cấp cho nhân viên hoặc quản lý.",
                style = MaterialTheme.typography.bodyMedium, color = PharmaPalette.Muted)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.take(120); error = null; attempted = false },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email nội bộ") },
                placeholder = { Text("ten@pharmasiu.demo") },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                isError = attempted && email.isBlank(),
                shape = RoundedCornerShape(14.dp),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it.take(64); error = null; attempted = false },
                modifier = Modifier.fillMaxWidth().focusRequester(passwordFocus),
                label = { Text("Mật khẩu") },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword },
                        modifier = Modifier.size(48.dp)) {
                        Icon(
                            if (showPassword) Icons.Outlined.VisibilityOff
                            else Icons.Outlined.Visibility,
                            contentDescription = if (showPassword) "Ẩn mật khẩu" else "Hiện mật khẩu",
                        )
                    }
                },
                visualTransformation =
                    if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (email.isNotBlank() && password.isNotBlank()) submit()
                    else { attempted = true; error = "Hãy nhập email và mật khẩu." }
                }),
                singleLine = true,
                isError = attempted && (password.isBlank() || error != null),
                shape = RoundedCornerShape(14.dp),
            )
            if (error != null) {
                Surface(color = PharmaPalette.WarningSurface, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Info, contentDescription = null,
                            tint = PharmaPalette.Warning, modifier = Modifier.size(20.dp))
                        Text(error!!, color = PharmaPalette.Warning,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f))
                    }
                }
            }
            Button(
                enabled = email.isNotBlank() && password.isNotBlank(),
                onClick = ::submit,
                modifier = Modifier.fillMaxWidth().heightIn(min = 55.dp),
                shape = RoundedCornerShape(13.dp),
            ) {
                Text("Đăng nhập", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.ArrowForward, contentDescription = null)
            }
            Surface(color = PharmaPalette.Paper, shape = RoundedCornerShape(14.dp)) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().clickable { showDemoAccounts = !showDemoAccounts }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = null,
                            tint = PharmaPalette.Emerald)
                        Spacer(Modifier.width(10.dp))
                        Text("Tài khoản dùng thử", modifier = Modifier.weight(1f),
                            color = PharmaPalette.Emerald, fontWeight = FontWeight.SemiBold)
                        Icon(
                            if (showDemoAccounts) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = if (showDemoAccounts) "Thu gọn" else "Xem tài khoản demo",
                            tint = PharmaPalette.Emerald,
                        )
                    }
                    if (showDemoAccounts) {
                        HorizontalDivider(color = PharmaPalette.Line)
                        StaffDemoAccess.demoAccounts.forEach { account ->
                            Surface(
                                onClick = {
                                    email = account.email
                                    password = account.password
                                    error = null
                                    attempted = false
                                    keyboard?.hide()
                                    focusManager.clearFocus()
                                },
                                color = PharmaPalette.Paper,
                            ) {
                                Row(
                                    Modifier.fillMaxWidth()
                                        .padding(horizontal = 15.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        if (account.role == StaffRole.ADMIN)
                                            Icons.Outlined.AdminPanelSettings
                                        else Icons.Outlined.PointOfSale,
                                        contentDescription = null, tint = PharmaPalette.Emerald,
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(account.displayName, fontWeight = FontWeight.SemiBold,
                                            color = PharmaPalette.Ink)
                                        Text(account.email, color = PharmaPalette.Muted,
                                            fontSize = 12.sp)
                                    }
                                    Text("Dùng thử", color = PharmaPalette.Emerald,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Icon(Icons.Outlined.Info, contentDescription = null,
                    tint = PharmaPalette.Muted, modifier = Modifier.size(17.dp))
                Text(
                    "Đăng nhập minh họa trên thiết bị. Tài khoản và mật khẩu demo công khai; " +
                        "chưa có xác thực máy chủ hoặc bảo mật tài khoản thực.",
                    style = MaterialTheme.typography.bodySmall, color = PharmaPalette.Muted,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
