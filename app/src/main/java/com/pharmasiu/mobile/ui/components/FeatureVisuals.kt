package com.pharmasiu.mobile.ui.components

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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.ui.theme.PharmaPalette
import com.pharmasiu.mobile.ui.components.MedicineMedia
import java.text.NumberFormat
import java.util.Locale

/** Brand tokens reused by all four customer catalog features. */
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

data class MedicineView(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: String,
    val requiresPrescription: Boolean,
    val price: Long?,
    val unit: String,
    val description: String,
    val ingredient: String,
)

/** No stock or Rx approval state can be inferred from a catalog record. */
fun Medicine.asFeatureView() = MedicineView(
    id = id,
    name = name,
    subtitle = subtitle,
    category = category,
    requiresPrescription = requiresPrescription,
    price = priceVnd.takeIf { it > 0 },
    unit = unit,
    description = description,
    ingredient = "Vui lòng tham khảo bao bì hoặc hỏi dược sĩ",
)

fun Long.toVnd(): String = formatVnd(this)

@Composable
fun FeatureHeader(title: String, onBack: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    PharmaTopBar(title = title, onBack = onBack, trailing = trailing)
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
            disabledContentColor = FeatureColors.muted,
        ),
    ) { Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
}

@Composable
fun FeatureTag(text: String, prescription: Boolean = false) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
            .background(if (prescription) FeatureColors.rxSurface else FeatureColors.mint)
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape)
            .background(if (prescription) FeatureColors.rx else FeatureColors.emerald))
        Text(text, color = if (prescription) FeatureColors.rx else FeatureColors.emerald,
            fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Brand-shaped art remains available when the catalog does not include real product imagery. */
@Composable
fun MedicineArt(modifier: Modifier = Modifier, seed: String = "", compact: Boolean = false) {
    val background = when ((seed.hashCode() and Int.MAX_VALUE) % 3) {
        0 -> Color(0xFFE1F2E2)
        1 -> Color(0xFFE4F0DA)
        else -> Color(0xFFF0F5E1)
    }
    Box(
        modifier.clip(RoundedCornerShape(if (compact) 17.dp else 25.dp))
            .background(Brush.linearGradient(listOf(background, Color(0xFFF6F9ED)))),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension
            drawCircle(Color.White.copy(alpha = .7f), r * .40f, Offset(size.width * .84f, size.height * .16f))
            drawCircle(FeatureColors.lime.copy(alpha = .56f), r * .30f, Offset(size.width * .18f, size.height * .84f))
            val bottleWidth = size.width * .37f
            val bottleHeight = size.height * .57f
            val left = (size.width - bottleWidth) / 2f
            val top = (size.height - bottleHeight) / 2f + r * .04f
            drawRoundRect(Color(0xFFB8CDBB), Offset(left + bottleWidth * .08f, top + bottleHeight * .96f), Size(bottleWidth, bottleHeight * .09f), CornerRadius(r * .03f))
            drawRoundRect(Color.White, Offset(left, top + bottleHeight * .13f), Size(bottleWidth, bottleHeight * .85f), CornerRadius(r * .07f))
            drawRoundRect(FeatureColors.emerald, Offset(left + bottleWidth * .1f, top), Size(bottleWidth * .8f, bottleHeight * .24f), CornerRadius(r * .025f))
            drawRoundRect(FeatureColors.lime, Offset(left, top + bottleHeight * .43f), Size(bottleWidth, bottleHeight * .33f), CornerRadius(r * .01f))
            val middle = Offset(left + bottleWidth / 2f, top + bottleHeight * .58f)
            val stroke = bottleWidth * .09f
            drawRoundRect(FeatureColors.emerald, Offset(middle.x - stroke / 2, middle.y - bottleWidth * .15f), Size(stroke, bottleWidth * .30f), CornerRadius(stroke * .2f))
            drawRoundRect(FeatureColors.emerald, Offset(middle.x - bottleWidth * .15f, middle.y - stroke / 2), Size(bottleWidth * .30f, stroke), CornerRadius(stroke * .2f))
            rotate(35f, Offset(size.width * .78f, size.height * .63f)) {
                drawRoundRect(Color.White, Offset(size.width * .69f, size.height * .58f), Size(r * .32f, r * .12f), CornerRadius(r * .055f))
                drawRoundRect(FeatureColors.emerald.copy(alpha = .75f), Offset(size.width * .69f, size.height * .58f), Size(r * .17f, r * .12f), CornerRadius(r * .055f))
            }
        }
    }
}

@Composable
fun MedicineCard(medicine: Medicine, onOpenProduct: (String) -> Unit, modifier: Modifier = Modifier) {
    val product = medicine.asFeatureView()
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White)
            .border(1.dp, FeatureColors.line, RoundedCornerShape(22.dp))
            .clickable(role = Role.Button, onClickLabel = "Xem chi tiết ${product.name}") { onOpenProduct(product.id) }
            .padding(10.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(126.dp)) {
            MedicineMedia(
                medicine = medicine,
                modifier = Modifier.fillMaxSize(),
                fallback = { place -> MedicineArt(place, product.id, compact = true) },
            )
            if (product.requiresPrescription) {
                Box(Modifier.align(Alignment.TopStart).padding(8.dp)
                    .background(FeatureColors.emerald, RoundedCornerShape(7.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp)) {
                    Text("Rx", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(product.category.uppercase(Locale("vi", "VN")), color = FeatureColors.muted,
            fontSize = 10.sp, letterSpacing = .8.sp, fontWeight = FontWeight.Bold,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(5.dp))
        Text(product.name, fontSize = 14.sp, lineHeight = 19.sp, color = FeatureColors.deep,
            fontWeight = FontWeight.Bold, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(product.subtitle, color = FeatureColors.muted, fontSize = 11.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(9.dp))
        Text(product.price?.toVnd() ?: "Giá khi xác nhận", color = FeatureColors.emerald,
            fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        Text("Kiểm tra khi đặt trước", color = FeatureColors.muted, fontSize = 10.sp, lineHeight = 14.sp)
        if (product.requiresPrescription) {
            Spacer(Modifier.height(4.dp))
            Text("Cần dược sĩ duyệt toa", color = FeatureColors.rx, fontSize = 10.sp,
                lineHeight = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EmptyFeatureState(title: String, detail: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(78.dp).clip(RoundedCornerShape(23.dp)).background(FeatureColors.mint),
            contentAlignment = Alignment.Center) {
            Text("+", color = FeatureColors.emerald, fontSize = 44.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.height(17.dp))
        Text(title, color = FeatureColors.deep, fontSize = 19.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(7.dp))
        Text(detail, color = FeatureColors.muted, fontSize = 13.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
    }
}
