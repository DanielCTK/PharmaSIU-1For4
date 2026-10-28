package com.pharmasiu.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

object PharmaPalette {
    val Emerald = Color(0xFF064D3A)
    val Forest = Color(0xFF073D30)
    val Lime = Color(0xFFB9F48B)
    val Mint = Color(0xFFE6F3E6)
    val Cream = Color(0xFFF6F7F2)
    val Paper = Color(0xFFFFFFFF)
    val Ink = Color(0xFF192A24)
    val Muted = Color(0xFF617168)
    val Line = Color(0xFFDCE5DC)
    val Warning = Color(0xFF9B4D17)
    val WarningSurface = Color(0xFFFFF1E4)
}

private val PharmaColors = lightColorScheme(
    primary = PharmaPalette.Emerald,
    onPrimary = Color.White,
    primaryContainer = PharmaPalette.Mint,
    onPrimaryContainer = PharmaPalette.Forest,
    secondary = PharmaPalette.Lime,
    onSecondary = PharmaPalette.Forest,
    background = PharmaPalette.Cream,
    onBackground = PharmaPalette.Ink,
    surface = PharmaPalette.Paper,
    onSurface = PharmaPalette.Ink,
    surfaceVariant = PharmaPalette.Mint,
    onSurfaceVariant = PharmaPalette.Muted,
    outline = PharmaPalette.Line,
)

private val PharmaType = Typography(
    displaySmall = TextStyle(fontSize = 35.sp, fontWeight = FontWeight.Bold, lineHeight = 39.sp, letterSpacing = (-1.1).sp),
    headlineLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp, letterSpacing = (-0.6).sp),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 31.sp),
    titleLarge = TextStyle(fontSize = 21.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 19.sp),
)

private val PharmaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

@Composable
fun PharmaSIUTheme(content: @Composable () -> Unit) {
    // Product baseline uses a deliberately controlled light palette.
    // isSystemInDarkTheme is intentionally not used until dark tokens are validated.
    MaterialTheme(
        colorScheme = PharmaColors,
        typography = PharmaType,
        shapes = PharmaShapes,
        content = content,
    )
}
