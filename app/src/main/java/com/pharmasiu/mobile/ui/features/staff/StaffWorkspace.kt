package com.pharmasiu.mobile.ui.features.staff

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pharmasiu.mobile.ui.theme.PharmaPalette

/** Shared UI utilities for the operations area; authentication lives at app root. */
@Composable
internal fun StaffNotice(message: String) {
    Surface(shape = RoundedCornerShape(13.dp), color = PharmaPalette.Mint) {
        Text(
            message,
            modifier = Modifier.fillMaxWidth().padding(13.dp),
            color = PharmaPalette.Forest,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
