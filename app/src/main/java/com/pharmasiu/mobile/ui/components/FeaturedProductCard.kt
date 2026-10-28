package com.pharmasiu.mobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.ui.theme.PharmaPalette

/** Two-column merchandising card; prices and images are explicitly mock data. */
@Composable
fun FeaturedProductCard(
    medicine: Medicine,
    pictureHeight: Dp,
    onClick: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = PharmaPalette.Paper,
        shape = RoundedCornerShape(19.dp),
        shadowElevation = 1.dp,
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(pictureHeight)
                .clickable(onClickLabel = "Xem " + medicine.name, onClick = onClick)) {
                MedicineMedia(
                    medicine = medicine,
                    modifier = Modifier.fillMaxSize(),
                    fallback = { imageModifier -> ProductShowcaseIllustration(medicine, imageModifier) },
                )
                Surface(
                    color = Color.White.copy(alpha = .94f),
                    shape = RoundedCornerShape(9.dp),
                    modifier = Modifier.align(Alignment.TopStart).padding(7.dp),
                ) {
                    Text(
                        text = if (medicine.requiresPrescription) "RX • CẦN TOA" else medicine.category.uppercase(),
                        color = PharmaPalette.Emerald,
                        fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                    )
                }
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.align(Alignment.TopEnd).padding(3.dp).size(44.dp)
                        .background(Color.White.copy(alpha = .9f), RoundedCornerShape(14.dp)),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = (if (isFavorite) "Bỏ yêu thích " else "Lưu yêu thích ") + medicine.name,
                        tint = PharmaPalette.Emerald,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth()
                    .clickable(onClickLabel = "Xem " + medicine.name, onClick = onClick)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(medicine.name, color = PharmaPalette.Ink,
                    fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold,
                    minLines = 2, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Text(medicine.subtitle, color = PharmaPalette.Muted,
                    fontSize = 11.sp, lineHeight = 15.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    if (medicine.priceVnd > 0) formatVnd(medicine.priceVnd) else "Chờ báo giá",
                    color = PharmaPalette.Emerald, style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                Text(
                    if (medicine.requiresPrescription) "Cần dược sĩ kiểm tra toa" else "Giá minh họa · mẫu UI",
                    color = if (medicine.requiresPrescription) PharmaPalette.Warning else PharmaPalette.Muted,
                    fontSize = 10.sp, lineHeight = 14.sp, maxLines = 2,
                )
            }
        }
    }
}

