package com.pharmasiu.mobile.ui.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pharmasiu.mobile.data.repository.DemoCatalogRepository
import com.pharmasiu.mobile.ui.components.FeaturedProductCard
import com.pharmasiu.mobile.ui.theme.PharmaPalette
import com.pharmasiu.mobile.ui.theme.PharmaSIUTheme

/** Preview starts at the masonry section itself; no scroll past the hero needed. */
@Preview(name = "Lưới nổi bật • 390dp", group = "PharmaSIU • Product Masonry", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun FeaturedShowcasePhone() {
    PharmaSIUTheme { FeaturedShowcaseContent() }
}

@Preview(name = "Lưới nổi bật • máy nhỏ", group = "PharmaSIU • Product Masonry", widthDp = 320, heightDp = 700, showBackground = true)
@Composable
private fun FeaturedShowcaseSmall() {
    PharmaSIUTheme { FeaturedShowcaseContent() }
}

@Composable
private fun FeaturedShowcaseContent() {
    val ids = listOf("beauty-02", "food-01", "beauty-01", "food-03", "device-02", "beauty-06", "food-05", "supp-01")
    val products = ids.mapNotNull(DemoCatalogRepository::byId)
    val heart = remember { mutableStateMapOf<String, Boolean>() }
    val heights = listOf(204.dp, 242.dp, 226.dp, 184.dp, 220.dp, 252.dp)
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 16.dp, bottom = 26.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalItemSpacing = 12.dp,
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Column(Modifier.padding(bottom = 8.dp)) {
                Text("Sản phẩm nổi bật", style = MaterialTheme.typography.titleLarge, color = PharmaPalette.Emerald)
                Text("Giá và hình ảnh minh họa • nhấn trái tim để thử lưu", style = MaterialTheme.typography.bodySmall, color = PharmaPalette.Muted)
            }
        }
        items(products, key = { it.id }) { product ->
            val position = products.indexOfFirst { it.id == product.id }
            FeaturedProductCard(
                medicine = product,
                pictureHeight = heights[position % heights.size],
                onClick = {},
                isFavorite = heart[product.id] == true,
                onToggleFavorite = { heart[product.id] = heart[product.id] != true },
            )
        }
    }
}
