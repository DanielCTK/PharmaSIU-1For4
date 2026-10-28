package com.pharmasiu.mobile.ui.features.staff

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.domain.CounterSale
import com.pharmasiu.mobile.domain.CounterPayment
import com.pharmasiu.mobile.domain.StaffRole
import com.pharmasiu.mobile.ui.components.formatVnd
import com.pharmasiu.mobile.ui.theme.PharmaPalette

/** Both roles share the same operating shell; only admin may change demo stock. */
@Composable
fun OperationsHub(role: StaffRole, products: List<Medicine>, store: StaffDemoStore, onLogout: () -> Unit) {
    var tab by rememberSaveable(role) { mutableStateOf("overview") }
    val saved = rememberSaveableStateHolder()
    val tabs = listOf(
        Triple("overview", "Tổng quan", Icons.Outlined.Dashboard),
        Triple("sell", "Bán hàng", Icons.Outlined.PointOfSale),
        Triple("stock", "Hàng hóa", Icons.Outlined.Inventory2),
        Triple("history", "Hóa đơn", Icons.Outlined.ReceiptLong),
    )
    BackHandler(enabled = tab != "overview") { tab = "overview" }
    Scaffold(
        containerColor = PharmaPalette.Cream,
        topBar = {
            Surface(color = PharmaPalette.Paper, tonalElevation = 1.dp) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(horizontal = 15.dp),
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(color = PharmaPalette.Emerald, shape = RoundedCornerShape(12.dp)) {
                        Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.LocalPharmacy, null, tint = PharmaPalette.Lime)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text("PharmaSIU", fontWeight = FontWeight.ExtraBold,
                            color = PharmaPalette.Emerald, style = MaterialTheme.typography.titleMedium)
                        Text("VẬN HÀNH NHÀ THUỐC", fontSize = 9.sp, letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold, color = PharmaPalette.Muted)
                    }
                    Surface(color = PharmaPalette.Mint, shape = RoundedCornerShape(11.dp)) {
                        Text(if (role == StaffRole.ADMIN) "Quản lý" else "Nhân viên",
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                            color = PharmaPalette.Emerald, fontSize = 11.sp)
                    }
                    IconButton(onClick = onLogout, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Outlined.Logout, contentDescription = "Đổi vai trò / thoát khu vận hành",
                            tint = PharmaPalette.Emerald)
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = PharmaPalette.Paper) {
                tabs.forEach { (id, label, symbol) ->
                    NavigationBarItem(
                        selected = tab == id, onClick = { tab = id },
                        icon = { Icon(symbol, null) },
                        label = { Text(label, fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PharmaPalette.Emerald,
                            selectedTextColor = PharmaPalette.Emerald,
                            indicatorColor = PharmaPalette.Lime,
                            unselectedIconColor = PharmaPalette.Muted,
                            unselectedTextColor = PharmaPalette.Muted,
                        ),
                    )
                }
            }
        },
    ) { inset ->
        Box(Modifier.fillMaxSize().padding(inset)) {
            saved.SaveableStateProvider(tab) {
                when (tab) {
                    "overview" -> OperationOverview(role, products, store,
                        onSell = { tab = "sell" }, onStock = { tab = "stock" },
                        onHistory = { tab = "history" })
                    "sell" -> CashierScreen(products, store, onLogout = onLogout, embedded = true)
                    "stock" -> OperationGoods(products, store, canEdit = role == StaffRole.ADMIN)
                    "history" -> OperationHistory(store)
                }
            }
        }
    }
}

