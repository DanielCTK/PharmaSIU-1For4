package com.pharmasiu.mobile.ui.previews

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pharmasiu.mobile.data.repository.DemoCatalogRepository
import com.pharmasiu.mobile.ui.features.catalog.CatalogScreen
import com.pharmasiu.mobile.ui.theme.PharmaPalette
import com.pharmasiu.mobile.ui.theme.PharmaSIUTheme

@Preview(name = "01 · Danh mục Emerald 390dp", group = "PharmaSIU · catalog redesign",
    widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CatalogEmeraldPhonePreview() {
    PharmaSIUTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = PharmaPalette.Cream) {
            CatalogScreen(
                medicines = DemoCatalogRepository.medicines,
                onOpenProduct = {},
                onBack = {},
            )
        }
    }
}

@Preview(name = "02 · Danh mục màn nhỏ 320dp", group = "PharmaSIU · catalog redesign",
    widthDp = 320, heightDp = 700, showBackground = true)
@Composable
private fun CatalogEmeraldCompactPreview() {
    PharmaSIUTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = PharmaPalette.Cream) {
            CatalogScreen(
                medicines = DemoCatalogRepository.medicines,
                onOpenProduct = {},
                onBack = {},
                initialCategory = "Mỹ phẩm",
            )
        }
    }
}

@Preview(name = "03 · Danh mục chữ lớn", group = "PharmaSIU · catalog redesign",
    widthDp = 390, heightDp = 844, fontScale = 1.5f, showBackground = true)
@Composable
private fun CatalogLargeTextPreview() {
    PharmaSIUTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = PharmaPalette.Cream) {
            CatalogScreen(
                medicines = DemoCatalogRepository.medicines,
                onOpenProduct = {},
                onBack = {},
            )
        }
    }
}
