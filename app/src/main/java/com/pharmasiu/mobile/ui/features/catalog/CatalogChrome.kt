package com.pharmasiu.mobile.ui.features.catalog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharmasiu.mobile.ui.theme.PharmaPalette

/**
 * A compact editorial header to occupy the previously empty upper-right region.
 * Pure Compose vectors: no network, unverified product photos or decorative controls.
 */
@Composable
fun CatalogIntroCard(
    productCount: Int,
    groupCount: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        color = PharmaPalette.Emerald,
    ) {
        Box(
            Modifier.fillMaxWidth().height(176.dp)
                .background(
                    Brush.linearGradient(
                        listOf(PharmaPalette.Forest, PharmaPalette.Emerald, Color(0xFF16745B)),
                    ),
                ),
        ) {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = .09f),
                    radius = size.width * .43f,
                    center = Offset(size.width * .90f, size.height * .13f),
                    style = Stroke(width = 1.4.dp.toPx()),
                )
                drawCircle(
                    color = PharmaPalette.Lime.copy(alpha = .17f),
                    radius = size.width * .31f,
                    center = Offset(size.width * .93f, size.height * .82f),
                )
                drawCircle(
                    color = Color.White.copy(alpha = .16f),
                    radius = 4.dp.toPx(),
                    center = Offset(size.width * .62f, size.height * .19f),
                )
            }
            Row(
                modifier = Modifier.fillMaxSize().padding(start = 20.dp, top = 18.dp, bottom = 16.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1.7f).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "TỦ ĐỒ CHĂM SÓC",
                        color = PharmaPalette.Lime,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                    )
                    Text(
                        "Chọn đúng\nđiều bạn cần.",
                        style = MaterialTheme.typography.headlineMedium,
                        lineHeight = 28.sp,
                        color = Color.White,
                        maxLines = 2,
                    )
                    Text(
                        productCount.toString() + " sản phẩm mẫu  •  " + groupCount + " nhóm",
                        color = Color(0xFFE6F3E8),
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    MiniCategoryToken(
                        background = Color(0xFFFFEBE6),
                        iconColor = Color(0xFFBB728B),
                        icon = { Icon(Icons.Outlined.Spa, null, tint = Color(0xFFBB728B), modifier = Modifier.size(28.dp)) },
                        modifier = Modifier.align(Alignment.TopEnd).offset(x = 1.dp, y = 2.dp).graphicsLayer { rotationZ = 11f },
                    )
                    MiniCategoryToken(
                        background = Color(0xFFFFEFCF),
                        iconColor = Color(0xFF997448),
                        icon = { Icon(Icons.Outlined.Restaurant, null, tint = Color(0xFF997448), modifier = Modifier.size(26.dp)) },
                        modifier = Modifier.align(Alignment.CenterStart).offset(x = 1.dp, y = 3.dp).graphicsLayer { rotationZ = -13f },
                    )
                    MiniCategoryToken(
                        background = Color(0xFFE9F6E5),
                        iconColor = PharmaPalette.Emerald,
                        icon = { Icon(Icons.Outlined.HealthAndSafety, null, tint = PharmaPalette.Emerald, modifier = Modifier.size(26.dp)) },
                        modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-2).dp).graphicsLayer { rotationZ = 7f },
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniCategoryToken(
    background: Color,
    iconColor: Color,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(63.dp),
        color = background,
        shape = RoundedCornerShape(19.dp),
        shadowElevation = 7.dp,
    ) {
        Box(contentAlignment = Alignment.Center) { icon() }
    }
}

/**
 * Replaces a visually empty "SẢN PHẨM / 30 kết quả / Đề xuất" line with a
 * balanced, compact toolbar. On small devices the left label can shrink safely.
 */
@Composable
fun CatalogResultsToolbar(
    resultCount: Int,
    sort: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onSortChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = PharmaPalette.Paper,
        shape = RoundedCornerShape(19.dp),
        tonalElevation = 1.dp,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 12.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Box(Modifier.size(width = 4.dp, height = 33.dp)
                .clip(RoundedCornerShape(3.dp)).background(PharmaPalette.Lime))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Sản phẩm",
                    color = PharmaPalette.Emerald,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    resultCount.toString() + " kết quả tìm thấy",
                    color = PharmaPalette.Muted,
                    fontSize = 11.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            Box {
                OutlinedButton(
                    onClick = { onExpandChange(true) },
                    modifier = Modifier.heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Outlined.Tune, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(sort, maxLines = 1, fontSize = 12.sp)
                    Spacer(Modifier.width(3.dp))
                    Icon(Icons.Outlined.KeyboardArrowDown, null, modifier = Modifier.size(16.dp))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { onExpandChange(false) }) {
                    listOf("Đề xuất", "Tên A–Z", "Giá thấp", "Giá cao").forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            leadingIcon = {
                                if (sort == item) Icon(Icons.Outlined.Check, null, tint = PharmaPalette.Emerald)
                            },
                            onClick = { onSortChange(item); onExpandChange(false) },
                        )
                    }
                }
            }
        }
    }
}
