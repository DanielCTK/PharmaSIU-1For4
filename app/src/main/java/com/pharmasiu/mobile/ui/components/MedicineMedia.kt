package com.pharmasiu.mobile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.domain.CatalogMediaRules

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
    fallback: @Composable (Modifier) -> Unit = { artModifier -> ProductArtwork(medicine, artModifier) },
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
