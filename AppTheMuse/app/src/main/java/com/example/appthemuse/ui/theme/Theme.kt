package com.example.appthemuse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color // Import Color

// 1. Cấu hình bảng màu chế độ Tối (Dark Mode)
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    background = BackgroundDark,
    surface = BackgroundDark,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = Color.White,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color.Gray,
    outline = SecondaryGray
)

// 2. Cấu hình bảng màu chế độ Sáng (Light Mode)
private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    background = BackgroundLight,
    surface = Color.White,
    onBackground = Color(0xFF1F2937),
    onSurface = Color(0xFF1F2937),
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = PrimaryIndigo,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = SecondaryGray,
    outline = SecondaryGray
)

@Composable
fun AppTheMuseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Luôn ưu tiên màu thương hiệu của nhóm
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Kết nối mượt mà với cấu trúc quản lý chữ tập trung
        content = content
    )
}