/** Generic offline product silhouettes, not actual product photographs/packaging. */
@Composable
fun ProductShowcaseIllustration(medicine: Medicine, modifier: Modifier = Modifier) {
    val category = medicine.category
    val palette = when (category) {
        "Mỹ phẩm" -> listOf(Color(0xFFF9DFE5), Color(0xFFFFF5ED), Color(0xFFFFFFFF), Color(0xFFBB7491))
        "Thực phẩm" -> listOf(Color(0xFFFFE5BD), Color(0xFFFFF6D8), Color(0xFFFBF1D4), Color(0xFFAB8046))
        "Thực phẩm bổ sung" -> listOf(Color(0xFFE2EBCD), Color(0xFFFBF9E5), Color(0xFFFFFFFF), Color(0xFF819E6B))
        "Thiết bị" -> listOf(Color(0xFFDFE9F7), Color(0xFFF7FBFD), Color.White, Color(0xFF7195B8))
        "Chăm sóc" -> listOf(Color(0xFFD8EDF3), Color(0xFFFCFBF2), Color.White, Color(0xFF76B0A5))
        else -> listOf(Color(0xFFE2EFE5), Color(0xFFF7F6E9), Color.White, Color(0xFF84A999))
    }
    Box(modifier.clip(RoundedCornerShape(15.dp))
        .background(Brush.linearGradient(listOf(palette[0], palette[1])))) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val accent = palette[3]
            val foreground = palette[2]
            drawCircle(Color.White.copy(alpha = .48f), radius = w * .58f, center = Offset(w * .88f, h * .17f))
            drawCircle(accent.copy(alpha = .10f), radius = w * .33f, center = Offset(w * .06f, h * .82f))
            drawOval(accent.copy(alpha = .16f), Offset(w * .20f, h * .81f), Size(w * .65f, h * .08f))
            if (category == "Thực phẩm") {
                drawOval(accent.copy(alpha = .24f), Offset(w * .17f, h * .32f), Size(w * .65f, h * .47f))
                drawOval(foreground, Offset(w * .17f, h * .27f), Size(w * .65f, h * .47f))
                drawOval(Color(0xFFDFBD88), Offset(w * .24f, h * .35f), Size(w * .51f, h * .32f))
                repeat(12) { i ->
                    drawCircle(Color(0xFFF6E6BE),
                        radius = w * .042f,
                        center = Offset(w * (.30f + .13f * (i % 4)), h * (.41f + .08f * (i / 4))))
                }
            } else if (category == "Vật tư y tế") {
                // A generic folded mask silhouette, instead of a bottle icon.
                drawOval(
                    accent.copy(alpha = .52f),
                    topLeft = Offset(w * .11f, h * .42f),
                    size = Size(w * .30f, h * .26f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(w * .018f),
                )
                drawOval(
                    accent.copy(alpha = .52f),
                    topLeft = Offset(w * .59f, h * .42f),
                    size = Size(w * .30f, h * .26f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(w * .018f),
                )
                drawRoundRect(
                    accent.copy(alpha = .15f),
                    topLeft = Offset(w * .22f, h * .37f),
                    size = Size(w * .58f, h * .34f),
                    cornerRadius = CornerRadius(w * .06f),
                )
                drawRoundRect(
                    foreground,
                    topLeft = Offset(w * .21f, h * .35f),
                    size = Size(w * .58f, h * .34f),
                    cornerRadius = CornerRadius(w * .06f),
                )
                repeat(3) { line ->
                    val lineY = h * (.46f + .07f * line)
                    drawLine(
                        color = accent.copy(alpha = .40f),
                        start = Offset(w * .31f, lineY),
                        end = Offset(w * .69f, lineY),
                        strokeWidth = w * .012f,
                    )
                }
            } else {
                val top = h * .23f
                val left = w * .28f
                val pw = w * .45f
                val ph = h * .57f
                val cap = if (category in listOf("Mỹ phẩm", "Chăm sóc", "Thực phẩm bổ sung")) h * .09f else 0f
                if (cap > 0f) drawRoundRect(accent.copy(alpha = .83f),
                    Offset(left + pw * .20f, top - cap), Size(pw * .60f, cap * 1.18f),
                    cornerRadius = CornerRadius(w * .024f))
                drawRoundRect(accent.copy(alpha = .16f), Offset(left + w * .023f, top + h * .02f),
                    Size(pw, ph), cornerRadius = CornerRadius(w * .075f))
                drawRoundRect(foreground, Offset(left, top), Size(pw, ph),
                    cornerRadius = CornerRadius(w * .075f))
                drawRoundRect(accent.copy(alpha = .72f),
                    Offset(left + pw * .13f, top + ph * .39f),
                    Size(pw * .74f, ph * .045f), cornerRadius = CornerRadius(w * .02f))
                drawRoundRect(accent.copy(alpha = .13f),
                    Offset(left + pw * .12f, top + ph * .50f),
                    Size(pw * .76f, ph * .23f), cornerRadius = CornerRadius(w * .04f))
                drawRoundRect(Color.White.copy(alpha = .6f),
                    Offset(left + pw * .08f, top + ph * .09f),
                    Size(pw * .11f, ph * .71f), cornerRadius = CornerRadius(w * .02f))
            }
            drawCircle(Color.White.copy(alpha = .6f), radius = w * .04f, center = Offset(w * .86f, h * .75f))
        }
        Text(
            "HÌNH MINH HỌA",
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
                .background(Color.White.copy(alpha = .75f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            color = PharmaPalette.Muted, fontSize = 8.sp, fontWeight = FontWeight.Medium,
        )
    }
}
