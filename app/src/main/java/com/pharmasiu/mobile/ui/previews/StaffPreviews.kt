package com.pharmasiu.mobile.ui.previews

import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.tooling.preview.Preview
import com.pharmasiu.mobile.data.repository.DemoCatalogRepository
import com.pharmasiu.mobile.domain.StaffRole
import com.pharmasiu.mobile.ui.features.staff.*
import com.pharmasiu.mobile.ui.theme.PharmaPalette
import com.pharmasiu.mobile.ui.theme.PharmaSIUTheme

@Preview(name = "01 · Đăng nhập nội bộ", group = "PharmaSIU · operations",
    widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun StaffAccessPreview() {
    PharmaSIUTheme {
        val data = remember { StaffDemoStore(DemoCatalogRepository.medicines) }
        Surface(Modifier.fillMaxSize(), color = PharmaPalette.Cream) {
            StaffLoginScreen(onExit = {}, onAuthenticated = {})
        }
    }
}

@Preview(name = "02 · Vận hành nhân viên", group = "PharmaSIU · operations",
    widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CashierSalesPreview() {
    PharmaSIUTheme {
        val data = remember {
            StaffDemoStore(DemoCatalogRepository.medicines).apply { openShift() }
        }
        Surface(Modifier.fillMaxSize(), color = PharmaPalette.Cream) {
            OperationsHub(StaffRole.CASHIER, DemoCatalogRepository.medicines, data, onLogout = {})
        }
    }
}

@Preview(name = "03 · Vận hành quản lý", group = "PharmaSIU · operations",
    widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun BasicAdminPreview() {
    PharmaSIUTheme {
        val data = remember { StaffDemoStore(DemoCatalogRepository.medicines) }
        Surface(Modifier.fillMaxSize(), color = PharmaPalette.Cream) {
            OperationsHub(StaffRole.ADMIN, DemoCatalogRepository.medicines, data, onLogout = {})
        }
    }
}
