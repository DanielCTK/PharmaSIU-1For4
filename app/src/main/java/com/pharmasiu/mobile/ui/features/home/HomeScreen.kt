package com.pharmasiu.mobile.ui.features.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.ui.components.DemoDisclosure
import com.pharmasiu.mobile.ui.components.EmptyFeatureState
import com.pharmasiu.mobile.ui.components.FeaturedProductCard
import com.pharmasiu.mobile.ui.components.PharmaTopBar
import com.pharmasiu.mobile.ui.theme.PharmaPalette

/**
 * Single native lazy scroll for the landing page.
 * Full-width header/hero + two-column staggered showcase share the same scroll
 * to avoid nesting an infinite-height vertical grid inside a LazyColumn.
 */
@Composable
fun HomeScreen(
    medicines: List<Medicine>,
    onOpenCatalog: () -> Unit,
    onOpenProduct: (String) -> Unit,
    onOpenCart: () -> Unit,
    onOpenCategory: (String) -> Unit = { onOpenCatalog() },
    favorites: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {},
) {
    var selectedShelf by rememberSaveable { mutableStateOf("Dành cho bạn") }
    val shelfLabels = listOf("Dành cho bạn", "Mỹ phẩm", "Thực phẩm", "Thiết bị", "Chăm sóc")
    // Editorial order deliberately varies sector and image heights. Mock only:
    // we do not invent "sold counts", discounts or availability.
    val scrollState = rememberLazyStaggeredGridState()
    val featured = remember(medicines, selectedShelf) {
        val selected = if (selectedShelf == "Dành cho bạn") {
            val picks = listOf(
                "beauty-02", "food-01", "beauty-01", "food-03", "device-02",
                "beauty-06", "food-02", "care-01", "beauty-04", "supp-01",
                "beauty-03", "food-05", "device-03", "care-02", "supplies-02",
                "beauty-05", "food-04", "supp-02", "otc-01", "thermometer-01",
            )
            picks.mapNotNull { id -> medicines.find { it.id == id } }
        } else {
            medicines.filter {
                it.category == selectedShelf ||
                    (selectedShelf == "Thực phẩm" && it.category == "Thực phẩm bổ sung")
            }
        }
        selected.distinctBy { it.id }
    }
    val imageHeights = remember { listOf(204.dp, 242.dp, 226.dp, 184.dp, 220.dp, 252.dp) }
    LazyVerticalStaggeredGrid(
        state = scrollState,
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(bottom = 34.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalItemSpacing = 12.dp,
    ) {
        item(key = "home-top", contentType = "header", span = StaggeredGridItemSpan.FullLine) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                PharmaTopBar("PharmaSIU", trailing = {
                IconButton(onClick = onOpenCart) {
                    Icon(Icons.Outlined.ShoppingBag, "Mở giỏ hàng", tint = PharmaPalette.Emerald)
                }
            })
            }
        }
        item(key = "home-search", contentType = "search", span = StaggeredGridItemSpan.FullLine) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(min = 56.dp)
                    .background(PharmaPalette.Paper, RoundedCornerShape(17.dp))
                    .clickable(onClickLabel = "Tìm kiếm trong danh mục") { onOpenCatalog() }
                    .padding(horizontal = 15.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Outlined.Search, null, tint = PharmaPalette.Muted)
                Text("Tìm thuốc, mỹ phẩm, thực phẩm...", color = PharmaPalette.Muted,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.ArrowForward, null, tint = PharmaPalette.Emerald, modifier = Modifier.size(20.dp))
            }
        }
        item(key = "home-hero", contentType = "hero", span = StaggeredGridItemSpan.FullLine) {
            Box(
                modifier = Modifier.fillMaxWidth().height(228.dp)
                    .background(PharmaPalette.Emerald).clipToBounds(),
            ) {
                Canvas(Modifier.matchParentSize()) {
                    val ring = Color(0xFF4E8B75)
                    drawCircle(ring, radius = size.height * .61f,
                        center = Offset(size.width * .91f, size.height * .13f), style = Stroke(2.dp.toPx()))
                    drawCircle(ring, radius = size.height * .36f,
                        center = Offset(size.width * .90f, size.height * .16f), style = Stroke(1.dp.toPx()))
                    drawCircle(PharmaPalette.Lime, radius = 24.dp.toPx(),
                        center = Offset(size.width * .91f, size.height * .78f))
                }
                Column(Modifier.fillMaxSize().padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PHARMASIU  •  KHÁM PHÁ", color = PharmaPalette.Lime,
                        fontSize = 10.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold)
                    Text("Chăm sóc mỗi ngày,\nan tâm mỗi bước.", color = Color.White,
                        style = MaterialTheme.typography.headlineMedium)
                    Text("Từ chăm sóc da đến nhu yếu phẩm, tất cả trong một không gian.",
                        color = Color(0xFFDCEAE1), style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = onOpenCatalog,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PharmaPalette.Lime, contentColor = PharmaPalette.Forest),
                        shape = RoundedCornerShape(15.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    ) { Text("Khám phá ngay", fontWeight = FontWeight.Bold) }
                }
            }
        }
        item(key = "home-groups", contentType = "groups", span = StaggeredGridItemSpan.FullLine) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                listOf("Mỹ phẩm", "Thực phẩm", "Không kê đơn").forEach { label ->
                    Surface(
                        modifier = Modifier.weight(1f).heightIn(min = 55.dp)
                            .clickable(onClickLabel = "Xem " + label) { onOpenCategory(label) },
                        color = PharmaPalette.Mint,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 8.dp)) {
                            Text(label, color = PharmaPalette.Emerald,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2)
                        }
                    }
                }
            }
        }
        item(key = "home-featured-title", contentType = "section", span = StaggeredGridItemSpan.FullLine) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 9.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Sản phẩm nổi bật", style = MaterialTheme.typography.titleLarge,
                            color = PharmaPalette.Emerald)
                        Text("Gợi ý hình ảnh và giá để trải nghiệm", color = PharmaPalette.Muted,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = onOpenCatalog) { Text("Xem tất cả") }
                }
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    shelfLabels.forEach { shelf ->
                        FilterChip(
                            selected = selectedShelf == shelf,
                            onClick = { selectedShelf = shelf },
                            label = { Text(shelf, maxLines = 1) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PharmaPalette.Emerald,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
            }
        }
        if (featured.isEmpty()) {
            item(key = "home-no-products", contentType = "empty", span = StaggeredGridItemSpan.FullLine) {
                EmptyFeatureState("Chưa có sản phẩm mẫu", "Thử đổi nhóm hoặc mở toàn bộ danh mục.")
            }
        } else {
            items(
                count = featured.size,
                key = { featured[it].id },
                contentType = { "featured-card" },
            ) { position ->
                val product = featured[position]
                FeaturedProductCard(
                    medicine = product,
                    pictureHeight = imageHeights[position % imageHeights.size],
                    onClick = { onOpenProduct(product.id) },
                    isFavorite = product.id in favorites,
                    onToggleFavorite = { onToggleFavorite(product.id) },
                    modifier = Modifier.padding(
                        start = if (position % 2 == 0) 16.dp else 5.dp,
                        end = if (position % 2 == 0) 5.dp else 16.dp,
                    ),
                )
            }
        }
        item(key = "home-footer", contentType = "footer", span = StaggeredGridItemSpan.FullLine) {
            Column(Modifier.padding(horizontal = 16.dp).padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onOpenCatalog, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Outlined.GridView, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(9.dp))
                    Text("Xem toàn bộ danh mục")
                }
                DemoDisclosure()
            }
        }
    }
}
