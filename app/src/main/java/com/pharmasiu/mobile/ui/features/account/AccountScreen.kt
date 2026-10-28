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
fun AccountScreen(
    profile: MockProfile?,
    favoriteCount: Int,
    requestCount: Int,
    onAuth: () -> Unit,
    onEdit: () -> Unit,
    onFavorites: () -> Unit,
    onOrders: () -> Unit,
    onAddresses: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onLogout: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        PharmaTopBar("Tài khoản")
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(17.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                color = PharmaPalette.Emerald,
            ) {
                Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.AccountCircle, null, tint = PharmaPalette.Lime, modifier = Modifier.size(43.dp))
                    Text(profile?.displayName ?: "Xin chào, khách ghé thăm!", style = MaterialTheme.typography.headlineMedium, color = androidx.compose.ui.graphics.Color.White)
                    Text(profile?.phone ?: "Đăng nhập để thử chỉnh sửa hồ sơ và quản lý yêu cầu.", color = PharmaPalette.Mint)
                    if (profile == null) Button(onClick = onAuth, colors = ButtonDefaults.buttonColors(containerColor = PharmaPalette.Lime, contentColor = PharmaPalette.Emerald)) { Text("Đăng nhập / Đăng ký demo") }
                    else Text("Tài khoản mẫu • không liên kết máy chủ", color = PharmaPalette.Lime)
                }
            }
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(17.dp),
            ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AccountStat(favoriteCount.toString(), "Yêu thích", Modifier.weight(1f))
                AccountStat(requestCount.toString(), "Đơn hàng demo", Modifier.weight(1f))
            }
            Text("Quản lý cá nhân", style = MaterialTheme.typography.titleLarge, color = PharmaPalette.Emerald)
            AccountAction("Chỉnh sửa thông tin", "Tên, số điện thoại, email", Icons.Outlined.Edit, profile != null, if (profile == null) onAuth else onEdit)
            AccountAction("Sổ địa chỉ liên hệ", "Lưu địa chỉ demo (không phải nơi nhận hàng)", Icons.Outlined.Place, profile != null, if (profile == null) onAuth else onAddresses)
            AccountAction("Đổi mật khẩu demo", "Chỉ thay đổi trong phiên ứng dụng", Icons.Outlined.Lock, profile != null, if (profile == null) onAuth else onChangePassword)
            AccountAction("Sản phẩm yêu thích", "Danh sách đã lưu trên thiết bị", Icons.Outlined.FavoriteBorder, true, onFavorites)
            AccountAction("Đơn hàng của tôi", "Đơn đã đặt trong phiên demo", Icons.Outlined.ReceiptLong, true, onOrders)
            if (profile != null) OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Đăng xuất khỏi phiên demo") }
            Text("Đây là ứng dụng mô phỏng. Không nhập thông tin đăng nhập, toa hoặc dữ liệu cá nhân thật.", color = PharmaPalette.Muted, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun AccountStat(value: String, title: String, modifier: Modifier = Modifier) {
    Surface(modifier, color = PharmaPalette.Mint, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = PharmaPalette.Emerald)
            Text(title, color = PharmaPalette.Muted)
        }
    }
}

@Composable
private fun AccountAction(
    title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean, onClick: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(19.dp), color = PharmaPalette.Paper) {
        Row(Modifier.fillMaxWidth().heightIn(min = 71.dp).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = PharmaPalette.Emerald, modifier = Modifier.size(27.dp))
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = PharmaPalette.Muted, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onClick) { Text(if (enabled) "Mở" else "Đăng nhập") }
        }
    }
}
