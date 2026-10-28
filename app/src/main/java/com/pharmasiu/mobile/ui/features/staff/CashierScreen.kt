package com.pharmasiu.mobile.ui.features.staff

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.domain.*
import com.pharmasiu.mobile.ui.components.PharmaTopBar
import com.pharmasiu.mobile.ui.components.formatVnd
import com.pharmasiu.mobile.ui.theme.PharmaPalette

@Composable
fun CashierScreen(
    products: List<Medicine>,
    store: StaffDemoStore,
    onLogout: () -> Unit,
    embedded: Boolean = false,
) {
    var page by rememberSaveable { mutableStateOf("sell") }
    var query by rememberSaveable { mutableStateOf("") }
    var payment by rememberSaveable { mutableStateOf(CounterPayment.CASH) }
    var cashInput by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var previewProduct by remember { mutableStateOf<Medicine?>(null) }
    val lines = store.cartLines
    val total = CounterRules.total(lines)
    BackHandler(enabled = page != "sell") {
        page = when (page) {
            "pay" -> "cart"
            "receipt" -> "sell"
            else -> "sell"
        }
    }
    Column(Modifier.fillMaxSize().background(PharmaPalette.Cream)) {
        if (!embedded) {
            PharmaTopBar(
                title = "Bán tại quầy",
                onBack = onLogout,
                trailing = { TextButton(onClick = { page = "shift" }) { Text("Ca bán") } },
            )
        } else {
            Row(
                Modifier.fillMaxWidth().background(PharmaPalette.Emerald)
                    .padding(horizontal = 18.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("BÁN HÀNG  /  POS DEMO",
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp, color = PharmaPalette.Lime)
                    Text(
                        when (page) {
                            "cart" -> "Giỏ bán hàng"
                            "pay" -> "Thanh toán"
                            "receipt" -> "Hoàn tất hóa đơn"
                            "history" -> "Hóa đơn trong phiên"
                            "shift" -> "Ca bán hàng"
                            else -> "Bán tại quầy"
                        },
                        style = MaterialTheme.typography.headlineMedium, color = Color.White,
                    )
                }
                Surface(
                    onClick = { page = "shift" },
                    color = PharmaPalette.Lime,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.Schedule, null, tint = PharmaPalette.Forest,
                            modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ca bán", fontWeight = FontWeight.Bold,
                            color = PharmaPalette.Forest, fontSize = 11.sp)
                    }
                }
            }
        }
        when (page) {
            "sell" -> {
                if (!store.shiftOpen) {
                    CashierClosedShift(
                        onOpen = { store.openShift(); error = "" },
                        onHistory = { page = "history" },
                    )
                } else {
                    Column(Modifier.fillMaxSize()) {
                        Text(
                            "Chạm ảnh để xem lớn • Rx chưa hỗ trợ bán ở quầy demo.",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            color = PharmaPalette.Muted, style = MaterialTheme.typography.bodySmall,
                        )
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            label = { Text("Tìm theo tên hoặc nhóm sản phẩm") },
                            leadingIcon = { Icon(Icons.Outlined.Search, null) },
                            singleLine = true,
                            shape = RoundedCornerShape(15.dp),
                        )
                        val matches = remember(query, products) {
                            val normalized = CatalogSearchRules.normalized(query)
                            products.filter {
                                normalized.isEmpty() || CatalogSearchRules.normalized(it.name + " " + it.category)
                                    .contains(normalized)
                            }
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            if (matches.isEmpty()) item {
                                Text("Không tìm thấy sản phẩm.", color = PharmaPalette.Muted)
                            }
                            items(matches, key = { it.id }) { product ->
                                CounterCatalogRow(
                                    product = product,
                                    available = store.stock[product.id] ?: 0,
                                    inCart = store.cart[product.id] ?: 0,
                                    onPreview = { previewProduct = product },
                                    onAdd = { if (!store.add(product)) error = "Sản phẩm không đủ điều kiện bán trong demo." },
                                )
                            }
                        }
                        if (error.isNotEmpty()) Text(
                            error, color = PharmaPalette.Warning, modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Surface(color = PharmaPalette.Paper, shadowElevation = 4.dp) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(lines.sumOf { it.quantity }.toString() + " sản phẩm",
                                        style = MaterialTheme.typography.bodySmall, color = PharmaPalette.Muted)
                                    Text(formatVnd(total), style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold, color = PharmaPalette.Emerald)
                                }
                                Button(
                                    enabled = lines.isNotEmpty(),
                                    onClick = { page = "cart"; error = "" },
                                    modifier = Modifier.heightIn(min = 52.dp),
                                ) {
                                    Icon(Icons.Outlined.ShoppingCart, null,
                                        modifier = Modifier.size(19.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Xem giỏ")
                                }
                            }
                        }
                    }
                }
            }
            "cart" -> Column(Modifier.fillMaxSize()) {
                Text("Kiểm tra giỏ bán", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(lines, key = { it.productId }) { line ->
                        Surface(color = PharmaPalette.Paper, shape = RoundedCornerShape(15.dp)) {
                            Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    products.firstOrNull { it.id == line.productId }?.let { product ->
                                        StaffProductThumbnail(
                                            medicine = product,
                                            onOpen = { previewProduct = product },
                                            size = 62.dp,
                                        )
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text(line.name, fontWeight = FontWeight.SemiBold)
                                        Text(formatVnd(line.unitPriceVnd) + " / " + line.unit,
                                            color = PharmaPalette.Muted)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedButton(onClick = { store.reduce(line.productId) }) { Text("−") }
                                    Text(line.quantity.toString(), modifier = Modifier.padding(horizontal = 18.dp))
                                    OutlinedButton(
                                        onClick = { products.firstOrNull { it.id == line.productId }?.let(store::add) },
                                        enabled = line.quantity < (store.stock[line.productId] ?: 0) &&
                                            line.quantity < CounterRules.MAX_PER_LINE,
                                    ) { Text("+") }
                                    Spacer(Modifier.weight(1f))
                                    Text(formatVnd(line.unitPriceVnd * line.quantity), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Tạm tính: " + formatVnd(total), fontWeight = FontWeight.Bold, color = PharmaPalette.Emerald)
                    Button(
                        onClick = { page = "pay"; cashInput = ""; error = "" },
                        enabled = lines.isNotEmpty() && store.shiftOpen,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    ) { Text("Tiếp tục thanh toán") }
                    OutlinedButton(onClick = { page = "sell" }, modifier = Modifier.fillMaxWidth()) { Text("Thêm sản phẩm") }
                    TextButton(onClick = { store.clearDraft(); page = "sell" }) { Text("Hủy giỏ đang lập") }
                }
            }
            "pay" -> Column(
                Modifier.fillMaxSize().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                Text("Xác nhận thanh toán demo", style = MaterialTheme.typography.titleLarge)
                Text(lines.sumOf { it.quantity }.toString() + " món • Tổng cộng " + formatVnd(total),
                    color = PharmaPalette.Emerald, fontWeight = FontWeight.Bold)
                Text("Phương thức", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = payment == CounterPayment.CASH,
                        onClick = { payment = CounterPayment.CASH },
                        label = { Text("Tiền mặt") },
                    )
                    FilterChip(
                        selected = payment == CounterPayment.OTHER_DEMO,
                        onClick = { payment = CounterPayment.OTHER_DEMO },
                        label = { Text("Khác (demo)") },
                    )
                }
                if (payment == CounterPayment.CASH) {
                    OutlinedTextField(
                        value = cashInput,
                        onValueChange = { cashInput = it.filter(Char::isDigit).take(12) },
                        label = { Text("Khách đưa (VNĐ)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    val received = cashInput.toLongOrNull() ?: 0
                    Text("Tiền trả khách: " + formatVnd(CounterRules.changeDue(total, received)),
                        color = PharmaPalette.Emerald, fontWeight = FontWeight.SemiBold)
                }
                StaffNotice("Mọi phương thức chỉ mô phỏng. Không thu tiền, kết nối máy POS hay xác nhận thanh toán thật.")
                if (error.isNotEmpty()) Text(error, color = PharmaPalette.Warning)
                Spacer(Modifier.weight(1f))
                val received = cashInput.toLongOrNull() ?: 0
                Button(
                    enabled = CounterRules.canConfirm(lines, store.stock, store.shiftOpen, payment, received),
                    onClick = {
                        if (store.complete(payment, received) != null) {
                            cashInput = ""; page = "receipt"; error = ""
                        } else error = "Không thể hoàn tất. Kiểm tra lại số lượng và trạng thái ca."
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 55.dp),
                ) { Text("Hoàn tất hóa đơn demo") }
                OutlinedButton(onClick = { page = "cart" }, modifier = Modifier.fillMaxWidth()) { Text("Kiểm tra lại giỏ") }
            }
            "receipt" -> CounterReceipt(store.lastReceipt, onNext = { page = "sell" }, onHistory = { page = "history" })
            "history" -> CounterHistory(store, onBack = { page = "sell" })
            "shift" -> CounterShift(store, onBack = { page = "sell" })
        }
    }
    previewProduct?.let { medicine ->
        StaffProductMediaPreview(medicine, onDismiss = { previewProduct = null })
    }
}

@Composable
private fun CounterCatalogRow(
    product: Medicine, available: Int, inCart: Int,
    onPreview: () -> Unit, onAdd: () -> Unit,
) {
    val canAdd = CounterRules.availableToAdd(product, inCart, available)
    Surface(color = PharmaPalette.Paper, shape = RoundedCornerShape(16.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StaffProductThumbnail(medicine = product, onOpen = onPreview, size = 68.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(product.name, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                Text(product.category + " · " + product.unit, color = PharmaPalette.Muted,
                    style = MaterialTheme.typography.bodySmall)
                Text(if (product.requiresPrescription) "Cần quy trình duyệt toa riêng"
                    else if (product.priceVnd <= 0) "Chưa có giá demo"
                    else formatVnd(product.priceVnd) + " • Tồn mẫu: " + available,
                    color = if (product.requiresPrescription) PharmaPalette.Warning else PharmaPalette.Emerald,
                    style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onAdd, enabled = canAdd, modifier = Modifier.heightIn(min = 44.dp)) {
                Text(if (inCart > 0) "+" + inCart else "Thêm", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun CashierClosedShift(onOpen: () -> Unit, onHistory: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Icon(Icons.Outlined.PointOfSale, null, tint = PharmaPalette.Emerald, modifier = Modifier.size(54.dp))
        Text("Chưa mở ca bán", style = MaterialTheme.typography.headlineMedium)
        Text("Mở ca để lập hóa đơn tại quầy. Mọi số lượng, tiền và hóa đơn đều là dữ liệu giả lập.",
            color = PharmaPalette.Muted)
        Button(onClick = onOpen, modifier = Modifier.fillMaxWidth().heightIn(min = 55.dp)) { Text("Mở ca bán demo") }
        OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth()) { Text("Xem hóa đơn đã lập") }
    }
}

@Composable
private fun CounterReceipt(sale: CounterSale?, onNext: () -> Unit, onHistory: () -> Unit) {
    if (sale == null) { Text("Chưa có hóa đơn trong phiên này."); return }
    Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState())) {
        Column(
            Modifier.fillMaxWidth().background(PharmaPalette.Emerald)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Icon(Icons.Outlined.CheckCircle, null, tint = PharmaPalette.Lime,
                modifier = Modifier.size(62.dp))
            Text("Hoàn tất hóa đơn!", style = MaterialTheme.typography.headlineMedium,
                color = Color.White, fontWeight = FontWeight.Bold)
            Text("Giao dịch mô phỏng đã được ghi lại.", color = PharmaPalette.Mint,
                style = MaterialTheme.typography.bodySmall)
        }
        Column(Modifier.fillMaxWidth().padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(color = PharmaPalette.Paper, shape = RoundedCornerShape(17.dp)) {
                Column(Modifier.fillMaxWidth().padding(17.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Text("HÓA ĐƠN / PHARMASIU",
                        fontSize = 10.sp, letterSpacing = 1.1.sp,
                        fontWeight = FontWeight.Bold, color = PharmaPalette.Muted)
                    Text(sale.reference, color = PharmaPalette.Emerald,
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Text(sale.createdAt + "  ·  " + sale.cashierName,
                        style = MaterialTheme.typography.bodySmall, color = PharmaPalette.Muted)
                    HorizontalDivider(color = PharmaPalette.Line)
                    sale.lines.forEach { line ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(line.name + " ×" + line.quantity,
                                modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Text(formatVnd(line.unitPriceVnd * line.quantity),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                    HorizontalDivider(color = PharmaPalette.Line)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("TỔNG CỘNG", modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelMedium, color = PharmaPalette.Muted)
                        Text(formatVnd(sale.totalVnd), style = MaterialTheme.typography.titleLarge,
                            color = PharmaPalette.Emerald, fontWeight = FontWeight.Bold)
                    }
                    Text("Phương thức: " +
                        if (sale.payment == CounterPayment.CASH) "Tiền mặt (demo)" else "Khác (demo)",
                        style = MaterialTheme.typography.bodySmall, color = PharmaPalette.Muted)
                    if (sale.payment == CounterPayment.CASH) {
                        Row {
                            Text("Khách đưa", Modifier.weight(1f))
                            Text(formatVnd(sale.receivedVnd))
                        }
                        Row {
                            Text("Tiền thối", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            Text(formatVnd(sale.changeVnd), fontWeight = FontWeight.Bold,
                                color = PharmaPalette.Emerald)
                        }
                    }
                }
            }
            StaffNotice("Chỉ là hóa đơn trong RAM. Không có thu tiền, in hóa đơn hoặc xuất chứng từ thật.")
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                shape = RoundedCornerShape(15.dp),
            ) {
                Icon(Icons.Outlined.PointOfSale, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(9.dp))
                Text("Tiếp tục bán hàng")
            }
            OutlinedButton(
                onClick = onHistory,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(15.dp),
            ) { Text("Xem lịch sử hóa đơn") }
        }
    }
}

@Composable
private fun CounterHistory(store: StaffDemoStore, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Hóa đơn trong phiên", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        if (store.sales.isEmpty()) Text("Chưa có hóa đơn demo.", color = PharmaPalette.Muted)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            items(store.sales.reversed(), key = { it.reference }) { sale ->
                Surface(color = PharmaPalette.Paper, shape = RoundedCornerShape(13.dp)) {
                    Column(Modifier.fillMaxWidth().padding(13.dp)) {
                        Text(sale.reference, color = PharmaPalette.Emerald, fontWeight = FontWeight.Bold)
                        Text(sale.createdAt + " • " + sale.lines.sumOf { it.quantity } + " món",
                            color = PharmaPalette.Muted, style = MaterialTheme.typography.bodySmall)
                        Text(formatVnd(sale.totalVnd), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Quay lại bán hàng") }
    }
}

@Composable
private fun CounterShift(store: StaffDemoStore, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Ca bán demo", style = MaterialTheme.typography.titleLarge)
        Text(if (store.shiftOpen) "Đang mở ca" else "Đã đóng ca",
            color = PharmaPalette.Emerald, fontWeight = FontWeight.Bold)
        Text("Hóa đơn trong ca: " + store.completedInShift)
        Text("Tổng giá trị hóa đơn trong ca: " + formatVnd(store.shiftRevenueVnd))
        StaffNotice("Số liệu chỉ ghi trong phiên app. Không có kiểm đếm quỹ, đối soát tiền mặt hoặc kết nối quản lý.")
        if (store.shiftOpen) {
            if (store.cart.isNotEmpty()) {
                Text("Hãy hoàn tất hoặc hủy giỏ đang lập trước khi đóng ca.", color = PharmaPalette.Warning)
            }
            Button(
                onClick = { store.closeShift(); onBack() },
                enabled = store.cart.isEmpty(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) { Text("Đóng ca demo") }
        } else Button(
            onClick = { store.openShift(); onBack() },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        ) { Text("Mở ca mới") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Quay lại") }
    }
}
