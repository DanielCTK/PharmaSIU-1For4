package com.pharmasiu.mobile.ui.previews

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pharmasiu.mobile.data.model.CartLine
import com.pharmasiu.mobile.data.repository.DemoCatalogRepository
import com.pharmasiu.mobile.ui.features.cart.CartScreen
import com.pharmasiu.mobile.ui.features.catalog.CatalogScreen
import com.pharmasiu.mobile.ui.features.checkout.CheckoutScreen
import com.pharmasiu.mobile.ui.features.home.HomeScreen
import com.pharmasiu.mobile.ui.features.orders.OrderStatusScreen
import com.pharmasiu.mobile.ui.features.prescription.PrescriptionScreen
import com.pharmasiu.mobile.ui.features.product.ProductDetailScreen
import com.pharmasiu.mobile.ui.theme.PharmaPalette
import com.pharmasiu.mobile.ui.theme.PharmaSIUTheme

/**
 * One-stop Android Studio Compose Preview gallery.
 *
 * Open this file, then select Split or Design and Build & Refresh.
 * No emulator, API connection, camera, account or real prescription is required.
 * All product names and prices below come from the DEMO catalog.
 *
 * These are visual previews; callbacks are intentionally no-ops. To inspect
 * interaction with forms/search, choose Interactive Mode in Android Studio.
 */
private val demoCatalog get() = DemoCatalogRepository.medicines

private val demoCart get() = listOf(
    CartLine(demoCatalog[0], 2),
    CartLine(demoCatalog[2], 1),
)

private val demoRxCart get() = listOf(
    CartLine(demoCatalog.first { it.requiresPrescription }, 1),
)

@Composable
private fun PreviewShell(content: @Composable () -> Unit) {
    PharmaSIUTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = PharmaPalette.Cream) {
            content()
        }
    }
}

@Preview(name = "01 • Trang chủ", group = "PharmaSIU • 390dp", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun HomePhonePreview() = PreviewShell {
    HomeScreen(
        medicines = demoCatalog,
        onOpenCatalog = {},
        onOpenProduct = {},
        onOpenCart = {},
    )
}

@Preview(name = "02 • Danh mục & tìm kiếm", group = "PharmaSIU • 390dp", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CatalogPhonePreview() = PreviewShell {
    CatalogScreen(
        medicines = demoCatalog,
        onOpenProduct = {},
        onBack = {},
    )
}

@Preview(name = "03 • Chi tiết sản phẩm", group = "PharmaSIU • 390dp", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun ProductPhonePreview() = PreviewShell {
    ProductDetailScreen(
        medicine = demoCatalog.first(),
        onAddToCart = {},
        onUploadPrescription = {},
        onBack = {},
    )
}

@Preview(name = "04 • Giỏ hàng", group = "PharmaSIU • 390dp", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CartPhonePreview() = PreviewShell {
    CartScreen(
        items = demoCart,
        onIncrement = {},
        onDecrement = {},
        onCheckout = {},
        onBack = {},
    )
}

@Preview(name = "05 • Toa thuốc chưa chọn", group = "PharmaSIU • 390dp", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun PrescriptionPhonePreview() = PreviewShell {
    PrescriptionScreen(
        onBack = {},
        onChooseFile = {},
        selectedFileName = null,
        onContinue = {},
    )
}

@Preview(name = "06 • Đặt trước có thuốc Rx", group = "PharmaSIU • 390dp", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CheckoutPhonePreview() = PreviewShell {
    CheckoutScreen(
        lines = demoRxCart,
        onBack = {},
        onPlaceRequest = { _, _ -> },
    )
}

@Preview(name = "07 • Bản xem trước yêu cầu", group = "PharmaSIU • 390dp", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun OrderStatusPhonePreview() = PreviewShell {
    OrderStatusScreen(reference = "DEMO-0001", onHome = {})
}

@Preview(name = "08 • Thuốc kê đơn", group = "PharmaSIU • 390dp", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun PrescriptionProductPhonePreview() = PreviewShell {
    ProductDetailScreen(
        medicine = demoCatalog.first { it.requiresPrescription },
        onAddToCart = {},
        onUploadPrescription = {},
        onBack = {},
    )
}

@Preview(name = "09 • Giỏ hàng trống", group = "PharmaSIU • 390dp", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun EmptyCartPhonePreview() = PreviewShell {
    CartScreen(
        items = emptyList(),
        onIncrement = {},
        onDecrement = {},
        onCheckout = {},
        onBack = {},
    )
}

@Preview(name = "10 • Trang chủ máy nhỏ", group = "PharmaSIU • 320dp", widthDp = 320, heightDp = 700, showBackground = true)
@Composable
private fun CompactHomePhonePreview() = PreviewShell {
    HomeScreen(
        medicines = demoCatalog,
        onOpenCatalog = {},
        onOpenProduct = {},
        onOpenCart = {},
    )
}
