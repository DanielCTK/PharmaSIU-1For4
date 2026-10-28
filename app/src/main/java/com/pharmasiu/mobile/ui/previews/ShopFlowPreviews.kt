package com.pharmasiu.mobile.ui.previews

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pharmasiu.mobile.data.model.*
import com.pharmasiu.mobile.data.repository.DemoCatalogRepository
import com.pharmasiu.mobile.ui.features.account.*
import com.pharmasiu.mobile.ui.features.catalog.CatalogScreen
import com.pharmasiu.mobile.ui.features.community.*
import com.pharmasiu.mobile.ui.features.onboarding.OnboardingScreen
import com.pharmasiu.mobile.ui.features.product.ProductDetailScreen
import com.pharmasiu.mobile.ui.theme.PharmaSIUTheme
import com.pharmasiu.mobile.ui.theme.PharmaPalette

private val demoItems get() = DemoCatalogRepository.medicines
private val demoProfile = MockProfile("Nguyễn Minh An", "0900000000", "demo@pharmasiu.example")
private val sampleRequest get() = MockRequest(
    reference = "DEMO-0012",
    lines = listOf(CartLine(demoItems.first { it.category == "Mỹ phẩm" }, 2)),
    customerName = demoProfile.displayName,
    contactPhone = demoProfile.phone,
    pickupPoint = "Điểm nhận mẫu A",
    displayTime = "28/10/2026 09:00",
)

@Composable
private fun MockPreviewShell(content: @Composable () -> Unit) {
    PharmaSIUTheme {
        Surface(Modifier.fillMaxSize(), color = PharmaPalette.Cream) { content() }
    }
}

@Preview(name = "01 • Chào mừng", group = "PharmaSIU • extended flows", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun OnboardingMockPreview() = MockPreviewShell { OnboardingScreen(onFinish = {}) }

@Preview(name = "02 • Đăng nhập demo", group = "PharmaSIU • extended flows", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun SignInMockPreview() = MockPreviewShell {
    AuthScreen(onBack = {}, onLogin = { _, _ -> null }, onRegister = { _, _, _ -> null }, onReset = { _, _ -> null })
}

@Preview(name = "03 • Tài khoản khách", group = "PharmaSIU • extended flows", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun GuestAccountPreview() = MockPreviewShell {
    AccountScreen(profile = null, favoriteCount = 0, requestCount = 0,
        onAuth = {}, onEdit = {}, onFavorites = {}, onOrders = {}, onLogout = {})
}

@Preview(name = "04 • Hồ sơ đăng nhập", group = "PharmaSIU • extended flows", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun SignedAccountPreview() = MockPreviewShell {
    AccountScreen(profile = demoProfile, favoriteCount = 3, requestCount = 2,
        onAuth = {}, onEdit = {}, onFavorites = {}, onOrders = {}, onLogout = {})
}

@Preview(name = "05 • Chỉnh sửa thông tin", group = "PharmaSIU • extended flows", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun EditAccountPreview() = MockPreviewShell {
    EditProfileScreen(demoProfile, onSave = { null }, onBack = {})
}

@Preview(name = "06 • Mỹ phẩm", group = "PharmaSIU • extended flows", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CosmeticsCategoryPreview() = MockPreviewShell {
    CatalogScreen(demoItems, onOpenProduct = {}, onBack = {}, initialCategory = "Mỹ phẩm")
}

@Preview(name = "07 • Thực phẩm", group = "PharmaSIU • extended flows", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun FoodCategoryPreview() = MockPreviewShell {
    CatalogScreen(demoItems, onOpenProduct = {}, onBack = {}, initialCategory = "Thực phẩm")
}

@Preview(name = "08 • Chi tiết thực phẩm bổ sung", group = "PharmaSIU • extended flows", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun SupplementPreview() = MockPreviewShell {
    ProductDetailScreen(demoItems.first { it.category == "Thực phẩm bổ sung" }, onAddToCart = {},
        onUploadPrescription = {}, onBack = {}, isFavorite = true)
}

@Preview(name = "09 • Sản phẩm yêu thích", group = "PharmaSIU • extended flows", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun SavedFavoritesPreview() = MockPreviewShell {
    FavoritesScreen(demoItems.filter { it.category in listOf("Mỹ phẩm", "Thực phẩm") }.take(3),
        onOpenProduct = {}, onRemoveFavorite = {}, onExplore = {})
}

@Preview(name = "10 • Lịch sử yêu cầu mẫu", group = "PharmaSIU • extended flows", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun MockOrdersPreview() = MockPreviewShell {
    RequestHistoryScreen(listOf(sampleRequest), onOpenRequest = {}, onExplore = {})
}

@Preview(name = "11 • Chi tiết yêu cầu mẫu", group = "PharmaSIU • extended flows", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun MockRequestPreview() = MockPreviewShell {
    MockRequestDetailScreen(sampleRequest, onBack = {}, onReorder = {}, onCancel = {})
}

@Preview(name = "12 • Sổ địa chỉ", group = "PharmaSIU • extended flows", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun AddressBookPreview() = MockPreviewShell {
    AddressBookScreen(
        addresses = listOf(MockAddress("1", "Nhà", "Nguyễn Minh An", "0900000000", "Địa chỉ minh họa, không phải nơi nhận hàng", true)),
        onBack = {}, onSave = {}, onRemove = {}, onSetDefault = {},
    )
}
