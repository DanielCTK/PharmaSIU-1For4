package com.pharmasiu.mobile.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.animation.ValueAnimator
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.pharmasiu.mobile.data.model.*
import com.pharmasiu.mobile.data.repository.DemoCatalogRepository
import com.pharmasiu.mobile.domain.CartRules
import com.pharmasiu.mobile.ui.features.account.*
import com.pharmasiu.mobile.ui.features.cart.CartScreen
import com.pharmasiu.mobile.ui.features.catalog.CatalogScreen
import com.pharmasiu.mobile.ui.features.checkout.CheckoutScreen
import com.pharmasiu.mobile.ui.features.community.*
import com.pharmasiu.mobile.ui.features.home.HomeScreen
import com.pharmasiu.mobile.ui.features.onboarding.OnboardingScreen
import com.pharmasiu.mobile.ui.features.orders.OrderStatusScreen
import com.pharmasiu.mobile.ui.features.prescription.PrescriptionScreen
import com.pharmasiu.mobile.ui.features.product.ProductDetailScreen
import com.pharmasiu.mobile.ui.theme.PharmaPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class DemoCredential(val profile: MockProfile, val password: String)
private data class OwnedRequest(val owner: String, val value: MockRequest)
private data class OwnedAddress(val owner: String, val value: MockAddress)

/**
 * Interactive offline prototype. Neither sign-in nor checkout reaches a
 * backend. Session, favorites, and requests deliberately stay in RAM.
 */
