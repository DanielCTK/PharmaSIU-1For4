package com.pharmasiu.mobile.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pharmasiu.mobile.data.model.CartLine
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.data.repository.DemoCatalogRepository
import com.pharmasiu.mobile.domain.CartRules
import com.pharmasiu.mobile.ui.features.cart.CartScreen
import com.pharmasiu.mobile.ui.features.catalog.CatalogScreen
import com.pharmasiu.mobile.ui.features.checkout.CheckoutScreen
import com.pharmasiu.mobile.ui.features.home.HomeScreen
import com.pharmasiu.mobile.ui.features.orders.OrderStatusScreen
import com.pharmasiu.mobile.ui.features.prescription.PrescriptionScreen
import com.pharmasiu.mobile.ui.features.product.ProductDetailScreen
import com.pharmasiu.mobile.ui.theme.PharmaPalette

/**
 * UI-only navigation and transient demo cart.
 *
 * There is no backend. Never display these local states as a confirmed order,
 * held stock or an approved prescription.
 */
@Composable
fun PharmaSIUApp() {
    val navigation = remember { mutableStateListOf("home") }
    val current = navigation.last()
    val quantities = remember { mutableStateMapOf<String, Int>() }
    var selectedPrescription by remember { mutableStateOf<String?>(null) }
    var demoCounter by rememberSaveable { mutableIntStateOf(0) }
    var demoReference by rememberSaveable { mutableStateOf("DEMO-0000") }
    val medicines = DemoCatalogRepository.medicines
    val cart = quantities.mapNotNull { (id, quantity) ->
        val medicine = DemoCatalogRepository.byId(id)
        if (medicine != null && quantity > 0) CartLine(medicine, quantity) else null
    }
    val cartCount = cart.sumOf { it.quantity }

    fun go(route: String) {
        if (navigation.lastOrNull() == route) return
        if (route in listOf("home", "catalog", "cart") && route in navigation) {
            val index = navigation.indexOf(route)
            while (navigation.size > index + 1) navigation.removeAt(navigation.lastIndex)
        } else {
            navigation.add(route)
        }
    }

    fun back() {
        if (navigation.size > 1) navigation.removeAt(navigation.lastIndex)
    }

    fun home() {
        navigation.clear()
        navigation.add("home")
    }

    fun add(medicine: Medicine) {
        val old = quantities[medicine.id] ?: 0
        quantities[medicine.id] = (old + 1).coerceAtMost(CartRules.MAX_DEMO_QUANTITY)
    }

    val choosePrescription = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        // Keep only transient selection state. No upload, copy or persistence occurs.
        if (uri != null) selectedPrescription = "Đã chọn ảnh trong thiết bị"
    }

    BackHandler(enabled = navigation.size > 1) {
        if (current == "receipt") home() else back()
    }

    val showBottomBar = current in listOf("home", "catalog", "cart")
    Scaffold(
        containerColor = PharmaPalette.Cream,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = PharmaPalette.Paper) {
                    listOf(
                        Triple("home", "Trang chủ", Icons.Outlined.Home),
                        Triple("catalog", "Danh mục", Icons.Outlined.GridView),
                        Triple("cart", "Giỏ hàng", Icons.Outlined.ShoppingBag),
                    ).forEach { (route, title, icon) ->
                        NavigationBarItem(
                            selected = current == route,
                            onClick = { go(route) },
                            icon = {
                                if (route == "cart" && cartCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = PharmaPalette.Lime,
                                                contentColor = PharmaPalette.Forest,
                                            ) { Text(if (cartCount > 99) "99+" else "$cartCount") }
                                        },
                                    ) { Icon(icon, contentDescription = null) }
                                } else {
                                    Icon(icon, contentDescription = null)
                                }
                            },
                            label = { Text(title) },
                            alwaysShowLabel = true,
                        )
                    }
                }
            }
        },
    ) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(padding),
            color = PharmaPalette.Cream,
        ) {
            when {
                current == "home" -> HomeScreen(
                    medicines = medicines,
                    onOpenCatalog = { go("catalog") },
                    onOpenProduct = { go("product:$it") },
                    onOpenCart = { go("cart") },
                )
                current == "catalog" -> CatalogScreen(
                    medicines = medicines,
                    onOpenProduct = { go("product:$it") },
                    onBack = ::back,
                )
                current.startsWith("product:") -> {
                    val medicine = DemoCatalogRepository.byId(current.substringAfter(":"))
                    if (medicine != null) ProductDetailScreen(
                        medicine = medicine,
                        onAddToCart = { add(it); go("cart") },
                        onUploadPrescription = { add(medicine); go("prescription") },
                        onBack = ::back,
                    ) else Text("Không tìm thấy sản phẩm")
                }
                current == "cart" -> CartScreen(
                    items = cart,
                    onIncrement = { id -> quantities[id] = ((quantities[id] ?: 0) + 1).coerceAtMost(CartRules.MAX_DEMO_QUANTITY) },
                    onDecrement = { id ->
                        val next = (quantities[id] ?: 0) - 1
                        if (next <= 0) quantities.remove(id) else quantities[id] = next
                    },
                    onCheckout = {
                        if (cart.isNotEmpty()) {
                            if (CartRules.requiresPrescription(cart) && selectedPrescription == null) go("prescription")
                            else go("checkout")
                        }
                    },
                    onBack = ::back,
                )
                current == "prescription" -> PrescriptionScreen(
                    onBack = ::back,
                    onChooseFile = { choosePrescription.launch("image/*") },
                    selectedFileName = selectedPrescription,
                    onContinue = {
                        if (selectedPrescription != null && cart.isNotEmpty()) go("checkout")
                        else if (cart.isEmpty()) go("catalog")
                    },
                )
                current == "checkout" -> CheckoutScreen(
                    lines = cart,
                    onBack = ::back,
                    onPlaceRequest = { _, _ ->
                        if (CartRules.canProceedToRequest(cart, selectedPrescription != null)) {
                            demoCounter += 1
                            demoReference = "DEMO-${demoCounter.toString().padStart(4, '0')}"
                            quantities.clear()
                            selectedPrescription = null
                            go("receipt")
                        }
                    },
                )
                current == "receipt" -> OrderStatusScreen(
                    reference = demoReference,
                    onHome = ::home,
                )
                else -> HomeScreen(medicines, { go("catalog") }, { go("product:$it") }, { go("cart") })
            }
        }
    }
}
