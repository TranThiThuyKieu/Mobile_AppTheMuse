package com.example.appthemuse.ui.screens.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appthemuse.domain.model.AdminBook
import com.example.appthemuse.ui.viewmodel.AdminDashboardViewModel
import kotlinx.coroutines.launch

// Màu chủ đạo admin
private val AdminPrimary = Color(0xFF6C63FF)
private val AdminPrimaryLight = Color(0xFFF0EEFF)
private val AdminGreen = Color(0xFF22C55E)
private val AdminRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color.White
            ) {
                AdminDrawerContent(
                    adminName = uiState.adminName,
                    adminRole = uiState.adminRole,
                    onLogout = onLogout,
                    onProfileClick = onProfileClick,
                    onEditProfileClick = onEditProfileClick,
                    onSecurityClick = onSecurityClick,
                    onSettingsClick = onSettingsClick
                )
            }
        }
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Header
            item {
                AdminDashboardHeader(
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }

            // Filter Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Bộ lọc: ${uiState.selectedFilter}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AdminPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Lọc thời gian", tint = AdminPrimary)
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            val filters = listOf("Hôm nay", "Tuần này", "Tuần trước", "Tháng này", "Năm này", "Tất cả")
                            filters.forEach { filter ->
                                DropdownMenuItem(
                                    text = { Text(filter) },
                                    onClick = {
                                        viewModel.onFilterChanged(filter)
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Stat Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCardAdmin(
                        modifier = Modifier.weight(1f),
                        label = "Người dùng mới",
                        value = uiState.newUserCount,
                        change = uiState.newUserChange,
                        isPositive = true,
                        icon = "👤"
                    )
                    StatCardAdmin(
                        modifier = Modifier.weight(1f),
                        label = "Lượt đọc ${uiState.selectedFilter}",
                        value = uiState.todayReadCount,
                        change = uiState.todayReadChange,
                        isPositive = true,
                        icon = "👁️"
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            // Chart
            item {
                ReadingTrendChart(
                    trendData = uiState.weeklyReadingTrend,
                    currentTab = if (uiState.selectedFilter.contains("Tuần")) uiState.selectedFilter else "Tuần này",
                    onTabSelected = { viewModel.onFilterChanged(it) }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            // Top sách hot title
            item {
                Text(
                    text = "Top sách hot (Lượt xem cao nhất)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Top books từ ViewModel
            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AdminPrimary)
                    }
                }
            } else {
                items(uiState.topBooks) { book ->
                    AdminTopBookRow(book = book)
                }
            }
        }
    }
}

@Composable
fun AdminDrawerContent(
    adminName: String, 
    adminRole: String, 
    onLogout: () -> Unit,
    onProfileClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSecurityClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(20.dp)
    ) {
        // Admin Profile Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AdminPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = AdminPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = adminName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = adminRole,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menu Items
        DrawerMenuItem(icon = Icons.Default.Group, label = "Hồ sơ", onClick = onProfileClick)
        DrawerMenuItem(icon = Icons.Default.Edit, label = "Chỉnh sửa thông tin cá nhân", onClick = onEditProfileClick)
        DrawerMenuItem(icon = Icons.Default.Security, label = "Mật khẩu và bảo mật", onClick = onSecurityClick)
        DrawerMenuItem(icon = Icons.Default.Settings, label = "Cài đặt", onClick = onSettingsClick)
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Logout Button at the bottom
        DrawerMenuItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            label = "Đăng xuất", 
            textColor = AdminRed,
            onClick = onLogout
        )
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector, 
    label: String, 
    textColor: Color = Color.DarkGray,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (textColor == AdminRed) AdminRed else AdminPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}

@Composable
private fun AdminDashboardHeader(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            text = "The Muse",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = MaterialTheme.colorScheme.onBackground)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, contentDescription = "Thông báo", tint = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
private fun StatCardAdmin(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    change: String,
    isPositive: Boolean,
    icon: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AdminPrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 18.sp)
                }
                Text(
                    text = change,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) AdminGreen else AdminRed
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ReadingTrendChart(
    trendData: List<Float>,
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf("Tuần này", "Tuần trước")
    val dayLabels = listOf("Th.2", "Th.3", "Th.4", "Th.5", "Th.6", "Th.7", "CN")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Xu hướng lượt\nđọc trong tuần",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                )
                // Tab switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    tabs.forEach { tab ->
                        val isSelected = currentTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) AdminPrimary else Color.Transparent)
                                .clickable { onTabSelected(tab) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Canvas line chart
            val lineColor = AdminPrimary
            val fillColor = AdminPrimary.copy(alpha = 0.08f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                if (trendData.isEmpty()) return@Canvas
                
                val maxVal = trendData.max().takeIf { it > 0 } ?: 100f
                val minVal = 0f
                val range = maxVal - minVal
                val stepX = size.width / (trendData.size - 1).coerceAtLeast(1)
                val pad = 8.dp.toPx()

                val points = trendData.mapIndexed { i, v ->
                    val x = i * stepX
                    val y = size.height - pad - ((v - minVal) / range) * (size.height - pad * 2)
                    Offset(x, y)
                }

                // Fill path
                val fillPath = Path().apply {
                    moveTo(points.first().x, size.height)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, size.height)
                    close()
                }
                drawPath(fillPath, fillColor)

                // Line path
                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val cx = (points[i - 1].x + points[i].x) / 2
                        cubicTo(cx, points[i - 1].y, cx, points[i].y, points[i].x, points[i].y)
                    }
                }
                drawPath(linePath, lineColor, style = Stroke(width = 3.dp.toPx()))

                // Dots
                points.forEach { p ->
                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = p)
                    drawCircle(color = lineColor, radius = 4.dp.toPx(), center = p, style = Stroke(width = 2.dp.toPx()))
                }
            }

            Spacer(Modifier.height(8.dp))

            // Day labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayLabels.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminTopBookRow(book: AdminBook) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ảnh bìa
        if (book.coverUrl.isNotEmpty()) {
            AsyncImage(
                model = book.coverUrl,
                contentDescription = book.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 56.dp, height = 76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AdminPrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Text("📚", fontSize = 22.sp)
            }
        }

        // Thông tin sách
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "ID Tác giả: ${book.authorId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "4.5", // Giả lập rating
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "·",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Text(
                    text = "${book.viewCount} lượt xem",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
}
