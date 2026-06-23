package com.example.appthemuse.ui.screens.user

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appthemuse.ui.viewmodel.ProfileViewModel

// Định nghĩa cấu trúc bảng màu
data class ProfileThemeColors(
    val backgroundColor: Color,
    val cardColor: Color,
    val titleTextColor: Color,
    val contentTextColor: Color,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onEditProfileClick: () -> Unit,
    viewModel: ProfileViewModel,
    onThemeChanged: (String) -> Unit = {},
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.refreshUserProfile()
    }
    val uiState by viewModel.uiState.collectAsState()

    // 🌟 ĐỒNG BỘ THEME TOÀN CỤC: Kiểm tra xem màu nền thực tế của hệ thống lúc này là Sáng hay Tối
    val isAppInDarkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // ĐỊNH NGHĨA BẢNG MÀU ĐỘNG THEO TRẠNG THÁI THỰC TẾ CỦA HỆ THỐNG
    val themeColors = if (!isAppInDarkMode) {
        ProfileThemeColors(
            backgroundColor = Color(0xFFF4F6FA), // Nền chính Trắng Xanh
            cardColor = Color(0xFFFFFFFF),       // Thẻ trắng tinh nổi bật trên nền
            titleTextColor = Color(0xFF1A2536),  // Chữ tiêu đề Xanh đen đậm
            contentTextColor = Color(0xFF5A677D),// Chữ phụ Xám xanh
            accentColor = Color(0xFF5D5FEF)      // Màu Tím nhấn chủ đạo
        )
    } else {
        ProfileThemeColors(
            backgroundColor = Color(0xFF0F1524), // Nền tối sâu
            cardColor = Color(0xFF1E2638),       // Ô thẻ tối trùng bộ
            titleTextColor = Color.White,        // Chữ tiêu đề trắng tương phản
            contentTextColor = Color.LightGray,  // Chữ phụ xám sáng
            accentColor = Color(0xFF5D5FEF)      // Màu tím Neon nổi bật
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ", color = themeColors.titleTextColor, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.backgroundColor),
                actions = {
                    IconButton(onClick = { /* Tìm kiếm */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = themeColors.titleTextColor)
                    }
                    IconButton(onClick = { /* Thông báo */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = themeColors.titleTextColor)
                    }
                }
            )
        },
        containerColor = themeColors.backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- THÔNG TIN USER (AVATAR & NAME) ---
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(if (isAppInDarkMode) Color.Gray else themeColors.contentTextColor.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.fillMaxSize().padding(16.dp), tint = themeColors.titleTextColor)
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(themeColors.accentColor)
                        .clickable { /* Edit avatar */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = uiState.user.username, color = themeColors.titleTextColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = uiState.user.email, color = themeColors.contentTextColor, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))

            // --- THÔNG SỐ ĐỌC SÁCH ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(modifier = Modifier.weight(1.0f), title = "ĐÃ ĐỌC", value = uiState.user.readCount.toString(), cardColor = themeColors.cardColor, accentColor = themeColors.accentColor, textColor = themeColors.titleTextColor)
                StatCard(modifier = Modifier.weight(1.0f), title = "YÊU THÍCH", value = uiState.user.favoriteCount.toString(), cardColor = themeColors.cardColor, accentColor = themeColors.accentColor, textColor = themeColors.titleTextColor)
                StatCard(modifier = Modifier.weight(1.0f), title = "ĐÃ TẢI", value = uiState.user.downloadedCount.toString(), cardColor = themeColors.cardColor, accentColor = themeColors.accentColor, textColor = themeColors.titleTextColor)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- TÙY CHỈNH GIAO DIỆN ---
            SectionTitle(title = "TÙY CHỈNH GIAO DIỆN")

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Kích cỡ chữ", color = themeColors.titleTextColor)
                Text(uiState.fontSize, color = themeColors.accentColor, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("A", color = themeColors.contentTextColor, fontSize = 12.sp)
                Slider(
                    value = uiState.fontSizeValue,
                    onValueChange = { viewModel.updateFontSize(it) },
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(thumbColor = themeColors.accentColor, activeTrackColor = themeColors.accentColor, inactiveTrackColor = themeColors.cardColor)
                )
                Text("A", color = themeColors.titleTextColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Khoảng cách dòng", color = themeColors.titleTextColor, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Dày", "Vừa", "Thưa").forEach { option ->
                    val isSelected = uiState.lineSpacing == option
                    Button(
                        onClick = { viewModel.updateLineSpacing(option) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) themeColors.accentColor else themeColors.cardColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(option, color = if (isSelected) Color.White else themeColors.titleTextColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Chế độ hiển thị", color = themeColors.titleTextColor, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val themesList = listOf(
                    Triple("Light", Color(0xFFF4F6FA), Color(0xFF1A2536)),
                    Triple("Dark", Color(0xFF0F1524), Color(0xFFFFFFFF))
                )
                themesList.forEach { (name, bg, fg) ->
                    val isSelected = (name == "Dark" && isAppInDarkMode) || (name == "Light" && !isAppInDarkMode)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clickable {
                                viewModel.updateThemeMode(name)
                                onThemeChanged(name)
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = bg),
                        border = if (isSelected) BorderStroke(2.dp, themeColors.accentColor) else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Aa", color = fg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- TÀI KHOẢN ---
            SectionTitle(title = "TÀI KHOẢN")
            MenuItem(icon = Icons.Default.MenuBook, title = "Góc tác giả", cardColor = themeColors.cardColor, textColor = themeColors.titleTextColor, onClick = { /* Góc tác giả */ })
            MenuItem(
                icon = Icons.Default.Person,
                title = "Thông tin cá nhân",
                cardColor = themeColors.cardColor,
                textColor = themeColors.titleTextColor,
                onClick = onEditProfileClick // Chạy mượt mà ngay lập tức!
            )
            MenuItem(icon = Icons.Default.Lock, title = "Mật khẩu & Bảo mật", cardColor = themeColors.cardColor, textColor = themeColors.titleTextColor, onClick = { /* Mật khẩu */ })

            Spacer(modifier = Modifier.height(16.dp))

            // --- HỖ TRỢ ---
            SectionTitle(title = "HỖ TRỢ")
            MenuItem(icon = Icons.Default.HelpOutline, title = "Trung tâm hỗ trợ", cardColor = themeColors.cardColor, textColor = themeColors.titleTextColor, onClick = { })
            MenuItem(icon = Icons.Default.Description, title = "Điều khoản sử dụng", cardColor = themeColors.cardColor, textColor = themeColors.titleTextColor, onClick = { })

            Spacer(modifier = Modifier.height(24.dp))

            // --- NÚT ĐĂNG XUẤT ---
            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF4D4D)),
                border = BorderStroke(1.5.dp, Color(0xFFFF4D4D)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đăng xuất", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, title: String, value: String, cardColor: Color, accentColor: Color, textColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = title, fontSize = 11.sp, color = accentColor, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 24.sp, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.Gray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    )
}

@Composable
fun MenuItem(icon: ImageVector, title: String, cardColor: Color, textColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(cardColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = textColor.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, color = textColor, fontSize = 15.sp)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}