@Composable
fun PharmaSIUApp() {
    val context = LocalContext.current
    val introduction = remember(context) {
        context.getSharedPreferences("pharmasiu_mock_setup", android.content.Context.MODE_PRIVATE)
    }
    val navigation = remember { mutableStateListOf(if (introduction.getBoolean("onboarding_complete", false)) "home" else "onboarding") }
    val current = navigation.last()
    val accounts = remember {
        mutableStateMapOf("0900000000" to DemoCredential(
            MockProfile("Khách hàng mẫu", "0900000000", "demo@pharmasiu.example"), "demo1234",
        ))
    }
    var activePhone by remember { mutableStateOf<String?>(null) }
    val profile = activePhone?.let { accounts[it]?.profile }
    val owner = activePhone ?: "guest"
    val savedFavorites = remember { mutableMapOf<String, Set<String>>() }
    val favorites = remember { mutableStateMapOf<String, Boolean>() }
    val requests = remember { mutableStateListOf<OwnedRequest>() }
    val addresses = remember { mutableStateListOf<OwnedAddress>() }
    val quantities = remember { mutableStateMapOf<String, Int>() }
    var selectedPrescription by remember { mutableStateOf<String?>(null) }
    var sequence by rememberSaveable { mutableIntStateOf(0) }
    var demoReference by rememberSaveable { mutableStateOf("DEMO-0000") }
    var submitting by remember { mutableStateOf(false) }
    val products = DemoCatalogRepository.medicines
    // Only recompute derived collections when their underlying snapshot data changes.
    // Scrolling a lazy grid must not rebuild a cart or a history collection per frame.
    val cart by remember { derivedStateOf {
        quantities.mapNotNull { (id, count) ->
            DemoCatalogRepository.byId(id)?.takeIf { count > 0 }?.let { CartLine(it, count) }
        }
    } }
    val myRequests by remember(owner) { derivedStateOf { requests.filter { it.owner == owner }.map { it.value } } }
    val myAddresses by remember(owner) { derivedStateOf { addresses.filter { it.owner == owner }.map { it.value } } }
    val favoriteIds by remember { derivedStateOf { favorites.filterValues { it }.keys.toSet() } }
    val screenStates = rememberSaveableStateHolder()
    val animationMillis = remember { if (ValueAnimator.areAnimatorsEnabled()) 130 else 0 }

    fun saveFavorites() { savedFavorites[owner] = favorites.filterValues { it }.keys.toSet() }
    fun switchOwner(newPhone: String?) {
        saveFavorites()
        val oldGuest = if (owner == "guest") favoriteIds else emptySet()
        activePhone = newPhone
        favorites.clear()
        (savedFavorites[newPhone ?: "guest"].orEmpty() + oldGuest).forEach { favorites[it] = true }
    }
    fun go(route: String) {
        if (navigation.lastOrNull() == route) return
        if (route in listOf("home", "catalog", "favorites", "orders", "account")) {
            navigation.clear()
        }
        navigation.add(route)
    }
    fun back() {
        if (navigation.size > 1) navigation.removeAt(navigation.lastIndex)
        else if (current != "home") go("home")
    }
    fun home() {
        navigation.clear()
        navigation.add("home")
    }
    fun add(product: Medicine) {
        quantities[product.id] = ((quantities[product.id] ?: 0) + 1).coerceAtMost(CartRules.MAX_DEMO_QUANTITY)
    }
    fun flipFavorite(id: String) {
        if (favorites[id] == true) favorites.remove(id) else favorites[id] = true
        saveFavorites()
    }
    val choosePrescription = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri).orEmpty()
            selectedPrescription = if (mime.startsWith("image/") || mime == "application/pdf") {
                "Đã chọn tệp toa tại máy • chưa tải lên"
            } else null
        }
    }
    BackHandler(enabled = navigation.size > 1 || current != "home") { back() }
    val topTabs = remember { listOf(
        Triple("home", "Trang chủ", Icons.Outlined.Home),
        Triple("catalog", "Danh mục", Icons.Outlined.GridView),
        Triple("favorites", "Yêu thích", Icons.Outlined.FavoriteBorder),
        Triple("orders", "Yêu cầu", Icons.Outlined.ReceiptLong),
        Triple("account", "Tài khoản", Icons.Outlined.PersonOutline),
    ) }
    val onTab = current.takeIf { it in topTabs.map { item -> item.first } }
    Scaffold(
        containerColor = PharmaPalette.Cream,
        bottomBar = {
            if (onTab != null) {
                NavigationBar(containerColor = PharmaPalette.Paper) {
                    topTabs.forEach { (route, title, symbol) ->
                        NavigationBarItem(
                            selected = current == route,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PharmaPalette.Emerald,
                                selectedTextColor = PharmaPalette.Emerald,
                                indicatorColor = PharmaPalette.Lime,
                                unselectedIconColor = PharmaPalette.Muted,
                                unselectedTextColor = PharmaPalette.Muted,
                            ),
                            onClick = { go(route) },
                            icon = {
                                val count = when (route) { "favorites" -> favoriteIds.size; "orders" -> myRequests.size; else -> 0 }
                                if (count > 0 && route in listOf("favorites", "orders")) {
                                    BadgedBox(badge = { Badge(containerColor = PharmaPalette.Lime) { Text("$count") } }) {
                                        Icon(symbol, contentDescription = null)
                                    }
                                } else Icon(symbol, contentDescription = null)
                            },
                            label = { Text(title, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            alwaysShowLabel = true,
                        )
                    }
                }
            }
        },
    ) { contentPadding ->
        Surface(Modifier.fillMaxSize().padding(contentPadding), color = PharmaPalette.Cream) {
            Crossfade(
                targetState = current,
                animationSpec = tween(durationMillis = animationMillis),
                label = "screen-navigation",
                modifier = Modifier.fillMaxSize(),
            ) { route ->
                screenStates.SaveableStateProvider(route) {
            when {
                route == "onboarding" -> OnboardingScreen(onFinish = {
                    introduction.edit().putBoolean("onboarding_complete", true).apply()
                    home()
                })
                route == "home" -> HomeScreen(
                    medicines = products,
                    onOpenCatalog = { go("catalog") },
                    onOpenProduct = { go("product:$it") },
                    onOpenCart = { go("cart") },
                    onOpenCategory = { go("category:$it") },
                    favorites = favoriteIds,
                    onToggleFavorite = ::flipFavorite,
                )
                route == "catalog" || route.startsWith("category:") -> CatalogScreen(
                    medicines = products,
                    onOpenProduct = { go("product:$it") },
                    onBack = ::back,
                    initialCategory = if (route.startsWith("category:")) route.substringAfter(":") else "Tất cả",
                    favorites = favoriteIds,
                    onToggleFavorite = ::flipFavorite,
                )
                route.startsWith("product:") -> {
                    val product = DemoCatalogRepository.byId(route.substringAfter(":"))
                    if (product == null) {
                        Text("Không tìm thấy sản phẩm trong danh mục mẫu")
                    } else {
                        ProductDetailScreen(
                            medicine = product,
                            onAddToCart = { add(it); go("cart") },
                            onUploadPrescription = { add(product); go("prescription") },
                            onBack = ::back,
                            isFavorite = product.id in favoriteIds,
                            onToggleFavorite = { flipFavorite(product.id) },
                        )
                    }
                }
                route == "favorites" -> FavoritesScreen(
                    products = products.filter { it.id in favoriteIds },
                    onOpenProduct = { go("product:$it") },
                    onRemoveFavorite = ::flipFavorite,
                    onExplore = { go("catalog") },
                )
                route == "orders" -> RequestHistoryScreen(
                    requests = myRequests,
                    onOpenRequest = { go("order:$it") },
                    onExplore = { go("catalog") },
                )
                route.startsWith("order:") -> {
                    val request = myRequests.firstOrNull { it.reference == route.substringAfter(":") }
                    if (request == null) Text("Bản xem trước không tồn tại trong phiên này.")
                    else MockRequestDetailScreen(
                        request = request,
                        onBack = ::back,
                        onReorder = { lines ->
                            lines.forEach { line ->
                                quantities[line.medicine.id] = ((quantities[line.medicine.id] ?: 0) + line.quantity)
                                    .coerceAtMost(CartRules.MAX_DEMO_QUANTITY)
                            }
                            selectedPrescription = null
                            submitting = false
                            go("cart")
                        },
                        onCancel = { reference ->
                            val index = requests.indexOfFirst { it.value.reference == reference && it.owner == owner }
                            if (index >= 0) requests[index] = requests[index].copy(
                                value = requests[index].value.copy(status = MockRequestStatus.CANCELLED_LOCALLY),
                            )
                        },
                    )
                }
                route == "account" -> AccountScreen(
                    profile = profile,
                    favoriteCount = favoriteIds.size,
                    requestCount = myRequests.size,
                    onAuth = { go("auth") },
                    onEdit = { go("edit-profile") },
                    onFavorites = { go("favorites") },
                    onOrders = { go("orders") },
                    onAddresses = { go("addresses") },
                    onChangePassword = { go("change-password") },
                    onLogout = { switchOwner(null); go("account") },
                )
                route == "addresses" && profile != null -> AddressBookScreen(
                    addresses = myAddresses,
                    onBack = ::back,
                    onSave = { address ->
                        val index = addresses.indexOfFirst { it.owner == owner && it.value.id == address.id }
                        val normalized = address.copy(isDefault = if (myAddresses.isEmpty()) true else
                            (myAddresses.firstOrNull { it.id == address.id }?.isDefault ?: false))
                        if (index >= 0) addresses[index] = OwnedAddress(owner, normalized)
                        else addresses.add(OwnedAddress(owner, normalized))
                    },
                    onRemove = { id ->
                        val index = addresses.indexOfFirst { it.owner == owner && it.value.id == id }
                        if (index >= 0) {
                            val defaultWasRemoved = addresses[index].value.isDefault
                            addresses.removeAt(index)
                            if (defaultWasRemoved) {
                                val next = addresses.indexOfFirst { it.owner == owner }
                                if (next >= 0) addresses[next] = addresses[next].copy(
                                    value = addresses[next].value.copy(isDefault = true))
                            }
                        }
                    },
                    onSetDefault = { id ->
                        addresses.indices.filter { addresses[it].owner == owner }.forEach { index ->
                            addresses[index] = addresses[index].copy(
                                value = addresses[index].value.copy(isDefault = addresses[index].value.id == id))
                        }
                    },
                )
                route == "change-password" && profile != null -> ChangePasswordScreen(
                    onBack = ::back,
                    onChange = { currentPassword, newPassword ->
                        val existing = activePhone?.let { accounts[it] }
                        if (existing?.password != currentPassword) "Mật khẩu mẫu hiện tại không đúng."
                        else {
                            accounts[owner] = existing.copy(password = newPassword)
                            go("account")
                            null
                        }
                    },
                )
                route == "auth" -> AuthScreen(
                    onBack = ::back,
                    onLogin = { phone, password ->
                        val record = accounts[phone]
                        if (record?.password != password) "Sai thông tin tài khoản mẫu."
                        else { switchOwner(phone); go("account"); null }
                    },
                    onRegister = { name, phone, password ->
                        if (accounts.containsKey(phone)) "Số điện thoại đã thuộc tài khoản mẫu khác."
                        else {
                            accounts[phone] = DemoCredential(MockProfile(name, phone), password)
                            switchOwner(phone); go("account"); null
                        }
                    },
                    onReset = { phone, newPass ->
                        val old = accounts[phone]
                        if (old == null) "Không tìm thấy tài khoản demo này."
                        else { accounts[phone] = old.copy(password = newPass); null }
                    },
                )
                route == "edit-profile" && profile != null -> EditProfileScreen(
                    profile = profile,
                    onSave = { updated ->
                        val old = activePhone?.let { accounts[it] }
                        if (old == null) "Không có phiên demo để lưu."
                        else if (accounts.containsKey(updated.phone) && updated.phone != activePhone) {
                            "Số điện thoại này đã thuộc tài khoản demo khác."
                        } else {
                            val previousOwner = owner
                            savedFavorites[updated.phone] = savedFavorites[previousOwner].orEmpty()
                            requests.indices.filter { requests[it].owner == previousOwner }.forEach { index ->
                                requests[index] = requests[index].copy(owner = updated.phone)
                            }
                            addresses.indices.filter { addresses[it].owner == previousOwner }.forEach { index ->
                                addresses[index] = addresses[index].copy(owner = updated.phone)
                            }
                            accounts.remove(activePhone)
                            accounts[updated.phone] = old.copy(profile = updated)
                            activePhone = updated.phone
                            go("account")
                            null
                        }
                    },
                    onBack = ::back,
                )
                route == "cart" -> CartScreen(
                    items = cart,
                    onIncrement = { id -> quantities[id] = ((quantities[id] ?: 0) + 1).coerceAtMost(CartRules.MAX_DEMO_QUANTITY) },
                    onDecrement = { id ->
                        val left = (quantities[id] ?: 0) - 1
                        if (left <= 0) quantities.remove(id) else quantities[id] = left
                    },
                    onCheckout = {
                        if (cart.isNotEmpty()) {
                            submitting = false
                            if (CartRules.requiresPrescription(cart) && selectedPrescription == null) go("prescription")
                            else go("checkout")
                        }
                    },
                    onBack = ::back,
                )
                route == "prescription" -> PrescriptionScreen(
                    onBack = ::back,
                    onChooseFile = { choosePrescription.launch(arrayOf("image/*", "application/pdf")) },
                    selectedFileName = selectedPrescription,
                    onContinue = { if (selectedPrescription != null && cart.isNotEmpty()) go("checkout") },
                    onRemoveFile = { selectedPrescription = null },
                )
                route == "checkout" -> CheckoutScreen(
                    lines = cart,
                    onBack = ::back,
                    onPlaceRequest = { name, phone, pickup ->
                        if (!submitting && CartRules.canProceedToRequest(cart, selectedPrescription != null)) {
                            submitting = true
                            sequence += 1
                            demoReference = "DEMO-${sequence.toString().padStart(4, '0')}"
                            requests.add(OwnedRequest(owner, MockRequest(
                                reference = demoReference,
                                lines = cart.toList(),
                                customerName = name,
                                contactPhone = phone,
                                pickupPoint = pickup,
                                displayTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN")).format(Date()),
                            )))
                            quantities.clear()
                            selectedPrescription = null
                            go("receipt")
                        }
                    },
                    initialName = profile?.displayName.orEmpty(),
                    initialPhone = profile?.phone.orEmpty(),
                )
                route == "receipt" -> OrderStatusScreen(demoReference, onHome = {
                    go("orders")
                })
                else -> HomeScreen(products, { go("catalog") }, { go("product:$it") }, { go("cart") })
            }
                }
            }
        }
    }
}
