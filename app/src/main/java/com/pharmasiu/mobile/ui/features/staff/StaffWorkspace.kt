package com.pharmasiu.mobile.ui.features.staff

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.domain.StaffDemoAccess
import com.pharmasiu.mobile.domain.StaffRole
import com.pharmasiu.mobile.ui.components.PharmaTopBar
import com.pharmasiu.mobile.ui.theme.PharmaPalette

/** One employee entrance, one shared operations UI, with role-scoped demo actions. */
@Composable
fun StaffWorkspace(
    products: List<Medicine>,
    store: StaffDemoStore,
    onExit: () -> Unit,
) {
    var role by remember { mutableStateOf<StaffRole?>(null) }
    BackHandler(enabled = role != null) { role = null }
    when (role) {
        null -> StaffDemoSignIn(onExit = onExit, onAuthenticated = { role = it })
        else -> OperationsHub(role!!, products, store, onLogout = { role = null })
    }
}

@Composable
private fun StaffDemoSignIn(onExit: () -> Unit, onAuthenticated: (StaffRole) -> Unit) {
    var selected by rememberSaveable { mutableStateOf(StaffRole.CASHIER) }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(PharmaPalette.Cream)) {
        PharmaTopBar("Vận hành nhà thuốc", onBack = onExit)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Surface(color = PharmaPalette.Emerald, shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 23.dp, vertical = 25.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Icon(Icons.Outlined.Storefront, null, tint = PharmaPalette.Lime, modifier = Modifier.size(40.dp))
                    Text("Một không gian.\nMọi ca làm.", color = Color.White,
                        style = MaterialTheme.typography.headlineLarge, lineHeight = 37.sp)
                    Text("Bán hàng và quản trị cơ bản trong một khu vực thống nhất.",
                        color = PharmaPalette.Mint, style = MaterialTheme.typography.bodyMedium)
                    Surface(color = PharmaPalette.Lime, shape = RoundedCornerShape(50)) {
                        Text("KHÔNG GIAN VẬN HÀNH DEMO", fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, color = PharmaPalette.Forest,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                }
            }
            Column(Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(17.dp)) {
                Text("Đăng nhập nội bộ", color = PharmaPalette.Emerald,
                    style = MaterialTheme.typography.titleLarge)
                Text("Chọn vai trò. Sau khi vào, cả hai dùng cùng một giao diện vận hành.",
                    color = PharmaPalette.Muted, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StaffRole.entries.forEach { option ->
                        val active = selected == option
                        Surface(
                            modifier = Modifier.weight(1f).heightIn(min = 102.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = if (active) PharmaPalette.Mint else PharmaPalette.Paper,
                            onClick = { selected = option; pin = ""; error = "" },
                        ) {
                            Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(if (option == StaffRole.ADMIN) Icons.Outlined.AdminPanelSettings
                                     else Icons.Outlined.PointOfSale, null,
                                    tint = PharmaPalette.Emerald, modifier = Modifier.size(25.dp))
                                Text(if (option == StaffRole.ADMIN) "Quản lý" else "Nhân viên",
                                    fontWeight = FontWeight.Bold, color = PharmaPalette.Emerald)
                                Text(if (active) "Đang chọn" else "Chọn vai trò",
                                    fontSize = 11.sp, color = PharmaPalette.Muted)
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter(Char::isDigit).take(4); error = "" },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Mã truy cập demo") },
                    singleLine = true, shape = RoundedCornerShape(15.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                )
                Text(if (selected == StaffRole.CASHIER) "Mã thử của nhân viên: 1111"
                    else "Mã thử của quản lý: 2222",
                    color = PharmaPalette.Muted, style = MaterialTheme.typography.bodySmall)
                if (error.isNotEmpty()) Text(error, color = PharmaPalette.Warning)
                Button(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 55.dp),
                    enabled = pin.length == 4,
                    onClick = {
                        if (StaffDemoAccess.accepts(selected, pin)) {
                            pin = ""
                            onAuthenticated(selected)
                        } else error = "Mã demo không đúng với vai trò đã chọn."
                    },
                ) {
                    Text("Vào khu vận hành", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Outlined.ArrowForward, null)
                }
                Text("PIN được công khai để thử giao diện, không phải phân quyền bảo mật thật. Dữ liệu chỉ lưu trong RAM.",
                    style = MaterialTheme.typography.bodySmall, color = PharmaPalette.Muted)
            }
        }
    }
}

@Composable
internal fun StaffNotice(message: String) {
    Surface(shape = RoundedCornerShape(13.dp), color = PharmaPalette.Mint) {
        Text(
            message,
            modifier = Modifier.fillMaxWidth().padding(13.dp),
            color = PharmaPalette.Forest,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
