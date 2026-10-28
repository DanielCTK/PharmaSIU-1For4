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
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.domain.StaffDemoAccess
import com.pharmasiu.mobile.domain.StaffRole
import com.pharmasiu.mobile.ui.components.PharmaTopBar
import com.pharmasiu.mobile.ui.theme.PharmaPalette

/** Separate role entry; no customer state or backend permissions are modified. */
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
        StaffRole.CASHIER -> CashierScreen(products, store, onLogout = { role = null })
        StaffRole.ADMIN -> AdminScreen(products, store, onLogout = { role = null })
    }
}

@Composable
private fun StaffDemoSignIn(onExit: () -> Unit, onAuthenticated: (StaffRole) -> Unit) {
    var selected by rememberSaveable { mutableStateOf(StaffRole.CASHIER) }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(PharmaPalette.Cream)) {
        PharmaTopBar("Khu vực nhân viên", onBack = onExit)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Surface(color = PharmaPalette.Emerald, shape = RoundedCornerShape(0.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.Storefront, null, tint = PharmaPalette.Lime, modifier = Modifier.size(38.dp))
                    Text("Làm việc tại nhà thuốc", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    Text("Chọn vai trò để thử đúng các màn hình nghiệp vụ.", color = PharmaPalette.Mint)
                }
            }
            Text("Chọn vai trò", color = PharmaPalette.Emerald, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StaffRole.entries.forEach { option ->
                    FilterChip(
                        selected = selected == option,
                        onClick = { selected = option; pin = ""; error = "" },
                        label = { Text(if (option == StaffRole.CASHIER) "Bán hàng" else "Quản trị") },
                    )
                }
            }
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter(Char::isDigit).take(4); error = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mã truy cập demo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
            )
            Text(
                if (selected == StaffRole.CASHIER) "Mã thử bán hàng: 1111" else "Mã thử quản trị: 2222",
                color = PharmaPalette.Muted, style = MaterialTheme.typography.bodyMedium,
            )
            if (error.isNotEmpty()) Text(error, color = PharmaPalette.Warning)
            Button(
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                enabled = pin.length == 4,
                onClick = {
                    if (StaffDemoAccess.accepts(selected, pin)) {
                        pin = ""
                        onAuthenticated(selected)
                    } else error = "Mã demo không đúng với vai trò đã chọn."
                },
            ) { Text("Vào khu vực " + if (selected == StaffRole.CASHIER) "bán hàng" else "quản trị") }
            Text(
                "Đây là cổng minh họa trên thiết bị, mã PIN được công khai để thử UI. Không phải đăng nhập nhân viên thật hoặc phân quyền hệ thống.",
                style = MaterialTheme.typography.bodySmall,
                color = PharmaPalette.Muted,
            )
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
