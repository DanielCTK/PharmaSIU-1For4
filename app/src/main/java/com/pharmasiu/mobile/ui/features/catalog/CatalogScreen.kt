package com.pharmasiu.mobile.ui.features.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharmasiu.mobile.data.model.*
import com.pharmasiu.mobile.ui.components.EmptyFeatureState
import com.pharmasiu.mobile.ui.components.FeatureColors
import com.pharmasiu.mobile.ui.components.FeatureHeader
import com.pharmasiu.mobile.ui.components.MedicineCard
import com.pharmasiu.mobile.ui.components.asFeatureView

@Composable
fun CatalogScreen(
    medicines: List<Medicine>,
    onOpenProduct: (String) -> Unit,
    onBack: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tất cả") }
    val filters = listOf("Tất cả", "Không kê đơn", "Kê đơn")
    val filtered = remember(medicines, search, selectedFilter) {
        medicines.filter { medicine ->
            val view = medicine.asFeatureView()
            val matchesQuery = search.isBlank() || listOf(view.name, view.subtitle, view.category)
                .any { it.contains(search.trim(), ignoreCase = true) }
            val matchesFilter = when (selectedFilter) {
                "Kê đơn" -> view.requiresPrescription
                "Không kê đơn" -> !view.requiresPrescription
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    Column(Modifier.fillMaxSize().background(FeatureColors.canvas)) {
        FeatureHeader(title = "Danh mục", onBack = onBack) {
            Box(Modifier.clip(CircleShape).background(FeatureColors.mint).padding(horizontal = 12.dp, vertical = 9.dp)) {
                Text("${medicines.size} sản phẩm", fontSize = 11.sp, color = FeatureColors.emerald, fontWeight = FontWeight.Bold)
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text("Tìm điều bạn cần.", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = FeatureColors.emerald)
            Spacer(Modifier.height(5.dp))
            Text("Mọi sản phẩm đều được kiểm tra khi đặt trước.", fontSize = 12.sp, color = FeatureColors.muted)
            Spacer(Modifier.height(19.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(17.dp))
                    .background(Color.White).border(1.dp, FeatureColors.line, RoundedCornerShape(17.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⌕", fontSize = 27.sp, color = FeatureColors.emerald)
                Spacer(Modifier.width(9.dp))
                BasicTextField(
                    value = search,
                    onValueChange = { search = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    textStyle = TextStyle(fontSize = 14.sp, color = FeatureColors.text),
                    modifier = Modifier.weight(1f).semantics { contentDescription = "Tìm sản phẩm theo tên hoặc nhóm" },
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (search.isEmpty()) Text("Tên hoặc nhóm sản phẩm...", color = FeatureColors.muted, fontSize = 13.sp)
                            inner()
                        }
                    }
                )
                if (search.isNotEmpty()) {
                    Box(Modifier.size(48.dp).clip(CircleShape)
                        .clickable(role = Role.Button, onClickLabel = "Xóa từ khóa tìm kiếm") { search = "" }, contentAlignment = Alignment.Center) {
                        Text("×", color = FeatureColors.muted, fontSize = 23.sp)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            filters.forEach { filter ->
                val selected = selectedFilter == filter
                Box(
                    modifier = Modifier.height(48.dp).clip(CircleShape)
                        .background(if (selected) FeatureColors.emerald else Color.White)
                        .border(1.dp, if (selected) FeatureColors.emerald else FeatureColors.line, CircleShape)
                        .clickable(role = Role.Button, onClickLabel = "Lọc: $filter") { selectedFilter = filter }
                        .padding(horizontal = 17.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(filter, fontSize = 12.sp, color = if (selected) Color.White else FeatureColors.text, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 21.dp, end = 21.dp, bottom = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("SẢN PHẨM", modifier = Modifier.weight(1f), color = FeatureColors.emerald, letterSpacing = 1.7.sp, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Text("${filtered.size} kết quả", color = FeatureColors.muted, fontSize = 11.sp)
        }
        if (filtered.isEmpty()) {
            EmptyFeatureState(
                if (medicines.isEmpty()) "Danh mục đang cập nhật" else "Chưa tìm thấy sản phẩm",
                if (medicines.isEmpty()) "Các sản phẩm sẽ được cập nhật trong danh mục." else "Hãy thử từ khóa hoặc bộ lọc khác.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 34.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.asFeatureView().id }) { medicine ->
                    MedicineCard(medicine, onOpenProduct)
                }
            }
        }
    }
}
