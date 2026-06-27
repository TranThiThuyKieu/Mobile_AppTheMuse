package com.example.appthemuse.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appthemuse.ui.viewmodel.AdminUserViewModel
import com.example.appthemuse.ui.viewmodel.UserAdminUi
import kotlinx.coroutines.launch

private val AdminPrimary = Color(0xFF6C63FF)
private val StatusGreen = Color(0xFF22C55E)
private val StatusRed = Color(0xFFEF4444)
private val BackgroundGrey = Color(0xFFF3F4F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    viewModel: AdminUserViewModel,
    onViewProfile: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
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
                    onLogout = onLogout
                )
            }
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Header with Menu Button
            AdminUserHeader(
                onMenuClick = {
                    scope.launch { drawerState.open() }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Quản lý người dùng",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                
                Spacer(Modifier.height(20.dp))

                // Search Bar
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    placeholder = { Text("Tìm thành viên theo tên, email...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BackgroundGrey,
                        unfocusedContainerColor = BackgroundGrey,
                        disabledContainerColor = BackgroundGrey,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                // Filter & Sort Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { showFilterMenu = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BackgroundGrey),
                            elevation = null
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF4B5563))
                            Spacer(Modifier.width(8.dp))
                            Text("Lọc", color = Color(0xFF4B5563))
                        }
                        DropdownMenu(expanded = showFilterMenu, onDismissRequest = { showFilterMenu = false }) {
                            listOf("Tất cả", "Hoạt động", "Đã khóa").forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status) },
                                    onClick = { 
                                        viewModel.onFilterStatusChanged(status)
                                        showFilterMenu = false 
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BackgroundGrey),
                            elevation = null
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF4B5563))
                            Spacer(Modifier.width(8.dp))
                            Text("Sắp xếp", color = Color(0xFF4B5563))
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            listOf("Mới nhất", "Tên A-Z").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { 
                                        viewModel.onSortOptionChanged(option)
                                        showSortMenu = false 
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AdminPrimary)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(uiState.filteredUsers) { user ->
                            UserAdminRow(
                                user = user,
                                onViewProfile = { onViewProfile(user.uid) },
                                onToggleBlock = { viewModel.toggleBlockUser(user) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminUserHeader(onMenuClick: () -> Unit) {
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
private fun AdminDrawerContent(adminName: String, adminRole: String, onLogout: () -> Unit) {
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
        DrawerMenuItem(icon = Icons.Default.Group, label = "Hồ sơ")
        DrawerMenuItem(icon = Icons.Default.Edit, label = "Chỉnh sửa thông tin cá nhân")
        DrawerMenuItem(icon = Icons.Default.Security, label = "Mật khẩu và bảo mật")
        DrawerMenuItem(icon = Icons.Default.Settings, label = "Cài đặt")

        Spacer(modifier = Modifier.weight(1f))
        
        // Logout Button at the bottom
        DrawerMenuItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp, 
            label = "Đăng xuất", 
            textColor = StatusRed,
            onClick = onLogout
        )
    }
}

@Composable
private fun DrawerMenuItem(
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
            tint = if (textColor == StatusRed) StatusRed else AdminPrimary,
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
private fun UserAdminRow(
    user: UserAdminUi,
    onViewProfile: () -> Unit,
    onToggleBlock: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar with Status Indicator
                Box(modifier = Modifier.size(60.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AdminPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = AdminPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    // Status dot
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(if (user.isBlocked) StatusRed else StatusGreen)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "@${user.email.split("@").firstOrNull() ?: user.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }

                IconButton(onClick = { /* More options */ }) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color(0xFF9CA3AF))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info rows
            InfoRow(label = "Ngày tham gia", value = user.joinDate)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Trạng thái", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = (if (user.isBlocked) StatusRed else StatusGreen).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (user.isBlocked) "BỊ KHÓA" else "HOẠT ĐỘNG",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (user.isBlocked) StatusRed else StatusGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onViewProfile,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BackgroundGrey),
                    elevation = null
                ) {
                    Text("Xem hồ sơ", color = Color(0xFF1F2937), fontWeight = FontWeight.Bold)
                }
                
                IconButton(
                    onClick = onToggleBlock,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StatusRed.copy(alpha = 0.1f))
                ) {
                    Icon(
                        if (user.isBlocked) Icons.Default.LockOpen else Icons.Default.Block,
                        contentDescription = null,
                        tint = StatusRed
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color(0xFF1F2937))
    }
}
