package com.pharmasiu.mobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.pharmasiu.mobile.R
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.ui.theme.PharmaPalette
import java.text.NumberFormat
import java.util.Locale

fun formatVnd(amount: Long): String =
    NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount) + " ₫"

@Composable
fun PharmaTopBar(title: String, onBack: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, stringResource(R.string.action_navigate_back), tint = PharmaPalette.Emerald) }
        } else {
            Box(
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(PharmaPalette.Emerald),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.LocalPharmacy, null, tint = PharmaPalette.Lime, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
        }
        Text(title, style = MaterialTheme.typography.titleLarge, color = PharmaPalette.Emerald,
            modifier = Modifier.weight(1f).semantics { heading() })
        trailing?.invoke()
    }
}

@Composable
fun StatusPill(text: String, isWarning: Boolean = false) {
    Surface(
        color = if (isWarning) PharmaPalette.WarningSurface else PharmaPalette.Mint,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = text,
            color = if (isWarning) PharmaPalette.Warning else PharmaPalette.Emerald,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
        )
    }
}

@Composable
fun ProductArtwork(medicine: Medicine, modifier: Modifier = Modifier) {
    val isRx = medicine.requiresPrescription
    val bg = if (isRx) Color(0xFFF5EADC) else PharmaPalette.Mint
    Box(modifier = modifier.clip(RoundedCornerShape(22.dp)).background(bg), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = if (isRx) Color(0xFFDBB58B) else Color(0xFFB0DBBF),
                radius = size.minDimension * 0.48f,
                center = Offset(size.width * .85f, size.height * .2f),
                style = Stroke(width = 1.5.dp.toPx()),
            )
            drawCircle(
                color = if (isRx) Color(0x55BA895A) else Color(0x5577B38C),
                radius = size.minDimension * .34f,
                center = Offset(size.width * .15f, size.height * .82f),
            )
        }
        Icon(
            imageVector = if (isRx) Icons.Outlined.HealthAndSafety else Icons.Outlined.LocalPharmacy,
            contentDescription = null,
            tint = if (isRx) Color(0xFF946038) else PharmaPalette.Emerald,
            modifier = Modifier.size(42.dp),
        )
    }
}

@Composable
fun ProductTile(medicine: Medicine, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClickLabel = "Xem ${medicine.name}", onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = PharmaPalette.Paper),
        shape = RoundedCornerShape(23.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            MedicineMedia(medicine, Modifier.size(94.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(medicine.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(medicine.subtitle, style = MaterialTheme.typography.bodyMedium, color = PharmaPalette.Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (medicine.requiresPrescription) {
                    StatusPill("Cần dược sĩ duyệt toa", isWarning = true)
                } else {
                    Text(
                        if (medicine.priceVnd > 0) formatVnd(medicine.priceVnd) else "Giá xác nhận tại quầy",
                        fontWeight = FontWeight.Bold,
                        color = PharmaPalette.Emerald,
                    )
                }
            }
            Icon(Icons.Outlined.ArrowForward, null, tint = PharmaPalette.Emerald, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun DemoDisclosure(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = PharmaPalette.Mint) {
        Text(
            "Bản minh họa • Giá, toa thuốc và tồn kho cần được nhà thuốc xác nhận.",
            color = PharmaPalette.Emerald,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(13.dp),
        )
    }
}
