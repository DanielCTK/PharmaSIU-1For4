package com.pharmasiu.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pharmasiu.mobile.ui.PharmaSIUApp
import com.pharmasiu.mobile.ui.theme.PharmaSIUTheme

/** Single-activity Compose entry point. Feature screens do not depend on this Activity. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PharmaSIUTheme { PharmaSIUApp() }
        }
    }
}
