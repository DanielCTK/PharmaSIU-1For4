package com.pharmasiu.mobile.ui.features.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.ui.components.PharmaTopBar
import com.pharmasiu.mobile.ui.components.formatVnd
import com.pharmasiu.mobile.ui.theme.PharmaPalette

/**
 * Intentional minimal admin scope: overview, demo stock and counter-sale review.
 * No users/roles CRUD, procurement, finance or backend configuration screens.
 */
@Composable
fun AdminScreen(products: List<Medicine>, store: StaffDemoStore, onLogout: () -> Unit) {
    var section by rememberSaveable { mutableStateOf("overview") }
    Column(Modifier.fillMaxSize().background(PharmaPalette.Cream)) {
        PharmaTopBar("Quản trị cơ bản", onBack = onLogout)
        StaffNotice("Quản trị chế độ demo • thay đổi chỉ nằm trên thiết bị; chưa có dữ liệu quản lý thật.")
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(
                "overview" to "Tổng quan",
                "stock" to "Hàng hóa",
                "sales" to "Bán hàng",
            ).forEach { (route, label) ->
                FilterChip(selected = section == route, onClick = { section = route }, label = { Text(label) })
            }
        }
        when (section) {
            "overview" -> {
                val eligible = products.count { !it.requiresPrescription && it.priceVnd > 0 }
                val lowStock = products.count { !it.requiresPrescription && (store.stock[it.id] ?: 0) < 5 }
                val turnover = store.sales.sumOf { it.totalVnd }
                Column(
                    Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text("Tổng quan phiên demo", style = MaterialTheme.typography.titleLarge, color = PharmaPalette.Emerald)
                    AdminStat("Sản phẩm có thể bán", eligible.toString())
                    AdminStat("Hóa đơn demo đã lập", store.sales.size.toString())
                    AdminStat("Tổng giá trị bán demo", formatVnd(turnover))
                    AdminStat("Mặt hàng tồn mẫu dưới 5", lowStock.toString())
                    StaffNotice("Thuốc kê đơn được khóa trong POS demo cho đến khi có luồng duyệt toa. Không tính tồn/lô FEFO thật.")
                }
            }
            "stock" -> {
                Column(Modifier.fillMaxSize()) {
                    Text(
                        "Danh mục & tồn mẫu · " + products.size + " mặt hàng",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(products, key = { it.id }) { product ->
                            Surface(color = PharmaPalette.Paper, shape = RoundedCornerShape(15.dp)) {
                                Column(
                                    Modifier.fillMaxWidth().padding(13.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(product.name, fontWeight = FontWeight.SemiBold)
                                    Text(product.category + " • " +
                                        if (product.priceVnd > 0) formatVnd(product.priceVnd) else "Chưa có giá",
                                        color = PharmaPalette.Muted, style = MaterialTheme.typography.bodySmall)
                                    if (product.requiresPrescription) {
                                        Text("Rx: không điều chỉnh/bán trong demo", color = PharmaPalette.Warning)
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Tồn giả lập: " + (store.stock[product.id] ?: 0),
                                                modifier = Modifier.weight(1f),
                                                color = PharmaPalette.Emerald)
                                            OutlinedButton(
                                                onClick = { store.adjustDemoStock(product.id, -1) },
                                                enabled = (store.stock[product.id] ?: 0) > (store.cart[product.id] ?: 0),
                                            ) { Text("−") }
                                            Spacer(Modifier.width(8.dp))
                                            OutlinedButton(
                                                onClick = { store.adjustDemoStock(product.id, 1) },
                                                enabled = (store.stock[product.id] ?: 0) < 999,
                                            ) { Text("+") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "sales" -> Column(Modifier.fillMaxSize()) {
                Text(
                    "Lịch sử bán tại quầy",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                )
                if (store.sales.isEmpty()) Text(
                    "Chưa có hóa đơn nào trong phiên app.",
                    modifier = Modifier.padding(16.dp),
                    color = PharmaPalette.Muted,
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(store.sales.reversed(), key = { it.reference }) { sale ->
                        Surface(color = PharmaPalette.Paper, shape = RoundedCornerShape(14.dp)) {
                            Column(Modifier.fillMaxWidth().padding(15.dp)) {
                                Text(sale.reference, color = PharmaPalette.Emerald, fontWeight = FontWeight.Bold)
                                Text(sale.createdAt, color = PharmaPalette.Muted, style = MaterialTheme.typography.bodySmall)
                                Text(sale.lines.sumOf { it.quantity }.toString() + " món • " + formatVnd(sale.totalVnd))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStat(label: String, value: String) {
    Surface(color = PharmaPalette.Paper, shape = RoundedCornerShape(16.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(value, color = PharmaPalette.Emerald, fontWeight = FontWeight.Bold)
        }
    }
}
