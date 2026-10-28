package com.pharmasiu.mobile.ui.features.cart

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharmasiu.mobile.data.model.*
import com.pharmasiu.mobile.domain.CartRules
import com.pharmasiu.mobile.ui.features.home.EmptyFeatureState
import com.pharmasiu.mobile.ui.features.home.FeatureButton
import com.pharmasiu.mobile.ui.features.home.FeatureColors
import com.pharmasiu.mobile.ui.features.home.FeatureHeader
import com.pharmasiu.mobile.ui.features.home.MedicineArt
import com.pharmasiu.mobile.ui.features.home.toVnd

@Composable
fun CartScreen(
    items: List<CartLine>,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onCheckout: () -> Unit,
    onBack: () -> Unit
) {
    val rxIncluded = CartRules.requiresPrescription(items)
    val unpricedCount = items.count { it.medicine.priceVnd <= 0L }
    val pricedSubtotal = items.sumOf { line ->
        if (line.medicine.priceVnd > 0L) line.medicine.priceVnd * line.quantity else 0L
    }
    Column(Modifier.fillMaxSize().background(FeatureColors.canvas)) {
        FeatureHeader(title = "Giỏ yêu cầu", onBack = onBack)
        if (items.isEmpty()) {
            Column(
                Modifier.weight(1f).fillMaxWidth().padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                EmptyFeatureState(
                    title = "Giỏ của bạn đang trống",
                    detail = "Chọn sản phẩm để gửi yêu cầu đặt trước và nhận tại nhà thuốc."
                )
                FeatureButton("Khám phá sản phẩm  ↗", onBack, Modifier.fillMaxWidth().padding(horizontal = 38.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 7.dp, bottom = 20.dp)
            ) {
                item {
                    Column(Modifier.padding(horizontal = 4.dp, vertical = 3.dp)) {
                        Text("Sắp xếp yêu cầu của bạn", color = FeatureColors.emerald, fontSize = 24.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        Text("${items.sumOf { it.quantity }} sản phẩm · Giá và tồn kho sẽ được kiểm tra trước khi xác nhận.", color = FeatureColors.muted, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
                items(items, key = { it.medicine.id }) { line ->
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .border(1.dp, FeatureColors.line, RoundedCornerShape(22.dp))
                            .padding(13.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            MedicineArt(Modifier.size(96.dp), seed = line.medicine.id, compact = true)
                            Column(Modifier.weight(1f)) {
                                Text(line.medicine.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FeatureColors.deep, maxLines = 2, lineHeight = 19.sp, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(5.dp))
                                Text(line.medicine.subtitle, color = FeatureColors.muted, fontSize = 11.sp, maxLines = 2, lineHeight = 15.sp, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(7.dp))
                                Text(if (line.medicine.priceVnd > 0) line.medicine.priceVnd.toVnd() else "Giá khi xác nhận", color = FeatureColors.emerald, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = FeatureColors.line)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Kiểm tra khi đặt trước", color = FeatureColors.muted, fontSize = 11.sp)
                                if (line.medicine.requiresPrescription) {
                                    Spacer(Modifier.height(3.dp))
                                    Text("Cần dược sĩ duyệt toa", color = FeatureColors.rx, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.size(44.dp).clip(CircleShape).background(FeatureColors.canvas), contentAlignment = Alignment.Center) {
                                    IconButton(onClick = { onDecrement(line.medicine.id) }, modifier = Modifier.size(44.dp)) {
                                        Icon(Icons.Outlined.Remove, contentDescription = if (line.quantity == 1) "Bỏ sản phẩm" else "Giảm số lượng", tint = FeatureColors.emerald, modifier = Modifier.size(17.dp))
                                    }
                                }
                                Text("${line.quantity}", modifier = Modifier.width(20.dp), color = FeatureColors.deep, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                Box(Modifier.size(44.dp).clip(CircleShape).background(FeatureColors.mint), contentAlignment = Alignment.Center) {
                                    IconButton(onClick = { onIncrement(line.medicine.id) }, enabled = line.quantity < CartRules.MAX_DEMO_QUANTITY, modifier = Modifier.size(44.dp)) {
                                        Icon(Icons.Outlined.Add, contentDescription = "Tăng số lượng", tint = FeatureColors.emerald, modifier = Modifier.size(17.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp))
                            .background(FeatureColors.mint).padding(19.dp)
                    ) {
                        Text("CHI PHÍ THAM KHẢO", color = FeatureColors.emerald, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                        Spacer(Modifier.height(7.dp))
                        Text(pricedSubtotal.toVnd(), color = FeatureColors.deep, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (unpricedCount > 0) "Chưa gồm $unpricedCount mặt hàng cần báo giá. Tổng tiền, giá và khả năng đáp ứng sẽ được xác nhận sau."
                            else "Đây là tạm tính minh họa. Nhà thuốc sẽ kiểm tra giá và khả năng đáp ứng trước khi xác nhận.",
                            color = FeatureColors.muted, fontSize = 12.sp, lineHeight = 18.sp
                        )
                    }
                }
                if (rxIncluded) {
                    item {
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                            .background(FeatureColors.rxSurface).padding(17.dp)) {
                            Text("CẦN XÉT DUYỆT TOA", color = FeatureColors.rx, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(7.dp))
                            Text("Sản phẩm kê đơn cần toa hợp lệ và dược sĩ phê duyệt. Tiếp tục để gửi toa; yêu cầu chưa được xác nhận hoặc giữ hàng.", color = FeatureColors.text, fontSize = 12.sp, lineHeight = 19.sp)
                        }
                    }
                }
            }
            Column(
                Modifier.fillMaxWidth().background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                FeatureButton(
                    text = if (rxIncluded) "Tiếp tục kiểm tra toa  ↗" else "Tiếp tục đặt trước  ↗",
                    onClick = onCheckout,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(7.dp))
                Text("Tiếp tục không đồng nghĩa với xác nhận đơn hoặc giữ hàng.", color = FeatureColors.muted, fontSize = 10.sp, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}
