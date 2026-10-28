package com.pharmasiu.mobile.ui.features.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.ui.components.DemoDisclosure
import com.pharmasiu.mobile.ui.components.EmptyFeatureState
import com.pharmasiu.mobile.ui.components.PharmaTopBar
import com.pharmasiu.mobile.ui.components.ProductTile
import com.pharmasiu.mobile.ui.theme.PharmaPalette

@Composable
fun HomeScreen(
    medicines: List<Medicine>,
    onOpenCatalog: () -> Unit,
    onOpenProduct: (String) -> Unit,
    onOpenCart: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            PharmaTopBar("PharmaSIU", trailing = {
                IconButton(onClick = onOpenCart) {
                    Icon(Icons.Outlined.ShoppingBag, "Mở giỏ hàng", tint = PharmaPalette.Emerald)
                }
            })
        }
        item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)
                    .height(294.dp).background(PharmaPalette.Emerald, RoundedCornerShape(30.dp)),
            ) {
                Canvas(Modifier.matchParentSize()) {
                    val ring = Color(0xFF45806C)
                    drawCircle(ring, radius = size.height * .64f, center = Offset(size.width * .9f, size.height * .08f), style = Stroke(2.dp.toPx()))
                    drawCircle(ring, radius = size.height * .43f, center = Offset(size.width * .92f, size.height * .14f), style = Stroke(1.dp.toPx()))
                    drawCircle(PharmaPalette.Lime, radius = 28.dp.toPx(), center = Offset(size.width * .89f, size.height * .75f))
                }
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Text("NHÀ THUỐC PHARMASIU", fontSize = 11.sp, color = PharmaPalette.Lime, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Text("Chăm sóc mỗi ngày,\nan tâm mỗi bước.", color = Color.White, style = MaterialTheme.typography.headlineLarge)
                    Text("Khám phá sản phẩm và gửi yêu cầu đặt trước để nhận tại quầy.", color = Color(0xFFD9E8E0), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = onOpenCatalog,
                        colors = ButtonDefaults.buttonColors(containerColor = PharmaPalette.Lime, contentColor = PharmaPalette.Forest),
                        shape = RoundedCornerShape(15.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    ) {
                        Text("Khám phá sản phẩm", fontWeight = FontWeight.Bold)
                        Icon(Icons.Outlined.ArrowForward, null, modifier = Modifier.padding(start = 10.dp).size(18.dp))
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Icon(Icons.Outlined.GridView, null, tint = PharmaPalette.Emerald)
                    Text("Khám phá theo nhu cầu", style = MaterialTheme.typography.titleLarge)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Vật tư y tế", "Chăm sóc", "Thiết bị").forEach { title ->
                        Box(
                            modifier = Modifier.weight(1f).height(82.dp)
                                .background(PharmaPalette.Paper, RoundedCornerShape(18.dp))
                                .clickable(onClickLabel = "Xem nhóm $title", onClick = onOpenCatalog)
                                .padding(12.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.SpaceBetween) {
                                Icon(Icons.Outlined.MedicalServices, null, tint = PharmaPalette.Emerald, modifier = Modifier.size(21.dp))
                                Text(title, color = PharmaPalette.Ink, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Danh mục nổi bật", style = MaterialTheme.typography.titleLarge)
                    Text("Khám phá sản phẩm tại nhà thuốc", color = PharmaPalette.Muted, style = MaterialTheme.typography.bodyMedium)
                }
                IconButton(onClick = onOpenCatalog) { Icon(Icons.Outlined.ArrowForward, "Xem tất cả", tint = PharmaPalette.Emerald) }
            }
        }
        if (medicines.isEmpty()) {
            item {
                EmptyFeatureState("Danh mục đang cập nhật", "Nhà thuốc sẽ cập nhật thông tin sản phẩm để bạn tham khảo.")
            }
        } else {
            items(medicines.take(3), key = { it.id }) { medicine ->
                Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    ProductTile(medicine, onClick = { onOpenProduct(medicine.id) })
                    Text(
                        "Kiểm tra khi đặt trước",
                        color = PharmaPalette.Muted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 13.dp),
                    )
                }
            }
        }
        item { DemoDisclosure(Modifier.padding(horizontal = 18.dp)) }
    }
}
