package com.pharmasiu.mobile.ui.features.staff

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.domain.CatalogMediaRules
import com.pharmasiu.mobile.ui.components.MedicineMedia
import com.pharmasiu.mobile.ui.components.ProductShowcaseIllustration
import com.pharmasiu.mobile.ui.components.formatVnd
import com.pharmasiu.mobile.ui.theme.PharmaPalette
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent

/**
 * Uses the same media as the customer catalogue (Coil, URL validation
 * and locally drawn preview). Never substitutes a generic inventory icon.
 */
@Composable
internal fun StaffProductThumbnail(
    medicine: Medicine,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp,
    photoUri: String? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(13.dp))
            .clickable(onClickLabel = "Xem hình sản phẩm " + medicine.name, onClick = onOpen),
    ) {
        StaffProductPicture(
            medicine = medicine,
            photoUri = photoUri,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
internal fun StaffProductPicture(
    medicine: Medicine,
    photoUri: String?,
    modifier: Modifier = Modifier,
) {
    if (photoUri == null) {
        MedicineMedia(
            medicine = medicine,
            modifier = modifier,
            fallback = { inner -> ProductShowcaseIllustration(medicine, inner) },
        )
    } else {
        SubcomposeAsyncImage(
            model = photoUri,
            contentDescription = "Hình đã chọn: " + medicine.name,
            contentScale = ContentScale.Crop,
            modifier = modifier,
            loading = {
                ProductShowcaseIllustration(medicine, Modifier.fillMaxSize())
            },
            error = {
                ProductShowcaseIllustration(medicine, Modifier.fillMaxSize())
            },
            success = { SubcomposeAsyncImageContent() },
        )
    }
}

/** Preview opens on thumbnail tap; local catalogue images are illustrations. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StaffProductMediaPreview(
    medicine: Medicine,
    onDismiss: () -> Unit,
    photoUri: String? = null,
    onReplacePhoto: (() -> Unit)? = null,
    onRemovePhoto: (() -> Unit)? = null,
) {
    val hasProductPhoto = CatalogMediaRules.supportedImageUrl(medicine.imageUrl) != null
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PharmaPalette.Paper,
    ) {
        Column(
            Modifier.fillMaxWidth().heightIn(max = 660.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Hình ảnh sản phẩm", style = MaterialTheme.typography.titleLarge,
                    color = PharmaPalette.Emerald, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Đóng hình sản phẩm")
                }
            }
            StaffProductPicture(
                medicine = medicine,
                photoUri = photoUri,
                modifier = Modifier.fillMaxWidth().height(270.dp),
            )
            Text(
                if (photoUri != null) "Ảnh chọn từ thiết bị • chỉ trong phiên demo"
                else if (hasProductPhoto) "Ảnh danh mục (hiển thị minh họa nếu tải ảnh thất bại)"
                else "Hình minh họa • sản phẩm mẫu chưa có ảnh chụp thực tế",
                style = MaterialTheme.typography.bodySmall,
                color = PharmaPalette.Muted,
            )
            Text(medicine.name, style = MaterialTheme.typography.titleLarge,
                color = PharmaPalette.Ink, fontWeight = FontWeight.Bold,
                maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text(medicine.subtitle, style = MaterialTheme.typography.bodyMedium,
                color = PharmaPalette.Muted)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(medicine.category, modifier = Modifier.weight(1f),
                    color = PharmaPalette.Muted, style = MaterialTheme.typography.bodySmall)
                Text(
                    if (medicine.requiresPrescription) "Cần duyệt toa"
                    else if (medicine.priceVnd > 0L) formatVnd(medicine.priceVnd)
                    else "Chưa có giá",
                    color = PharmaPalette.Emerald,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (onReplacePhoto != null) {
                OutlinedButton(
                    onClick = onReplacePhoto,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                ) { Text("Chọn ảnh khác từ điện thoại") }
            }
            if (photoUri != null && onRemovePhoto != null) {
                TextButton(onClick = onRemovePhoto) { Text("Bỏ ảnh đã chọn") }
            }
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) { Text("Đóng") }
            Spacer(Modifier.height(20.dp))
        }
    }
}
