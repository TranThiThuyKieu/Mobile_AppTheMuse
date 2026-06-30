package com.example.appthemuse.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appthemuse.domain.model.BookStatus
import com.example.appthemuse.ui.components.AdminBookRow
import com.example.appthemuse.ui.viewmodel.AdminBookManagementViewModel
import com.example.appthemuse.ui.viewmodel.AdminDashboardViewModel
import kotlinx.coroutines.launch

private val AdminPrimary = Color(0xFF6C63FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookManagementScreen(
    viewModel: AdminBookManagementViewModel,
    adminDashboardViewModel: AdminDashboardViewModel, // Dùng để lấy info admin cho Drawer
    onBookClick: (String) -> Unit,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSecurityClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val dashboardState by adminDashboardViewModel.uiState.collectAsState()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    var showStatusMenu by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                AdminDrawerContent(
                    adminName = dashboardState.adminName,
                    adminRole = dashboardState.adminRole,
                    onLogout = onLogout,
                    onProfileClick = onProfileClick,
                    onEditProfileClick = onEditProfileClick,
                    onSecurityClick = onSecurityClick,
                    onSettingsClick = onSettingsClick
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                AdminBookHeader(
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = "Quản lý sách",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Quản lý và biên tập danh mục sách trong hệ thống.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Search Bar
                item {
                    TextField(
                        value = uiState.keyword,
                        onValueChange = viewModel::updateKeyword,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        placeholder = { Text("Tìm chương, tác giả hoặc sách...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        singleLine = true
                    )
                }

                // Filters
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { /* Lọc theo thể loại */ },
                            modifier = Modifier.weight(1.2f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = null
                        ) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.width(4.dp))
                            Text("Tất cả thể loại", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                        }
                        
                        Button(
                            onClick = { /* Sắp xếp */ },
                            modifier = Modifier.weight(0.8f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = null
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Mới nhất", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                        }
                    }
                }

                // Status Selector
                item {
                    Column {
                        Text(
                            text = "TRẠNG THÁI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Card(
                                onClick = { showStatusMenu = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = uiState.selectedStatus?.label ?: "Tất cả trạng thái",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            DropdownMenu(
                                expanded = showStatusMenu,
                                onDismissRequest = { showStatusMenu = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Tất cả trạng thái") },
                                    onClick = { 
                                        viewModel.selectStatus(null)
                                        showStatusMenu = false
                                    }
                                )
                                BookStatus.entries.forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status.label) },
                                        onClick = { 
                                            viewModel.selectStatus(status)
                                            showStatusMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Lọc kết quả button
                item {
                    Button(
                        onClick = { viewModel.refresh() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = AdminPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Lọc kết quả", color = AdminPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                if (uiState.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AdminPrimary)
                        }
                    }
                } else {
                    items(uiState.books, key = { it.id }) { book ->
                        AdminBookRow(
                            book = book,
                            onClick = { onBookClick(book.id) },
                            onApprove = { viewModel.approveBook(book.id) },
                            onHide = { viewModel.hideBook(book.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminBookHeader(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
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
