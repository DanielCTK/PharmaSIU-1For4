package com.pharmasiu.mobile.ui.features.staff

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.domain.StaffRole
import com.pharmasiu.mobile.ui.theme.PharmaPalette

/** One employee entrance, one shared operations UI, with role-scoped demo actions. */
@Composable
fun StaffWorkspace(
    products: List<Medicine>,
    store: StaffDemoStore,
    onExit: () -> Unit,
) {
    var role by remember { mutableStateOf<StaffRole?>(null) }
    BackHandler(enabled = role != null) { role = null }
    when (role) {
        null -> StaffLoginScreen(onExit = onExit, onAuthenticated = { role = it })
        else -> OperationsHub(role!!, products, store, onLogout = { role = null })
    }
}

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