@Composable
private fun OperationOverview(
    role: StaffRole, products: List<Medicine>, store: StaffDemoStore,
    onSell: () -> Unit, onStock: () -> Unit, onHistory: () -> Unit,
) {
    var closeDialog by remember { mutableStateOf(false) }
    LazyColumn(
        Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 25.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item("hero") {
            Column(
                Modifier.fillMaxWidth().background(PharmaPalette.Emerald)
                    .padding(horizontal = 23.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                Text("PHARMASIU   /   OPERATIONS", color = PharmaPalette.Lime,
                    fontSize = 10.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold)
                Text("Một nơi cho\nmọi ca làm.", color = Color.White,
                    style = MaterialTheme.typography.headlineLarge, lineHeight = 36.sp)
                Text(if (role == StaffRole.ADMIN) "Bán hàng và quản lý cơ bản trong một khu vực."
                    else "Bán hàng đúng quy trình, thao tác nhanh.",
                    color = PharmaPalette.Mint, style = MaterialTheme.typography.bodySmall)
                Surface(
                    color = if (store.shiftOpen) PharmaPalette.Lime else PharmaPalette.Mint,
                    shape = RoundedCornerShape(50),
                ) {
                    Text(if (store.shiftOpen) "●  CA ĐANG MỞ" else "●  CA CHƯA MỞ",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PharmaPalette.Forest)
                }
            }
        }
        item("numbers") {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Nhịp vận hành", style = MaterialTheme.typography.titleLarge, color = PharmaPalette.Ink)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OperationMetric("HÓA ĐƠN", store.sales.size.toString().padStart(2, '0'),
                        "Đã lập trong phiên", Modifier.weight(1f))
                    OperationMetric("BÁN DEMO", formatVnd(store.sales.sumOf { it.totalVnd }),
                        "Chưa đối soát", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OperationMetric("GIỎ ĐANG LẬP", store.cart.values.sum().toString().padStart(2, '0'),
                        "Sản phẩm đã chọn", Modifier.weight(1f))
                    OperationMetric("TỒN MẪU THẤP",
                        products.count { !it.requiresPrescription && (store.stock[it.id] ?: 0) <= 4 }
                            .toString().padStart(2, '0'),
                        "Ngưỡng 4 đơn vị", Modifier.weight(1f))
                }
            }
        }
        item("actions") {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Thao tác nhanh", style = MaterialTheme.typography.titleLarge, color = PharmaPalette.Ink)
                Surface(onClick = onSell, color = PharmaPalette.Lime, shape = RoundedCornerShape(17.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(17.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(13.dp),
                    ) {
                        Icon(Icons.Outlined.PointOfSale, null, modifier = Modifier.size(32.dp),
                            tint = PharmaPalette.Forest)
                        Column(Modifier.weight(1f)) {
                            Text("Bán hàng tại quầy", fontWeight = FontWeight.Bold,
                                color = PharmaPalette.Forest)
                            Text("Chọn hàng → thanh toán → hóa đơn",
                                style = MaterialTheme.typography.bodySmall, color = PharmaPalette.Forest)
                        }
                        Icon(Icons.Outlined.ArrowForward, null, tint = PharmaPalette.Forest)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OperationQuickAction("Hàng hóa", "Xem tồn mẫu",
                        Icons.Outlined.Inventory2, Modifier.weight(1f), onStock)
                    OperationQuickAction("Hóa đơn", "Lịch sử bán",
                        Icons.Outlined.ReceiptLong, Modifier.weight(1f), onHistory)
                }
            }
        }
        item("shift") {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Ca làm việc", style = MaterialTheme.typography.titleLarge, color = PharmaPalette.Ink)
                Surface(color = PharmaPalette.Paper, shape = RoundedCornerShape(17.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(if (store.shiftOpen) "Ca đang hoạt động" else "Chưa mở ca",
                            fontWeight = FontWeight.Bold, color = PharmaPalette.Emerald)
                        Text("Trong ca: " + store.completedInShift + " hóa đơn • " +
                            formatVnd(store.shiftRevenueVnd),
                            color = PharmaPalette.Muted)
                        if (store.shiftOpen) OutlinedButton(
                            enabled = store.cart.isEmpty(),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            onClick = { closeDialog = true },
                        ) {
                            Text(if (store.cart.isEmpty()) "Đóng ca demo" else "Xử lý giỏ trước khi đóng ca")
                        } else Button(
                            onClick = store::openShift,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        ) { Text("Mở ca bán demo") }
                    }
                }
            }
        }
        item("disclosure") {
            Text("Thông tin, tiền và tồn chỉ là dữ liệu mô phỏng trong phiên. Chưa kết nối kho, thu tiền hoặc quản lý thật.",
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.bodySmall, color = PharmaPalette.Muted)
        }
    }
    if (closeDialog) AlertDialog(
        onDismissRequest = { closeDialog = false },
        title = { Text("Đóng ca demo?") },
        text = { Text("Hóa đơn đã lập vẫn được lưu trong phiên để xem lại.") },
        confirmButton = {
            TextButton(onClick = { store.closeShift(); closeDialog = false }) { Text("Đóng ca") }
        },
        dismissButton = {
            TextButton(onClick = { closeDialog = false }) { Text("Quay lại") }
        },
    )
}

