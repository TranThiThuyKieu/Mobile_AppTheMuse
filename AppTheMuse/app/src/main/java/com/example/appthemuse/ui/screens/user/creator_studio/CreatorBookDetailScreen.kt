package com.example.appthemuse.ui.screens.user.creator_studio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appthemuse.domain.model.Chapter
import com.example.appthemuse.ui.viewmodel.CreatorBookDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorBookDetailScreen(
    bookId: String,
    viewModel: CreatorBookDetailViewModel,
    onBackClick: () -> Unit,
    onPostChapterClick: (String, Int) -> Unit
) {
    LaunchedEffect(bookId) {
        viewModel.loadBookDetails(bookId)
    }

    // 🔄 Reload khi quay lại màn hình (sau khi thêm chương)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadBookDetails(bookId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val uiState by viewModel.uiState.collectAsState()
    val isAppInDarkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val themeColors = if (!isAppInDarkMode) {
        CreatorThemeColors(
            backgroundColor = Color(0xFFF9F9FB),
            cardColor = Color(0xFFFFFFFF),
            titleTextColor = Color(0xFF1A2536),
            contentTextColor = Color(0xFF5A677D),
            accentColor = Color(0xFF4C4DDC), // Xanh dương đậm theo thiết kế
            positiveColor = Color(0xFFE8505B),
            infoColor = Color(0xFF4A90E2)
        )
    } else {
        CreatorThemeColors(
            backgroundColor = Color(0xFF0F1524),
            cardColor = Color(0xFF1E2638),
            titleTextColor = Color.White,
            contentTextColor = Color.LightGray,
            accentColor = Color(0xFF5D5FEF),
            positiveColor = Color(0xFFFF6B6B),
            infoColor = Color(0xFF64B5F6)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "chi tiết tác phẩm",
                        color = themeColors.titleTextColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = themeColors.accentColor)
                    }
                },
                actions = {
                    // Spacer để cân bằng title ở giữa
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.backgroundColor)
            )
        },
        containerColor = themeColors.backgroundColor
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = themeColors.accentColor)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.error ?: "", color = themeColors.positiveColor)
            }
        } else {
            val book = uiState.book
            if (book != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))

                        // --- ẢNH BÌA ---
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.LightGray)
                            ) {
                                AsyncImage(
                                    model = book.cover_url,
                                    contentDescription = "Cover",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            
                            // Nút đổi ảnh bìa
                            Box(
                                modifier = Modifier
                                    .offset(x = 12.dp, y = 12.dp)
                                    .size(36.dp)
                                    .background(themeColors.cardColor, CircleShape)
                                    .clickable { /* Đổi ảnh bìa */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Edit cover", tint = themeColors.contentTextColor, modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // --- HUY HIỆU (Nếu có) ---
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEEF0FF), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = themeColors.accentColor, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("BIÊN TẬP VIÊN LỰA CHỌN", fontSize = 10.sp, color = themeColors.accentColor, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- TÊN TÁC PHẨM ---
                        Text(
                            text = book.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.titleTextColor,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- MÔ TẢ ---
                        Text(
                            text = book.description.ifEmpty { "Chưa có mô tả" },
                            fontSize = 14.sp,
                            color = themeColors.contentTextColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- RATING & THỂ LOẠI ---
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = String.format("%.1f", book.rating), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.titleTextColor)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BadgeItem(uiState.categoryName, themeColors)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val isPending = book.status.lowercase() == "pending"
                        val isCompleted = book.status.lowercase() == "completed" || book.status.lowercase() == "hoàn thành"
                        val isHidden = book.status.lowercase() == "hidden"
                        val isOngoing = book.status.lowercase() == "ongoing" || book.status.lowercase() == "đang cập nhật"

                        // --- NÚT BẤM ---
                        // Nút Đăng chương mới: chỉ hiện khi ONGOING (không ẩn, không pending, không hoàn thành)
                        if (isOngoing) {
                            Button(
                                onClick = { onPostChapterClick(book.id, uiState.chapters.size + 1) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accentColor)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Đăng chương mới", fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Nếu ongoing thì hiện nút Đánh dấu hoàn thành
                        if (isOngoing) {
                            OutlinedButton(
                                onClick = { viewModel.updateBookStatus(bookId, "completed") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, themeColors.accentColor.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = themeColors.accentColor, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ĐÁNH DẤU HOÀN THÀNH", color = themeColors.accentColor, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Nút Ẩn / Hiện tác phẩm: luôn hiển thị ở mọi trạng thái
                        OutlinedButton(
                            onClick = {
                                viewModel.toggleHideBook(bookId, book.status, book.previousStatus)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, themeColors.positiveColor.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                if (isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = themeColors.positiveColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isHidden) "HIỆN TÁC PHẨM" else "ẨN TÁC PHẨM",
                                color = themeColors.positiveColor,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // --- STATS ---
                        DetailStatCard(
                            icon = Icons.Default.Visibility,
                            iconBgColor = Color(0xFFEEF0FF),
                            iconColor = themeColors.accentColor,
                            value = uiState.totalReadsStr,
                            label = "TỔNG LƯỢT ĐỌC",
                            trend = "+12% tuần này",
                            trendColor = Color(0xFF4CAF50),
                            themeColors = themeColors
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        DetailStatCard(
                            icon = Icons.Default.Favorite,
                            iconBgColor = Color(0xFFFFEBEE),
                            iconColor = Color(0xFFE91E63),
                            value = uiState.totalVotesStr,
                            label = "LƯỢT BÌNH CHỌN",
                            trend = "Top 5%",
                            trendColor = Color(0xFFE91E63),
                            themeColors = themeColors
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        DetailStatCard(
                            icon = Icons.Default.ChatBubble,
                            iconBgColor = Color(0xFFF5F5F5),
                            iconColor = Color(0xFF607D8B),
                            value = uiState.totalCommentsStr,
                            label = "BÌNH LUẬN",
                            trend = "24h qua",
                            trendColor = Color(0xFF607D8B),
                            themeColors = themeColors
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // --- DANH SÁCH CHƯƠNG HEADER ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Danh sách chương",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.titleTextColor
                            )
                            Text(
                                "${uiState.chapters.size} chương",
                                fontSize = 12.sp,
                                color = themeColors.contentTextColor
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // --- DANH SÁCH CHƯƠNG ITEMS ---
                    if (uiState.chapters.isEmpty()) {
                        item {
                            Text(
                                "Chưa có chương nào.",
                                color = themeColors.contentTextColor,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(uiState.chapters) { chapter ->
                            ChapterItem(chapter, themeColors)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Padding dưới
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeItem(text: String, themeColors: CreatorThemeColors) {
    Box(
        modifier = Modifier
            .background(Color.Transparent, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = themeColors.titleTextColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DetailStatCard(
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    value: String,
    label: String,
    trend: String,
    trendColor: Color,
    themeColors: CreatorThemeColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
                
                Text(
                    text = trend,
                    fontSize = 10.sp,
                    color = trendColor,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.titleTextColor
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = label,
                fontSize = 10.sp,
                color = themeColors.contentTextColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun ChapterItem(chapter: Chapter, themeColors: CreatorThemeColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { /* Xem chi tiết chương */ },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Chương ${chapter.chapter_number}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.titleTextColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFFEEF0FF), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = chapter.status,
                        fontSize = 9.sp,
                        color = themeColors.accentColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = chapter.title,
                fontSize = 13.sp,
                color = themeColors.contentTextColor
            )
        }
        
        val dateString = if (chapter.created_at != null) {
            val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            sdf.format(chapter.created_at.toDate())
        } else {
            ""
        }
        
        Text(
            text = dateString,
            fontSize = 12.sp,
            color = themeColors.contentTextColor
        )
    }
    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
}
