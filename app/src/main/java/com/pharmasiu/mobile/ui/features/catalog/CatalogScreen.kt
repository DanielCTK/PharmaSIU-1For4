package com.pharmasiu.mobile.ui.features.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.domain.CatalogSearchRules
import com.pharmasiu.mobile.ui.components.DemoDisclosure
import com.pharmasiu.mobile.ui.components.EmptyFeatureState
import com.pharmasiu.mobile.ui.components.FeatureColors
import com.pharmasiu.mobile.ui.components.FeatureHeader
import com.pharmasiu.mobile.ui.components.MedicineCard

/**
 * Catalog layout: one continuous vertical scroll instead of a large fixed
 * heading + tiny independently scrollable product grid.
 */
@Composable
fun CatalogScreen(
    medicines: List<Medicine>,
    onOpenProduct: (String) -> Unit,
    onBack: () -> Unit,
    initialCategory: String = "Tất cả",
    favorites: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {},
) {
    var search by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable(initialCategory) { mutableStateOf(initialCategory) }
    var selectedSort by rememberSaveable { mutableStateOf("Đề xuất") }
    var sortExpanded by remember { mutableStateOf(false) }
    val filters = remember(medicines) {
        (
            listOf("Tất cả", "Mỹ phẩm", "Thực phẩm", "Không kê đơn", "Kê đơn") +
                medicines.map { it.category }
        ).distinct().filter { group ->
            group == "Tất cả" || group == "Kê đơn" || medicines.any { it.category == group }
        }
    }
    val filtered = remember(medicines, search, selectedFilter, selectedSort) {
        CatalogSearchRules.filter(medicines, search, selectedFilter, selectedSort)
    }
    val groupCount = remember(medicines) { medicines.map { it.category }.distinct().size }
    val gridState = rememberLazyGridState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier.fillMaxSize().background(FeatureColors.canvas),
        contentPadding = PaddingValues(bottom = 34.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "catalog-top", contentType = "header", span = { GridItemSpan(maxLineSpan) }) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                FeatureHeader(title = "Danh mục", onBack = onBack)
            }
        }
        item(key = "catalog-hero", contentType = "hero", span = { GridItemSpan(maxLineSpan) }) {
            CatalogIntroCard(productCount = medicines.size, groupCount = groupCount)
        }
        item(key = "catalog-search", contentType = "search", span = { GridItemSpan(maxLineSpan) }) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(17.dp),
                    label = { Text("Tìm sản phẩm") },
                    placeholder = { Text("Tên, danh mục hoặc nhu cầu...", maxLines = 1) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        if (search.isNotBlank()) {
                            IconButton(onClick = { search = "" }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Xóa từ khóa")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = FeatureColors.emerald,
                        unfocusedBorderColor = FeatureColors.line,
                    ),
                )
                Text(
                    "Thông tin và giá chỉ để tham khảo trong bản mẫu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FeatureColors.muted,
                    modifier = Modifier.padding(start = 3.dp),
                )
            }
        }
        item(key = "catalog-categories", contentType = "filters", span = { GridItemSpan(maxLineSpan) }) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                filters.forEach { option ->
                    FilterChip(
                        selected = option == selectedFilter,
                        onClick = { selectedFilter = option },
                        modifier = Modifier.heightIn(min = 48.dp),
                        label = {
                            Text(option, maxLines = 1, fontSize = 12.sp,
                                fontWeight = if (option == selectedFilter) FontWeight.Bold else FontWeight.Medium)
                        },
                        shape = RoundedCornerShape(17.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White,
                            selectedContainerColor = FeatureColors.emerald,
                            selectedLabelColor = Color.White,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = option == selectedFilter,
                            borderColor = FeatureColors.line,
                            selectedBorderColor = FeatureColors.emerald,
                        ),
                    )
                }
            }
        }
        item(key = "catalog-toolbar", contentType = "toolbar", span = { GridItemSpan(maxLineSpan) }) {
            CatalogResultsToolbar(
                resultCount = filtered.size,
                sort = selectedSort,
                expanded = sortExpanded,
                onExpandChange = { sortExpanded = it },
                onSortChange = { selectedSort = it },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        if (filtered.isEmpty()) {
            item(key = "catalog-empty", contentType = "empty", span = { GridItemSpan(maxLineSpan) }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    EmptyFeatureState(
                        title = if (medicines.isEmpty()) "Danh mục đang cập nhật" else "Không tìm thấy sản phẩm",
                        detail = if (medicines.isEmpty()) "Danh mục mẫu sẽ được bổ sung."
                            else "Thử tìm tên khác hoặc chuyển sang nhóm Tất cả.",
                    )
                    if (medicines.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { search = ""; selectedFilter = "Tất cả"; selectedSort = "Đề xuất" },
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) { Text("Xóa bộ lọc") }
                    }
                }
            }
        } else {
            itemsIndexed(filtered, key = { _, product -> product.id }, contentType = { _, _ -> "product" }) { index, product ->
                MedicineCard(
                    medicine = product,
                    onOpenProduct = onOpenProduct,
                    modifier = Modifier.padding(
                        start = if (index % 2 == 0) 16.dp else 5.dp,
                        end = if (index % 2 == 0) 5.dp else 16.dp,
                    ),
                    isFavorite = product.id in favorites,
                    onToggleFavorite = { onToggleFavorite(product.id) },
                )
            }
        }
        item(key = "catalog-disclosure", contentType = "footer", span = { GridItemSpan(maxLineSpan) }) {
            DemoDisclosure(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 6.dp))
        }
    }
}
