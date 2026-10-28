package com.pharmasiu.mobile.ui.features.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharmasiu.mobile.data.model.*
import com.pharmasiu.mobile.ui.theme.PharmaPalette
import com.pharmasiu.mobile.ui.components.ProductTile
import java.text.NumberFormat
import java.util.Locale

/** Shared feature presentation colors match the Emerald / Lime brand specification. */
object FeatureColors {
    val emerald = PharmaPalette.Emerald
    val deep = PharmaPalette.Forest
    val lime = PharmaPalette.Lime
    val canvas = PharmaPalette.Cream
    val text = PharmaPalette.Ink
    val muted = PharmaPalette.Muted
    val line = PharmaPalette.Line
    val mint = PharmaPalette.Mint
    val rx = PharmaPalette.Warning
    val rxSurface = PharmaPalette.WarningSurface
}

/** Presentation-only mapping; stock and prescription approval are never inferred from Medicine. */
data class MedicineView(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: String,
    val requiresPrescription: Boolean,
    val price: Long?,
    val unit: String,
    val description: String,
    val ingredient: String
)

fun Medicine.asFeatureView(): MedicineView = MedicineView(
    id = id,
    name = name,
    subtitle = subtitle,
    category = category,
    requiresPrescription = requiresPrescription,
    price = priceVnd.takeIf { it > 0 },
    unit = unit,
    description = description,
    ingredient = "Vui lòng tham khảo thành phần trên bao bì hoặc tư vấn của dược sĩ"
)

fun Long.toVnd(): String = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(this) + " ₫"

@Composable
fun FeatureHeader(title: String, onBack: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier.size(44.dp).semantics { contentDescription = "Quay lại" }.clip(CircleShape)
                    .border(1.dp, FeatureColors.line, CircleShape)
                    .clickable(role = Role.Button, onClick = onBack),
                contentAlignment = Alignment.Center
            ) { Text("←", fontSize = 25.sp, color = FeatureColors.emerald) }
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = FeatureColors.emerald,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        trailing?.invoke()
    }
}

@Composable
fun FeatureButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, dark: Boolean = true, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (dark) FeatureColors.emerald else FeatureColors.lime,
            contentColor = if (dark) Color.White else FeatureColors.deep,
            disabledContainerColor = FeatureColors.line,
            disabledContentColor = FeatureColors.muted
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) { Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
}

