package com.pharmasiu.mobile.ui.features.staff

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pharmasiu.mobile.domain.CounterRules
import com.pharmasiu.mobile.ui.theme.PharmaPalette

/** Simple mock-only SKU creation. Does not create purchase orders, inventory lots or SQL. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateDemoProductSheet(
    onDismiss: () -> Unit,
    onPickPhoto: () -> Unit,
    photoSelected: Boolean,
    onSave: (name: String, category: String, unit: String, priceVnd: Long, stock: Int) -> Boolean,
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(StaffDemoStore.STAFF_DEMO_CATEGORIES.first()) }
    var unit by remember { mutableStateOf("Hộp") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("0") }
    var error by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = PharmaPalette.Paper) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(.85f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Thêm mặt hàng", style = MaterialTheme.typography.headlineSmall,
                color = PharmaPalette.Emerald, fontWeight = FontWeight.Bold)
            Text("Tạo mặt hàng để bán thử tại quầy. Đây không phải phiếu nhập kho thực tế.",
                color = PharmaPalette.Muted, style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = name, onValueChange = { name = it.take(90); error = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tên mặt hàng *") }, singleLine = true,
                shape = RoundedCornerShape(13.dp),
            )
            Text("Nhóm sản phẩm", fontWeight = FontWeight.SemiBold, color = PharmaPalette.Ink)
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StaffDemoStore.STAFF_DEMO_CATEGORIES.forEach {
                    FilterChip(
                        selected = it == category,
                        onClick = { category = it; error = "" },
                        label = { Text(it) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PharmaPalette.Emerald,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                        ),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = unit, onValueChange = { unit = it.take(24) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Đơn vị *") }, singleLine = true,
                    shape = RoundedCornerShape(13.dp),
                )
                OutlinedTextField(
                    value = stock, onValueChange = { stock = it.filter(Char::isDigit).take(3); error = "" },
                    modifier = Modifier.weight(1f),
                    label = { Text("Tồn ban đầu *") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(13.dp),
                )
            }
            OutlinedTextField(
                value = price, onValueChange = { price = it.filter(Char::isDigit).take(9); error = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Giá bán demo (VNĐ) *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(13.dp),
            )
            OutlinedButton(
                onClick = onPickPhoto,
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            ) {
                Icon(Icons.Outlined.AddPhotoAlternate, null)
                Spacer(Modifier.width(8.dp))
                Text(if (photoSelected) "Đã chọn ảnh • Đổi ảnh" else "Chọn ảnh sản phẩm từ điện thoại")
            }
            StaffNotice("Không thêm thuốc kê đơn qua form nhanh. Ảnh, mặt hàng và tồn được giữ trong phiên demo; khởi động lại sẽ về mẫu ban đầu.")
            if (error.isNotEmpty()) Text(error, color = PharmaPalette.Warning)
            Button(
                onClick = {
                    val parsedPrice = price.toLongOrNull()
                    val parsedStock = stock.toIntOrNull()
                    if (parsedPrice == null || parsedStock == null ||
                        name.trim().length < 2 || unit.trim().isEmpty() ||
                        parsedPrice !in 1..100_000_000L || parsedStock !in 0..999
                    ) {
                        error = "Vui lòng nhập tên, giá (1–100.000.000đ), đơn vị và tồn (0–999) hợp lệ."
                    } else if (!onSave(name, category, unit, parsedPrice, parsedStock)) {
                        error = "Không thể lưu. Có thể mặt hàng này đã tồn tại trong demo."
                    } else {
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
            ) { Text("Lưu mặt hàng demo") }
        }
    }
}

/** Explicit quantity input: do not confuse a demo stock adjustment with a real receipt. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReceiveDemoStockSheet(
    productName: String,
    currentStock: Int,
    onDismiss: () -> Unit,
    onReceive: (Int) -> Boolean,
) {
    var quantity by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = PharmaPalette.Paper) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(Icons.Outlined.Inventory2, null, tint = PharmaPalette.Emerald,
                modifier = Modifier.size(32.dp))
            Text("Nhập thêm tồn demo", style = MaterialTheme.typography.headlineSmall,
                color = PharmaPalette.Emerald)
            Text(productName, fontWeight = FontWeight.SemiBold)
            Text("Tồn hiện tại: $currentStock", color = PharmaPalette.Muted)
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter(Char::isDigit).take(3); error = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số lượng nhập thêm *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            StaffNotice("Đây là tăng tồn mẫu để thử bán hàng, chưa ghi nhận phiếu nhập, nhà cung cấp, lô hoặc hạn dùng.")
            if (error.isNotEmpty()) Text(error, color = PharmaPalette.Warning)
            Button(
                onClick = {
                    val parsed = quantity.toIntOrNull()
                    if (parsed == null || !onReceive(parsed)) {
                        error = "Số lượng phải từ 1 đến 999 và tổng tồn không vượt 999."
                    } else onDismiss()
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) { Text("Cộng tồn demo") }
            Spacer(Modifier.height(20.dp))
        }
    }
}
