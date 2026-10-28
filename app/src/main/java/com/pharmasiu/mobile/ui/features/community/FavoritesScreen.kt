package com.pharmasiu.mobile.ui.features.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pharmasiu.mobile.data.model.*
import com.pharmasiu.mobile.ui.components.*
import com.pharmasiu.mobile.ui.theme.PharmaPalette

@Composable
fun FavoritesScreen(
    products: List<Medicine>,
    onOpenProduct: (String) -> Unit,
    onRemoveFavorite: (String) -> Unit,
    onExplore: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        PharmaTopBar("Đã yêu thích", trailing = {
            Text("${products.size} món", color = PharmaPalette.Muted, style = MaterialTheme.typography.bodyMedium)
        })
        if (products.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.FavoriteBorder, null, tint = PharmaPalette.Emerald, modifier = Modifier.size(65.dp))
                Spacer(Modifier.height(17.dp))
                Text("Chưa có sản phẩm yêu thích", style = MaterialTheme.typography.titleLarge)
                Text("Chạm biểu tượng trái tim tại danh mục hoặc chi tiết để lưu sản phẩm.", color = PharmaPalette.Muted)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onExplore) { Text("Khám phá danh mục") }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text("Được lưu trong phiên ứng dụng hiện tại • dữ liệu mô phỏng", style = MaterialTheme.typography.bodyMedium, color = PharmaPalette.Muted) }
                items(products, key = { it.id }) { product ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProductTile(product, onClick = { onOpenProduct(product.id) }, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onRemoveFavorite(product.id) }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Outlined.Favorite, "Bỏ yêu thích ${product.name}", tint = PharmaPalette.Emerald)
                        }
                    }
                }
            }
        }
    }
}