@Composable
fun FeatureTag(text: String, prescription: Boolean = false) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
            .background(if (prescription) FeatureColors.rxSurface else FeatureColors.mint)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(Modifier.size(5.dp).clip(CircleShape).background(if (prescription) FeatureColors.rx else FeatureColors.emerald))
        Text(text, color = if (prescription) FeatureColors.rx else FeatureColors.emerald, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun MedicineArt(modifier: Modifier = Modifier, seed: String = "", compact: Boolean = false) {
    val tint = when ((seed.hashCode() and Int.MAX_VALUE) % 3) {
        0 -> Color(0xFFDFF2DD)
        1 -> Color(0xFFE1F1DB)
        else -> Color(0xFFE7EFDC)
    }
    Box(
        modifier = modifier.clip(RoundedCornerShape(if (compact) 17.dp else 26.dp))
            .background(Brush.linearGradient(listOf(tint, Color(0xFFF0F7E7)))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension
            drawCircle(Color.White.copy(alpha = .5f), radius = radius * .45f, center = Offset(size.width * .78f, size.height * .15f))
            drawCircle(FeatureColors.lime.copy(alpha = .43f), radius = radius * .29f, center = Offset(size.width * .20f, size.height * .86f))
            drawCircle(FeatureColors.emerald.copy(alpha = .06f), radius = radius * .16f, center = Offset(size.width * .85f, size.height * .80f))
            val w = size.width * .39f
            val h = size.height * .60f
            val left = (size.width - w) / 2f
            val top = (size.height - h) / 2f + size.height * .045f
            drawRoundRect(Color(0xFFB4CDBB).copy(alpha = .6f), Offset(left + w * .07f, top + h * .94f), Size(w * .95f, h * .10f), androidx.compose.ui.geometry.CornerRadius(radius * .03f))
            drawRoundRect(Color.White, Offset(left, top + h * .14f), Size(w, h * .85f), androidx.compose.ui.geometry.CornerRadius(radius * .08f))
            drawRoundRect(FeatureColors.emerald, Offset(left + w * .1f, top), Size(w * .8f, h * .23f), androidx.compose.ui.geometry.CornerRadius(radius * .035f))
            drawRoundRect(FeatureColors.lime, Offset(left, top + h * .40f), Size(w, h * .36f), androidx.compose.ui.geometry.CornerRadius(radius * .015f))
            val crossCenter = Offset(left + w / 2f, top + h * .58f)
            val bar = w * .095f
            drawRoundRect(FeatureColors.emerald, Offset(crossCenter.x - bar / 2, crossCenter.y - w * .135f), Size(bar, w * .27f), androidx.compose.ui.geometry.CornerRadius(bar / 3))
            drawRoundRect(FeatureColors.emerald, Offset(crossCenter.x - w * .135f, crossCenter.y - bar / 2), Size(w * .27f, bar), androidx.compose.ui.geometry.CornerRadius(bar / 3))
            rotate(35f, Offset(size.width * .78f, size.height * .62f)) {
                drawRoundRect(Color.White, Offset(size.width * .68f, size.height * .58f), Size(radius * .35f, radius * .12f), androidx.compose.ui.geometry.CornerRadius(radius * .06f))
                drawRoundRect(FeatureColors.emerald.copy(alpha = .73f), Offset(size.width * .68f, size.height * .58f), Size(radius * .18f, radius * .12f), androidx.compose.ui.geometry.CornerRadius(radius * .06f))
            }
        }
    }
}

@Composable
fun MedicineCard(medicine: Medicine, onOpenProduct: (String) -> Unit, modifier: Modifier = Modifier) {
    val item = medicine.asFeatureView()
    Column(
        modifier = modifier.clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(1.dp, FeatureColors.line, RoundedCornerShape(22.dp))
            .clickable(role = Role.Button) { onOpenProduct(item.id) }
            .padding(10.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(125.dp)) {
            MedicineArt(Modifier.fillMaxSize(), item.id, compact = true)
            if (item.requiresPrescription) {
                Box(Modifier.align(Alignment.TopStart).padding(8.dp)
                    .background(FeatureColors.emerald, RoundedCornerShape(7.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp)) {
                    Text("Rx", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(11.dp))
        Text(item.category.uppercase(Locale("vi", "VN")), color = FeatureColors.muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(item.name, color = FeatureColors.text, fontSize = 14.sp, fontWeight = FontWeight.Bold, lineHeight = 19.sp, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(item.subtitle, color = FeatureColors.muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(8.dp))
        Text(item.price?.toVnd() ?: "Giá khi xác nhận", color = FeatureColors.emerald, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Kiểm tra khi đặt trước", color = FeatureColors.muted, fontSize = 10.sp, lineHeight = 13.sp)
        if (item.requiresPrescription) {
            Spacer(Modifier.height(3.dp))
            Text("Cần dược sĩ duyệt toa", color = FeatureColors.rx, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, lineHeight = 13.sp)
        }
    }
}

@Composable
fun EmptyFeatureState(title: String, detail: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(78.dp).clip(RoundedCornerShape(24.dp)).background(FeatureColors.mint), contentAlignment = Alignment.Center) {
            Text("+", fontSize = 44.sp, fontWeight = FontWeight.Light, color = FeatureColors.emerald)
        }
        Spacer(Modifier.height(18.dp))
        Text(title, color = FeatureColors.emerald, fontSize = 19.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(detail, color = FeatureColors.muted, fontSize = 13.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
    }
}
