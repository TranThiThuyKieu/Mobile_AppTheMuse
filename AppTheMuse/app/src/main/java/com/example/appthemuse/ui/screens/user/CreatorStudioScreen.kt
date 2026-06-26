package com.example.appthemuse.ui.screens.user.creator_studio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.navigation.NavController
import com.example.appthemuse.ui.viewmodel.CreatorStudioViewModel

// Định nghĩa cấu trúc bảng màu cho Creator Studio
data class CreatorThemeColors(
    val backgroundColor: Color,
    val cardColor: Color,
    val titleTextColor: Color,
    val contentTextColor: Color,
    val accentColor: Color,
    val positiveColor: Color,
    val infoColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorStudioScreen(
    viewModel: CreatorStudioViewModel,
    navController: NavController,
    onBackClick: () -> Unit,
    onCreateBookClick: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    val uiState by viewModel.uiState.collectAsState()

    // 🌟 ĐỒNG BỘ THEME TOÀN CỤC
    val isAppInDarkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val themeColors = if (!isAppInDarkMode) {
        CreatorThemeColors(
            backgroundColor = Color(0xFFF9F9FB),
            cardColor = Color(0xFFFFFFFF),
            titleTextColor = Color(0xFF1A2536),
            contentTextColor = Color(0xFF5A677D),
            accentColor = Color(0xFF6B66FF),
            positiveColor = Color(0xFFE8505B),
            infoColor = Color(0xFF4A90E2)
        )
    } else {
        CreatorThemeColors(
            backgroundColor = Color(0xFF0F1524),
            cardColor = Color(0xFF1E2638),
            titleTextColor = Color.White,
            contentTextColor = Color.LightGray,
            accentColor = Color(0xFF6B66FF),
            positiveColor = Color(0xFFFF6B6B),
            infoColor = Color(0xFF64B5F6)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Góc tác giả", color = themeColors.titleTextColor, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).padding(end = 48.dp))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = themeColors.accentColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.backgroundColor)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateBookClick,
                containerColor = themeColors.accentColor,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = themeColors.backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Tiêu đề chính
            Text(
                text = "GÓC TÁC GIẢ",
                color = themeColors.titleTextColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Chào mừng trở lại, hãy tiếp tục hành trình kể chuyện của bạn.",
                color = themeColors.contentTextColor,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Nút Tạo tác phẩm mới
            Button(
                onClick = onCreateBookClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accentColor)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tạo tác phẩm mới", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Thẻ thống kê
            StatItemCard(
                icon = Icons.Default.MenuBook,
                title = "TỔNG LƯỢT ĐỌC",
                value = uiState.totalReadsStr,
                subText = "Theo dõi sự tăng trưởng",
                subTextColor = themeColors.infoColor,
                themeColors = themeColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            StatItemCard(
                icon = Icons.Default.Group,
                title = "NGƯỜI THEO DÕI",
                value = uiState.totalFollowersStr,
                subText = "Cộng đồng của bạn",
                subTextColor = Color(0xFFC2185B),
                themeColors = themeColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            StatItemCard(
                icon = Icons.Default.LibraryBooks,
                title = "TỔNG SỐ CHƯƠNG",
                value = uiState.totalChaptersStr,
                subText = "Duy trì tiến độ tốt",
                subTextColor = themeColors.accentColor,
                themeColors = themeColors
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = themeColors.accentColor
                )
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = themeColors.positiveColor,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                // --- Tác phẩm của bạn ---
                SectionHeader(title = "Tác phẩm của bạn", themeColors = themeColors)
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.publishedBooks.isEmpty()) {
                    Text(
                        text = "Bạn chưa có tác phẩm nào.",
                        color = themeColors.contentTextColor,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    uiState.publishedBooks.forEach { book ->
                        val statusStr = if (book.status.lowercase() == "hoàn thành") "HOÀN THÀNH" else "ĐANG CẬP NHẬT"
                        val statusColor = if (statusStr == "HOÀN THÀNH") Color(0xFFE8F5E9) else Color(0xFFE8EAF6)
                        val statusTextColor = if (statusStr == "HOÀN THÀNH") Color(0xFF388E3C) else themeColors.accentColor

                        WorkItem(
                            title = book.title,
                            description = book.description.ifEmpty { "Chưa có mô tả" },
                            status = statusStr,
                            statusBgColor = statusColor,
                            statusTextColor = statusTextColor,
                            chapterCount = "${book.chapter_count} Chương",
                            readCount = book.view_count.toString(), // TODO: Format number
                            coverUrl = book.cover_url,
                            themeColors = themeColors,
                            onClick = {
                                navController.navigate("creator_book_detail/${book.id}")
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- Bản thảo gần đây ---
                SectionHeader(title = "Bản thảo gần đây", themeColors = themeColors)
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.drafts.isEmpty()) {
                    Text(
                        text = "Không có bản thảo nào.",
                        color = themeColors.contentTextColor,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    uiState.drafts.forEach { draft ->
                        DraftItem(
                            title = draft.title,
                            subText = "Lưu gần đây", // TODO: Tính toán thời gian từ created_at
                            themeColors = themeColors
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // padding cho FAB
        }
    }
}

@Composable
fun StatItemCard(
    icon: ImageVector,
    title: String,
    value: String,
    subText: String,
    subTextColor: Color,
    themeColors: CreatorThemeColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = themeColors.accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = themeColors.contentTextColor,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                color = themeColors.titleTextColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subText,
                fontSize = 12.sp,
                color = subTextColor
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, themeColors: CreatorThemeColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.titleTextColor
            )
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .width(40.dp)
                    .height(3.dp)
                    .background(themeColors.accentColor, RoundedCornerShape(1.5.dp))
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { /* TODO */ }
        ) {
            Text(
                text = "Xem thêm",
                fontSize = 13.sp,
                color = themeColors.accentColor,
                fontWeight = FontWeight.Medium
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = themeColors.accentColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun WorkItem(
    title: String,
    description: String,
    status: String,
    statusBgColor: Color,
    statusTextColor: Color,
    chapterCount: String,
    readCount: String,
    coverUrl: String,
    themeColors: CreatorThemeColors,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        // Ảnh bìa sách
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(130.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (coverUrl.isNotEmpty()) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "NO COVER",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Badge Status
            Box(
                modifier = Modifier
                    .background(statusBgColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = status,
                    fontSize = 10.sp,
                    color = statusTextColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = themeColors.titleTextColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = themeColors.contentTextColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = themeColors.contentTextColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = chapterCount,
                    fontSize = 12.sp,
                    color = themeColors.contentTextColor
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    Icons.Default.Visibility,
                    contentDescription = null,
                    tint = themeColors.contentTextColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = readCount,
                    fontSize = 12.sp,
                    color = themeColors.contentTextColor
                )
            }
        }
    }
}

@Composable
fun DraftItem(
    title: String,
    subText: String,
    themeColors: CreatorThemeColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFEEF0FF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoFixHigh,
                    contentDescription = null,
                    tint = themeColors.accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = themeColors.titleTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subText,
                    fontSize = 12.sp,
                    color = themeColors.contentTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = { /* TODO */ }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = themeColors.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}