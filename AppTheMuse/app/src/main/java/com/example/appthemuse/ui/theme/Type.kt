package com.example.appthemuse.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    // Dành cho tên thương hiệu lớn ở màn hình chào mừng (Thay cho code gõ cứng 42.sp)
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        letterSpacing = 4.sp
    ),
    // Dành cho các tiêu đề màn hình chính hoặc tiêu đề phụ lớn (Ví dụ: "Sở thích của bạn?")
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    // Dành cho các đoạn văn bản, slogan hướng dẫn (Thay cho code gõ cứng 16.sp)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
)