@Composable
private fun OperationMetric(label: String, value: String, hint: String, modifier: Modifier) {
    Surface(modifier = modifier.heightIn(min = 117.dp),
        color = PharmaPalette.Paper, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = PharmaPalette.Muted, fontSize = 9.sp,
                letterSpacing = 0.7.sp, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.titleLarge,
                color = PharmaPalette.Emerald, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(hint, color = PharmaPalette.Muted, fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable
private fun OperationQuickAction(
    title: String, note: String, symbol: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier, onClick: () -> Unit,
) {
    Surface(modifier = modifier.heightIn(min = 102.dp),
        shape = RoundedCornerShape(16.dp), color = PharmaPalette.Paper, onClick = onClick) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(symbol, null, tint = PharmaPalette.Emerald)
            Text(title, fontWeight = FontWeight.Bold, color = PharmaPalette.Ink)
            Text(note, fontSize = 11.sp, color = PharmaPalette.Muted)
        }
    }
}

@Composable
private fun OperationGoods(products: List<Medicine>, store: StaffDemoStore, canEdit: Boolean) {
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("Tất cả") }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().background(PharmaPalette.Emerald)
            .padding(horizontal = 19.dp, vertical = 17.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("DANH MỤC  /  TỒN MẪU", color = PharmaPalette.Lime,
                fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
            Text("Hàng hóa", color = Color.White,
                style = MaterialTheme.typography.headlineMedium)
            Text(if (canEdit) "Quản lý có thể điều chỉnh tồn demo."
                else "Chế độ xem; điều chỉnh thuộc quyền quản lý.",
                color = PharmaPalette.Mint, style = MaterialTheme.typography.bodySmall)
        }
        OutlinedTextField(
            value = search, onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 11.dp),
            label = { Text("Tìm hàng hóa") },
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            singleLine = true, shape = RoundedCornerShape(15.dp),
        )
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("Tất cả", "Tồn thấp", "Rx").forEach {
                FilterChip(
                    selected = filter == it, onClick = { filter = it }, label = { Text(it) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = PharmaPalette.Paper,
                        selectedContainerColor = PharmaPalette.Emerald,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }
        val filtered = products.filter { p ->
            (search.isBlank() || (p.name + " " + p.category).contains(search, ignoreCase = true)) &&
                when (filter) {
                    "Tồn thấp" -> !p.requiresPrescription && (store.stock[p.id] ?: 0) <= 4
                    "Rx" -> p.requiresPrescription
                    else -> true
                }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(filtered.size.toString() + " mặt hàng", fontWeight = FontWeight.Bold,
                color = PharmaPalette.Ink)
            Text(if (canEdit) "CHỈNH TỒN DEMO" else "CHỈ XEM",
                color = PharmaPalette.Muted, fontSize = 10.sp, letterSpacing = 0.7.sp)
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (filtered.isEmpty()) item("empty") {
                Text("Không có sản phẩm phù hợp.", color = PharmaPalette.Muted)
            }
            items(filtered, key = { it.id }) { p ->
                val stock = store.stock[p.id] ?: 0
                Surface(color = PharmaPalette.Paper, shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(11.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = PharmaPalette.Mint, shape = RoundedCornerShape(13.dp)) {
                                Box(Modifier.size(43.dp), contentAlignment = Alignment.Center) {
                                    Icon(if (p.requiresPrescription) Icons.Outlined.Medication
                                        else Icons.Outlined.Inventory2, null, tint = PharmaPalette.Emerald)
                                }
                            }
                            Column(Modifier.weight(1f)) {
                                Text(p.name, fontWeight = FontWeight.SemiBold, maxLines = 2,
                                    color = PharmaPalette.Ink, overflow = TextOverflow.Ellipsis)
                                Text(p.category + "  ·  " +
                                    if (p.priceVnd > 0) formatVnd(p.priceVnd) else "Chưa báo giá",
                                    color = PharmaPalette.Muted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        HorizontalDivider(color = PharmaPalette.Line)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (p.requiresPrescription) "Rx · chưa bán trong demo"
                                else "Tồn mẫu: " + stock,
                                modifier = Modifier.weight(1f),
                                color = if (p.requiresPrescription || stock <= 4)
                                    PharmaPalette.Warning else PharmaPalette.Emerald,
                                fontWeight = FontWeight.SemiBold)
                            if (canEdit && !p.requiresPrescription) {
                                IconButton(
                                    onClick = { store.adjustDemoStock(p.id, -1) },
                                    enabled = stock > (store.cart[p.id] ?: 0),
                                    modifier = Modifier.size(48.dp),
                                ) { Icon(Icons.Outlined.Remove, "Giảm tồn của " + p.name) }
                                IconButton(
                                    onClick = { store.adjustDemoStock(p.id, 1) },
                                    enabled = stock < 999, modifier = Modifier.size(48.dp),
                                ) { Icon(Icons.Outlined.Add, "Tăng tồn của " + p.name) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OperationHistory(store: StaffDemoStore) {
    var detail by remember { mutableStateOf<CounterSale?>(null) }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().background(PharmaPalette.Emerald)
            .padding(horizontal = 19.dp, vertical = 19.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("LỊCH SỬ  /  POS DEMO", color = PharmaPalette.Lime,
                fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
            Text("Hóa đơn tại quầy", style = MaterialTheme.typography.headlineMedium,
                color = Color.White)
            Text(store.sales.size.toString() + " hóa đơn • " +
                formatVnd(store.sales.sumOf { it.totalVnd }),
                color = PharmaPalette.Mint, style = MaterialTheme.typography.bodySmall)
        }
        LazyColumn(
            Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (store.sales.isEmpty()) item("empty") {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Outlined.ReceiptLong, null, tint = PharmaPalette.Muted,
                        modifier = Modifier.size(46.dp))
                    Text("Chưa có hóa đơn", fontWeight = FontWeight.Bold)
                    Text("Hóa đơn demo sẽ hiện tại đây.", color = PharmaPalette.Muted)
                }
            }
            items(store.sales.reversed(), key = { it.reference }) { sale ->
                Surface(
                    onClick = { detail = sale }, color = PharmaPalette.Paper,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = PharmaPalette.Mint, shape = RoundedCornerShape(12.dp)) {
                            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.ReceiptLong, null, tint = PharmaPalette.Emerald)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(sale.reference, color = PharmaPalette.Emerald, fontWeight = FontWeight.Bold)
                            Text(sale.createdAt + " · " + sale.lines.sumOf { it.quantity } + " món",
                                style = MaterialTheme.typography.bodySmall, color = PharmaPalette.Muted)
                            Text(formatVnd(sale.totalVnd), fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Outlined.ChevronRight, null, tint = PharmaPalette.Muted)
                    }
                }
            }
        }
    }
    detail?.let { sale ->
        ModalBottomSheet(onDismissRequest = { detail = null }) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                Text("Chi tiết " + sale.reference, style = MaterialTheme.typography.titleLarge,
                    color = PharmaPalette.Emerald)
                Text(sale.createdAt + " · " + sale.cashierName, color = PharmaPalette.Muted)
                HorizontalDivider()
                sale.lines.forEach { line ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(line.name + " ×" + line.quantity, Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall)
                        Text(formatVnd(line.unitPriceVnd * line.quantity),
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                HorizontalDivider()
                Text("Tổng " + formatVnd(sale.totalVnd),
                    color = PharmaPalette.Emerald, fontWeight = FontWeight.Bold)
                Text(if (sale.payment == CounterPayment.CASH)
                    "Tiền mặt demo · thối " + formatVnd(sale.changeVnd)
                    else "Phương thức khác (demo)", color = PharmaPalette.Muted)
                Text("Dữ liệu minh họa, không có thanh toán thực tế.",
                    style = MaterialTheme.typography.bodySmall, color = PharmaPalette.Muted)
                Spacer(Modifier.height(25.dp))
            }
        }
    }
}
