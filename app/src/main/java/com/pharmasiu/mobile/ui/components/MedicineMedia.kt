package com.pharmasiu.mobile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.domain.CatalogMediaRules
import com.pharmasiu.mobile.ui.theme.PharmaPalette

/** Abstract category illustration, NOT a depiction of any specific medicine. */
@Composable
fun CategoryArtwork(medicine: Medicine, modifier: Modifier = Modifier) {
    val category = medicine.category
    val base = when (category) {
        "Mỹ phẩm" -> Color(0xFFF7E3E9)
        "Thực phẩm", "Thực phẩm bổ sung" -> Color(0xFFF7EAD8)
        "Thiết bị" -> Color(0xFFE5EEF8)
        "Không kê đơn", "Thuốc kê đơn" -> Color(0xFFE2F0E9)
        else -> PharmaPalette.Mint
    }
    val icon = when (category) {
        "Mỹ phẩm" -> Icons.Outlined.Spa
        "Thực phẩm", "Thực phẩm bổ sung" -> Icons.Outlined.Restaurant
        "Thiết bị" -> Icons.Outlined.Devices
        "Không kê đơn", "Thuốc kê đơn" -> Icons.Outlined.Medication
        "Chăm sóc" -> Icons.Outlined.HealthAndSafety
        else -> Icons.Outlined.MedicalServices
    }
    Box(
        modifier.clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(base, Color.White))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = PharmaPalette.Emerald, modifier = Modifier.size(49.dp))
    }
}

/**
 * Single adapter for all product media.
 *
 * Coil handles loading, downsampling, lifecycle and caching. The locally rendered
 * branded artwork stays visible while loading and if a remote image fails.
 * DemoCatalogRepository deliberately has no URLs, so previews remain offline.
 */
@Composable
fun MedicineMedia(
    medicine: Medicine,
    modifier: Modifier = Modifier,
    fallback: @Composable (Modifier) -> Unit = { artModifier -> CategoryArtwork(medicine, artModifier) },
) {
    val verifiedUrl = remember(medicine.imageUrl) {
        CatalogMediaRules.supportedImageUrl(medicine.imageUrl)
    }
    Box(modifier = modifier.clip(RoundedCornerShape(22.dp))) {
        fallback(Modifier.matchParentSize())
        if (verifiedUrl != null) {
            val context = LocalContext.current
            val request = remember(context, verifiedUrl) {
                ImageRequest.Builder(context)
                    .data(verifiedUrl)
                    .crossfade(true)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = "Hình ảnh sản phẩm: ${medicine.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}
